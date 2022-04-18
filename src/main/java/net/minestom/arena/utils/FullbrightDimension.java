package net.minestom.arena.utils;

import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;

public class FullbrightDimension {
    public static final DimensionType INSTANCE = DimensionType.builder(NamespaceID.from("minestom:full_bright"))
            .ambientLight(2.0f)
            .build();

    static {
        MinecraftServer.getDimensionTypeManager().addDimension(INSTANCE);
    }
}
