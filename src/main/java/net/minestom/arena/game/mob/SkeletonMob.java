package net.minestom.arena.game.mob;

import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.RangedAttackGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.time.TimeUnit;

import java.time.Duration;
import java.util.List;

final class SkeletonMob extends ArenaMob {
    public SkeletonMob(int level) {
        super(EntityType.SKELETON, level);
        setItemInMainHand(ItemStack.of(Material.BOW));

        RangedAttackGoal rangedAttackGoal = new RangedAttackGoal(
                this, Duration.of(40, TimeUnit.SERVER_TICK),
                16, 8, true, 1, 0.1);

        rangedAttackGoal.setProjectileGenerator((entity) -> {
            EntityProjectile projectile = new EntityProjectile(entity, EntityType.ARROW);
            projectile.scheduleRemove(Duration.of(100, TimeUnit.SERVER_TICK));
            return projectile;
        });

        addAIGroup(
                List.of(rangedAttackGoal),
                List.of(new ClosestEntityTarget(this, 32, Player.class))
        );
    }
}
