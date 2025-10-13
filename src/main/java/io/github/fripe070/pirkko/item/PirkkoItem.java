package io.github.fripe070.pirkko.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import io.github.fripe070.pirkko.Pirkko;
import io.github.fripe070.pirkko.PirkkoKind;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    public static @Nullable PirkkoKind getPirkkoKind(ItemStack stack) {
        if (stack.isEmpty() || !stack.isOf(Pirkko.PIRKKO_ITEM)) {
            return null;
        }
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
        PirkkoKind kind = getPirkkoKind(itemStack);
        if (kind == null) return stack; // Should never happen
        stack.set(DataComponentTypes.RARITY, kind.getRarity());

        Text translationName = stack.get(DataComponentTypes.ITEM_NAME);
        if (!kind.equals(PirkkoKind.BLANK)) {
            translationName = Text.translatable(this.translationKey + "." + kind.getTranslationKey());
        }
        stack.set(DataComponentTypes.ITEM_NAME, translationName);
        return stack;
    }

    @Override
    public ActionResult useOnEntity(ItemStack pirkkoStack, PlayerEntity user, LivingEntity entity, Hand hand) {
        ActionResult result = super.useOnEntity(pirkkoStack, user, entity, hand);
        if (result != ActionResult.PASS) return result;
        if (!(entity instanceof PlayerEntity target)) return result;

        PirkkoKind kind = getPirkkoKind(pirkkoStack);
        if (kind == null) return ActionResult.PASS;
        String kindTransformed = kindTransform(kind);

        var inventory = target.getInventory();
        for (ItemStack slotStack : inventory) {
            PirkkoKind slotKind = getPirkkoKind(slotStack);
            if (slotKind == null) continue; // Not a Pirkko
            if (!kindTransformed.equals(kindTransform(slotKind))) continue; // Different kind of pirkko

            user.getEntityWorld().playSound(user, user.getBlockPos(), SoundEvents.BLOCK_VAULT_CLOSE_SHUTTER, SoundCategory.PLAYERS, 0.4F, 1);
            user.sendMessage(Text.translatable("message.pirkko.already_have"), true);
            return ActionResult.FAIL;
        }

        boolean insertSuccess = inventory.insertStack(pirkkoStack.copyWithCount(1));
        if (insertSuccess) {
            if (!user.getAbilities().creativeMode) {
                pirkkoStack.decrement(1);
            }
            user.getEntityWorld().playSound(null, user.getBlockPos(), kind.getSound(), SoundCategory.PLAYERS, 0.1F, 1);
            target.sendMessage(Text.translatable("message.pirkko.received"), true);

            user.incrementStat(Pirkko.PIRKKO_TRANSFER_STAT);
            Pirkko.TRANSFER_PIRKKO.trigger((ServerPlayerEntity) user,
                ((ServerPlayerEntity) user).getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Pirkko.PIRKKO_TRANSFER_STAT)));
        }

        return ActionResult.CONSUME;
    }

    /**
     * Transform the kind to a string used for loose equivalence checking.
     * All colors will for example return the same
     */
    private static String kindTransform(PirkkoKind kind) {
        if (kind == null) return "blank";
        return kind.getPath().split("/", 2)[0];
    }
}