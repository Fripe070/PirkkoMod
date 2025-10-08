package io.github.fripe070.pirkko;

import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import io.github.fripe070.pirkko.block.PirkkoBlock;
import io.github.fripe070.pirkko.effect.PirkkoPowerEffect;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.util.Rarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Function;

public class Pirkko implements ModInitializer {
    public static final String MOD_ID = "pirkko";
    public static final Logger LOGGER = LoggerFactory.getLogger("pirkko");

    public static final Block PIRKKO_BLOCK = registerBlock(
        "pirkko",
        PirkkoBlock::new,
        AbstractBlock.Settings.create(),
        true,
        new Item.Settings()
            .maxCount(16)
            .fireproof()
            .rarity(Rarity.EPIC)
    );
    public static final SoundEvent PIRKKO_SOUND = registerSoundEvent("pirkko", SoundEvents.ENTITY_COD_FLOP);
    public static final StatusEffect PIRKKO_POWER = new PirkkoPowerEffect();

    @Override
    public void onInitialize() {
        PolymerResourcePackUtils.addModAssets(MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register((itemGroup) -> {
            itemGroup.add(PIRKKO_BLOCK.asItem());
        });
        Registry.register(Registries.STATUS_EFFECT, Identifier.of(MOD_ID, "pirkko_power"), PIRKKO_POWER);

        PolymerResourcePackUtils.markAsRequired();
    }

    private static SoundEvent registerSoundEvent(String name, SoundEvent soundEvent) {
        Identifier id = Identifier.of(MOD_ID, name);
        var event = Registry.register(Registries.SOUND_EVENT, id, new SoundEvent(id, Optional.empty()));
        PolymerSoundEvent.registerOverlay(event, soundEvent);
        RegistrySyncUtils.setServerEntry(Registries.SOUND_EVENT, event);
        return event;
    }

    public static Item registerItem(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
        // Create the item key.
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, name));
        // Create the item instance.
        Item item = itemFactory.apply(settings.registryKey(itemKey));
        // Register the item.
        Registry.register(Registries.ITEM, itemKey, item);
        return item;
    }

    private static Block registerBlock(String name, Function<AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings blockSettings, boolean shouldRegisterItem, Item.Settings itemSettings) {
        // Create a registry key for the block
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, name));
        // Create the block instance
        Block block = blockFactory.apply(blockSettings.registryKey(blockKey));
        // Sometimes, you may not want to register an item for the block.
        // Eg: if it's a technical block like `minecraft:moving_piston` or `minecraft:end_gateway`
//        if (shouldRegisterItem) {
//            // Items need to be registered with a different type of registry key, but the ID
//            // can be the same.
//            RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, blockKey.getValue());
//            PolymerBlockItem blockItem = new PolymerBlockItem(block, new Item.Settings().registryKey(itemKey).useBlockPrefixedTranslationKey());
//            Registry.register(Registries.ITEM, itemKey, blockItem);
//        }
        if (shouldRegisterItem) registerBlockItem(name, block, itemSettings);

        return Registry.register(Registries.BLOCK, blockKey, block);
    }

    private static PolymerBlockItem registerBlockItem(String name, Block block, Item.Settings settings) {
        // Create a registry key for the item
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, name));
        // Create the item instance
        PolymerBlockItem blockItem = new PolymerBlockItem(block, settings.registryKey(itemKey).useBlockPrefixedTranslationKey());
        // Register the item
        return Registry.register(Registries.ITEM, itemKey, blockItem);
    }
}
