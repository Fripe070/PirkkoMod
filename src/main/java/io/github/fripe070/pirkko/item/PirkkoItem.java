package io.github.fripe070.pirkko.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import io.github.fripe070.pirkko.Pirkko;
import io.github.fripe070.pirkko.PirkkoKind;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

// TODO: Render kind as a tooltip
public class PirkkoItem extends BlockItem implements PolymerItem {
    public PirkkoItem(Block block, Settings settings) {
        super(block, settings);
    }

    public static ItemStack getStack(PirkkoKind kind) {
        var itemStack = Pirkko.DEFAULT_PIRKKO_ITEM.getDefaultStack();
        var oldModelData = itemStack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        if (oldModelData == null) {
            oldModelData = new CustomModelDataComponent(List.of(), List.of(), List.of(), List.of());
        }
        if (kind == null) {
            kind = PirkkoKind.BLANK;
        }
        itemStack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(
                oldModelData.floats(),
                oldModelData.flags(),
                List.of(kind.asString()),
                oldModelData.colors()));
        return itemStack;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.TRIAL_KEY;
    }

    @Override
    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return PolymerItem.super.getPolymerItemModel(stack, context);
    }

    @Override
    public ActionResult place(ItemPlacementContext context) {
        return super.place(context);
    }

    public static String getPirkkoKindString(ItemStack stack) {
        var modelData = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        if (modelData == null || modelData.strings().isEmpty()) {
            return "standard";
        }
        var kind = modelData.strings().getFirst();
        return kind;
    }
    public static PirkkoKind getPirkkoKind(ItemStack stack) {
        var pirkkoKindString = getPirkkoKindString(stack);
        var pirkkoKind = PirkkoKind.fromName(pirkkoKindString);
        if (pirkkoKind == null) {
            return PirkkoKind.BLANK;
        }
        return pirkkoKind;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context) {
        var stack = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, context);
        String kind = getPirkkoKindString(stack);
        Text translationName = stack.get(DataComponentTypes.ITEM_NAME);
        if (!kind.equals("standard")) {
            translationName = Text.translatable("item.pirkko." + kind + "_pirkko");
        }
        stack.set(DataComponentTypes.ITEM_NAME, translationName);
        return stack;
    }

    @Override
    protected @Nullable BlockState getPlacementState(ItemPlacementContext context) {
        BlockState blockState = this.getBlock().getPlacementState(context);
        return blockState != null && this.canPlace(context, blockState) ? blockState : null;
//        return super.getPlacementState(context);
    }

    @Override
    public void modifyClientTooltip(List<Text> tooltip, ItemStack stack, PacketContext context) {
        PolymerItem.super.modifyClientTooltip(tooltip, stack, context);
    }
}