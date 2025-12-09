package io.github.fripe070.pirkko.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PirkkoTransferCriterion extends SimpleCriterionTrigger<PirkkoTransferCriterion.@NotNull Conditions> {
    @Override
    public @NotNull Codec<PirkkoTransferCriterion.Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer player, int totalTransfers) {
        trigger(player, conditions -> conditions.requirementsMet(totalTransfers));
    }

    public record Conditions(Optional<ContextAwarePredicate> playerPredicate, int requiredTransfers) implements SimpleCriterionTrigger.SimpleInstance {
        public static Codec<PirkkoTransferCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
            Codec.INT.fieldOf("requiredTransfers").forGetter(Conditions::requiredTransfers)
        ).apply(instance, Conditions::new));

        @Override
        public @NotNull Optional<ContextAwarePredicate> player() {
            return playerPredicate;
        }

        public boolean requirementsMet(int totalTransfers) {
            return totalTransfers >= requiredTransfers;
        }
    }
}
