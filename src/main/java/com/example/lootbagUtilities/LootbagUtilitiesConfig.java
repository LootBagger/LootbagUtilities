package com.example.lootbagUtilities;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

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
			keyName = "removeLootingBagDestroy",
			name = "Remove Destroy on Looting Bag",
			description = "Remove destroy option from looting bag right-click menu when outside the wilderness"
	)
	default boolean RemoveLootingBagDestroyOption() { return true; }

	@ConfigSection(
			name = "Other Destroyables",
			description = "Remove destroy option on various misc. items",
			position = 99
	)
	String miscDestroyables = "miscDestroyables";

	@ConfigItem(
			keyName = "removeRunePouchDestroy",
			name = "Remove Destroy on Rune Pouch",
			description = "Remove destroy option from Rune Pouch right-click menu",
			section = miscDestroyables
	)
	default boolean RemoveRunePouchDestroy() { return false; }

	@ConfigItem(
			keyName = "removeBoltPouchDestroy",
			name = "Remove Destroy on Bolt Pouch",
			description = "Remove destroy option from Bolt Pouch right-click menu",
			section = miscDestroyables
	)
	default boolean RemoveBoltPouchDestroy() { return false; }

	@ConfigItem(
			keyName = "removeSeedBoxDestroy",
			name = "Remove Destroy on Seed Box",
			description = "Remove destroy option from Seed Box right-click menu",
			section = miscDestroyables
	)
	default boolean RemoveSeedBoxDestroy() { return false; }

	@ConfigItem(
			keyName = "removeHerbSackDestroy",
			name = "Remove Destroy on Herb Sack",
			description = "Remove destroy option from Herb Sack right-click menu",
			section = miscDestroyables
	)
	default boolean RemoveHerbSackDestroy() { return false; }

	@ConfigItem(
			keyName = "removeCoalBagDestroy",
			name = "Remove Destroy on Coal Bag",
			description = "Remove destroy option from Coal Bag right-click menu",
			section = miscDestroyables
	)
	default boolean RemoveCoalBagDestroy() { return false; }

	@ConfigItem(
			keyName = "removeFishBarrelDestroy",
			name = "Remove Destroy on Fish Barrel",
			description = "Remove destroy option from Fish Barrel right-click menu",
			section = miscDestroyables
	)
	default boolean RemoveFishBarrelDestroy() { return false; }

	@ConfigItem(
			keyName = "removeGemBagDestroy",
			name = "Remove Destroy on Gem Bag",
			description = "Remove destroy option from Gem Bag right-click menu",
			section = miscDestroyables
	)
	default boolean RemoveGemBagDestroy() { return false; }

	@ConfigItem(
			keyName = "removeTackleBoxDestroy",
			name = "Remove Destroy on Tackle Box",
			description = "Remove destroy option from Tackle Box right-click menu",
			section = miscDestroyables
	)
	default boolean RemoveTackleBoxDestroy() { return false; }
}
