package net.minestom.arena.game.mob;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.pathfinding.Navigator;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.time.Cooldown;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

final class SpiderMob extends ArenaMob {
    public SpiderMob(int stage) {
        super(EntityType.SPIDER, stage);
        addAIGroup(
                List.of(new ThrowWebAttackGoal(this, 16, 10, TimeUnit.SECOND)),
                List.of(new ClosestEntityTarget(this, 32, Player.class))
        );
    }

    private static class ThrowWebAttackGoal extends GoalSelector {
        private final Cooldown cooldown = new Cooldown(Duration.of(5, TimeUnit.SERVER_TICK));

        private long lastHit;
        private final double range;
        private final Duration delay;

        private boolean stop;
        private Entity cachedTarget;

        /**
         * @param entityCreature the entity to add the goal to
         * @param range          the allowed range the entity can attack others.
         * @param delay          the delay between each attacks
         * @param timeUnit       the unit of the delay
         */
        public ThrowWebAttackGoal(@NotNull EntityCreature entityCreature, double range, int delay, @NotNull TemporalUnit timeUnit) {
            this(entityCreature, range, Duration.of(delay, timeUnit));
        }

        /**
         * @param entityCreature the entity to add the goal to
         * @param range          the allowed range the entity can attack others.
         * @param delay          the delay between each attacks
         */
        public ThrowWebAttackGoal(@NotNull EntityCreature entityCreature, double range, Duration delay) {
            super(entityCreature);
            this.range = range;
            this.delay = delay;
        }

        public @NotNull Cooldown getCooldown() {
            return this.cooldown;
        }

        @Override
        public boolean shouldStart() {
            this.cachedTarget = findTarget();
            return this.cachedTarget != null;
        }

        @Override
        public void start() {
            final Point targetPosition = this.cachedTarget.getPosition();
            entityCreature.getNavigator().setPathTo(targetPosition);
        }

        @Override
        public void tick(long time) {
            Entity target;
            if (this.cachedTarget != null) {
                target = this.cachedTarget;
                this.cachedTarget = null;
            } else {
                target = findTarget();
            }

            this.stop = target == null;

            if (!stop) {

                // Attack the target entity
                if (entityCreature.getDistance(target) <= range) {
                    entityCreature.lookAt(target);
                    if (!Cooldown.hasCooldown(time, lastHit, delay)) {
                        // TODO Add animation for throwing web

                        Pos pos = target.getPosition();
                        Random random = ThreadLocalRandom.current();
                        Instance instance = target.getInstance();
                        if (instance == null) return;

                        for (int i = 0; i < 8; i++) {
                            Pos spawnAt = pos.add(
                                random.nextInt(-1, 1),
                                random.nextInt(0, 2),
                                random.nextInt(-1, 1)
                            );

                            if (instance.getBlock(spawnAt).isAir()) {
                                instance.setBlock(spawnAt, Block.COBWEB);
                            }
                        }

                        this.lastHit = time;
                    }
                    return;
                }

                // Move toward the target entity
                Navigator navigator = entityCreature.getNavigator();
                final var pathPosition = navigator.getPathPosition();
                final var targetPosition = target.getPosition();
                if (pathPosition == null || pathPosition.distance(targetPosition) > range) {
                    if (this.cooldown.isReady(time)) {
                        this.cooldown.refreshLastUpdate(time);
                        navigator.setPathTo(targetPosition);
                    }
                }
            }
        }

        @Override
        public boolean shouldEnd() {
            return stop;
        }

        @Override
        public void end() {
            // Stop following the target
            entityCreature.getNavigator().setPathTo(null);
        }
    }
}
