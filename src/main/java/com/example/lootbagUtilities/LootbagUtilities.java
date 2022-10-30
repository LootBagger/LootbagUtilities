package com.example.lootbagUtilities;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;

import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.Arrays;

@Slf4j
@PluginDescriptor(
        name = "LootbagUtilities"
)
public class LootbagUtilities extends Plugin {
    @Inject
    private Client client;

    @Inject
    private LootbagUtilitiesConfig config;

    // declarative way to remove destroy options on items
    DestroyableItem[] removeDestroyList;

    // Represents an item that can be destroyed and a function
    // to decide if the destroy option should be removed
    static final class DestroyableItem {
        // Whether to remove the destroy option on this item
        public BooleanSupplier removeDestroy;
        // The itemId of the item with a destroy option
        public int itemId;
        // The name of the item with a destroy option. Used only for logging
        public String itemName;

        DestroyableItem(BooleanSupplier removeDestroy, int itemId, String itemName) {
            super();
            this.removeDestroy = removeDestroy;
            this.itemId = itemId;
            this.itemName = itemName;
        }
    }

    // check varbits to determine if player is in wilderness
    // logs any unexpected varbit
    boolean getInWilderness() {
        int wilderness_varbit = client.getVarbitValue(Varbits.IN_WILDERNESS);
        if (wilderness_varbit == 1) {
            return true;
        } else if (wilderness_varbit == 0) {
            return false;
        } else {
            // This should not happen
            log.warn("Cannot determine if player is in wilderness. IN_WILDERNESS has value {}",
                    wilderness_varbit);
            return false;
        }
    }

    static Boolean removeLootingBagDestroy(LootbagUtilitiesConfig.LootingBagDestroySetting setting, boolean inWilderness) {
        if (setting == LootbagUtilitiesConfig.LootingBagDestroySetting.REMOVE) {
            return true;
        } else if (setting == LootbagUtilitiesConfig.LootingBagDestroySetting.ALLOW) {
            return false;
        } else if (setting == LootbagUtilitiesConfig.LootingBagDestroySetting.ALLOW_IN_WILDY) {
            return inWilderness;
        } else {
            log.error("An enum was added to LootingBagDestroySetting and this if/else was not updated");
            assert(false);
            return true;
        }
    }

    static DestroyableItem[] genDestroyList(LootbagUtilitiesConfig config, BooleanSupplier getInWilderness) {
        return new DestroyableItem[]{
                new DestroyableItem(
                        () -> removeLootingBagDestroy(config.LootingBagDestroySetting(), !getInWilderness.getAsBoolean()),
                        ItemID.LOOTING_BAG,
                        "Looting Bag"
                ),
                new DestroyableItem(
                        () -> removeLootingBagDestroy(config.LootingBagDestroySetting(), !getInWilderness.getAsBoolean()),
                        ItemID.LOOTING_BAG_22586,
                        "Open Looting Bag"
                ),
                new DestroyableItem(config::RemoveRunePouchDestroy, ItemID.RUNE_POUCH, "Rune Pouch"),
                new DestroyableItem(config::RemoveRunePouchDestroy, ItemID.RUNE_POUCH_23650, "Rune Pouch"),
                new DestroyableItem(config::RemoveRunePouchDestroy, ItemID.RUNE_POUCH_L, "Rune Pouch(l)"),
                new DestroyableItem(config::RemoveRunePouchDestroy, ItemID.DIVINE_RUNE_POUCH, "Divine Rune Pouch"),
                new DestroyableItem(config::RemoveRunePouchDestroy, ItemID.DIVINE_RUNE_POUCH_L, "Divine Rune Pouch(l)"),
                new DestroyableItem(config::RemoveSeedBoxDestroy, ItemID.SEED_BOX, "Seed Box"),
                new DestroyableItem(config::RemoveSeedBoxDestroy, ItemID.OPEN_SEED_BOX, "Open Seed Box"),
                new DestroyableItem(config::RemoveBoltPouchDestroy, ItemID.BOLT_POUCH, "Bolt Pouch"),
                new DestroyableItem(config::RemoveHerbSackDestroy, ItemID.HERB_SACK, "Herb Sack"),
                new DestroyableItem(config::RemoveHerbSackDestroy, ItemID.OPEN_HERB_SACK, "Herb Sack"),
                new DestroyableItem(config::RemoveCoalBagDestroy, ItemID.COAL_BAG, "Coal Bag"),
                new DestroyableItem(config::RemoveCoalBagDestroy, ItemID.OPEN_COAL_BAG, "Open Coal Bag"),
                new DestroyableItem(config::RemoveCoalBagDestroy, ItemID.COAL_BAG_12019, "Alt Coal Bag"),
                new DestroyableItem(config::RemoveCoalBagDestroy, ItemID.COAL_BAG_25627, "Alt Coal Bag"),
                new DestroyableItem(config::RemoveFishBarrelDestroy, ItemID.FISH_BARREL, "Fish Barrel"),
                new DestroyableItem(config::RemoveFishBarrelDestroy, ItemID.FISH_SACK_BARREL, "Fish Sack Barrel"),
                new DestroyableItem(config::RemoveFishBarrelDestroy, ItemID.OPEN_FISH_BARREL, "Open Fish Barrel"),
                new DestroyableItem(config::RemoveFishBarrelDestroy, ItemID.OPEN_FISH_SACK_BARREL, "Open Fish Sack Barrel"),
                new DestroyableItem(config::RemoveGemBagDestroy, ItemID.GEM_BAG, "Gem Bag"),
                new DestroyableItem(config::RemoveGemBagDestroy, ItemID.GEM_BAG_12020, "Alt Gem Bag"),
                new DestroyableItem(config::RemoveGemBagDestroy, ItemID.GEM_BAG_25628, "Alt Gem Bag"),
                new DestroyableItem(config::RemoveGemBagDestroy, ItemID.OPEN_GEM_BAG, "Open Gem Bag"),
                new DestroyableItem(config::RemoveTackleBoxDestroy, ItemID.TACKLE_BOX, "Tackle Box"),
        };
    }

