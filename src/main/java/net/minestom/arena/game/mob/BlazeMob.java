package net.minestom.arena.game.mob;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.utils.time.Cooldown;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

final class BlazeMob extends ArenaMob {
    private static final int MAX_HEIGHT = 5;

    public BlazeMob(MobGenerationContext context) {
        super(EntityType.BLAZE, context);

        // Custom attack goal, blaze is stationary and shouldn't follow their targets
        BlazeAttackGoal rangedAttackGoal = new BlazeAttackGoal(
                this, Duration.of(10, TimeUnit.SERVER_TICK),
                16, 1, 0.5, entity -> {
                    EntityProjectile projectile = new FireballProjectile(entity);
                    projectile.scheduleRemove(Duration.of(30, TimeUnit.SERVER_TICK));
                    return projectile;
                });

        addAIGroup(
                List.of(rangedAttackGoal),
                List.of(new ClosestEntityTarget(this, 32, Player.class))
        );
    }
    @Override
    public @NotNull Vec getVelocity() {
        return super.getVelocity()
                .withY(y -> getPosition().y() > MobArena.HEIGHT + MAX_HEIGHT // If above max height
                    ? y / 5 // Stop flying up
                    : Math.abs(y)); // Fly up
    }

    private static final class FireballProjectile extends EntityProjectile {
        public FireballProjectile(@NotNull Entity shooter) {
            super(shooter, EntityType.SMALL_FIREBALL);
            setNoGravity(true);
        }

        @Override
        public void tick(long time) {
            super.tick(time);
            if (isOnGround()) remove();
        }
    }

    private static final class BlazeAttackGoal extends GoalSelector {
        private long lastShot;
        private final Duration delay;
        private final int attackRangeSquared;
        private final double power;
        private final double spread;
        private final Function<Entity, EntityProjectile> projectileGenerator;
        private boolean stop;
        private Entity cachedTarget;

        public BlazeAttackGoal(@NotNull EntityCreature entityCreature, Duration delay, int attackRange, double power, double spread, Function<Entity, EntityProjectile> projectileGenerator) {
            super(entityCreature);
            this.delay = delay;
            this.attackRangeSquared = attackRange * attackRange;
            this.power = power;
            this.spread = spread;
            this.projectileGenerator = projectileGenerator;
        }

        @Override
        public boolean shouldStart() {
            this.cachedTarget = findTarget();
            return this.cachedTarget != null;
        }

        @Override
        public void start() {}

        @Override
        public void tick(long time) {
            Entity target;
            if (this.cachedTarget != null) {
                target = this.cachedTarget;
                this.cachedTarget = null;
            } else {
                target = findTarget();
            }
            if (target == null) {
                this.stop = true;
                return;
            }
            double distanceSquared = this.entityCreature.getDistanceSquared(target);
            if (distanceSquared <= this.attackRangeSquared) {
                if (!Cooldown.hasCooldown(time, this.lastShot, this.delay)) {
                    if (entityCreature.hasLineOfSight(target)) {
                        final var to = target.getPosition().add(0D, target.getEyeHeight(), 0D);

                        EntityProjectile projectile = projectileGenerator.apply(entityCreature);
                        projectile.setInstance(entityCreature.getInstance(), entityCreature.getPosition().add(0D, entityCreature.getEyeHeight(), 0D));

                        projectile.shoot(to, power, spread);
                        this.lastShot = time;
                    }
                }
            }
            entityCreature.lookAt(target);
        }

        @Override
        public boolean shouldEnd() {
            return stop;
        }

        @Override
        public void end() {}
    }
}
