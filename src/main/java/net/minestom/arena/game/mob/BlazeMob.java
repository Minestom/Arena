package net.minestom.arena.game.mob;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.RangedAttackGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityShootEvent;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

final class BlazeMob extends ArenaMob {
    public BlazeMob(int stage) {
        super(EntityType.BLAZE, stage);
        RangedAttackGoal rangedAttackGoal = new RangedAttackGoal(
                this, Duration.of(10, TimeUnit.SERVER_TICK),
                16, 12, false, 1, 0.5);

        rangedAttackGoal.setProjectileGenerator((entity) -> {
            EntityProjectile projectile = new FireballProjectile(entity);
            projectile.scheduleRemove(Duration.of(20, TimeUnit.SERVER_TICK));
            return projectile;
        });

        addAIGroup(
                List.of(rangedAttackGoal),
                List.of(new ClosestEntityTarget(this, 32, Player.class))
        );
    }

    private static class FireballProjectile extends EntityProjectile {
        public FireballProjectile(@Nullable Entity shooter) {
            super(shooter, EntityType.SMALL_FIREBALL);
            setNoGravity(true);
        }

        public void shoot(Point to, double power, double spread) {
            final EntityShootEvent shootEvent = new EntityShootEvent(getShooter(), this, to, power, spread);
            EventDispatcher.call(shootEvent);
            if (shootEvent.isCancelled()) {
                remove();
                return;
            }

            final Pos from = getShooter().getPosition().add(0D, getShooter().getEyeHeight(), 0D);
            double dx = to.x() - from.x();
            double dy = to.y() - from.y();
            double dz = to.z() - from.z();

            final double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            dx /= length;
            dy /= length;
            dz /= length;

            final Random random = ThreadLocalRandom.current();
            spread *= 0.007499999832361937D;
            dx += random.nextGaussian() * spread;
            dy += random.nextGaussian() * spread;
            dz += random.nextGaussian() * spread;

            final double mul = 20 * power;
            velocity = new Vec(dx * mul, dy * mul, dz * mul);
            setView(
                    (float) Math.toDegrees(Math.atan2(dx, dz)),
                    (float) Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)))
            );
        }
    }
}
