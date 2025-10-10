package io.github.fripe070.pirkko.datagen;

import io.github.fripe070.pirkko.Pirkko;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.data.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
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
                ShapedRecipeJsonBuilder.create(itemLookup, RecipeCategory.DECORATIONS, Pirkko.DEFAULT_PIRKKO_ITEM)
                    .pattern(" t ")
                    .pattern("trt")
                    .pattern(" t ")
                    .input('r', Items.RESIN_BRICK)
                    .input('t', Items.TERRACOTTA)
                    .group(Identifier.of(Pirkko.MOD_ID, "pirkko").toString())
                    .criterion(hasItem(Items.RESIN_BRICK), conditionsFromItem(Items.RESIN_BRICK))
                    .offerTo(exporter);

                for (var dye : DyeColor.values()) {
                    var stack = new ItemStack(Pirkko.DEFAULT_PIRKKO_ITEM);
//                    stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(dye.getEntityColor()));
                    CustomModelDataComponent oldModelData = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
                    if (oldModelData == null) {
                        oldModelData = new CustomModelDataComponent(List.of(), List.of(), List.of(), List.of());
                    }
                    var strings = new ArrayList<>(oldModelData.strings());
                    if (strings.isEmpty()) strings.add("standard");
                    strings.set(0, "color/" + dye.getId());
                    var modelData = new CustomModelDataComponent(oldModelData.floats(), oldModelData.flags(), strings, oldModelData.colors());
                    stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, modelData);

                    ShapelessRecipeJsonBuilder.create(itemLookup, RecipeCategory.DECORATIONS, stack)
                        .group(Identifier.of(Pirkko.MOD_ID, "pirkko_dye").toString())
                        .input(Pirkko.DEFAULT_PIRKKO_ITEM)
                        .input(Registries.ITEM.get(Identifier.ofVanilla(dye.asString() + "_dye")))
                        .criterion(hasItem(Pirkko.DEFAULT_PIRKKO_ITEM), conditionsFromItem(Pirkko.DEFAULT_PIRKKO_ITEM))
                        .offerTo(exporter, Identifier.of(Pirkko.MOD_ID, dye.asString() + "_pirkko").toString());
                }
            }
        };
    }

    @Override
    public String getName() {
        return "PirkkoRecipes";
    }
}
