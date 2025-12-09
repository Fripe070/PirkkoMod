package io.github.fripe070.pirkko.effect;

import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import io.github.fripe070.pirkko.Pirkko;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class PirkkoPowerEffect extends MobEffect implements PolymerStatusEffect {
    public PirkkoPowerEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xff6f21);
    }

    @Override
    public @Nullable MobEffect getPolymerReplacement(MobEffect potion, PacketContext context) {
        return MobEffects.INVISIBILITY.value();
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(@NotNull ServerLevel world, LivingEntity entity, int amplifier) {
        entity.setInvisible(true);
//        entity.getBoundingBox().getLengthY()
        return super.applyEffectTick(world, entity, amplifier);
    }

    @Override
    public void addAttributeModifiers(@NotNull AttributeMap attributeContainer, int amplifier) {
        Pirkko.LOGGER.info("Applied");
        super.addAttributeModifiers(attributeContainer, amplifier);
    }
    @Override
    public void removeAttributeModifiers(@NotNull AttributeMap attributeContainer) {
        Pirkko.LOGGER.info("Removed");
        super.removeAttributeModifiers(attributeContainer);
    }
//        @Override
//    public ParticleEffect createParticle(StatusEffectInstance effect) {
//        return TintedParticleEffect.create(ParticleTypes.ENTITY_EFFECT, ColorHelper.withAlpha(50, this.getColor()));
//    }
}
