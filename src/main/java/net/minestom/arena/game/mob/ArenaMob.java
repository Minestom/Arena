package net.minestom.arena.game.mob;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.arena.game.mobdrops.RandomDrop;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

abstract class ArenaMob extends EntityCreature {
    private static final int BLOCK_LENGTH = 6;
    private static final String EMPTY_BLOCK_CHAR = "□";
    private static final String FULL_BLOCK_CHAR = "■";

    public ArenaMob(@NotNull EntityType entityType) {
        super(entityType);
        setCustomName(generateHealthBar(getMaxHealth(), getHealth()));
        setCustomNameVisible(true);
        eventNode().addListener(EntityDamageEvent.class, event ->
                setCustomName(generateHealthBar(getMaxHealth(), getHealth())));
        eventNode().addListener(EntityDeathEvent.class, event ->
                RandomDrop.getDrop().setInstance(event.getInstance(), event.getEntity().getPosition()));
    }

    @Contract(pure = true)
    private static @NotNull Component generateHealthBar(float maxHealth, float minHealth) {
        // Converts the health percentage into a number from 0-{blockLength} -- only 0 if the mob's health is 0
        final int charHealth = (int) Math.ceil((minHealth / maxHealth) * BLOCK_LENGTH);
        return Component.text()
                .append(Component.text("[", NamedTextColor.DARK_GRAY))
                .append(Component.text(FULL_BLOCK_CHAR.repeat(charHealth) + EMPTY_BLOCK_CHAR.repeat(BLOCK_LENGTH - charHealth), NamedTextColor.RED))
                .append(Component.text("]", NamedTextColor.DARK_GRAY))
                .build();
    }
}
