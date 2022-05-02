package net.minestom.arena.game.mob;

import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record Kit(@NotNull ItemStack[] inventory, @Nullable ItemStack helmet, @Nullable ItemStack chestplate, @Nullable ItemStack leggings, @Nullable ItemStack boots) {
    public static final Tag<Boolean> KIT_ITEM_TAG = Tag.Boolean("kit_item").defaultValue(false);

    public void apply(Player player) {
        final PlayerInventory playerInventory = player.getInventory();
        final ItemStack[] items = playerInventory.getItemStacks();

        for (int i = 0; i < items.length; i++) {
            if (items[i].getTag(KIT_ITEM_TAG)) {
                player.getInventory().setItemStack(i, ItemStack.AIR);
            }
        }

        for (ItemStack item : inventory) {
            playerInventory.addItemStack(item.withTag(KIT_ITEM_TAG, true));
        }

        if (helmet == null) player.setHelmet(ItemStack.AIR);
        else player.setHelmet(helmet.withTag(KIT_ITEM_TAG, true));
        if (chestplate == null) player.setChestplate(ItemStack.AIR);
        else player.setChestplate(chestplate.withTag(KIT_ITEM_TAG, true));
        if (leggings == null) player.setLeggings(ItemStack.AIR);
        else player.setLeggings(leggings.withTag(KIT_ITEM_TAG, true));
        if (boots == null) player.setBoots(ItemStack.AIR);
        else player.setBoots(boots.withTag(KIT_ITEM_TAG, true));
    }
}
