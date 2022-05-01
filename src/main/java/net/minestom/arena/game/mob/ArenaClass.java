package net.minestom.arena.game.mob;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

record ArenaClass(String name, String icon, ItemStack item, Kit kit, int cost) {
    public void apply(Player player) {
        kit.apply(player);
    }
}
