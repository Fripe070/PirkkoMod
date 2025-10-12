package io.github.fripe070.pirkko.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public class PirkkoClickCriterion extends AbstractCriterion<PirkkoClickCriterion.Conditions> {
    @Override
    public Codec<PirkkoClickCriterion.Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, int totalClicks) {
        trigger(player, conditions -> conditions.requirementsMet(totalClicks));
    }

    public record Conditions(Optional<LootContextPredicate> playerPredicate, int requiredClicks) implements AbstractCriterion.Conditions {
        public static Codec<PirkkoClickCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
            Codec.INT.fieldOf("requiredTransfers").forGetter(Conditions::requiredClicks)
        ).apply(instance, Conditions::new));

        @Override
        public Optional<LootContextPredicate> player() {
            return playerPredicate;
        }

        public boolean requirementsMet(int totalClicks) {
            return totalClicks >= requiredClicks;
        }
    }
}
