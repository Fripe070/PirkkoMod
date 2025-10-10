package io.github.fripe070.pirkko.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class LootTableProvider extends FabricBlockLootTableProvider {
    protected LootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        System.out.println("Generating loot tables...");
//        for (PirkkoBlock pirkkoBlock : Pirkko.PIRKKO_ITEMS) {
//            this.addDrop(pirkkoBlock);
//        }
    }
}
