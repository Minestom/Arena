package net.minestom.arena.game.mob;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record Kit(@NotNull ItemStack[] inventory, @Nullable ItemStack helmet, @Nullable ItemStack chestplate, @Nullable ItemStack leggings, @Nullable ItemStack boots) {
    public static final Tag<Boolean> KIT_ITEM_TAG = Tag.Boolean("kit-item").defaultValue(false);

    public void apply(Player player) {
        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];

            if (item != null)
                player.getInventory().setItemStack(i, item.withTag(KIT_ITEM_TAG, true));
        }

        if (helmet != null) player.setHelmet(helmet.withTag(KIT_ITEM_TAG, true));
        if (chestplate != null) player.setChestplate(chestplate.withTag(KIT_ITEM_TAG, true));
        if (leggings != null) player.setLeggings(leggings.withTag(KIT_ITEM_TAG, true));
        if (boots != null)  player.setBoots(boots.withTag(KIT_ITEM_TAG, true));
    }
}
