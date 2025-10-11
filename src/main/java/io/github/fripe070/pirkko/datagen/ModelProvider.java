package io.github.fripe070.pirkko.datagen;

import io.github.fripe070.pirkko.Pirkko;
import io.github.fripe070.pirkko.PirkkoKind;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.*;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.SelectItemModel;
import net.minecraft.client.render.item.property.select.CustomModelDataStringProperty;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModelProvider extends FabricModelProvider {
    public ModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        var blankPirkkoId = Identifier.of(Pirkko.MOD_ID, "item/pirkko/base");
        var pirkkoModel = new Model(Optional.of(blankPirkkoId), Optional.empty(), TextureKey.TEXTURE);

        List<SelectItemModel.SwitchCase<String>> pirkkoModels = new ArrayList<>();
        for (PirkkoKind kind : PirkkoKind.values()) {
            // Skip the default blank kind
            if (kind == PirkkoKind.BLANK) continue;

            var subModel = pirkkoModel.upload(
                ModelIds.getItemSubModelId(Pirkko.PIRKKO_ITEM, "/" + kind.getPath()),
                TextureMap.texture(TextureMap.getSubId(Pirkko.PIRKKO_ITEM, "/" + kind.getPath())),
                itemModelGenerator.modelCollector
            );
            ItemModel.Unbaked model = ItemModels.basic(subModel);
            pirkkoModels.add(ItemModels.switchCase(kind.getId(), model));
        }

        // Create the json file at models/item/pirkko.json
        itemModelGenerator.output.accept(
            Pirkko.PIRKKO_ITEM,
            ItemModels.select(
                new CustomModelDataStringProperty(0),
                ItemModels.basic(blankPirkkoId),
                pirkkoModels
            )
        );
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
    }
}
