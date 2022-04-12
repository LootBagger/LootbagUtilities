package com.example.lootbagUtilities;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;

import net.runelite.api.Client;
import net.runelite.api.Varbits;
import net.runelite.api.ItemID;
import net.runelite.api.MenuEntry;
import net.runelite.api.GameState;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
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

    // Represents an item that can be destroyed. Plain old data.
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

    static DestroyableItem[] genDestroyList(LootbagUtilitiesConfig config, BooleanSupplier getInWilderness) {
        return new DestroyableItem[]{
                new DestroyableItem(
                        () -> config.RemoveLootingBagDestroy() && !getInWilderness.getAsBoolean(),
                        ItemID.LOOTING_BAG,
                        "Looting Bag"
                ),
                new DestroyableItem(
                        () -> config.RemoveLootingBagDestroy() && !getInWilderness.getAsBoolean(),
                        ItemID.LOOTING_BAG_22586,
                        "Open Looting Bag"
                ),
                new DestroyableItem(config::RemoveRunePouchDestroy, ItemID.RUNE_POUCH, "Rune Pouch"),
                new DestroyableItem(config::RemoveRunePouchDestroy, ItemID.RUNE_POUCH_23650, "Rune Pouch"),
                new DestroyableItem(config::RemoveRunePouchDestroy, ItemID.RUNE_POUCH_L, "Rune Pouch"),
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
        log.info("LootbagUtilities started!");
        removeDestroyList = genDestroyList(config, this::getInWilderness);
    }

    @Override
    protected void shutDown() {
        log.info("LootbagUtilities stopped!");
    }

    static boolean isLootingBag(int itemId) {
        return itemId == ItemID.LOOTING_BAG || itemId == ItemID.LOOTING_BAG_22586;
    }

    static MenuEntry[] doInventorySwaps(MenuEntry[] entries) {
        //swap open with use on looting bag
        int openIdx = -1;
        int useIdx = -1;
        for (int i = 0; i < entries.length; i++) {
            MenuEntry entry = entries[i];
            if (isLootingBag(entry.getIdentifier())) {
                if (entry.getOption().equals("Use")) {
                    useIdx = i;
                }
                if (entry.getOption().equals("Open")) {
                    openIdx = i;
                }
            }
        }
        if (openIdx != -1 && useIdx != -1) {
            MenuEntry tmp = entries[openIdx];
            entries[openIdx] = entries[useIdx];
            entries[useIdx] = tmp;
        }
        return entries;
    }

    @Subscribe
    public void onClientTick(ClientTick clientTick) {
        // don't swap if menu is open, otherwise items repeatedly swap back and forth
        if (client.getGameState() != GameState.LOGGED_IN || client.isMenuOpen()) {
            return;
        }

        if (config.leftClickUseLootingBag()) {
            client.setMenuEntries(doInventorySwaps(client.getMenuEntries()));
        }
    }

    // Does all processing related to removing "destroy" MenuEntries.
    // this method is static and takes arguments to make testing easier,
    // even though in this class all instances will be called with this class' instances
    static MenuEntry[] removeDestroy(MenuEntry[] entries, DestroyableItem[] removeDestroyList) {
        // Remove destroy options on various items
        Stream<MenuEntry> entryStream = Arrays.stream(entries);
        for (DestroyableItem r : removeDestroyList) {
            Predicate<MenuEntry> p = (MenuEntry entry) -> {
                if (entry.getIdentifier() == r.itemId) {
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

    @Subscribe
    public void onMenuOpened(MenuOpened _unused) {
        MenuEntry[] entries = client.getMenuEntries();
        // Remove destroy options on various items
        entries = removeDestroy(entries, removeDestroyList);

        client.setMenuEntries(entries);
    }

    static boolean consumeEvent(MenuOptionClicked clickedOption, BooleanSupplier inWildy) {
        boolean targetsLootingBag = isLootingBag(clickedOption.getId());
        return targetsLootingBag
                && clickedOption.getMenuOption().equals("Destroy")
                && !inWildy.getAsBoolean();
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked clickedOption) {
        if(consumeEvent(clickedOption, this::getInWilderness)) {
            //consume event (it is not sent to server)
            clickedOption.consume();
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
                    "A magical force (LootbagUtilities plugin) prevents you from destroying your looting bag",
                    ""
            );
            log.debug("User tried to destroy looting bag outside the wilderness");
        }
    }

    @Provides
    LootbagUtilitiesConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(LootbagUtilitiesConfig.class);
    }
}
