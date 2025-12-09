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
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.StatFormatter;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class Pirkko implements ModInitializer {
    public static final String MOD_ID = "pirkko";
    public static final Logger LOGGER = LoggerFactory.getLogger("pirkko");

    public static final PirkkoBlock PIRKKO_BLOCK = registerPirkkoBlock("pirkko");
    public static final PirkkoItem PIRKKO_ITEM = registerPirkkoItem("pirkko", PIRKKO_BLOCK);

    public static final CreativeModeTab ITEM_GROUP = PolymerItemGroupUtils.builder()
        .title(Component.translatable("itemgroup.pirkko"))
        .icon(() -> new ItemStack(PIRKKO_ITEM))
        .displayItems((context, entries) -> {
            for (PirkkoKind kind : PirkkoKind.values())
                entries.accept(PirkkoItem.getStack(kind));
        })
        .build();

    public static final SoundEvent DEFAULT_PIRKKO_SOUND = registerSoundEvent("pirkko/pirkko", SoundEvents.COD_FLOP);
    public static final MobEffect PIRKKO_POWER = new PirkkoPowerEffect();

    public static final Identifier PIRKKO_CLICK_STAT = PolymerStat.registerStat(id("interact_with_pirkko"), StatFormatter.DEFAULT);
    public static final Identifier PIRKKO_TRANSFER_STAT = PolymerStat.registerStat(id("transfer_pirkko"), StatFormatter.DEFAULT);

    public static final PirkkoClickCriterion CLICK_PIRKKO = CriteriaTriggers.register(id("click_pirkko").toString(), new PirkkoClickCriterion());
    public static final PirkkoTransferCriterion TRANSFER_PIRKKO = CriteriaTriggers.register(id("transfer_pirkko").toString(), new PirkkoTransferCriterion());

    @Override
    public void onInitialize() {
        PolymerResourcePackUtils.addModAssets(MOD_ID);
        PolymerResourcePackUtils.markAsRequired();

        PolymerItemGroupUtils.registerPolymerItemGroup(id("pirkko_group"), ITEM_GROUP);
        Registry.register(BuiltInRegistries.MOB_EFFECT, id("pirkko_power"), PIRKKO_POWER);

        for (PirkkoKind kind : PirkkoKind.values()) {
            if (!kind.usesCustomSound()) continue;
            var sound = kind.getSound();
            Registry.register(BuiltInRegistries.SOUND_EVENT, sound.location(), sound);
            PolymerSoundEvent.registerOverlay(sound);
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static PirkkoBlock registerPirkkoBlock(String name) {
        var registryKey = id(name);
        var block = new PirkkoBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.FIRE)
            .instabreak()
            .noOcclusion()
            .setId(ResourceKey.create(Registries.BLOCK, registryKey)));
        Registry.register(BuiltInRegistries.BLOCK, registryKey, block);
        return block;
    }

    private static PirkkoItem registerPirkkoItem(String name, PirkkoBlock block) {
        var registryKey = id(name);
        var item = new PirkkoItem(block, new Item.Properties()
            .stacksTo(63)
            .fireResistant()
            .equippableUnswappable(EquipmentSlot.HEAD)
            .setId(ResourceKey.create(Registries.ITEM, registryKey))
            .useBlockDescriptionPrefix()
        );
        Registry.register(BuiltInRegistries.ITEM, id(name), item);
        return item;
    }

    private static SoundEvent registerSoundEvent(String name, SoundEvent soundEvent) {
        Identifier id = id(name);
        var event = Registry.register(BuiltInRegistries.SOUND_EVENT, id, new SoundEvent(id, Optional.empty()));
        PolymerSoundEvent.registerOverlay(event, soundEvent);
        RegistrySyncUtils.setServerEntry(BuiltInRegistries.SOUND_EVENT, event);
        return event;
    }
}
