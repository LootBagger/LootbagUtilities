package com.example.lootbagUtilities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MockLootbagUtilitiesConfig implements LootbagUtilitiesConfig{
    boolean leftClickUseLootingBag;
    LootingBagDestroySetting RemoveLootingBagDestroy;
    boolean RemoveRunePouchDestroy;
    boolean RemoveBoltPouchDestroy;
    boolean RemoveSeedBoxDestroy;
    boolean RemoveHerbSackDestroy;
    boolean RemoveCoalBagDestroy;
    boolean RemoveFishBarrelDestroy;
    boolean RemoveGemBagDestroy;
    boolean RemoveTackleBoxDestroy;

    @Override
    public boolean leftClickUseLootingBag() { return leftClickUseLootingBag; }
    @Override
    public LootingBagDestroySetting LootingBagDestroySetting() { return RemoveLootingBagDestroy; }
    @Override
    public boolean RemoveRunePouchDestroy() { return RemoveRunePouchDestroy; }
    @Override
    public boolean RemoveBoltPouchDestroy() { return RemoveBoltPouchDestroy; }
    @Override
    public boolean RemoveSeedBoxDestroy() { return RemoveSeedBoxDestroy; }
    @Override
    public boolean RemoveHerbSackDestroy() { return RemoveHerbSackDestroy; }
    @Override
    public boolean RemoveCoalBagDestroy() { return RemoveCoalBagDestroy; }
    @Override
    public boolean RemoveFishBarrelDestroy() { return RemoveFishBarrelDestroy; }
    @Override
    public boolean RemoveGemBagDestroy() { return RemoveGemBagDestroy; }
    @Override
    public boolean RemoveTackleBoxDestroy() { return RemoveTackleBoxDestroy; }
}
