package com.example.lootbagUtilities;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Predicate;
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

    @Inject
    private ItemManager itemManager;

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


    // Sorts elements matching predicate of an array according to comparator
    // leaving elements not matching predicate untouched
    static <T> void sortSparseArrayView(T[] array, Predicate<T> predicate, Comparator<T> comparator) {
        ArrayList<Integer> indices = new ArrayList<>();
        ArrayList<T> indexed_entries = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            T item = array[i];
            if (predicate.test(item)) {
                indices.add(i);
                indexed_entries.add(item);
            }
        }
        indexed_entries.sort(comparator);
        for (int i = 0; i < indices.size(); i++) {
            int index = indices.get(i);
            array[index] = indexed_entries.get(i);
        }
    }

    // Code that gets called both onClientTick and onMenuOpened
    private void doGroundItemSwaps(MenuEntry[] entries) {
        // it is possible to right-click on more than one tile, so store all tiles
        HashSet<WorldPoint> menuPoints = new HashSet<>();
        // param0 and param1 on "take" menu options are scene coordinates
        Arrays
                .stream(entries)
                .filter((entry) -> entry.getOption().equals("Take"))
                .forEach((entry) -> {
                    int x = entry.getParam0();
                    int y = entry.getParam1();
                    WorldPoint worldPoint = WorldPoint.fromScene(client, x, y, client.getPlane());
                    menuPoints.add(worldPoint);
                });
        //find ground items located at points
        HashMap<WorldPoint, Tile> tiles = new HashMap<>();
        //include only tiles from menuPoints
        for (Tile[][] tiles2d : client.getScene().getTiles()) {
            for (Tile[] tiles1d : tiles2d) {
                for (Tile tile : tiles1d) {
                    if (tile != null) {
                        WorldPoint tileLocation = tile.getWorldLocation();
                        if (menuPoints.contains(tileLocation)) {
                            tiles.put(tileLocation, tile);
                        }
                    }
                }
            }
        }

        Comparator<MenuEntry> comparator;
        SortOrder order = config.orderGroundItemsBy();
        if (order == SortOrder.ALCHEMY) {
            comparator = Comparator.comparingInt((entry) ->
                    getQuantity(entry, tiles) * getPrice(entry.getIdentifier()));
        } else if (order == SortOrder.ALPHABETICAL) {
            comparator = (MenuEntry left, MenuEntry right) ->
                    -left.getTarget().compareToIgnoreCase(right.getTarget());
        } else if (order == SortOrder.GRAND_EXCHANGE) {
            comparator = Comparator.comparingInt((entry) ->
                    getQuantity(entry, tiles) * getGePrice(entry.getIdentifier()));
        } else /*(order == SortOrder.ACTIVELY_TRADED)*/ {
            comparator = Comparator.comparingInt((entry) ->
                    getQuantity(entry, tiles) * getActivePrice(entry.getIdentifier()));
        }

        Comparator<MenuEntry> final_comparator = comparator;
        if (config.untradeablesOnTop()) {
            final_comparator = (MenuEntry left, MenuEntry right) -> {
                boolean left_tradeable = itemManager.getItemComposition(left.getIdentifier()).isTradeable();
                boolean right_tradeable = itemManager.getItemComposition(right.getIdentifier()).isTradeable();
                if (left_tradeable && !right_tradeable) {
                    return -1;
                } else if (right_tradeable && !left_tradeable) {
                    return 1;
                } else {
                    return comparator.compare(left, right);
                }
            };
        }

        sortSparseArrayView(entries, (entry) -> entry.getOption().equals("Take"), final_comparator);

        client.setMenuEntries(entries);
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

    int getPrice(int itemId) {
        return itemManager.getItemComposition(itemId).getPrice();
    }

    int getGePrice(int itemId) {
        return itemManager.getItemPriceWithSource(itemId, false);
    }

    int getActivePrice(int itemId) {
        return itemManager.getItemPriceWithSource(itemId, true);
    }

    private Integer getQuantity(MenuEntry entry, HashMap<WorldPoint, Tile> relevant_tiles) {
        int x = entry.getParam0();
        int y = entry.getParam1();
        WorldPoint location = WorldPoint.fromScene(client, x, y, client.getPlane());
        return relevant_tiles
                .get(location)
                .getGroundItems()
                .stream()
                .filter((TileItem tileItem) -> tileItem.getId() == entry.getIdentifier())
                .findAny()
                .map(tileItem -> tileItem.getQuantity())
                .orElse(1);
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

        MenuEntry[] entries = client.getMenuEntries();
        if (config.reorderGroundItems()) {
            boolean groundItems = Arrays
                    .stream(entries)
                    .anyMatch((entry) -> entry.getOption().equals("Take"));
            if (groundItems) {
                doGroundItemSwaps(entries);
            }
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

        entries = client.getMenuEntries();
        if (config.reorderGroundItems()) {
            // alphabetize items on ground
            boolean groundItems = Arrays
                    .stream(entries)
                    .anyMatch((entry) -> entry.getOption().equals("Take"));
            if (groundItems) {
                doGroundItemSwaps(entries);
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
