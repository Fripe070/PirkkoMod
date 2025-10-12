package io.github.fripe070.pirkko.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public class PirkkoTransferCriterion extends AbstractCriterion<PirkkoTransferCriterion.Conditions> {
    @Override
    public Codec<PirkkoTransferCriterion.Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, int totalTransfers) {
        trigger(player, conditions -> conditions.requirementsMet(totalTransfers));
    }

    public record Conditions(Optional<LootContextPredicate> playerPredicate, int requiredTransfers) implements AbstractCriterion.Conditions {
        public static Codec<PirkkoTransferCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
            Codec.INT.fieldOf("requiredTransfers").forGetter(Conditions::requiredTransfers)
        ).apply(instance, Conditions::new));

        @Override
        public Optional<LootContextPredicate> player() {
            return playerPredicate;
        }

        public boolean requirementsMet(int totalTransfers) {
            return totalTransfers >= requiredTransfers;
        }
    }
}
