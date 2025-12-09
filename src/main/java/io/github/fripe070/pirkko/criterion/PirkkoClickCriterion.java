package io.github.fripe070.pirkko.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PirkkoClickCriterion extends SimpleCriterionTrigger<PirkkoClickCriterion.@NotNull Conditions> {
    @Override
    public @NotNull Codec<PirkkoClickCriterion.Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer player, int totalClicks) {
        trigger(player, conditions -> conditions.requirementsMet(totalClicks));
    }

    public record Conditions(Optional<ContextAwarePredicate> playerPredicate, int requiredClicks) implements SimpleCriterionTrigger.SimpleInstance {
        public static Codec<PirkkoClickCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
            Codec.INT.fieldOf("requiredTransfers").forGetter(Conditions::requiredClicks)
        ).apply(instance, Conditions::new));

        @Override
        public @NotNull Optional<ContextAwarePredicate> player() {
            return playerPredicate;
        }

        public boolean requirementsMet(int totalClicks) {
            return totalClicks >= requiredClicks;
        }
    }
}
