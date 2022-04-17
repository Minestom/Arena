package net.minestom.arena.game;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.instance.RemoveEntityFromInstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public interface SingleInstanceArena extends Arena {
    @NotNull Instance instance();

    @NotNull Pos spawnPosition();

    void start();

    @Override
    default void join(@NotNull Player player) {
        Instance instance = instance();
        // Register this arena
        MinecraftServer.getInstanceManager().registerInstance(instance);

        instance.eventNode().addListener(RemoveEntityFromInstanceEvent.class, (event) -> {
            // We don't care about entities, only players.
            if ((event.getEntity() instanceof Player)) return;
            for (Player p : instance.getPlayers()) {
                // There is still a player in this instance which is not scheduled to be removed.
                if (p != event.getEntity()) {
                    return;
                }
            }
            // All players have left. We can remove this instance.
            MinecraftServer.getInstanceManager().unregisterInstance(instance);
        });
        player.setInstance(instance, spawnPosition()).thenRun(this::start);
    }
}
