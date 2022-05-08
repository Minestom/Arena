package net.minestom.arena.game.mob;

import net.minestom.arena.group.Group;

record GenerationContext(MobArena arena, int generated) {
    int stage() {
        return arena.stage();
    }

    Group group() {
        return arena.group();
    }
}
