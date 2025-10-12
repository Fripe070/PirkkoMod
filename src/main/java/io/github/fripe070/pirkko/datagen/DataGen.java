package io.github.fripe070.pirkko.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class DataGen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(LootTableProvider::new);
        pack.addProvider(RecipesProvider::new);
        pack.addProvider(ModelProvider::new);
        pack.addProvider(AdvancementProvider::new);
    }
}
