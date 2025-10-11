package io.github.fripe070.pirkko.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import io.github.fripe070.pirkko.Pirkko;
import io.github.fripe070.pirkko.PirkkoKind;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

// TODO: Render kind as a tooltip
public class PirkkoItem extends BlockItem implements PolymerItem {
    public PirkkoItem(Block block, Settings settings) {
        super(block, settings);
    }

    public static ItemStack getStack(@NotNull PirkkoKind kind) {
        var stack = Pirkko.PIRKKO_ITEM.getDefaultStack();
        var oldModelData = stack.getOrDefault(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(List.of(), List.of(), List.of(), List.of()));
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(
                oldModelData.floats(),
                oldModelData.flags(),
                List.of(kind.getId()),
                oldModelData.colors()));
        return stack;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.TRIAL_KEY;
    }

    public static @NotNull PirkkoKind getPirkkoKind(ItemStack stack) {
        var modelData = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        if (modelData == null || modelData.strings().isEmpty()) {
            return PirkkoKind.BLANK;
        }
        var pirkkoKind = PirkkoKind.fromId(modelData.strings().getFirst());
        if (pirkkoKind == null) {
            return PirkkoKind.BLANK;
        }
        return pirkkoKind;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context) {
        var stack = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, context);
        PirkkoKind kind = getPirkkoKind(stack);
        stack.set(DataComponentTypes.RARITY, kind.getRarity());

        Text translationName = stack.get(DataComponentTypes.ITEM_NAME);
        if (!kind.equals(PirkkoKind.BLANK)) {
            translationName = Text.translatable(this.translationKey + "." + kind.getTranslationKey());
        }
        stack.set(DataComponentTypes.ITEM_NAME, translationName);
        return stack;
    }
}