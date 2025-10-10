package io.github.fripe070.pirkko.datagen;

import io.github.fripe070.pirkko.Pirkko;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.*;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.SelectItemModel;
import net.minecraft.client.render.item.property.select.CustomModelDataStringProperty;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ModelProvider extends FabricModelProvider {
    public ModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
    }


    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        var standardPirkkoId = Identifier.of(Pirkko.MOD_ID, "item/pirkko/standard");
        var pirkkoModel = new Model(Optional.of(standardPirkkoId), Optional.empty(), TextureKey.TEXTURE);

        List<SelectItemModel.SwitchCase<String>> pirkkoModels = new ArrayList<>();
        for (String kind : Pirkko.PIRKKO_KINDS) {
            var subModel = pirkkoModel.upload(
                ModelIds.getItemSubModelId(Pirkko.DEFAULT_PIRKKO_ITEM, "/" + kind),
                TextureMap.texture(TextureMap.getSubId(Pirkko.DEFAULT_PIRKKO_ITEM, "/" + kind)),
                itemModelGenerator.modelCollector
            );
            ItemModel.Unbaked model = ItemModels.basic(subModel);
            pirkkoModels.add(ItemModels.switchCase(kind, model));
        }

        // Create the json file at models/item/pirkko.json
        itemModelGenerator.output.accept(
            Pirkko.DEFAULT_PIRKKO_ITEM,
            ItemModels.select(
                new CustomModelDataStringProperty(0),
                ItemModels.basic(standardPirkkoId),
                pirkkoModels
            )
        );
    }
}
