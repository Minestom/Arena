package net.minestom.arena.game.mob;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.TransactionOption;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

record Kit(@NotNull List<ItemStack> inventory, Map<EquipmentSlot, ItemStack> equipments) {
    public static final Tag<Boolean> KIT_ITEM_TAG = Tag.Boolean("kit_item").defaultValue(false);

    Kit {
        inventory = List.copyOf(inventory);
        equipments = Map.copyOf(equipments);
    }

    public void apply(Player player) {
        final PlayerInventory playerInventory = player.getInventory();
        // Clear previous kit items
        for (ItemStack item : playerInventory.getItemStacks()) {
            if (item.getTag(KIT_ITEM_TAG))
                player.getInventory().takeItemStack(item, TransactionOption.ALL);
        }

        // Equipment
        for (EquipmentSlot slot : EquipmentSlot.armors()) {
            final ItemStack item = equipments.get(slot);
            if (item != null) player.setEquipment(slot, item.withTag(KIT_ITEM_TAG, true));
            else player.setEquipment(slot, ItemStack.AIR);
        }
        // Misc
        for (ItemStack item : inventory) {
            playerInventory.addItemStack(item.withTag(KIT_ITEM_TAG, true));
        }
    }
}
