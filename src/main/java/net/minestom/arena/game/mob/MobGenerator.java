package net.minestom.arena.game.mob;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@FunctionalInterface
interface MobGenerator {
    @NotNull List<ArenaMob> generate(int stage, int maximum);
}
