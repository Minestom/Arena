package net.minestom.arena.game.mob;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.utils.time.Cooldown;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

final class EndermanMob extends ArenaMob {
    public EndermanMob(MobGenerationContext context) {
        super(EntityType.ENDERMAN, context);
        addAIGroup(
                List.of(
                        new TeleportGoal(this, Duration.ofSeconds(10), 8),
                        new MeleeAttackGoal(this, 1.2, 20, TimeUnit.SERVER_TICK)
                ),
                List.of(new ClosestEntityTarget(this, 32, Player.class))
        );
        getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(getAttributeValue(Attribute.MOVEMENT_SPEED) * 2);
    }

    private static final class TeleportGoal extends GoalSelector {
        private final Duration cooldown;
        private final int distanceSquared;

        private long lastTeleport;
        private Entity target;

        public TeleportGoal(@NotNull EntityCreature entityCreature, @NotNull Duration cooldown, int distance) {
            super(entityCreature);
            this.cooldown = cooldown;
            distanceSquared = distance * distance;
        }

        @Override
        public boolean shouldStart() {
            Entity target = entityCreature.getTarget();
            if (target == null) target = findTarget();
            if (target == null) return false;
            if (Cooldown.hasCooldown(System.currentTimeMillis(), lastTeleport, cooldown)) return false;
            final boolean result = target.getPosition().distanceSquared(entityCreature.getPosition()) >= distanceSquared;
            if (result) this.target = target;
            return result;
        }

        @Override
        public void start() {
            if (target == null) return;
            entityCreature.setTarget(target);
        }

        @Override
        public void tick(long time) {
            if (!Cooldown.hasCooldown(time, lastTeleport, cooldown)) {
                final Pos targetPos = entityCreature.getTarget() != null
                        ? entityCreature.getTarget().getPosition() : null;
                lastTeleport = time;

                if (targetPos != null)
                    entityCreature.teleport(targetPos.sub(targetPos.direction()));
            }
        }

        @Override
        public boolean shouldEnd() {
            final Entity target = entityCreature.getTarget();
            return target == null || target.isRemoved() ||
                    Cooldown.hasCooldown(System.currentTimeMillis(), lastTeleport, cooldown) ||
                    target.getPosition().distanceSquared(entityCreature.getPosition()) < distanceSquared;
        }

        @Override
        public void end() {}
    }
}
