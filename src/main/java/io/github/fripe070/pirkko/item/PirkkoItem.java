package io.github.fripe070.pirkko.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import io.github.fripe070.pirkko.Pirkko;
import io.github.fripe070.pirkko.PirkkoKind;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

// TODO: Render kind as a tooltip
public class PirkkoItem extends BlockItem implements PolymerItem {
    public PirkkoItem(Block block, Properties settings) {
        super(block, settings);
    }

    public static ItemStack getStack(@NotNull PirkkoKind kind) {
        var stack = Pirkko.PIRKKO_ITEM.getDefaultInstance();
        var oldModelData = stack.getOrDefault(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(), List.of()));
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(
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
        if (stack.isEmpty() || !stack.is(Pirkko.PIRKKO_ITEM)) {
            return null;
        }
        var modelData = stack.get(DataComponents.CUSTOM_MODEL_DATA);
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
    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context) {
        var stack = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, context);
        PirkkoKind kind = getPirkkoKind(itemStack);
        if (kind == null) return stack; // Should never happen
        stack.set(DataComponents.RARITY, kind.getRarity());

        Component translationName = stack.get(DataComponents.ITEM_NAME);
        if (!kind.equals(PirkkoKind.BLANK)) {
            translationName = Component.translatable(this.descriptionId + "." + kind.getTranslationKey());
        }
        stack.set(DataComponents.ITEM_NAME, translationName);
        return stack;
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack pirkkoStack, @NotNull Player user, @NotNull LivingEntity entity, @NotNull InteractionHand hand) {
        InteractionResult result = super.interactLivingEntity(pirkkoStack, user, entity, hand);
        if (result != InteractionResult.PASS) return result;
        if (!(entity instanceof Player target)) return result;

        PirkkoKind kind = getPirkkoKind(pirkkoStack);
        if (kind == null) return InteractionResult.PASS;
        String kindTransformed = kindTransform(kind);

        var inventory = target.getInventory();
        for (ItemStack slotStack : inventory) {
            PirkkoKind slotKind = getPirkkoKind(slotStack);
            if (slotKind == null) continue; // Not a Pirkko
            if (!kindTransformed.equals(kindTransform(slotKind))) continue; // Different kind of pirkko

            user.level().playSound(user, user.blockPosition(), SoundEvents.VAULT_CLOSE_SHUTTER, SoundSource.PLAYERS, 0.4F, 1);
            user.displayClientMessage(Component.translatable("message.pirkko.already_have"), true);
            return InteractionResult.FAIL;
        }

        boolean insertSuccess = inventory.add(pirkkoStack.copyWithCount(1));
        if (insertSuccess) {
            if (!user.getAbilities().instabuild) {
                pirkkoStack.shrink(1);
            }
            user.level().playSound(null, user.blockPosition(), kind.getSound(), SoundSource.PLAYERS, 0.1F, 1);
            target.displayClientMessage(Component.translatable("message.pirkko.received"), true);

            user.awardStat(Pirkko.PIRKKO_TRANSFER_STAT);
            Pirkko.TRANSFER_PIRKKO.trigger((ServerPlayer) user,
                ((ServerPlayer) user).getStats().getValue(Stats.CUSTOM.get(Pirkko.PIRKKO_TRANSFER_STAT)));
        }

        return InteractionResult.CONSUME;
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