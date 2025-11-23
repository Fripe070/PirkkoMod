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
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
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
                // Base pirkko
                ShapedRecipeJsonBuilder.create(itemLookup, RecipeCategory.DECORATIONS, Pirkko.PIRKKO_ITEM)
                    .pattern(" t ")
                    .pattern("trt")
                    .pattern(" t ")
                    .input('r', Items.RESIN_BRICK)
                    .input('t', Items.TERRACOTTA)
                    .criterion(hasItem(Items.RESIN_BRICK), conditionsFromItem(Items.RESIN_BRICK))
                    .offerTo(exporter);

                // Color pirkko
                for (var dye : DyeColor.values()) {
                    var kind = PirkkoKind.fromPath("color/" + dye.getId());
                    if (kind == null) {
                        Pirkko.LOGGER.warn("Unknown dye color: {}", dye.getId());
                        continue;
                    }
                    this.registerDyeRecipe(itemLookup, kind, "pirkko_color", dye);
                }

                String specialGroup = "pirkko_special";
                // Pride pirkko
                registerDyeRecipe(itemLookup, PirkkoKind.PRIDE_AROMANTIC, specialGroup,
                    DyeColor.GREEN, DyeColor.LIGHT_GRAY, DyeColor.WHITE, DyeColor.BLACK);
                registerDyeRecipe(itemLookup, PirkkoKind.PRIDE_ASEXUAL, specialGroup,
                    DyeColor.BLACK, DyeColor.GRAY, DyeColor.WHITE, DyeColor.PURPLE);
                registerDyeRecipe(itemLookup, PirkkoKind.PRIDE_BISEXUAL, specialGroup,
                    DyeColor.PINK, DyeColor.PURPLE, DyeColor.BLUE);
                registerDyeRecipe(itemLookup, PirkkoKind.PRIDE_GAY_M, specialGroup,
                    DyeColor.GREEN, DyeColor.WHITE, DyeColor.BLUE);
                registerDyeRecipe(itemLookup, PirkkoKind.PRIDE_GENDER_FLUID, specialGroup,
                    DyeColor.PINK, DyeColor.WHITE, DyeColor.MAGENTA, DyeColor.BLACK, DyeColor.BLUE);
                registerDyeRecipe(itemLookup, PirkkoKind.PRIDE_LESBIAN, specialGroup,
                    DyeColor.ORANGE, DyeColor.WHITE, DyeColor.PINK);
                registerDyeRecipe(itemLookup, PirkkoKind.PRIDE_NONBINARY, specialGroup,
                    DyeColor.YELLOW, DyeColor.WHITE, DyeColor.PURPLE, DyeColor.BLACK);
                registerDyeRecipe(itemLookup, PirkkoKind.PRIDE_RAINBOW, specialGroup,
                    DyeColor.RED, DyeColor.ORANGE, DyeColor.YELLOW, DyeColor.GREEN, DyeColor.BLUE, DyeColor.PURPLE);
                registerDyeRecipe(itemLookup, PirkkoKind.PRIDE_TRANSGENDER, specialGroup,
                    DyeColor.LIGHT_BLUE, DyeColor.PINK, DyeColor.WHITE, DyeColor.PINK, DyeColor.LIGHT_BLUE);

                // Special pirkko
                this.registerSpecialPirkko(itemLookup, PirkkoKind.LASERVIOLETT, specialGroup,
                    new Item[]{Items.AMETHYST_SHARD});
                this.registerSpecialPirkko(itemLookup, PirkkoKind.CERISE, specialGroup,
                    new Item[]{Items.PORKCHOP});
                this.registerSpecialPirkko(itemLookup, PirkkoKind.PHOZ, specialGroup,
                    new Item[]{Items.LEATHER, Items.POISONOUS_POTATO});
                this.registerSpecialPirkko(itemLookup, PirkkoKind.KONGLIG, specialGroup,
                    new Item[]{Items.REDSTONE_TORCH});
                this.registerSpecialPirkko(itemLookup, PirkkoKind.GHOST, specialGroup,
                    new Item[]{Items.PHANTOM_MEMBRANE});
                this.registerSpecialPirkko(itemLookup, PirkkoKind.RED_MUSHROOM, specialGroup,
                    new Item[]{Items.RED_MUSHROOM});
                this.registerSpecialPirkko(itemLookup, PirkkoKind.BROWN_MUSHROOM, specialGroup,
                    new Item[]{Items.BROWN_MUSHROOM});
                this.registerSpecialPirkko(itemLookup, PirkkoKind.JUDGE, specialGroup,
                    new Item[]{Items.WRITABLE_BOOK});
                this.registerSpecialPirkko(itemLookup, PirkkoKind.JACKO, specialGroup,
                    new Item[]{Items.JACK_O_LANTERN});
                this.registerSpecialPirkko(itemLookup, PirkkoKind.WARDEN, specialGroup,
                    new Item[]{Items.SCULK});
                this.registerSpecialPirkko(itemLookup, PirkkoKind.SANTA, specialGroup,
                    new Item[]{Items.SNOW_BLOCK, Items.COOKIE});
            }

            private void registerDyeRecipe(RegistryWrapper.Impl<Item> itemLookup, PirkkoKind kind, @Nullable String group, DyeColor ...dyes) {
                Item[] dyeItems = Arrays.stream(dyes).map(d -> getDyeItem(d)).toArray(Item[]::new);
                this.registerSpecialPirkko(itemLookup, kind, group, dyeItems);
            }

            private void registerSpecialPirkko(RegistryWrapper.Impl<Item> itemLookup, PirkkoKind kind, @Nullable String group, Item[] items) {
                var builder = ShapelessRecipeJsonBuilder.create(itemLookup, RecipeCategory.DECORATIONS, PirkkoItem.getStack(kind));
                for (var item : items) builder.input(item);
                if (group != null) builder.group(Pirkko.id(group).toString());
                builder
                    .input(Pirkko.PIRKKO_ITEM)
                    .criterion(hasItem(Pirkko.PIRKKO_ITEM), conditionsFromItem(Pirkko.PIRKKO_ITEM))
                    .offerTo(exporter, Pirkko.id(kind.getId()).toString());
            }

            private static Item getDyeItem(DyeColor color) {
                return Registries.ITEM.get(Identifier.ofVanilla(color.asString() + "_dye"));
            }
        };
    }

    @Override
    public String getName() {
        return "PirkkoRecipes";
    }
}
