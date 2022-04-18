package net.minestom.arena.game.mob;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.FollowTargetGoal;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.utils.time.TimeUnit;

import java.time.Duration;
import java.util.List;

public final class ZombieMob extends ArenaMob {
    public ZombieMob(int level, EventNode<InstanceEvent> node) {
        super(EntityType.ZOMBIE, node);
        addAIGroup(
                List.of(new MeleeAttackGoal(this, 2, 20, TimeUnit.SERVER_TICK),
                        new FollowTargetGoal(this, Duration.of(3, TimeUnit.SERVER_TICK))),
                List.of(new ClosestEntityTarget(this, 20, Player.class))
        );
    }
}
