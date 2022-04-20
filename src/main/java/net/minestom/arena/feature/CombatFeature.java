package net.minestom.arena.feature;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.hologram.Hologram;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.function.ToDoubleBiFunction;

/**
 * @param playerCombat Allow player combat
 * @param damageModifier Multiplies final damage by return value arg 1 is attacker arg 2 is victim
 */
record CombatFeature(boolean playerCombat, ToDoubleBiFunction<Entity, Entity> damageModifier) implements Feature {
    public CombatFeature(boolean playerCombat) {
        this(playerCombat, (a, v) -> 1);
    }

    private void takeKnockback(Entity target, Entity source) {
        target.takeKnockback(
                0.5f,
                Math.sin(source.getPosition().yaw() * (Math.PI / 180)),
                -Math.cos(source.getPosition().yaw() * (Math.PI / 180))
        );
    }

    private void spawnHologram(Entity target, Entity source, float damage) {
        Hologram hologram = new Hologram(
                target.getInstance(),
                target.getPosition().add(0, target.getEyeHeight(), 0),
                Component.text(damage, NamedTextColor.RED)
        );

        hologram.getEntity().setVelocity(source.getPosition().direction().withY(-1).normalize().mul(3));

        MinecraftServer.getSchedulerManager()
                .buildTask(hologram::remove)
                .delay(Duration.of(30, TimeUnit.SERVER_TICK)).
                schedule();
    }

    @Override
    public void hook(@NotNull EventNode<Event> node) {
        node.addListener(ProjectileCollideWithEntityEvent.class, event -> {
            if (!(event.getTarget() instanceof LivingEntity target)) return;
            if (!(event.getEntity() instanceof EntityProjectile projectile)) return;

            float damage = (float) damageModifier.applyAsDouble(projectile, target);

            target.damage(DamageType.fromProjectile(projectile.getShooter(), projectile), damage);

            takeKnockback(target, projectile);
            spawnHologram(target, projectile, damage);

            projectile.remove();
        }).addListener(EntityAttackEvent.class, event -> {
            if (event.getTarget() instanceof LivingEntity target) {
                // PVP is disabled and two players have attempted to hit each other
                if (!playerCombat && event.getTarget() instanceof Player && event.getEntity() instanceof Player) return;

                // Can't have dead sources attacking things
                if (((LivingEntity) event.getEntity()).isDead()) return;

                float damage = (float) damageModifier.applyAsDouble(event.getEntity(), target);

                target.damage(DamageType.fromEntity(event.getEntity()), damage);

                takeKnockback(target, event.getEntity());
                spawnHologram(target, event.getEntity(), damage);
            }
        });
    }
}
