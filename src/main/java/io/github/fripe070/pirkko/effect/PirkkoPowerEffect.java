package io.github.fripe070.pirkko.effect;

import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.world.ServerWorld;
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
        return super.applyUpdateEffect(world, entity, amplifier);
    }
}
