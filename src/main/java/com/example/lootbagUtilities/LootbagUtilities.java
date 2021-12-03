package com.example.lootbagUtilities;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
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

    // This is just plain old data. In a language with a more expressive type system,
    // this class would not exist, a tuple of BooleanSupplier, int, String
    // would be used instead
    private static final class DestroyableItem {
        public BooleanSupplier config;
        public int itemId;
        public String itemName;

        DestroyableItem(BooleanSupplier config, int itemId, String itemName) {
            super();
            this.config = config;
            this.itemId = itemId;
            this.itemName = itemName; // only used for logging
        }
    }

    // declarative way to remove destroy options on items
    private final DestroyableItem[] removeDestroyList = new DestroyableItem[]{
            //TODO: figure out what ItemID.LOOTING_BAG_22586 is
            new DestroyableItem(
                    () -> config.RemoveLootingBagDestroyOption() && !getInWilderness(),
                    ItemID.LOOTING_BAG,
                    "Looting Bag"
            ),
            new DestroyableItem(() -> config.RemoveRunePouchDestroy(), ItemID.RUNE_POUCH, "Rune Pouch"),
            new DestroyableItem(() -> config.RemoveSeedBoxDestroy(), ItemID.SEED_BOX, "Seed Box"),
            new DestroyableItem(() -> config.RemoveSeedBoxDestroy(), ItemID.OPEN_SEED_BOX, "Open Seed Box"),
            new DestroyableItem(() -> config.RemoveBoltPouchDestroy(), ItemID.BOLT_POUCH, "Bolt Pouch"),
            new DestroyableItem(() -> config.RemoveHerbSackDestroy(), ItemID.HERB_SACK, "Herb Sack"),
            //TODO: look into coal bag alternate ids
            new DestroyableItem(() -> config.RemoveCoalBagDestroy(), ItemID.COAL_BAG, "Coal Bag"),
            new DestroyableItem(() -> config.RemoveCoalBagDestroy(), ItemID.OPEN_COAL_BAG, "Open Coal Bag"),
            new DestroyableItem(() -> config.RemoveFishBarrelDestroy(), ItemID.FISH_BARREL, "Fish Barrel"),
            new DestroyableItem(() -> config.RemoveFishBarrelDestroy(), ItemID.FISH_SACK_BARREL, "Fish Sack Barrel"),
            new DestroyableItem(() -> config.RemoveFishBarrelDestroy(), ItemID.OPEN_FISH_BARREL, "Open Fish Barrel"),
            new DestroyableItem(() -> config.RemoveFishBarrelDestroy(), ItemID.OPEN_FISH_SACK_BARREL, "Open Fish Sack Barrel"),
            //TODO: look into gem bag alternate ids
            new DestroyableItem(() -> config.RemoveGemBagDestroy(), ItemID.GEM_BAG, "Gem Bag"),
            new DestroyableItem(() -> config.RemoveGemBagDestroy(), ItemID.OPEN_GEM_BAG, "Open Gem Bag"),
            new DestroyableItem(() -> config.RemoveTackleBoxDestroy(), ItemID.TACKLE_BOX, "Tackle Box"),
    };

    @Override
    protected void startUp() {
        log.info("LootbagUtilities started!");
    }

    @Override
    protected void shutDown() {
        log.info("LootbagUtilities stopped!");
    }

    // check varbits to determine if player is in wilderness
    // logs any unexpected varbit
    boolean getInWilderness() {
        int wilderness_varbit = client.getVarbitValue(Varbits.IN_WILDERNESS.getId());
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

    // Removes "destroy" options from an array of MenuEntry if they satisfy
    // the predicate p (return true)
    static <P extends Predicate<MenuEntry>> MenuEntry[] removeDestroyOption(MenuEntry[] entries, P p) {
        Stream<MenuEntry> new_entries_stream = Arrays.stream(entries).filter(
                (MenuEntry entry) -> !entry.getOption().equals("Destroy") || !p.test(entry)
        );
        return new_entries_stream.toArray(MenuEntry[]::new);
    }

    private void doInventorySwaps(MenuEntry[] entries) {
        //swap open with use on looting bag
        int openIdx = -1;
        int useIdx = -1;
        for (int i = 0; i < entries.length; i++) {
            MenuEntry entry = entries[i];
            if (entry.getIdentifier() == ItemID.LOOTING_BAG) {
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
        client.setMenuEntries(entries);
    }

    @Subscribe
    public void onClientTick(ClientTick clientTick) {
        // don't swap if menu is open, otherwise items repeatedly swap back and forth
        if (client.getGameState() != GameState.LOGGED_IN || client.isMenuOpen()) {
            return;
        }

        if (config.leftClickUseLootingBag()) {
            doInventorySwaps(client.getMenuEntries());
        }
    }

    @Subscribe
    public void onMenuOpened(MenuOpened _unused) {
        MenuEntry[] entries = client.getMenuEntries();
        // Remove destroy options on various items
        for (DestroyableItem r : removeDestroyList) {
            if (r.config.getAsBoolean()) {
                entries = removeDestroyOption(entries, (MenuEntry entry) -> {
                    if (entry.getIdentifier() == r.itemId) {
                        log.debug("Removing destroy option on {}", r.itemName);
                        return true;
                    } else {
                        return false;
                    }
                });
            }
        }

        client.setMenuEntries(entries);
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked clickedOption) {
        boolean targets_looting_bag = clickedOption.getId() == ItemID.LOOTING_BAG
                || clickedOption.getMenuTarget().endsWith("Looting bag");
        // clicked destroy on looting bag outside wilderness
        if (targets_looting_bag
                && clickedOption.getMenuOption().equals("Destroy")
                && !getInWilderness()
        ) {
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
