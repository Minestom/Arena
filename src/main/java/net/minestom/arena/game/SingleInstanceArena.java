package net.minestom.arena.game;

import net.minestom.arena.feature.Feature;
import net.minestom.arena.utils.VoidFuture;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.instance.RemoveEntityFromInstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class SingleInstanceArena extends Arena {
    @NotNull
    protected abstract Instance instance();

    @NotNull
    protected abstract Pos spawnPosition(@NotNull Player player);

    @NotNull
    protected abstract List<Feature> features();

    @Override
    public final VoidFuture init() {
        Instance instance = instance();
        // Register this arena
        MinecraftServer.getInstanceManager().registerInstance(instance);

        instance.eventNode().addListener(RemoveEntityFromInstanceEvent.class, (event) -> {
            // We don't care about entities, only players.
            if (!(event.getEntity() instanceof Player)) return;
            // Ensure there is only this player in the instance
            if (instance.getPlayers().size() > 1) return;
            // Handle shutdown
            stop(true);
        });

        for (Feature feature : features()) {
            feature.hook(instance.eventNode());
        }

        CompletableFuture<?>[] futures =
                group().members().stream()
                    .map(player -> player.setInstance(instance, spawnPosition(player)))
                    .toArray(CompletableFuture<?>[]::new);

        final VoidFuture future = new VoidFuture();
        CompletableFuture.allOf(futures).thenRun(future::complete);
        return future;
    }

    protected abstract VoidFuture handleOnStop(boolean normalEnding);

    @Override
    protected final VoidFuture onStop(boolean normalEnding) {
        // All players have left. We can remove this instance once the player is removed.
        instance().scheduleNextTick(ignored -> MinecraftServer.getInstanceManager().unregisterInstance(instance()));
        return handleOnStop(normalEnding);
    }
}
