package io.github.fripe070.pirkko;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
import eu.pb4.polymer.core.api.other.PolymerStat;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import io.github.fripe070.pirkko.block.PirkkoBlock;
import io.github.fripe070.pirkko.criterion.PirkkoClickCriterion;
import io.github.fripe070.pirkko.criterion.PirkkoTransferCriterion;
import io.github.fripe070.pirkko.effect.PirkkoPowerEffect;
import io.github.fripe070.pirkko.item.PirkkoItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.StatFormatter;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

public class Pirkko implements ModInitializer {
    public static final String MOD_ID = "pirkko";
    public static final Logger LOGGER = LoggerFactory.getLogger("pirkko");

    public static final PirkkoBlock PIRKKO_BLOCK = registerPirkkoBlock("pirkko");
    public static final PirkkoItem PIRKKO_ITEM = registerPirkkoItem("pirkko", PIRKKO_BLOCK);

    public static final ItemGroup ITEM_GROUP = PolymerItemGroupUtils.builder()
        .displayName(Text.translatable("itemgroup.pirkko"))
        .icon(() -> new ItemStack(PIRKKO_ITEM))
        .entries((context, entries) -> {
            for (PirkkoKind kind : PirkkoKind.values())
                entries.add(PirkkoItem.getStack(kind));
        })
        .build();

    public static final SoundEvent DEFAULT_PIRKKO_SOUND = registerSoundEvent("pirkko/pirkko", SoundEvents.ENTITY_COD_FLOP);
    public static final StatusEffect PIRKKO_POWER = new PirkkoPowerEffect();

    public static final Identifier PIRKKO_CLICK_STAT = PolymerStat.registerStat(id("interact_with_pirkko"), StatFormatter.DEFAULT);
    public static final Identifier PIRKKO_TRANSFER_STAT = PolymerStat.registerStat(id("transfer_pirkko"), StatFormatter.DEFAULT);

    public static final PirkkoClickCriterion CLICK_PIRKKO = Criteria.register(id("click_pirkko").toString(), new PirkkoClickCriterion());
    public static final PirkkoTransferCriterion TRANSFER_PIRKKO = Criteria.register(id("transfer_pirkko").toString(), new PirkkoTransferCriterion());

    @Override
    public void onInitialize() {
        PolymerResourcePackUtils.addModAssets(MOD_ID);
        PolymerResourcePackUtils.markAsRequired();

        PolymerItemGroupUtils.registerPolymerItemGroup(id("item_group"), ITEM_GROUP);
        Registry.register(Registries.STATUS_EFFECT, id("pirkko_power"), PIRKKO_POWER);

        for (PirkkoKind kind : PirkkoKind.values()) {
            if (!kind.usesCustomSound()) continue;
            var sound = kind.getSound();
            Registry.register(Registries.SOUND_EVENT, sound.id(), sound);
            PolymerSoundEvent.registerOverlay(sound);
        }
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    private static PirkkoBlock registerPirkkoBlock(String name) {
        var registryKey = id(name);
        var block = new PirkkoBlock(AbstractBlock.Settings.create()
            .mapColor(MapColor.BRIGHT_RED)
            .breakInstantly()
            .nonOpaque()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, registryKey)));
        Registry.register(Registries.BLOCK, registryKey, block);
        return block;
    }

    private static PirkkoItem registerPirkkoItem(String name, PirkkoBlock block) {
        var registryKey = id(name);
        var item = new PirkkoItem(block, new Item.Settings()
            .maxCount(63)
            .fireproof()
            .equippableUnswappable(EquipmentSlot.HEAD)
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, registryKey))
            .useBlockPrefixedTranslationKey()
        );
        Registry.register(Registries.ITEM, id(name), item);
        return item;
    }

    private static SoundEvent registerSoundEvent(String name, SoundEvent soundEvent) {
        Identifier id = id(name);
        var event = Registry.register(Registries.SOUND_EVENT, id, new SoundEvent(id, Optional.empty()));
        PolymerSoundEvent.registerOverlay(event, soundEvent);
        RegistrySyncUtils.setServerEntry(Registries.SOUND_EVENT, event);
        return event;
    }
}
