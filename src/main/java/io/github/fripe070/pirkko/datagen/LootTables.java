package io.github.fripe070.pirkko.datagen;

import io.github.fripe070.pirkko.Pirkko;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class LootTables extends FabricBlockLootTableProvider {
    protected LootTables(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        System.out.println("Generating loot tables...");
        this.addDrop(Pirkko.PIRKKO_BLOCK);
    }
}
