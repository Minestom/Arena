package net.minestom.arena.game.procedural;

import de.articdive.jnoise.JNoise;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Wave function collapse is an algorithm used to generate structures from individual pieces that have connection rules.
 * <p>
 * <p>
 * You start with a single random piece, this piece is placed down.
 * After this piece is placed down, the surrounding pieces are restricted by the existing piece's allowed connections.
 * <p>
 * Find the next piece with the lowest amount of allowed connections, and place it down, apply the same process again.
 * This process is repeated until there are no more pieces to place down.
 */
class WaveFunctionCollapseGenerator implements Generator {

    private final Random random;
    private final JNoise noise;

    WaveFunctionCollapseGenerator(long seed) {
        this.random = new Random(seed);
        this.noise = JNoise.newBuilder()
                .superSimplex()
                .setFrequency(0.1)
                .setSeed(seed)
                .build();
    }

    @Override
    public void generate(@NotNull GenerationUnit unit) {
        // Check if unit contains 0, 0, 0
        Point start = unit.absoluteStart();
        Point end = unit.absoluteEnd();

        if (
                start.x() <= 0 && start.y() <= 0 && start.z() <= 0 &&
                        end.x() > 0 && end.y() > 0 && end.z() > 0
        ) {
            // Success, we now generate the arena
            unit.fork(this::generate);
        }
    }

    // This is only ran once at section 0, 0, 0
    private void generate(@NotNull Block.Setter setter) {

        // This queue will contain all the super positions and is sorted by lowest entropy first
        PriorityQueue<SuperPosition> queue = new PriorityQueue<>();

        // This map is used to find the super position for a given position
        Map<Vec, SuperPosition> positions = new HashMap<>();

        // Create the super position at the center of the arena
        SuperPosition origin = new SuperPosition(new Vec(0, 0, 0), positions);
        origin.states.remove(GenerationData.AIR);
        queue.add(origin);
        positions.put(new Vec(0, 0, 0), origin);

        while (!queue.isEmpty()) {
            handleNextSuperPosition(setter, queue, positions);
        }

        // Debug
//        MinecraftServer.getGlobalEventHandler().addListener(PlayerHandAnimationEvent.class, event -> {
//            while (true) {
//                SuperPosition position = handleNextSuperPosition(event.getInstance(), queue, positions);
//                if (position != null) {
//                    if (position.placed && position.states.size() > 0) {
//                        event.getPlayer().teleport(event.getPlayer().getPosition().withCoord(position.pos.mul(2)));
//                        break;
//                    }
//                }
//            }
//        });
    }

    private SuperPosition handleNextSuperPosition(Block.Setter setter, PriorityQueue<SuperPosition> queue,
                                                  Map<Vec, SuperPosition> positions) {
        SuperPosition pos = queue.poll();
        if (pos == null) {
            return null;
        }
        handleSuperPosition(pos, setter, queue, positions);
        return pos;
    }

    private void handleSuperPosition(SuperPosition pos, Block.Setter setter,
                                     PriorityQueue<SuperPosition> queue,
                                     Map<Vec, SuperPosition> positions) {

        // Get a random state from within the super position
        if (pos.states.isEmpty()) {
            pos.placed = true;
        } else {
            GenerationData.BlockGroup group = pos.randomState(random);
            {
                group.place(setter, pos.pos);
//                int entropy = pos.states.size();
//                Audiences.all().sendMessage(Component.text("Placed " + group + " at " + pos.pos + " with entropy " + entropy).color(NamedTextColor.GRAY));
                pos.states = Set.of(group);
                pos.placed = true;
            }

            // Ensure that pos has all it's neighbors
            pos.neighbors.createIfNotExists(pos, queue, positions);
        }

        // Update surrounding super positions
        for (Direction direction : Direction.values()) {
            SuperPosition neighbor = pos.neighbors.towards(direction);
            if (neighbor == null) {
                continue;
            }
            if (neighbor.updateThisStateFrom(direction.opposite())) {
                queue.remove(neighbor);
                queue.add(neighbor);
            }
        }
    }

    private static class SuperPosition implements Comparable<SuperPosition> {
        private static final List<GenerationData.BlockGroup> ALL_STATES = GenerationData.ALL_BLOCK_GROUPS.stream()
                .filter(group -> group != GenerationData.AIR)
                .toList();

        private final Vec pos;
        private final Neighbors neighbors;

        private Set<GenerationData.BlockGroup> states;
        private boolean placed = false;
        private final int id;

