package net.minestom.arena.game;

import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.DimensionType;

import java.util.UUID;

public final class ArenaInstance extends InstanceContainer {
    public ArenaInstance() {
        super(UUID.randomUUID(), DimensionType.OVERWORLD);
        setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.SAND));
    }
}
