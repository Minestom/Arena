package net.minestom.arena.game.procedural;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;
import java.util.function.Consumer;

import static net.minestom.server.instance.block.Block.*;

public class GenerationData {

    public static final List<BlockGroup> ALL_BLOCK_GROUPS = List.of(BlockGroups.values());
    public static final double MAX_DISTANCE = 100;
    public static final BlockGroup AIR = BlockGroups.AIR;

    /**
     * Blockgroups are 2x2 sections of the world.
     * The order of the blocks is as follows:
     * 0 -> x+, y+, z+
     * 1 -> x+, y+, z-
     * 2 -> x+, y-, z+
     * 3 -> x+, y-, z-
     * 4 -> x-, y+, z+
     * 5 -> x-, y+, z-
     * 6 -> x-, y-, z+
     * 7 -> x-, y-, z-
     * <p>
     * To help visualise this I suggest the use of a 2x2x2 rubiks cube.
     * Help the cube with white facing up, and green facing towards you.
     * 0 and 1 are the two corners between white and red
     * 2 and 3 are the two corners between yellow and red
     * 4 and 5 are the two corners between white and orange
     * 6 and 7 are the two corners between yellow and orange
     * <p>
     * Note that the piece in each pair that is closest to you is first.
     * <p>
     * null blocks are valid, they mean they will not replace previous blocks if applicable
     */
    interface BlockGroup {
        /**
         * @param direction the direction to check
         * @return the blockgroups that are allowed to be adjacent to this blockgroup in the specified direction
         */
        Set<BlockGroup> allowsTo(Direction direction);

        void place(Setter setter, int x, int y, int z);

        default void place(Setter setter, Point point) {
            place(setter, point.blockX(), point.blockY(), point.blockZ());
        }
    }

    // All the blockgroups
    private enum BlockGroups implements BlockGroup {
        AIR(
                null, null,
                null, null,
                null, null,
                null, null
        ),
        COBBLE_PATH_LEFT(
                null, null,
                COBBLESTONE, COBBLESTONE,
                COBBLESTONE_WALL, COBBLESTONE_WALL,
                COBBLESTONE, COBBLESTONE
        ),
        COBBLE_PATH(
                null, null,
                COBBLESTONE, COBBLESTONE,
                null, null,
                COBBLESTONE, COBBLESTONE
        ),
        COBBLE_PATH_RIGHT(
                COBBLESTONE_WALL, COBBLESTONE_WALL,
                COBBLESTONE, COBBLESTONE,
                null, null,
                COBBLESTONE, COBBLESTONE
        ),
        ;

        // Connections
        private static final Map<BlockGroup, Connections> group2Connections = new HashMap<>();

        static {
            // AIR connects to nothing
            connections(AIR, towards -> {});

            // Cobble paths
            connections(COBBLE_PATH_LEFT, towards -> {
                towards.right(COBBLE_PATH, COBBLE_PATH_RIGHT);
                towards.front().back().apply(COBBLE_PATH_LEFT);
            });
            connections(COBBLE_PATH, towards -> {
                towards.horizontal().apply(COBBLE_PATH);
                towards.left(COBBLE_PATH_LEFT);
                towards.right(COBBLE_PATH_RIGHT);
            });
            connections(COBBLE_PATH_RIGHT, towards -> {
                towards.left(COBBLE_PATH_LEFT, COBBLE_PATH);
                towards.front().back().apply(COBBLE_PATH_RIGHT);
            });
        }

        // Fields
        private final Block block0;
        private final Block block1;
        private final Block block2;
        private final Block block3;
        private final Block block4;
        private final Block block5;
        private final Block block6;
        private final Block block7;
        private @UnknownNullability Connections connections;

        BlockGroups(Block block0, Block block1, Block block2, Block block3,
                    Block block4, Block block5, Block block6, Block block7) {
            this.block0 = block0;
            this.block1 = block1;
            this.block2 = block2;
            this.block3 = block3;
            this.block4 = block4;
            this.block5 = block5;
            this.block6 = block6;
            this.block7 = block7;
        }

        @Override
        public Set<BlockGroup> allowsTo(Direction direction) {
            return switch (direction) {
                case NORTH -> connections().north;
                case SOUTH -> connections().south;
                case EAST -> connections().east;
                case WEST -> connections().west;
                case UP -> connections().up;
                case DOWN -> connections().down;
            };
        }

