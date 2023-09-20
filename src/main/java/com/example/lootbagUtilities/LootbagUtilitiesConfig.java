package com.example.lootbagUtilities;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("lootbagUtilities")
public interface LootbagUtilitiesConfig extends Config
{
	enum LootingBagDestroySetting {
		ALLOW,
		REMOVE,
		ALLOW_IN_WILDY,
	}

	enum LootingBagStoreAll {
		@SuppressWarnings("unused")
		RIGHT_CLICK,
		LEFT_CLICK,
		SHIFT_CLICK,
	}
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
			name = "Allow Destroying the Looting Bag",
			description = "Allow destroying looting bag depending whether in the wilderness"
	)
	default LootingBagDestroySetting LootingBagDestroySetting() {
		return LootingBagDestroySetting.ALLOW_IN_WILDY;
	}

	@ConfigItem(
			keyName = "swapStoreAll",
			name = "Swap store-all in looting bag deposit",
			description = "Make store-all the default option in looting bag deposit interface"
	)
	default LootingBagStoreAll LootingBagStoreAll() {
		return LootingBagStoreAll.SHIFT_CLICK;
	}

	@ConfigSection(
			name = "Other Destroyables",
			description = "Remove destroy option on misc. items",
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

	@ConfigItem(
			keyName = "removeLogBasketDestroy",
			name = "Remove Destroy on Log Basket",
			description = "Remove destroy option from Log Basket right-click menu",
			section = miscDestroyables
	)
	default boolean RemoveLogBasketDestroy() { return false; }

	@ConfigItem(
			keyName = "removeForestryBasketDestroy",
			name = "Remove Destroy on Forestry Basket",
			description = "Remove destroy option from Forestry Basket right-click menu",
			section = miscDestroyables
	)
	default boolean RemoveForestryBasketDestroy() { return false; }
}
