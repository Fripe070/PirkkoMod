package io.github.fripe070.pirkko.datagen;

import io.github.fripe070.pirkko.Pirkko;
import io.github.fripe070.pirkko.PirkkoKind;
import io.github.fripe070.pirkko.item.PirkkoItem;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.DyeColor;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

class RecipesProvider extends FabricRecipeProvider {
    public RecipesProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected @NotNull RecipeProvider createRecipeProvider(HolderLookup.@NotNull Provider registryLookup, @NotNull RecipeOutput exporter) {
        return new RecipeProvider(registryLookup, exporter) {
            @Override
            public void buildRecipes() {
                HolderLookup.RegistryLookup<@NotNull Item> itemLookup = registries.lookupOrThrow(Registries.ITEM);
                // Base pirkko
                ShapedRecipeBuilder.shaped(itemLookup, RecipeCategory.DECORATIONS, Pirkko.PIRKKO_ITEM)
                    .pattern(" t ")
                    .pattern("trt")
                    .pattern(" t ")
                    .define('r', Items.RESIN_BRICK)
                    .define('t', Items.TERRACOTTA)
                    .unlockedBy(getHasName(Items.RESIN_BRICK), has(Items.RESIN_BRICK))
                    .save(output);

                // Color pirkko
                for (var dye : DyeColor.values()) {
                    var kind = PirkkoKind.fromPath("color/" + dye.getName());
                    if (kind == null) {
                        Pirkko.LOGGER.warn("Unknown dye color: {}", dye.getName());
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

            private void registerDyeRecipe(HolderLookup.RegistryLookup<@NotNull Item> itemLookup, PirkkoKind kind, @Nullable String group, DyeColor...dyes) {
                Item[] dyeItems = Arrays.stream(dyes).map(d -> getDyeItem(d)).toArray(Item[]::new);
                this.registerSpecialPirkko(itemLookup, kind, group, dyeItems);
            }

            private void registerSpecialPirkko(HolderLookup.RegistryLookup<@NotNull Item> itemLookup, PirkkoKind kind, @Nullable String group, Item[] items) {
                var builder = ShapelessRecipeBuilder.shapeless(itemLookup, RecipeCategory.DECORATIONS, PirkkoItem.getStack(kind));
                for (var item : items) builder.requires(item);
                if (group != null) builder.group(Pirkko.id(group).toString());
                builder
                    .requires(Pirkko.PIRKKO_ITEM)
                    .unlockedBy(getHasName(Pirkko.PIRKKO_ITEM), has(Pirkko.PIRKKO_ITEM))
                    .save(output, Pirkko.id(kind.getId()).toString());
            }

            private static Item getDyeItem(DyeColor color) {
                return BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(color.getSerializedName() + "_dye"));
            }
        };
    }

    @Override
    public @NotNull String getName() {
        return "PirkkoRecipes";
    }
}
