package net.minestom.arena.feature;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.hologram.Hologram;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToLongFunction;

/**
 * @param playerCombat Allow player combat
 * @param damageFunction Uses the return value as damage to apply (in lambda arg 1 is attacker, arg 2 is victim)
 * @param invulnerabilityFunction Uses the return value as time an entity is invulnerable after getting attacked (in lambda arg 1 is victim)
 */
record CombatFeature(boolean playerCombat, ToDoubleBiFunction<Entity, Entity> damageFunction, ToLongFunction<Entity> invulnerabilityFunction) implements Feature {
    private static final Tag<Long> INVULNERABLE_UNTIL_TAG = Tag.Long("invulnerable_until").defaultValue(0L);

    private void takeKnockback(Entity target, Entity source) {
        target.takeKnockback(
                0.5f,
                Math.sin(source.getPosition().yaw() * (Math.PI / 180)),
                -Math.cos(source.getPosition().yaw() * (Math.PI / 180))
        );
    }

    private void spawnHologram(Entity target, Entity source, float damage) {
        damage = MathUtils.round(damage, 2);

        new DamageHologram(
                target.getInstance(),
                target.getPosition().add(0, target.getEyeHeight(), 0),
                Component.text(damage, NamedTextColor.RED)
        );
    }

    @Override
    public void hook(@NotNull EventNode<InstanceEvent> node) {
        node.addListener(ProjectileCollideWithEntityEvent.class, event -> {
            if (!(event.getTarget() instanceof LivingEntity target)) return;
            if (!(event.getEntity() instanceof EntityProjectile projectile)) return;

            // Don't apply damage if entity is invulnerable
            final long now = System.currentTimeMillis();
            final long invulnerableUntil = target.getTag(INVULNERABLE_UNTIL_TAG);
            if (invulnerableUntil > now) return;

            float damage = (float) damageFunction.applyAsDouble(projectile, target);

            target.damage(DamageType.fromProjectile(projectile.getShooter(), projectile), damage);
            target.setTag(INVULNERABLE_UNTIL_TAG, now + invulnerabilityFunction.applyAsLong(target));

            takeKnockback(target, projectile);
            spawnHologram(target, projectile, damage);

            projectile.remove();
        }).addListener(EntityAttackEvent.class, event -> {
            if (!(event.getTarget() instanceof LivingEntity target)) return;

            // PVP is disabled and two players have attempted to hit each other
            if (!playerCombat && target instanceof Player && event.getEntity() instanceof Player) return;

            // Can't have dead sources attacking things
            if (((LivingEntity) event.getEntity()).isDead()) return;

            // Don't apply damage if entity is invulnerable
            final long now = System.currentTimeMillis();
            final long invulnerableUntil = target.getTag(INVULNERABLE_UNTIL_TAG);
            if (invulnerableUntil > now) return;

            float damage = (float) damageFunction.applyAsDouble(event.getEntity(), target);

            target.damage(DamageType.fromEntity(event.getEntity()), damage);
            target.setTag(INVULNERABLE_UNTIL_TAG, now + invulnerabilityFunction.applyAsLong(target));

            takeKnockback(target, event.getEntity());
            spawnHologram(target, event.getEntity(), damage);
        });
    }

    private static class DamageHologram extends Hologram {
        public DamageHologram(Instance instance, Pos spawnPosition, Component text) {
            super(instance, spawnPosition, text, true, true);
            getEntity().getEntityMeta().setHasNoGravity(false);

            Random random = ThreadLocalRandom.current();
            getEntity().setVelocity(getPosition()
                    .direction()
                    .withX(random.nextDouble(2))
                    .withY(3)
                    .withZ(random.nextDouble(2))
                    .normalize().mul(3));

            MinecraftServer.getSchedulerManager()
                    .buildTask(this::remove)
                    .delay(Duration.of(15, TimeUnit.SERVER_TICK)).
                    schedule();
        }
    }
}