        @Override
        public void place(Setter setter, int x, int y, int z) {
            /*
             * 0 -> x+, y+, z+
             * 1 -> x+, y+, z-
             * 2 -> x+, y-, z+
             * 3 -> x+, y-, z-
             * 4 -> x-, y+, z+
             * 5 -> x-, y+, z-
             * 6 -> x-, y-, z+
             * 7 -> x-, y-, z-
             */
            int xOffset = x * 2;
            int yOffset = y * 2;
            int zOffset = z * 2;

            if (block0 != null) setter.setBlock(xOffset + 1, yOffset + 1, zOffset + 1, block0);
            if (block1 != null) setter.setBlock(xOffset + 1, yOffset + 1, zOffset + 0, block1);
            if (block2 != null) setter.setBlock(xOffset + 1, yOffset + 0, zOffset + 1, block2);
            if (block3 != null) setter.setBlock(xOffset + 1, yOffset + 0, zOffset + 0, block3);
            if (block4 != null) setter.setBlock(xOffset + 0, yOffset + 1, zOffset + 1, block4);
            if (block5 != null) setter.setBlock(xOffset + 0, yOffset + 1, zOffset + 0, block5);
            if (block6 != null) setter.setBlock(xOffset + 0, yOffset + 0, zOffset + 1, block6);
            if (block7 != null) setter.setBlock(xOffset + 0, yOffset + 0, zOffset + 0, block7);
        }

        private @NotNull Connections connections() {
            if (connections == null) {
                connections = group2Connections.get(this);
                if (connections == null) {
                    throw new IllegalStateException("Connections not registered for " + this);
                }
            }
            return connections;
        }

        private static void connections(BlockGroup group, Consumer<ConnectionRegistry> consumer) {
            Connections connections = group2Connections.computeIfAbsent(group, ignored -> new Connections());

            // Define the registry
            ConnectionRegistry registry = (direction, groups) -> {
                boolean result = false;
                Set<BlockGroup> registered = switch (direction) {
                    case UP -> connections.up;
                    case DOWN -> connections.down;
                    case NORTH -> connections.north;
                    case SOUTH -> connections.south;
                    case EAST -> connections.east;
                    case WEST -> connections.west;
                };
                for (BlockGroup g : groups) {
                    result |= registered.add(g);
                }
                return result;
            };

            // Apply the consumer
            consumer.accept(registry);
        }

        public static class Connections {
            private final Set<BlockGroup> east = new HashSet<>();
            private final Set<BlockGroup> west = new HashSet<>();
            private final Set<BlockGroup> up = new HashSet<>();
            private final Set<BlockGroup> down = new HashSet<>();
            private final Set<BlockGroup> south = new HashSet<>();
            private final Set<BlockGroup> north = new HashSet<>();

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Connections that = (Connections) o;
                return Objects.equals(east, that.east) &&
                        Objects.equals(west, that.west) &&
                        Objects.equals(up, that.up) &&
                        Objects.equals(down, that.down) &&
                        Objects.equals(south, that.south) &&
                        Objects.equals(north, that.north);
            }

