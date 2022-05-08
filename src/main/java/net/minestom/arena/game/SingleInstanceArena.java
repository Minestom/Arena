package net.minestom.arena.game;

import net.minestom.arena.LobbySidebarDisplay;
import net.minestom.arena.feature.Feature;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.instance.RemoveEntityFromInstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class SingleInstanceArena extends Game {
    @NotNull
    protected abstract Instance instance();

    @NotNull
    protected abstract Pos spawnPosition(@NotNull Player player);

    @NotNull
    protected abstract List<Feature> features();

    @Override
    public final CompletableFuture<Void> init() {
        Instance instance = instance();
        // Register this arena
        MinecraftServer.getInstanceManager().registerInstance(instance);

        instance.eventNode().addListener(RemoveEntityFromInstanceEvent.class, event -> {
            // We don't care about entities, only players.
            if (!(event.getEntity() instanceof Player)) return;
            // Ensure there is only this player in the instance
            if (instance.getPlayers().size() > 1) return;
            // Handle shutdown
            end();
        });

        for (Feature feature : features()) {
            feature.hook(instance.eventNode());
        }

        //TODO Move to start
        CompletableFuture<?>[] futures =
                group().members().stream()
                    .map(player -> player.setInstance(instance, spawnPosition(player)))
                    .toArray(CompletableFuture<?>[]::new);

        final CompletableFuture<Void> future = new CompletableFuture<>();
        CompletableFuture.allOf(futures).thenRun(() -> future.complete(null));
        group().members().forEach(Player::refreshCommands);
        return future;
    }

    protected abstract CompletableFuture<Void> handleOnStop();

    @Override
    protected final CompletableFuture<Void> onEnd() {
        // All players have left. We can remove this instance once the player is removed.
        instance().scheduleNextTick(ignored -> MinecraftServer.getInstanceManager().unregisterInstance(instance()));
        group().setDisplay(new LobbySidebarDisplay(group()));
        return handleOnStop();
    }
}
