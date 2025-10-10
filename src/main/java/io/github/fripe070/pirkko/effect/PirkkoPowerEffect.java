package io.github.fripe070.pirkko.effect;

import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import io.github.fripe070.pirkko.Pirkko;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.TintedParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class PirkkoPowerEffect extends StatusEffect implements PolymerStatusEffect {
    public PirkkoPowerEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0xff6f21);
    }

    @Override
    public @Nullable StatusEffect getPolymerReplacement(StatusEffect potion, PacketContext context) {
        return StatusEffects.INVISIBILITY.value();
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        entity.setInvisible(true);
//        entity.getBoundingBox().getLengthY()
        return super.applyUpdateEffect(world, entity, amplifier);
    }

    @Override
    public void onApplied(AttributeContainer attributeContainer, int amplifier) {
        Pirkko.LOGGER.info("Applied");
        super.onApplied(attributeContainer, amplifier);
    }
    @Override
    public void onRemoved(AttributeContainer attributeContainer) {
        Pirkko.LOGGER.info("Removed");
        super.onRemoved(attributeContainer);
    }
//        @Override
//    public ParticleEffect createParticle(StatusEffectInstance effect) {
//        return TintedParticleEffect.create(ParticleTypes.ENTITY_EFFECT, ColorHelper.withAlpha(50, this.getColor()));
//    }
}
