package net.minestom.arena.game;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.minestom.arena.Messenger;
import net.minestom.arena.utils.ConcurrentUtils;
import net.minestom.arena.utils.FutureUtils;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class GameManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameManager.class);
    /**
     * Duration to wait for games to end naturally
     */
    private static final Duration STOP_TIMEOUT = Duration.ofMinutes(20);
    /**
     * Duration in which games must stop after calling the shutdown method
     */
    private static final Duration SHUTDOWN_TIMEOUT = Duration.ofMinutes(15);
    private static boolean isStopping = false;
    private static final Component STOP_KICK_MESSAGE = Component.text("Server shutting down", Messenger.RED_COLOR);
    private static final Set<Game> GAMES = Collections.newSetFromMap(new WeakHashMap<>());

    /**
     * Used to register a game, so on shutdown they won't be interrupted
     * @param game the game to register
     */
    public static void registerGame(Game game) {
        GAMES.add(game);
    }

    public static boolean canStartGame(@Nullable Audience audience) {
        if (isStopping && audience != null) {
            Messenger.error(audience, "Server is shutting down, new games cannot be started!");
        }
        return !isStopping;
    }

    public static void stopServer() {
        if (isStopping) return;
        LOGGER.info("Preparing to stop server.");
        Messenger.warn(PacketGroupingAudience.of(MinecraftServer.getConnectionManager().getOnlinePlayers()),
                Component.text("Server will shut down after games ended or within ")
                        .append(Component.text(STOP_TIMEOUT.toMinutes()+" minutes", Messenger.RED_COLOR)
                                .hoverEvent(HoverEvent.showText(Component
                                        .text("At " + new Date().toInstant().plus(STOP_TIMEOUT)))))
                        .append(Component.text(" at the latest, new games cannot be started!")));
        isStopping = true;

        final Predicate<Game> notEndedGameFilter = x -> x.getState().isBefore(GameState.ENDED);
        var gamesInProgress = GAMES.stream().filter(notEndedGameFilter).toList();

        LOGGER.info("Waiting for {} game(s) to finish within {} minute(s)", gamesInProgress.size(), STOP_TIMEOUT.toMinutes());

        ConcurrentUtils.thenRunOrTimeout(
                ConcurrentUtils.joinFutureStream(gamesInProgress.stream().map(Game::getGameFuture)),
                STOP_TIMEOUT, (timeoutReached) -> {
                   if (timeoutReached) {
                       final var stillNotEndedGames = gamesInProgress.stream().filter(notEndedGameFilter).toList();
                       if (stillNotEndedGames.size() > 0) {
                           LOGGER.info("{} game(s) still not ended, explicitly requesting game shutdown.", stillNotEndedGames.size());
                           ConcurrentUtils.thenRunOrTimeout(
                                   ConcurrentUtils.joinFutureStream(stillNotEndedGames.stream().map(Game::shutdown)),
                                   SHUTDOWN_TIMEOUT,
                                   (timeout) -> {
                                       if (timeout) {
                                           for (String game : stillNotEndedGames.stream().filter(notEndedGameFilter).map(Object::getClass)
                                                   .map(Class::getCanonicalName).collect(Collectors.toSet())) {
                                               LOGGER.warn("Game with type of {} didn't shutdown in the given time limit!", game);
                                           }
                                       }
                                       kickPlayers();
                                   }
                           );
                       } else {
                           kickPlayers();
                       }
                   } else {
                       kickPlayers();
                   }
                });
    }

    private static void kickPlayers() {
        LOGGER.info("All games ended, kicking {} online player(s)", MinecraftServer.getConnectionManager().getOnlinePlayers().size());

        // Kick players and shut down server
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> player.kick(STOP_KICK_MESSAGE));
        FutureUtils.completeAfter(Duration.ofMillis(150)).thenRun(MinecraftServer::stopCleanly);
    };
}
