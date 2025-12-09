package io.github.fripe070.pirkko.datagen;

import io.github.fripe070.pirkko.Pirkko;
import io.github.fripe070.pirkko.PirkkoKind;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.properties.select.CustomModelDataProperty;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModelProvider extends FabricModelProvider {
    public ModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateItemModels(@NotNull ItemModelGenerators itemModelGenerator) {
        var blankPirkkoId = Pirkko.id("item/pirkko/base");
        var basePirkkoModel = new ModelTemplate(Optional.of(blankPirkkoId), Optional.empty(), TextureSlot.TEXTURE);

        List<SelectItemModel.SwitchCase<@NotNull String>> pirkkoModels = new ArrayList<>();
        for (PirkkoKind kind : PirkkoKind.values()) {
            // Skip the default blank kind
            if (kind == PirkkoKind.BLANK) continue;
            Identifier modelId;
            if (kind.usesCustomModel()) {
                modelId = Pirkko.id("item/pirkko/" + kind.getPath());
            } else {
                modelId = basePirkkoModel.create(
                    ModelLocationUtils.getModelLocation(Pirkko.PIRKKO_ITEM, "/" + kind.getPath()),
                    TextureMapping.defaultTexture(TextureMapping.getItemTexture(Pirkko.PIRKKO_ITEM, "/" + kind.getPath())),
                    itemModelGenerator.modelOutput
                );
            }

            ItemModel.Unbaked model = ItemModelUtils.plainModel(modelId);
            pirkkoModels.add(ItemModelUtils.when(kind.getId(), model));
        }

        // Create the json file at models/item/pirkko.json
        itemModelGenerator.itemModelOutput.accept(
            Pirkko.PIRKKO_ITEM,
            ItemModelUtils.select(
                new CustomModelDataProperty(0),
                ItemModelUtils.plainModel(blankPirkkoId),
                pirkkoModels
            )
        );
    }

    @Override
    public void generateBlockStateModels(@NotNull BlockModelGenerators blockStateModelGenerator) {
    }
}
