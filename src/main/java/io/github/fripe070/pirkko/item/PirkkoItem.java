package io.github.fripe070.pirkko.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class PirkkoItem extends SimplePolymerItem {
    private static final Identifier MODEL_ID = Identifier.of("pirkko", "item/pirkko");

    public PirkkoItem(Settings settings) {
        super(settings, Items.GLOW_INK_SAC);
    }

    @Override
    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return MODEL_ID;
    }
}
