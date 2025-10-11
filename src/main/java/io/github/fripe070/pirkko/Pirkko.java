package io.github.fripe070.pirkko;

import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import io.github.fripe070.pirkko.block.PirkkoBlock;
import io.github.fripe070.pirkko.effect.PirkkoPowerEffect;
import io.github.fripe070.pirkko.item.PirkkoItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.util.Rarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Pirkko implements ModInitializer {
    public static final String MOD_ID = "pirkko";
    public static final Logger LOGGER = LoggerFactory.getLogger("pirkko");

    public static final PirkkoBlock PIRKKO_BLOCK = registerPirkkoBlock("pirkko");
    public static final Item DEFAULT_PIRKKO_ITEM = registerPirkkoItem("pirkko", PIRKKO_BLOCK);

    public static final String[] PIRKKO_KINDS = Stream.concat(
        Arrays.stream(new String[]{"phoz", "ghost", "konglig", "laserviolett", "cerise"}),
        Arrays.stream(DyeColor.values()).map((color) -> "color/" + color.getId())
    ).toArray(String[]::new);

    public static final SoundEvent PIRKKO_SOUND = registerSoundEvent("pirkko", SoundEvents.ENTITY_COD_FLOP);
    public static final StatusEffect PIRKKO_POWER = new PirkkoPowerEffect();

    @Override
    public void onInitialize() {
        PolymerResourcePackUtils.addModAssets(MOD_ID);
        PolymerResourcePackUtils.markAsRequired();

        registerAllPirkkoItems();
        Registry.register(Registries.STATUS_EFFECT, Identifier.of(MOD_ID, "pirkko_power"), PIRKKO_POWER);
    }

//    public static Item getPirkkoByColor(DyeColor color) {
//        return PIRKKO_ITEMS.get(color.getIndex() + 1); // +1 because default pirkko is at index 0
//    }

    private static List<Item> registerAllPirkkoItems() {
        List<Item> blocks = new ArrayList<>();
        blocks.add(DEFAULT_PIRKKO_ITEM);
        for (DyeColor color : DyeColor.values()) {
            blocks.add(registerPirkkoItem(color.getId() + "_pirkko", PIRKKO_BLOCK));
        }
        return blocks;
    }

    private static PirkkoBlock registerPirkkoBlock(String name) {
        var registryKey = Identifier.of(MOD_ID, name);
        var block = new PirkkoBlock(AbstractBlock.Settings.create()
            .mapColor(MapColor.BRIGHT_RED)
            .breakInstantly()
            .nonOpaque()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, registryKey)));
        Registry.register(Registries.BLOCK, registryKey, block);
        return block;
    }
    private static Item registerPirkkoItem(String name, PirkkoBlock block) {
        var registryKey = Identifier.of(MOD_ID, name);
//        var item = new PolymerBlockItem(block, new Item.Settings()
        var item = new PirkkoItem(block, new Item.Settings()
            .maxCount(65)
            .fireproof()
            .rarity(Rarity.EPIC)
            .equippable(EquipmentSlot.HEAD)
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, registryKey))
//            .useBlockPrefixedTranslationKey()
        );
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, name), item);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(item); // Default  version
            // Add all the variations
            for (String kind : Pirkko.PIRKKO_KINDS) {
                var stack = item.getDefaultStack();
                stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(List.of(), List.of(), List.of(kind), List.of()));
                entries.add(stack);
            }
        });
        return item;
    }

    private static SoundEvent registerSoundEvent(String name, SoundEvent soundEvent) {
        Identifier id = Identifier.of(MOD_ID, name);
        var event = Registry.register(Registries.SOUND_EVENT, id, new SoundEvent(id, Optional.empty()));
        PolymerSoundEvent.registerOverlay(event, soundEvent);
        RegistrySyncUtils.setServerEntry(Registries.SOUND_EVENT, event);
        return event;
    }
}
