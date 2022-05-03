package net.minestom.arena.game.procedural;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minestom.arena.game.Arena;
import net.minestom.arena.group.Group;
import net.minestom.arena.utils.FullbrightDimension;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ProceduralArena implements Arena {

    private final Group group;

    public ProceduralArena(Group group) {
        this.group = group;
    }

    @Override
    public @NotNull Group group() {
        return group;
    }

    @Override
    public @NotNull CompletableFuture<Void> init() {
        return CompletableFuture.runAsync(() -> {
            // Show the loading arena title until the arena is loaded
            group.audience().showTitle(
                    Title.title(Component.text("Loading arena..."),
                            Component.empty(),
                            Title.Times.times(
                                    Duration.of(1, TimeUnit.SECOND),
                                    Duration.of(365, TimeUnit.DAY),
                                    Duration.ZERO
                            )
                    )
            );

            // Load the arena
            InstanceContainer instanceContainer = MinecraftServer.getInstanceManager()
                    .createInstanceContainer(FullbrightDimension.INSTANCE);
            instanceContainer.setGenerator(new WaveFunctionCollapseGenerator(System.currentTimeMillis()));

            // Add the players to the arena
            CompletableFuture<?>[] futures = group.members().stream()
                    .map(player -> player.setInstance(instanceContainer, new Pos(0, 0, 0)))
                    .toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(futures).join();
            for (Player member : group.members()) {
                member.setGameMode(GameMode.SPECTATOR);
            }

            // Remove the loading arena title
            group.audience()
                    .showTitle(
                            Title.title(Component.empty(), Component.empty(),
                                    Title.Times.times(Duration.of(1, TimeUnit.SECOND),
                                            Duration.of(1, TimeUnit.SECOND), Duration.ZERO)));
        });
    }
}
