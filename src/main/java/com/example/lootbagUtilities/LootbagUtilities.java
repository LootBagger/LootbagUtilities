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
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@PluginDescriptor(
        name = "LootbagUtilities"
)
public class LootbagUtilities extends Plugin {
    @Inject
    private Client client;

    @Inject
    private LootbagUtilitiesConfig config;

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
            log.warn("Cannot determine if player is in wilderness or not. IN_WILDERNESS has value {}",
                    wilderness_varbit);
            return false;
        }
    }

    // Removes all "destroy" options from an array of MenuEntry
    static MenuEntry[] removeDestroyOption(MenuEntry[] entries) {
        Stream<MenuEntry> new_entries_stream = Arrays.stream(entries).filter(
                (MenuEntry entry) -> !entry.getOption().equals("Destroy")
        );
        return new_entries_stream.toArray(MenuEntry[]::new);
    }

    private void doInventorySwaps(MenuEntry[] entries) {
        //swap open with use on looting bag
        int openIdx = -1;
        int useIdx = -1;
        for (int i=0; i< entries.length; i++) {
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
        if (config.RemoveDestroyOption()) {
            // one menu entry targets the looting bag
            boolean targets_looting_bag = Arrays
                    .stream(entries)
                    .anyMatch((MenuEntry entry) -> entry.getIdentifier() == ItemID.LOOTING_BAG);
            // right-clicking on a looting bag outside the wilderness
            if (config.RemoveDestroyOption() && targets_looting_bag && !getInWilderness()) {
                log.debug("Removing destroy entry on looting bag menu...");
                client.setMenuEntries(removeDestroyOption(entries));
            }
        }
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
