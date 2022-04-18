package net.minestom.arena.game;

import net.minestom.arena.feature.Feature;
import net.minestom.arena.group.Group;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.RemoveEntityFromInstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SingleInstanceArena extends Arena {
    @NotNull Instance instance();

    @NotNull Pos spawnPosition();

    @NotNull List<Feature> features();

    void start();

    @Override
    default void join(@NotNull Group group) {
        Instance instance = instance();
        // Register this arena
        MinecraftServer.getInstanceManager().registerInstance(instance);

        instance.eventNode().addListener(RemoveEntityFromInstanceEvent.class, (event) -> {
            // We don't care about entities, only players.
            if (!(event.getEntity() instanceof Player)) return;
            // Ensure there is only this player in the instance
            if (instance.getPlayers().size() > 1) return;
            // All players have left. We can remove this instance once the player is removed.
            instance.scheduleNextTick(ignored -> MinecraftServer.getInstanceManager().unregisterInstance(instance));
        });

        for (var feature : features()) {
            feature.hook(EventNode.class.cast(instance.eventNode()));
        }

        final List<Player> members = group.members();
        @SuppressWarnings("rawtypes") CompletableFuture[] futures = new CompletableFuture[members.size()];
        for (int i = 0; i < members.size(); i++) {
            Player player = members.get(i);
            futures[i] = player.setInstance(instance, spawnPosition());
        }
        CompletableFuture.allOf(futures).thenRun(this::start);
    }
}
