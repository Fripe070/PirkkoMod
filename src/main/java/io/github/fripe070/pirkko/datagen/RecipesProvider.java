package io.github.fripe070.pirkko.datagen;

import io.github.fripe070.pirkko.Pirkko;
import io.github.fripe070.pirkko.PirkkoKind;
import io.github.fripe070.pirkko.item.PirkkoItem;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.data.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.DyeColor;
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
                ShapedRecipeJsonBuilder.create(itemLookup, RecipeCategory.DECORATIONS, Pirkko.PIRKKO_ITEM)
                    .pattern(" t ")
                    .pattern("trt")
                    .pattern(" t ")
                    .input('r', Items.RESIN_BRICK)
                    .input('t', Items.TERRACOTTA)
                    .group(Identifier.of(Pirkko.MOD_ID, "pirkko").toString())
                    .criterion(hasItem(Items.RESIN_BRICK), conditionsFromItem(Items.RESIN_BRICK))
                    .offerTo(exporter);

                generateDyeingRecipes(itemLookup);
            }

            private void generateDyeingRecipes(RegistryWrapper.Impl<Item> itemLookup) {
                String recipeGroup = Identifier.of(Pirkko.MOD_ID, "pirkko_dyeing").toString();
                for (var dye : DyeColor.values()) {
                    var kind = PirkkoKind.fromPath("color/" + dye.getId());
                    if (kind == null) {
                        Pirkko.LOGGER.warn("Unknown dye color: {}", dye.getId());
                        continue;
                    }

                    var dyeItem = Registries.ITEM.get(Identifier.ofVanilla(dye.asString() + "_dye"));
                    String recipeId = Identifier.of(Pirkko.MOD_ID, dye.asString() + "_pirkko").toString();

                    ShapelessRecipeJsonBuilder.create(itemLookup, RecipeCategory.DECORATIONS, PirkkoItem.getStack(kind))
                        .group(recipeGroup)
                        .input(Pirkko.PIRKKO_ITEM)
                        .input(dyeItem)
                        .criterion(hasItem(Pirkko.PIRKKO_ITEM), conditionsFromItem(Pirkko.PIRKKO_ITEM))
                        .offerTo(exporter, recipeId);
                }
            }
        };
    }

    @Override
    public String getName() {
        return "PirkkoRecipes";
    }
}
