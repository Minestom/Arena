package net.minestom.arena.game.mob;

import net.minestom.arena.game.GenerationContext;
import net.minestom.arena.group.Group;

final class MobGenerationContext implements GenerationContext {
    private final MobArena arena;

    private int generated = 0;

    MobGenerationContext(MobArena arena) {
        this.arena = arena;
    }

    public int stage() {
        return arena.stage();
    }

    public Group group() {
        return arena.group();
    }

    public MobArena arena() {
        return arena;
    }

    @Override
    public int generated() {
        return generated;
    }

    @Override
    public void setGenerated(int generated) {
        this.generated = generated;
    }
}
