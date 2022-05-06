package net.minestom.arena.game.mob.upgrades;

import net.kyori.adventure.text.format.TextColor;
import net.minestom.arena.game.mob.ArenaUpgrade;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;

public class CombatUpgrade extends ArenaUpgrade {

    private static final AttributeModifier MODIFIER = new AttributeModifier("mobarena-combat-training", 0.1f, AttributeOperation.MULTIPLY_TOTAL);

    public CombatUpgrade() {
        super("Combat Training", "All physical attacks deal 10% more damage.", TextColor.color(0xff5c3c), Material.IRON_SWORD, 10);
    }

    @Override
    public void apply(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.ATTACK_DAMAGE);
        if (!attribute.getModifiers().contains(MODIFIER)) attribute.addModifier(MODIFIER);
    }

    @Override
    public void reset(Player player) {
        player.getAttribute(Attribute.ATTACK_DAMAGE).removeModifier(MODIFIER);
    }

}
