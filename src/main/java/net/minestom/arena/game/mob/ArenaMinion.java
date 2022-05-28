package net.minestom.arena.game.mob;

import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;

class ArenaMinion extends ArenaMob {
    private final ArenaMob owner;

    public ArenaMinion(@NotNull EntityType entityType, @NotNull ArenaMob owner) {
        super(entityType, owner.context);
        this.owner = owner;
    }

    public ArenaMob owner() {
        return owner;
    }
}
