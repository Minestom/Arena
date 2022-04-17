package net.minestom.arena.combat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.hologram.Hologram;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.utils.time.TimeUnit;

import java.time.Duration;

public class CombatEvent {

    public static void hook(EventNode<Event> node) {
        node.addListener(EntityAttackEvent.class, event -> {
            if (event.getTarget() instanceof EntityCreature target && !(event.getTarget() instanceof Player)) {

                int damage = 1;

                target.damage(DamageType.fromEntity(event.getEntity()), damage);

                Hologram hologram = new Hologram(
                        target.getInstance(),
                        target.getPosition().add(0, target.getEyeHeight(), 0),
                        Component.text(damage, NamedTextColor.RED)
                );

                hologram.getEntity().setVelocity(event.getEntity().getPosition().direction().normalize().mul(3));

                MinecraftServer.getSchedulerManager()
                        .buildTask(hologram::remove)
                        .delay(Duration.of(30, TimeUnit.SERVER_TICK)).
                        schedule();
            }
        });
    }

}
