package net.minestom.arena.game.mob;

import de.articdive.jnoise.JNoise;
import de.articdive.jnoise.modules.octavation.OctavationModule;
import net.minestom.arena.Metrics;
import net.minestom.arena.utils.FullbrightDimension;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.MathUtils;

import java.util.UUID;

final class MobArenaInstance extends InstanceContainer {
    private final JNoise noise = JNoise.newBuilder()
            .fastSimplex()
            .setFrequency(0.0025)
            .addModule(OctavationModule.newBuilder()
                    .setOctaves(6)
                    .build())
            .build();

    MobArenaInstance() {
        super(UUID.randomUUID(), FullbrightDimension.INSTANCE);
        getWorldBorder().setDiameter(100);
        setGenerator(unit -> {
            final Point start = unit.absoluteStart();
            for (int x = 0; x < unit.size().x(); x++) {
                for (int z = 0; z < unit.size().z(); z++) {
                    Point bottom = start.add(x, 0, z);
                    // Ensure flat terrain in the fighting area
                    final double modifier = MathUtils.clamp((bottom.distance(Pos.ZERO.withY(bottom.y())) - 75) / 50, 0, 1);
                    double y = noise.getNoise(bottom.x(), bottom.z()) * modifier;
                    y = (y > 0 ? y * 4 : y) * 8 + MobArena.HEIGHT;
                    unit.modifier().fill(bottom, bottom.add(1, 0, 1).withY(y), Block.SAND);
                }
            }
        });

        int x = MobArena.SPAWN_RADIUS;
        int y = 0;
        int xChange = 1 - (MobArena.SPAWN_RADIUS << 1);
        int yChange = 0;
        int radiusError = 0;

        while (x >= y) {
            for (int i = -x; i <= x; i++) {
                setBlock(i, 15, y, Block.RED_SAND);
                setBlock(i, 15, -y, Block.RED_SAND);
            }
            for (int i = -y; i <= y; i++) {
                setBlock(i, 15, x, Block.RED_SAND);
                setBlock(i, 15, -x, Block.RED_SAND);
            }

            y++;
            radiusError += yChange;
            yChange += 2;
            if (((radiusError << 1) + xChange) > 0) {
                x--;
                radiusError += xChange;
                xChange += 2;
            }
        }
    }

    @Override
    protected void setRegistered(boolean registered) {
        super.setRegistered(registered);
        if (!registered) {
            Metrics.ENTITIES.dec(getEntities().size());
        }
    }
}
