package com.example.lootbagUtilities;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("lootbagUtilities")
public interface LootbagUtilitiesConfig extends Config
{
	@ConfigSection(
			name = "Ground Items",
			description = "The Ignore items and Ignore groups options",
			position = -2
	)
	String groundItems = "Ground Items";

	@ConfigItem(
			keyName = "reorderGroundItems",
			name = "Reorder ground items",
			description = "Reorder ground items to more easily find one." +
					"This is expected to make deathpiling easier",
			section = groundItems
	)
	default boolean reorderGroundItems() { return true; }

	@ConfigItem(
			keyName = "orderGroundItemsBy",
			name = "How to sort ground items",
			description = "What value to use in sorting ground items",
			section = groundItems
	)
	default SortOrder orderGroundItemsBy() { return SortOrder.ALPHABETICAL; }

	@ConfigItem(
			keyName = "untradeablesOnTop",
			name = "Prioritize Untradeables",
			description = "Should untradeables be on the top of the ground item menu",
			section = groundItems
	)
	default boolean untradeablesOnTop() { return false; }

	@ConfigItem(
			keyName = "leftClickUseLootingBag",
			name = "Default 'use' looting bag",
			description = "Make 'use' the default option on the looting bag" +
					"This should make wilderness bagging quicker, as you can always left-click" +
					"the looting bag"
	)
	default boolean leftClickUseLootingBag() { return true; }

	@ConfigItem(
			keyName = "removeDestroy",
			name = "Remove Destroy Option",
			description = "Remove destroy option from looting bag right-click menu when outside the wilderness"
	)
	default boolean RemoveDestroyOption() { return true;}

}
