package net.minestom.arena.game.mob;

import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;

record ArenaClass(String name, String description, String icon, Material material, Kit kit, int cost) {
    public void apply(Player player) {
        kit.apply(player);
    }
}
