package com.example.lootbagUtilities;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("lootbagUtilities")
public interface LootbagUtilitiesConfig extends Config
{
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