    @Override
    protected void startUp() {
        removeDestroyList = genDestroyList(config, this::getInWilderness);
    }

    @Override
    protected void shutDown() {
    }

    static boolean isLootingBag(int itemId) {

        return
                itemId == ItemID.LOOTING_BAG // Closed looting bag
                        || itemId == ItemID.LOOTING_BAG_22586; //Opened looting bag
    }

    //swap open with use on looting bag
    //mutates in place
    static void doInventorySwaps(MenuEntry[] entries) {
        //can't swap 1 or fewer things
        if (entries.length < 2) {
            return;
        }

        //index of the option at the top of the list/left click option
        int firstIdx = entries.length-1;
        int useIdx = -1;
        for (int i = 0; i < entries.length; i++) {
            MenuEntry entry = entries[i];
            Widget widget = entry.getWidget();
            //right click options of a looting bag
            if (widget!=null && isLootingBag(widget.getItemId())) {
                if (entry.getOption().equals("Use")) {
                    useIdx = i;
                }
            }
        }
        if (useIdx != -1) {
            MenuEntry tmp = entries[firstIdx];
            entries[firstIdx] = entries[useIdx];
            entries[useIdx] = tmp;
        }
    }

    static<T> void swap(T[] array, int first, int second) {
        T tmp = array[first];
        array[first] = array[second];
        array[second] = tmp;
    }

    // do MenuEntry swaps on options in the check/deposit interface of the looting bag
    static void doLootingBagSwaps(MenuEntry[] entries) {
        if (entries.length < 2) {
            return;
        }
        int firstIdx = entries.length-1;
        int depositAllIdx = -1;
        for (int i = 0; i< entries.length; i++) {
            MenuEntry entry = entries[i];
            if (entry.getOption().equals("Store-All")) {
                depositAllIdx = i;
                break;
            }
        }
        //swap option and move other options forward
        if (depositAllIdx != -1) {
            swap(entries, firstIdx, depositAllIdx);
            for(int i=depositAllIdx; i<firstIdx-1; i++) {
                swap(entries, i, i+1);
            }
        }
    }

    @Subscribe
    public void onClientTick(ClientTick clientTick) {
        // don't swap if menu is open, otherwise items repeatedly swap back and forth
        if (client.getGameState() != GameState.LOGGED_IN || client.isMenuOpen()) {
            return;
        }

        MenuEntry[] entries = client.getMenuEntries();

        if (config.leftClickUseLootingBag()) {
            doInventorySwaps(entries);
        }
        if ((config.LootingBagStoreAll() == LootbagUtilitiesConfig.LootingBagStoreAll.LEFT_CLICK ||
                config.LootingBagStoreAll() == LootbagUtilitiesConfig.LootingBagStoreAll.SHIFT_CLICK && client.isKeyPressed(KeyCode.KC_SHIFT))
                && isLootingBagInterfaceOpen()) {
            doLootingBagSwaps(entries);
        }
        client.setMenuEntries(entries);
    }

    // Does all processing related to removing "destroy" MenuEntries.
    // this method is static and takes arguments to make testing easier,
    static MenuEntry[] removeDestroy(MenuEntry[] entries, DestroyableItem[] removeDestroyList) {
        // Remove destroy options on various items
        Stream<MenuEntry> entryStream = Arrays.stream(entries);
        for (DestroyableItem r : removeDestroyList) {
            Predicate<MenuEntry> p = (MenuEntry entry) -> {
                if (entry.getItemId() == r.itemId) {
                    log.debug("Removing destroy option on {}", r.itemName);
                    return true;
                } else {
                    return false;
                }
            };
            if (r.removeDestroy.getAsBoolean()) {
                entryStream = entryStream.filter(
                        (MenuEntry entry) -> !entry.getOption().equals("Destroy") || !p.test(entry)
                );
            }
        }
        return entryStream.toArray(MenuEntry[]::new);
    }

    private boolean isLootingBagInterfaceOpen() {
        return client.getWidget(WidgetInfo.LOOTING_BAG_CONTAINER) != null;
    }

    @Subscribe
    public void onMenuOpened(MenuOpened _unused) {
        MenuEntry[] entries = client.getMenuEntries();
        // Remove destroy options on various items
        entries = removeDestroy(entries, removeDestroyList);

        if ((config.LootingBagStoreAll() == LootbagUtilitiesConfig.LootingBagStoreAll.LEFT_CLICK ||
                config.LootingBagStoreAll() == LootbagUtilitiesConfig.LootingBagStoreAll.SHIFT_CLICK && client.isKeyPressed(KeyCode.KC_SHIFT))
                && isLootingBagInterfaceOpen()) {
            doLootingBagSwaps(entries);
        }

        client.setMenuEntries(entries);
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked clickedOption) {
        Widget clickedWidget = clickedOption.getWidget();
        if ( clickedWidget != null
                && isLootingBag(clickedWidget.getItemId())
                && clickedOption.getMenuOption().equals("Destroy")
                && removeLootingBagDestroy(config.LootingBagDestroySetting(), getInWilderness())
        ) {
            //consume event (it is not sent to server)
            clickedOption.consume();
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
                                  "A magical force (LootbagUtilities plugin) prevents you from destroying your looting bag",
                                  ""
            );
            log.debug("User tried to destroy looting bag when not allowed");
        }
    }

    @Provides
    LootbagUtilitiesConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(LootbagUtilitiesConfig.class);
    }
}
