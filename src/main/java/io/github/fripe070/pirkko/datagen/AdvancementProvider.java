package io.github.fripe070.pirkko.datagen;

import io.github.fripe070.pirkko.Pirkko;
import io.github.fripe070.pirkko.criterion.PirkkoClickCriterion;
import io.github.fripe070.pirkko.criterion.PirkkoTransferCriterion;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.*;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AdvancementProvider extends FabricAdvancementProvider {
    protected AdvancementProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(output, registryLookup);
    }

    @Override
    public void generateAdvancement(RegistryWrapper.WrapperLookup wrapperLookup, Consumer<AdvancementEntry> consumer) {
        AdvancementEntry root = Advancement.Builder.create().build(Identifier.ofVanilla("adventure/root"));
        AdvancementEntry pirkkoRoot = Advancement.Builder.create()
            .parent(root)
            .display(
                Pirkko.PIRKKO_ITEM,
                Text.translatable("advancements.pirkko.root.title"),
                Text.translatable("advancements.pirkko.root.description"),
                Identifier.ofVanilla("textures/gui/advancements/block/red_terracotta.png"),
                AdvancementFrame.TASK,
                true,
                true,
                false
            )
            .criterion("obtained_pirkko", InventoryChangedCriterion.Conditions.items(Pirkko.PIRKKO_ITEM))
            .build(consumer, Pirkko.id("root").toString());

        this.registerClickGoals(consumer, pirkkoRoot);
        this.registerTransferGoals(consumer, pirkkoRoot);
    }
    private void registerClickGoals(Consumer<AdvancementEntry> consumer, AdvancementEntry root) {
        int[] clickGoals = {1, 100, 1_000, 5_000, 10_000, 100_000, 1_000_000};
        AdvancementEntry pirkkoClickParent = root;
        for (int goal : clickGoals) {
            AdvancementFrame frame = AdvancementFrame.TASK;
            if (goal >= 1_000) frame = AdvancementFrame.GOAL;
            if (goal >= 100_000) frame = AdvancementFrame.CHALLENGE;

            pirkkoClickParent = Advancement.Builder.create()
                .parent(pirkkoClickParent)
                .display(
                    Pirkko.PIRKKO_ITEM,
                    Text.translatable("advancements.pirkko.click_" + goal + "_pirkko.title"),
                    Text.translatable("advancements.pirkko.click_" + goal + "_pirkko.description"),
                    Identifier.ofVanilla("textures/gui/advancements/block/red_terracotta.png"),
                    frame,
                    true,
                    true,
                    goal > 100
                )
                .criterion("clicked_" + goal + "_pirkko", Pirkko.CLICK_PIRKKO.create(
                    new PirkkoClickCriterion.Conditions(Optional.empty(), goal)))
                .build(consumer, Pirkko.id("click_" + goal + "_pirkko").toString());
        }
    }
    private void registerTransferGoals(Consumer<AdvancementEntry> consumer, AdvancementEntry root) {
        int[] transferGoals = {1, 5, 10, 20, 50};
        AdvancementEntry pirkkoTransferParent = root;
        for (int goal : transferGoals) {
            AdvancementFrame frame = AdvancementFrame.TASK;
            if (goal >= 10) frame = AdvancementFrame.GOAL;

            pirkkoTransferParent = Advancement.Builder.create()
                .parent(pirkkoTransferParent)
                .display(
                    Pirkko.PIRKKO_ITEM,
                    Text.translatable("advancements.pirkko.transfer_" + goal + "_pirkko.title"),
                    Text.translatable("advancements.pirkko.transfer_" + goal + "_pirkko.description"),
                    Identifier.ofVanilla("textures/gui/advancements/block/red_terracotta.png"),
                    frame,
                    true,
                    goal > 5,
                    false
                )
                .criterion("transferred_" + goal + "_pirkko", Pirkko.TRANSFER_PIRKKO.create(
                    new PirkkoTransferCriterion.Conditions(Optional.empty(), goal)))
                .build(consumer, Pirkko.id("transferred_" + goal + "_pirkko").toString());
        }
    }
}