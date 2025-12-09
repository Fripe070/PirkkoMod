package io.github.fripe070.pirkko.datagen;

import io.github.fripe070.pirkko.Pirkko;
import io.github.fripe070.pirkko.criterion.PirkkoClickCriterion;
import io.github.fripe070.pirkko.criterion.PirkkoTransferCriterion;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AdvancementProvider extends FabricAdvancementProvider {
    protected AdvancementProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup);
    }

    @Override
    public void generateAdvancement(HolderLookup.@NotNull Provider wrapperLookup, @NotNull Consumer<AdvancementHolder> consumer) {
        AdvancementHolder root = Advancement.Builder.advancement().build(Identifier.withDefaultNamespace("adventure/root"));
        AdvancementHolder pirkkoRoot = Advancement.Builder.advancement()
            .parent(root)
            .display(
                Pirkko.PIRKKO_ITEM,
                Component.translatable("advancements.pirkko.root.title"),
                Component.translatable("advancements.pirkko.root.description"),
                Identifier.withDefaultNamespace("textures/gui/advancements/block/red_terracotta.png"),
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .addCriterion("obtained_pirkko", InventoryChangeTrigger.TriggerInstance.hasItems(Pirkko.PIRKKO_ITEM))
            .save(consumer, Pirkko.id("root").toString());

        this.registerClickGoals(consumer, pirkkoRoot);
        this.registerTransferGoals(consumer, pirkkoRoot);
    }
    private void registerClickGoals(Consumer<AdvancementHolder> consumer, AdvancementHolder root) {
        int[] clickGoals = {1, 100, 1_000, 5_000, 10_000, 100_000, 1_000_000};
        AdvancementHolder pirkkoClickParent = root;
        for (int goal : clickGoals) {
            AdvancementType frame = AdvancementType.TASK;
            if (goal >= 1_000) frame = AdvancementType.GOAL;
            if (goal >= 100_000) frame = AdvancementType.CHALLENGE;

            pirkkoClickParent = Advancement.Builder.advancement()
                .parent(pirkkoClickParent)
                .display(
                    Pirkko.PIRKKO_ITEM,
                    Component.translatable("advancements.pirkko.click_" + goal + "_pirkko.title"),
                    Component.translatable("advancements.pirkko.click_" + goal + "_pirkko.description"),
                    Identifier.withDefaultNamespace("textures/gui/advancements/block/red_terracotta.png"),
                    frame,
                    true,
                    true,
                    goal > 100
                )
                .addCriterion("clicked_" + goal + "_pirkko", Pirkko.CLICK_PIRKKO.createCriterion(
                    new PirkkoClickCriterion.Conditions(Optional.empty(), goal)))
                .save(consumer, Pirkko.id("click_" + goal + "_pirkko").toString());
        }
    }
    private void registerTransferGoals(Consumer<AdvancementHolder> consumer, AdvancementHolder root) {
        int[] transferGoals = {1, 5, 10, 20, 50};
        AdvancementHolder pirkkoTransferParent = root;
        for (int goal : transferGoals) {
            AdvancementType frame = AdvancementType.TASK;
            if (goal >= 10) frame = AdvancementType.GOAL;

            pirkkoTransferParent = Advancement.Builder.advancement()
                .parent(pirkkoTransferParent)
                .display(
                    Pirkko.PIRKKO_ITEM,
                    Component.translatable("advancements.pirkko.transfer_" + goal + "_pirkko.title"),
                    Component.translatable("advancements.pirkko.transfer_" + goal + "_pirkko.description"),
                    Identifier.withDefaultNamespace("textures/gui/advancements/block/red_terracotta.png"),
                    frame,
                    true,
                    goal > 5,
                    false
                )
                .addCriterion("transferred_" + goal + "_pirkko", Pirkko.TRANSFER_PIRKKO.createCriterion(
                    new PirkkoTransferCriterion.Conditions(Optional.empty(), goal)))
                .save(consumer, Pirkko.id("transferred_" + goal + "_pirkko").toString());
        }
    }
}