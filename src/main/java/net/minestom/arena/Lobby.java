package net.minestom.arena;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.DimensionType;

import java.util.UUID;

public final class Lobby extends InstanceContainer {
    public static final Lobby INSTANCE = new Lobby();

    static {
        MinecraftServer.getInstanceManager().registerInstance(INSTANCE);
    }

    private Lobby() {
        super(UUID.randomUUID(), DimensionType.OVERWORLD);
        setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));
    }
}
