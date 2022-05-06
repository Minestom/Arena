package net.minestom.arena.game.mob.upgrades;

import net.kyori.adventure.text.format.TextColor;
import net.minestom.arena.game.mob.ArenaUpgrade;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;

public class HealthcareUpgrade extends ArenaUpgrade {

    private static final AttributeModifier MODIFIER = new AttributeModifier("mobarena-healthcare", 4f, AttributeOperation.ADDITION);

    public HealthcareUpgrade() {
        super("Improved Healthcare", "Increases max health by two hearts.", TextColor.color(0x63ff52), Material.POTION, 10);
    }

    @Override
    public void apply(Player player) {
        // Since this upgrade includes healing, check if they have the modifier first
        // before healing them another two hearts
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (!attribute.getModifiers().contains(MODIFIER)) {
            attribute.addModifier(MODIFIER);
            player.setHealth(player.getHealth() + MODIFIER.getAmount());
        }
    }

    @Override
    public void reset(Player player) {
        player.getAttribute(Attribute.MAX_HEALTH).removeModifier(MODIFIER);
    }

}
