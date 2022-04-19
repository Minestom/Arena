package net.minestom.arena;

import net.minestom.arena.utils.FullbrightDimension;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.instance.AddEntityToInstanceEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;

import java.util.UUID;

public final class Lobby extends InstanceContainer {
    public static final Lobby INSTANCE = new Lobby();

    static {
        MinecraftServer.getInstanceManager().registerInstance(INSTANCE);
    }

    private Lobby() {
        super(UUID.randomUUID(), FullbrightDimension.INSTANCE);
        setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));
        getWorldBorder().setDiameter(100);
        setTimeRate(0);

        eventNode().addListener(AddEntityToInstanceEvent.class, event -> {
            final Entity entity = event.getEntity();
            if (entity instanceof Player player) {
                // Refresh visible commands if the player previously was in an arena
                final Instance instance = player.getInstance();
                if (instance != null) player.scheduler().scheduleNextTick(player::refreshCommands);
            }
        }).addListener(ItemDropEvent.class, event -> event.setCancelled(true));
    }
}
