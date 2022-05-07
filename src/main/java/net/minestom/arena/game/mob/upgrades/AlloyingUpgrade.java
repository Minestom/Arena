package net.minestom.arena.game.mob.upgrades;

import net.kyori.adventure.text.format.TextColor;
import net.minestom.arena.game.mob.ArenaUpgrade;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;

public class AlloyingUpgrade extends ArenaUpgrade {

    public AlloyingUpgrade() {
        super("Alloying", "Increase armor effectiveness by 25%.", TextColor.color(0xf9ff87), Material.LAVA_BUCKET, 10);
    }

    @Override
    public void apply(Player player) {}

    @Override
    public void reset(Player player) {}

}
