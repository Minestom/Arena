package net.minestom.arena.game.mob;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.RangedAttackGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

final class SkeletonMob extends ArenaMob {
    public SkeletonMob(MobGenerationContext context) {
        super(EntityType.SKELETON, context);
        setItemInMainHand(ItemStack.of(Material.BOW));

        RangedAttackGoal rangedAttackGoal = new RangedAttackGoal(
                this, Duration.of(40, TimeUnit.SERVER_TICK),
                16, 8, true, 1, 0.1);

        rangedAttackGoal.setProjectileGenerator(entity -> {
            HomingArrow projectile = new HomingArrow(entity, EntityType.PLAYER);
            projectile.scheduleRemove(Duration.of(100, TimeUnit.SERVER_TICK));
            return projectile;
        });

        addAIGroup(
                List.of(rangedAttackGoal),
                List.of(new ClosestEntityTarget(this, 32, Player.class))
        );
    }

    private static final class HomingArrow extends EntityProjectile {
        private final EntityType target;

        public HomingArrow(@Nullable Entity shooter, EntityType target) {
            super(shooter, EntityType.ARROW);
            this.target = target;
        }

        @Override
        public void tick(long time) {
            super.tick(time);
            if (instance == null) return;
            if (isOnGround()) return;

            for (Entity entity : instance.getNearbyEntities(position, 5.0)) {
                if (entity.getEntityType() != target) continue;

                final Vec target = position.withLookAt(entity.getPosition()).direction();
                final Vec newVelocity = velocity.add(target);

                setVelocity(newVelocity);

                return;
            }
        }
    }
}