            @Override
            public int hashCode() {
                return Objects.hash(east, west, up, down, south, north);
            }
        }
    }

    @SuppressWarnings("unused")
    private interface ConnectionRegistry {

        /**
         * Register a connection from this blockgroup to the given blockgroups in the given direction.
         *
         * @param group The blockgroups to connect to.
         * @return True if the connection was registered, false if the connection was already registered.
         */
        boolean towards(@NotNull Direction direction, @NotNull BlockGroup... group);

        /**
         * Creates a new connection builder with the direction set to the given direction.
         *
         * @return The new connection builder.
         */
        default @NotNull ConnectionBuilder towards(@NotNull Direction direction) {
            return new ConnectionBuilder(this).direction(direction);
        }

        /**
         * Register a connection from this blockgroup to the given blockgroups in the north direction.
         *
         * @param group The blockgroups to connect to.
         * @return True if the connection was registered, false if the connection was already registered.
         */
        default boolean north(@NotNull BlockGroup... group) {
            return towards(Direction.NORTH, group);
        }

        /**
         * Creates a new connection builder with the direction set to the north direction.
         *
         * @return The new connection builder.
         */
        default @NotNull ConnectionBuilder north() {
            return towards(Direction.NORTH);
        }

        /**
         * Register a connection from this blockgroup to the given blockgroups in the east direction.
         *
         * @param group The blockgroups to connect to.
         * @return True if the connection was registered, false if the connection was already registered.
         */
        default boolean east(@NotNull BlockGroup... group) {
            return towards(Direction.EAST, group);
        }

        /**
         * Creates a new connection builder with the direction set to the east direction.
         *
         * @return The new connection builder.
         */
        default @NotNull ConnectionBuilder east() {
            return towards(Direction.EAST);
        }

        /**
         * Register a connection from this blockgroup to the given blockgroups in the south direction.
         *
         * @param group The blockgroups to connect to.
         * @return True if the connection was registered, false if the connection was already registered.
         */
        default boolean south(@NotNull BlockGroup... group) {
            return towards(Direction.SOUTH, group);
        }

        /**
         * Creates a new connection builder with the direction set to the south direction.
         *
         * @return The new connection builder.
         */
        default @NotNull ConnectionBuilder south() {
            return towards(Direction.SOUTH);
        }

        /**
         * Register a connection from this blockgroup to the given blockgroups in the west direction.
         *
         * @param group The blockgroups to connect to.
         * @return True if the connection was registered, false if the connection was already registered.
         */
        default boolean west(@NotNull BlockGroup... group) {
            return towards(Direction.WEST, group);
        }

        /**
         * Creates a new connection builder with the direction set to the west direction.
         *
         * @return The new connection builder.
         */
        default @NotNull ConnectionBuilder west() {
            return towards(Direction.WEST);
        }

        /**
         * Register a connection from this blockgroup to the given blockgroups in the up direction.
         *
         * @param group The blockgroups to connect to.
         * @return True if the connection was registered, false if the connection was already registered.
         */
        default boolean up(@NotNull BlockGroup... group) {
            return towards(Direction.UP, group);
        }

        /**
         * Creates a new connection builder with the direction set to the up direction.
         *
         * @return The new connection builder.
         */
        default @NotNull ConnectionBuilder up() {
            return towards(Direction.UP);
        }

        /**
         * Register a connection from this blockgroup to the given blockgroups in the down direction.
         *
         * @param group The blockgroups to connect to.
         * @return True if the connection was registered, false if the connection was already registered.
         */
        default boolean down(@NotNull BlockGroup... group) {
            return towards(Direction.DOWN, group);
        }

        /**
         * Creates a new connection builder with the direction set to the down direction.
         *
         * @return The new connection builder.
         */
        default @NotNull ConnectionBuilder down() {
            return towards(Direction.DOWN);
        }

        /**
         * Register a connection from this blockgroup to the given blockgroups in the north direction.
         *
         * @param group The blockgroups to connect to.
         * @return True if the connection was registered, false if the connection was already registered.
         */
        default boolean back(@NotNull BlockGroup... group) {
            return towards(Direction.NORTH, group);
        }

        /**
         * Creates a new connection builder with the direction set to the north direction.
         *
         * @return The new connection builder.
         */
        default @NotNull ConnectionBuilder back() {
            return towards(Direction.NORTH);
        }

        /**
         * Register a connection from this blockgroup to the given blockgroups in the east direction.
         *
         * @param group The blockgroups to connect to.
         * @return True if the connection was registered, false if the connection was already registered.
         */
        default boolean right(@NotNull BlockGroup... group) {
            return towards(Direction.EAST, group);
        }

        /**
         * Creates a new connection builder with the direction set to the east direction.
         *
         * @return The new connection builder.
         */
        default @NotNull ConnectionBuilder right() {
            return towards(Direction.EAST);
        }

        /**
         * Register a connection from this blockgroup to the given blockgroups in the south direction.
         *
         * @param group The blockgroups to connect to.
         * @return True if the connection was registered, false if the connection was already registered.
         */
        default boolean front(@NotNull BlockGroup... group) {
            return towards(Direction.SOUTH, group);
        }

        /**
         * Creates a new connection builder with the direction set to the south direction.
         *
         * @return The new connection builder.
         */
        default @NotNull ConnectionBuilder front() {
            return towards(Direction.SOUTH);
        }

        /**
         * Register a connection from this blockgroup to the given blockgroups in the west direction.
         *
         * @param group The blockgroups to connect to.
         * @return True if the connection was registered, false if the connection was already registered.
         */
        default boolean left(@NotNull BlockGroup... group) {
            return towards(Direction.WEST, group);
        }

        /**
         * Creates a new connection builder with the direction set to the west direction.
         *
         * @return The new connection builder.
         */
        default @NotNull ConnectionBuilder left() {
            return towards(Direction.WEST);
        }

        /**
         * Register a connection from this blockgroup to the given blockgroups in the up direction.
         *
         * @param group The blockgroups to connect to.
         * @return True if the connection was registered, false if the connection was already registered.
         */
        default boolean top(@NotNull BlockGroup... group) {
            return towards(Direction.UP, group);
        }

        /**
         * Creates a new connection builder with the direction set to the up direction.
         *
         * @return The new connection builder.
         */
        default @NotNull ConnectionBuilder top() {
            return towards(Direction.UP);
        }

        /**
         * Register a connection from this blockgroup to the given blockgroups in the down direction.
         *
         * @param group The blockgroups to connect to.
         * @return True if the connection was registered, false if the connection was already registered.
         */
        default boolean bottom(@NotNull BlockGroup... group) {
            return towards(Direction.DOWN, group);
        }

        /**
         * Creates a new connection builder with the direction set to the down direction.
         *
         * @return The new connection builder.
         */
        default @NotNull ConnectionBuilder bottom() {
            return towards(Direction.DOWN);
        }

        /**
         * Register a connection from this blockgroup to the given blockgroups in the north, east, south, and west directions.
         *
         * @return True if the connection was registered, false if the connection was already registered.
         */
        default boolean horizontal(@NotNull BlockGroup... group) {
            boolean result = north(group);
            result |= east(group);
            result |= south(group);
            result |= west(group);
            return result;
        }

        /**
         * Creates a new connection builder with the directions set to the north, east, south, and west directions.
         *
         * @return The new connection builder.
         */
        default @NotNull ConnectionBuilder horizontal() {
            return north().east().south().west();
        }

        class ConnectionBuilder {
            private final Set<Direction> directions = new HashSet<>();
            private final ConnectionRegistry registry;

            private ConnectionBuilder(@NotNull ConnectionRegistry registry) {
                this.registry = registry;
            }

            /**
             * Adds the given direction to the list of directions to connect to.
             *
             * @param direction The direction to connect to.
             * @return This builder.
             */
            public @NotNull ConnectionBuilder direction(@NotNull Direction direction) {
                directions.add(direction);
                return this;
            }

            /**
             * Adds the north direction to the list of directions to connect to.
             *
             * @return This builder.
             */
            public @NotNull ConnectionBuilder north() {
                return direction(Direction.NORTH);
            }

            /**
             * Adds the east direction to the list of directions to connect to.
             *
             * @return This builder.
             */
            public @NotNull ConnectionBuilder east() {
                return direction(Direction.EAST);
            }

            /**
             * Adds the south direction to the list of directions to connect to.
             *
             * @return This builder.
             */
            public @NotNull ConnectionBuilder south() {
                return direction(Direction.SOUTH);
            }

            /**
             * Adds the west direction to the list of directions to connect to.
             *
             * @return This builder.
             */
            public @NotNull ConnectionBuilder west() {
                return direction(Direction.WEST);
            }

            /**
             * Adds the up direction to the list of directions to connect to.
             *
             * @return This builder.
             */
            public @NotNull ConnectionBuilder up() {
                return direction(Direction.UP);
            }

            /**
             * Adds the down direction to the list of directions to connect to.
             *
             * @return This builder.
             */
            public @NotNull ConnectionBuilder down() {
                return direction(Direction.DOWN);
            }

            /**
             * Adds the north direction to the list of directions to connect to.
             *
             * @return This builder.
             */
            public @NotNull ConnectionBuilder back() {
                return north();
            }

            /**
             * Adds the east direction to the list of directions to connect to.
             *
             * @return This builder.
             */
            public @NotNull ConnectionBuilder right() {
                return east();
            }

            /**
             * Adds the south direction to the list of directions to connect to.
             *
             * @return This builder.
             */
            public @NotNull ConnectionBuilder front() {
                return south();
            }

            /**
             * Adds the west direction to the list of directions to connect to.
             *
             * @return This builder.
             */
            public @NotNull ConnectionBuilder left() {
                return west();
            }

            /**
             * Adds the up direction to the list of directions to connect to.
             *
             * @return This builder.
             */
            public @NotNull ConnectionBuilder top() {
                return up();
            }

            /**
             * Adds the down direction to the list of directions to connect to.
             *
             * @return This builder.
             */
            public @NotNull ConnectionBuilder bottom() {
                return down();
            }

            /**
             * Applies the specified connection to the blockgroup.
             *
             * @return True if the connection made any changes to the existing connections.
             */
            public boolean apply(BlockGroup... groups) {
                boolean changed = false;
                for (Direction direction : directions) {
                    for (BlockGroup group : groups) {
                        changed |= registry.towards(direction, group);
                    }
                }
                return changed;
            }
        }
    }
}