        public SuperPosition(Vec pos, Map<Vec, SuperPosition> positions) {
            this.pos = pos;
            this.states = new HashSet<>(ALL_STATES);
            this.neighbors = new Neighbors(pos, positions);
            this.id = positions.size();
        }

        @Override
        public int compareTo(@NotNull WaveFunctionCollapseGenerator.SuperPosition o) {
            int entropy = Integer.compare(states.size(), o.states.size());

            if (entropy != 0) {
                return entropy;
            }

            double distance = Double.compare(pos.distanceSquared(0, 0, 0), o.pos.distanceSquared(0, 0, 0));

            if (distance != 0) {
                return (int) Math.signum(distance);
            }

            return Integer.compare(id, o.id);
        }

        public @NotNull GenerationData.BlockGroup randomState(Random random) {
            if (states.isEmpty()) {
                return GenerationData.AIR;
            }
            // Get a random state
            int i = random.nextInt(states.size());
            int current = 0;
            for (GenerationData.BlockGroup state : states) {
                if (current == i) {
                    return state;
                }
                current++;
            }
            throw new IllegalStateException("There is no state for this super position???");
        }

        /**
         * Updates only this super position's state from surrounding neighbor's state
         */
        public boolean updateThisState() {
            if (placed) {
                return false;
            }
            boolean changed = false;
            for (Direction direction : Direction.values()) {
                changed |= updateThisStateFrom(direction);
            }
            return changed;
        }

        public Set<GenerationData.BlockGroup> allowedInDirection(Direction direction) {
            Set<GenerationData.BlockGroup> out = new HashSet<>(ALL_STATES);
            for (GenerationData.BlockGroup state : states) {
                out.retainAll(state.allowsTo(direction));
            }
            return out;
        }

        public boolean updateThisStateFrom(@NotNull Direction direction) {
            if (placed) {
                return false;
            }
            SuperPosition neighbor = neighbors.towards(direction);
            if (neighbor == null) {
                return false;
            }

            direction = direction.opposite();
            var allowed = neighbor.allowedInDirection(direction);
//            int entropy = states.size();
            boolean retainChanged = this.states.retainAll(allowed);

//            if (retainChanged) {
//                String coord = String.format("Updated (%s, %s, %s): ", pos.x(), pos.y(), pos.z());
//                String info = entropy + " -> " + states.size();
//                Audiences.all().sendMessage(Component.text(coord + info).color(NamedTextColor.DARK_GRAY));
//            }

            return retainChanged;
        }

        public static class Neighbors implements Iterable<SuperPosition> {
            private final Vec pos;
            private final Map<Vec, SuperPosition> positions;

            public Neighbors(Vec pos, Map<Vec, SuperPosition> positions) {
                this.pos = pos;
                this.positions = positions;
            }

            public Map<Direction, SuperPosition> all() {
                Map<Direction, SuperPosition> out = new HashMap<>();
                for (Direction direction : Direction.values()) {
                    out.put(direction, towards(direction));
                }
                return out;
            }

            public @Nullable SuperPosition towards(Direction direction) {
                return positions.get(pos.add(direction.normalX(), direction.normalY(), direction.normalZ()));
            }

            public void createIfNotExists(SuperPosition pos, PriorityQueue<SuperPosition> queue,
                                          Map<Vec, SuperPosition> positions) {
                for (Direction direction : Direction.values()) {
                    if (towards(direction) == null) {
                        int x = pos.pos.blockX() + direction.normalX();
                        int y = pos.pos.blockY() + direction.normalY();
                        int z = pos.pos.blockZ() + direction.normalZ();
                        if (Math.sqrt(x * x + y * y + z * z) > GenerationData.MAX_DISTANCE) {
                            continue;
                        }

                        // Find a neighbor in the map
                        Vec newVec = new Vec(x, y, z);

                        positions.computeIfAbsent(newVec, ignored -> {
                            SuperPosition newPos = new SuperPosition(newVec, positions);
                            queue.add(newPos);
                            return newPos;
                        });
                    }
                }
            }

            @NotNull
            @Override
            public Iterator<SuperPosition> iterator() {
                Direction[] dirs = Direction.values();
                int[] i = {0};

                return new Iterator<>() {
                    @Override
                    public boolean hasNext() {
                        return i[0] < dirs.length;
                    }

                    @Override
                    public @Nullable SuperPosition next() {
                        return towards(dirs[i[0]++]);
                    }
                };
            }
        }
    }
}
