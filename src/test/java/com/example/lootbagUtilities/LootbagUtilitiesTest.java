package com.example.lootbagUtilities;

import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import org.junit.Test;
import com.example.lootbagUtilities.LootbagUtilities.DestroyableItem;

import net.runelite.client.menus.TestMenuEntry;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class LootbagUtilitiesTest {
    // Wrapper around annoying MenuEntry setters
    private static MenuEntry newMenuEntry(
            String option,
            String target,
            /*int identifier,*/
            int itemId,
            MenuAction type,
            int param0,
            int param1)
    {
        TestMenuEntry entry = new TestMenuEntry();
        entry.setOption(option);
        entry.setTarget(target);
        //entry.setIdentifier(identifier);
        //no idea what the numbers in this constructor mean, they were scraped from right-clicking
        //a looting bag in game to best match real values
        Widget widget = new TestWidget(9764864, 5, 0, 0);
        widget.setItemId(itemId);
        entry.setItemId(itemId);
        entry.setWidget(widget);
        entry.setType(type);
        entry.setParam0(param0);
        entry.setParam1(param1);
        return entry;
    }

    //Script to extract menu entries (copy and paste into runelite jshell):
	/*
subscribe(MenuOpened.class, (MenuOpened e) -> {
    for(MenuEntry entry: client.getMenuEntries()) {
        log.info(
            "newMenuEntry(\"{}\", \"{}\", {}, MenuAction.{}, {}, {}),",
            entry.getOption(),
            entry.getTarget(),
            entry.getIdentifier(),
            entry.getType(),
            entry.getParam0(),
            entry.getParam1()
        );
    }
});
	 */
    static MenuEntry cancelOption = newMenuEntry("Cancel", "", 0, MenuAction.CANCEL, 0, 0);
    static int emerald = ItemID.UNCUT_EMERALD+1;
    static MenuEntry[] rightClickNotedEmeralds = {
            cancelOption,
            newMenuEntry("Examine", "<col=ff9040>Uncut emerald", emerald, MenuAction.CC_OP_LOW_PRIORITY, 5, 9764864),
            newMenuEntry("Drop", "<col=ff9040>Uncut emerald", emerald, MenuAction.CC_OP_LOW_PRIORITY, 5, 9764864),
            newMenuEntry("Use", "<col=ff9040>Uncut emerald", emerald, MenuAction.WIDGET_TARGET, 5, 9764864),
    };
    static MenuEntry[] rightClickGuard = {
            cancelOption,
            newMenuEntry("Examine", "<col=ffff00>Guard<col=ff00>  (level-21)", 9334, MenuAction.EXAMINE_NPC, 0, 0),
            newMenuEntry("Walk here", "", 0, MenuAction.WALK, 228, 34),
            newMenuEntry("Pickpocket", "<col=ffff00>Guard<col=ff00>  (level-21)", 9334, MenuAction.NPC_THIRD_OPTION, 0, 0),
            newMenuEntry("Attack", "<col=ffff00>Guard<col=ff00>  (level-21)", 9334, MenuAction.NPC_SECOND_OPTION, 0, 0),
    };
    static MenuEntry[] rightClickTreeAndSkeleton = {
            cancelOption,
            newMenuEntry("Examine", "<col=ffff>Oak", 10820, MenuAction.EXAMINE_OBJECT, 83, 78),
            newMenuEntry("Examine", "<col=ffff00>Skeleton<col=ff00>  (level-22)", 22109, MenuAction.EXAMINE_NPC, 0, 0),
            newMenuEntry("Walk here", "", 0, MenuAction.WALK, 333, 89),
            newMenuEntry("Chop down", "<col=ffff>Oak", 10820, MenuAction.GAME_OBJECT_FIRST_OPTION, 83, 78),
            newMenuEntry("Attack", "<col=ffff00>Skeleton<col=ff00>  (level-22)", 22109, MenuAction.NPC_SECOND_OPTION, 0, 0),
    };
    static int eek = ItemID.EEK;
    static MenuEntry[] rightClickInventoryEek = {
            cancelOption,
            newMenuEntry("Examine", "<col=ff9040>Eek", eek, MenuAction.CC_OP_LOW_PRIORITY, 9, 9764864),
            newMenuEntry("Destroy", "<col=ff9040>Eek", eek, MenuAction.CC_OP_LOW_PRIORITY, 9, 9764864),
            newMenuEntry("Use", "<col=ff9040>Eek", eek, MenuAction.WIDGET_TARGET, 9, 9764864),
            newMenuEntry("Talk-to", "<col=ff9040>Eek", eek, MenuAction.CC_OP, 9, 9764864),
            newMenuEntry("Hold", "<col=ff9040>Eek", eek, MenuAction.CC_OP, 9, 9764864),
    };
    static MenuEntry[] rightClickEquippedEek = {
            cancelOption,
            newMenuEntry("Examine", "<col=ff9040>Eek</col>", 10, MenuAction.CC_OP_LOW_PRIORITY, -1, 25362450),
            newMenuEntry("Talk-to", "<col=ff9040>Eek</col>", 2, MenuAction.CC_OP, -1, 25362450),
            newMenuEntry("Remove", "<col=ff9040>Eek</col>", 1, MenuAction.CC_OP, -1, 25362450),
    };
    static int plankSack = ItemID.PLANK_SACK;
    static MenuEntry[] shiftRightClickPlankSack = {
            cancelOption,
            newMenuEntry("Examine", "<col=ff9040>Plank sack", plankSack, MenuAction.CC_OP_LOW_PRIORITY, 23, 9764864),
            newMenuEntry("Drop", "<col=ff9040>Plank sack", plankSack, MenuAction.CC_OP_LOW_PRIORITY, 23, 9764864),
            newMenuEntry("Empty", "<col=ff9040>Plank sack", plankSack, MenuAction.CC_OP_LOW_PRIORITY, 23, 9764864),
            newMenuEntry("Use", "<col=ff9040>Plank sack", plankSack, MenuAction.WIDGET_TARGET, 23, 9764864),
            newMenuEntry("Check", "<col=ff9040>Plank sack", plankSack, MenuAction.CC_OP, 23, 9764864),
            newMenuEntry("Fill", "<col=ff9040>Plank sack", plankSack, MenuAction.CC_OP, 23, 9764864),
    };
    static int lootingBag = ItemID.LOOTING_BAG;
    static MenuEntry[] rightClickLootingBag = {
            cancelOption,
            newMenuEntry("Examine", "<col=ff9040>Looting bag", lootingBag, MenuAction.CC_OP_LOW_PRIORITY, 27, 9764864),
            newMenuEntry("Destroy", "<col=ff9040>Looting bag", lootingBag, MenuAction.CC_OP_LOW_PRIORITY, 27, 9764864),
            newMenuEntry("Settings", "<col=ff9040>Looting bag", lootingBag, MenuAction.CC_OP_LOW_PRIORITY, 27, 9764864),
            newMenuEntry("Use", "<col=ff9040>Looting bag", lootingBag, MenuAction.WIDGET_TARGET, 27, 9764864),
            newMenuEntry("Deposit", "<col=ff9040>Looting bag", lootingBag, MenuAction.CC_OP, 27, 9764864),
            newMenuEntry("Check", "<col=ff9040>Looting bag", lootingBag, MenuAction.CC_OP, 27, 9764864),
            newMenuEntry("Open", "<col=ff9040>Looting bag", lootingBag, MenuAction.CC_OP, 27, 9764864),
    };
    static int runePouch = ItemID.RUNE_POUCH;
    static MenuEntry[] rightClickRunePouch = {
            cancelOption,
            newMenuEntry("Examine", "<col=ff9040>Rune pouch", runePouch, MenuAction.CC_OP_LOW_PRIORITY, 24, 9764864),
            newMenuEntry("Destroy", "<col=ff9040>Rune pouch", runePouch, MenuAction.CC_OP_LOW_PRIORITY, 24, 9764864),
            newMenuEntry("Empty", "<col=ff9040>Rune pouch", runePouch, MenuAction.CC_OP_LOW_PRIORITY, 24, 9764864),
            newMenuEntry("Use", "<col=ff9040>Rune pouch", runePouch, MenuAction.WIDGET_TARGET, 24, 9764864),
            newMenuEntry("Open", "<col=ff9040>Rune pouch", runePouch, MenuAction.CC_OP, 24, 9764864),
    };
    static int seedBox = ItemID.OPEN_SEED_BOX;
    static MenuEntry[] rightClickSeedBox = {
            cancelOption,
            newMenuEntry("Examine", "<col=ff9040>Open seed box", seedBox, MenuAction.CC_OP_LOW_PRIORITY, 26, 9764864),
            newMenuEntry("Destroy", "<col=ff9040>Open seed box", seedBox, MenuAction.CC_OP_LOW_PRIORITY, 26, 9764864),
            newMenuEntry("Close", "<col=ff9040>Open seed box", seedBox, MenuAction.CC_OP_LOW_PRIORITY, 26, 9764864),
            newMenuEntry("Use", "<col=ff9040>Open seed box", seedBox, MenuAction.WIDGET_TARGET, 26, 9764864),
            newMenuEntry("Check", "<col=ff9040>Open seed box", seedBox, MenuAction.CC_OP, 26, 9764864),
            newMenuEntry("Empty", "<col=ff9040>Open seed box", seedBox, MenuAction.CC_OP, 26, 9764864),
            newMenuEntry("Fill", "<col=ff9040>Open seed box", seedBox, MenuAction.CC_OP, 26, 9764864),
    };

    // Assert that calling removeDestroyOptions
    static void assertOptionsUnchanged(MenuEntry[] entries, DestroyableItem[] config) {
        MenuEntry[] modifiedEntries = LootbagUtilities.removeDestroy(entries, config);
        assertArrayEquals(entries, modifiedEntries);
    }

    static void assertNoDestroyOption(MenuEntry[] entries)  {
        for(MenuEntry e: entries) {
            assert(!e.getOption().equals("Destroy"));
        }
    }

    @Test
    public void testRemoveDestroy() {
        for (int i = 0; i < 2*2*2; i++) {
            LootbagUtilitiesConfig.LootingBagDestroySetting removeLootingBagDestroy =
            (i % 2 == 0) ?
                    LootbagUtilitiesConfig.LootingBagDestroySetting.ALLOW
                    : LootbagUtilitiesConfig.LootingBagDestroySetting.REMOVE;
            boolean removeRunePouchDestroy = i % 2 == 0;
            boolean removeSeedBoxDestroy = i / 2 % 2 == 0;
            MockLootbagUtilitiesConfig config = new MockLootbagUtilitiesConfig(
                    true,
                    removeLootingBagDestroy,
                    removeRunePouchDestroy,
                    false,
                    removeSeedBoxDestroy,
                    false,
                    false,
                    false,
                    false,
                    false
            );

            DestroyableItem[] removeConfig = LootbagUtilities.genDestroyList(config, () -> false);
            // doesn't mess with right-clicks on a few miscellaneous objects
            assertOptionsUnchanged(rightClickNotedEmeralds, removeConfig);
            assertOptionsUnchanged(rightClickGuard, removeConfig);
            assertOptionsUnchanged(rightClickTreeAndSkeleton, removeConfig);
            assertOptionsUnchanged(rightClickInventoryEek, removeConfig);
            assertOptionsUnchanged(rightClickEquippedEek, removeConfig);
            assertOptionsUnchanged(shiftRightClickPlankSack, removeConfig);

            // Properly removes the destroy option and only the destroy option
            MenuEntry[] modifiedRightClickLootingBag = LootbagUtilities.removeDestroy(rightClickLootingBag, removeConfig);
            if (removeLootingBagDestroy == LootbagUtilitiesConfig.LootingBagDestroySetting.REMOVE) {
                assertEquals(rightClickLootingBag.length - 1, modifiedRightClickLootingBag.length);
                assertNoDestroyOption(modifiedRightClickLootingBag);
            } else {
                assertArrayEquals(rightClickLootingBag, modifiedRightClickLootingBag);
            }

            MenuEntry[] modifiedRightClickRunePouch =  LootbagUtilities.removeDestroy(rightClickRunePouch, removeConfig);
            if (removeRunePouchDestroy) {
                assertEquals(rightClickRunePouch.length - 1, modifiedRightClickRunePouch.length);
                assertNoDestroyOption(modifiedRightClickRunePouch);
            } else {
                assertArrayEquals(rightClickRunePouch, modifiedRightClickRunePouch);
            }

            MenuEntry[] modifiedRightClickSeedBox = LootbagUtilities.removeDestroy(rightClickSeedBox, removeConfig);
            if (removeSeedBoxDestroy) {
                assertEquals(rightClickSeedBox.length - 1, modifiedRightClickSeedBox.length);
                assertNoDestroyOption(modifiedRightClickSeedBox);
            } else {
                assertArrayEquals(rightClickSeedBox, modifiedRightClickSeedBox);
            }
        }
    }

    @Test
    public void testConsumeEvent() {
        //TODO: test LootBagUtilities.consumeEvent
    }
}
