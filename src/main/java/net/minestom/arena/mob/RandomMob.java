package net.minestom.arena.mob;

import net.minestom.server.entity.EntityCreature;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public class RandomMob {

    private static final List<Function<Integer, EntityCreature>> mobGenerationLambdas = List.of(
            ZombieMob::new
    );

    public static EntityCreature random(int level) {
        Function<Integer, EntityCreature> randomMobGenerator = mobGenerationLambdas.get(
                ThreadLocalRandom.current().nextInt(mobGenerationLambdas.size()) % mobGenerationLambdas.size()
        );

        return randomMobGenerator.apply(level);
    }

}
