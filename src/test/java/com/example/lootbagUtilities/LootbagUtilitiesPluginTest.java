package com.example.lootbagUtilities;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class LootbagUtilitiesPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(LootbagUtilities.class);
		RuneLite.main(args);
	}
}