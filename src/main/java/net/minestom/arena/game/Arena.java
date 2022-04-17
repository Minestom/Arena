package net.minestom.arena.game;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.instance.AddEntityToInstanceEvent;
import net.minestom.server.event.instance.RemoveEntityFromInstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Arena {

    @NotNull Tag<UUID> arenaTag = Tag.UUID("arena");

    @NotNull Instance getArenaInstance();

    CompletableFuture<Void> join(@NotNull Player player);

    default void init() {
        // Register this arena
        MinecraftServer.getInstanceManager().registerInstance(getArenaInstance());

        getArenaInstance().eventNode().addListener(AddEntityToInstanceEvent.class, (event) -> {
            if (event.getEntity() instanceof Player player)
                player.setTag(arenaTag, getArenaInstance().getUniqueId());
        });

        getArenaInstance().eventNode().addListener(RemoveEntityFromInstanceEvent.class, (event) -> {
            // We don't care about entities, only players.
            if ((event.getEntity() instanceof Player)) return;

            // If a player leaves the instance, remove the tag from them.
            event.getEntity().removeTag(arenaTag);

            for (Player player : getArenaInstance().getPlayers()) {
                // There is still a player in this instance which is not scheduled to be removed.
                if (player != event.getEntity()) {
                    return;
                }
            }

            // All players have left. We can remove this instance.
            MinecraftServer.getInstanceManager().unregisterInstance(getArenaInstance());
        });
    }

    void start();

}
