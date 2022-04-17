package net.minestom.arena.mob;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.utils.time.TimeUnit;

import java.util.List;

public class ZombieMob extends EntityCreature {

    public ZombieMob(int level) {
        super(EntityType.ZOMBIE);

        addAIGroup(
                List.of(new MeleeAttackGoal(this, 5, 1000, TimeUnit.MILLISECOND)),
                List.of(new ClosestEntityTarget(this, 10))
        );
    }

}
