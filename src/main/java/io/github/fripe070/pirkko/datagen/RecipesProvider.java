package io.github.fripe070.pirkko.datagen;

import io.github.fripe070.pirkko.Pirkko;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.data.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

class RecipesProvider extends FabricRecipeProvider {
    public RecipesProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup registryLookup, RecipeExporter exporter) {
        return new RecipeGenerator(registryLookup, exporter) {
            @Override
            public void generate() {
                RegistryWrapper.Impl<Item> itemLookup = registries.getOrThrow(RegistryKeys.ITEM);
                ShapedRecipeJsonBuilder.create(itemLookup, RecipeCategory.DECORATIONS, Pirkko.PIRKKO_BLOCK)
                    .pattern(" t ")
                    .pattern("trt")
                    .pattern(" t ")
                    .input('r', Items.RESIN_BRICK)
                    .input('t', Items.TERRACOTTA)
                    .group(Identifier.of(Pirkko.MOD_ID, "pirkko").toString())
                    .criterion(hasItem(Items.RESIN_BRICK), conditionsFromItem(Items.RESIN_BRICK))
                    .offerTo(exporter);
            }
        };
    }

    @Override
    public String getName() {
        return "PirkkoRecipes";
    }
}
