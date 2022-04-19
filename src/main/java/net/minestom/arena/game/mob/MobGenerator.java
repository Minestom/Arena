package net.minestom.arena.game.mob;

import java.util.List;

@FunctionalInterface
interface MobGenerator {
    List<ArenaMob> generate(int stage, int maximum);
}
