package net.minestom.arena.game.mob;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.trait.InstanceEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class ArenaMob extends EntityCreature {

    private static final int blockLength = 6;
    private static final String emptyBlockChar = "□";
    private static final String fullBlockChar = "■";

    @Contract(pure =  true)
    private static @NotNull Component generateHealthBar(float maxHealth, float minHealth) {
        // Converts the health percentage into a number from 0-{blockLength} -- only 0 if the mob's health is 0
        final int charHealth = (int) Math.ceil((minHealth / maxHealth) * blockLength);

        return Component.text()
                .append(Component.text("[", NamedTextColor.DARK_GRAY))
                .append(Component.text(fullBlockChar.repeat(charHealth) + emptyBlockChar.repeat(blockLength - charHealth), NamedTextColor.RED))
                .append(Component.text("]", NamedTextColor.DARK_GRAY))
                .build();
    }

    public ArenaMob(@NotNull EntityType entityType, @NotNull EventNode<InstanceEvent> node) {
        super(entityType);

        setCustomName(generateHealthBar(getMaxHealth(), getHealth()));
        setCustomNameVisible(true);

        node.addListener(EntityDamageEvent.class, event ->
            setCustomName(generateHealthBar(getMaxHealth(), getHealth()))
        );
    }

}
