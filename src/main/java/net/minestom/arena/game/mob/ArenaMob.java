package net.minestom.arena.game.mob;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

abstract class ArenaMob extends EntityCreature {
    private static final int BLOCK_LENGTH = 6;
    private static final List<String> CHARACTERS = List.of(
            "", "▏", "▎", "▍",
            "▌", "▋", "▊", "▉"
    );
    private static final String FULL_BLOCK_CHAR = "█";

    public ArenaMob(@NotNull EntityType entityType, int stage) {
        super(entityType);
        getAttribute(Attribute.MAX_HEALTH).setBaseValue(getMaxHealth() + stage * 2);
        getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(1 + stage / 4f);
        heal();
        setCustomName(generateHealthBar(getMaxHealth(), getHealth()));
        setCustomNameVisible(true);
        eventNode().addListener(EntityDamageEvent.class, event ->
                setCustomName(generateHealthBar(getMaxHealth(), getHealth())))
            .addListener(EntityDeathEvent.class, event ->
                setCustomName(generateHealthBar(getMaxHealth(), 0)));
    }

    @Contract(pure = true)
    private static @NotNull Component generateHealthBar(float maxHealth, float minHealth) {
        // Converts the health percentage into a number from 0-{blockLength} -- only 0 if the mob's health is 0
        final double charHealth = (minHealth / maxHealth) * BLOCK_LENGTH;
        return Component.text()
                .append(Component.text("[", NamedTextColor.DARK_GRAY))
                .append(Component.text(
                        FULL_BLOCK_CHAR.repeat((int) Math.floor(charHealth)),
                        NamedTextColor.RED
                )).append(Component.text(CHARACTERS.get((int) Math.round(
                        (charHealth - Math.floor(charHealth)) // number from 0-1
                        * (CHARACTERS.size() - 1) // indexes start at 0
                )), NamedTextColor.YELLOW))
                .append(Component.text("]", NamedTextColor.DARK_GRAY))
                .build();
    }
}
