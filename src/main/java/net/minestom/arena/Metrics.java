package net.minestom.arena;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Info;
import io.prometheus.client.Summary;
import io.prometheus.client.exemplars.Exemplar;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.GarbageCollectorExports;
import io.prometheus.client.hotspot.MemoryPoolsExports;
import net.minestom.arena.config.ConfigHandler;
import net.minestom.arena.utils.NetworkUsage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.timer.TaskSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class Metrics {
    private static final Logger LOGGER = LoggerFactory.getLogger(Metrics.class);
    public static final Gauge ENTITIES = Gauge.build().name("entities")
            .help("Total entities alive (excluding players)").register();
    public static final Gauge GAMES_IN_PROGRESS = Gauge.build().name("games_in_progress")
            .labelNames("type").help("Games currently running").register();
    public static final Counter GAMES_PLAYED = Counter.build().name("games_played")
            .labelNames("type").help("Number of games played").register();
    public static final Summary TICK_TIME = Summary.build().name("tick_time")
            .help("ms per tick").quantile(0, 1).quantile(.5, .01).quantile(1, 0)
            .maxAgeSeconds(5).unit("ms").register();
    public static final Summary ACQUISITION_TIME = Summary.build().name("acquisition_time")
            .help("ms per acquisition").quantile(0, 1).quantile(.5, .01).quantile(1, 0)
            .maxAgeSeconds(5).unit("ms").register();
    public static final Counter EXCEPTIONS = Counter.build().name("exceptions")
            .help("Number of exceptions").labelNames("simple_name").register();
    private static final Counter PACKETS = Counter.build().name("packets").help("Number of packets by direction")
            .labelNames("direction").register();
    private static final Gauge ONLINE_PLAYERS = Gauge.build().name("online_players")
            .help("Number of currently online players").register();
    private static final Gauge NETWORK_IO = Gauge.build().name("network_io").unit("bytes").labelNames("direction")
            .help("Network usage").register();
    private static final Info GENERIC_INFO = Info.build().name("generic").help("Generic system information")
            .register();


    public static void init() {
        try {
            final String unknown = "unknown";
            GENERIC_INFO.info(
                    "java_version", System.getProperty("java.version", unknown),
                    "java_vendor", System.getProperty("java.vendor", unknown),
                    "os_arch", System.getProperty("os.arch", unknown),
                    "os_name", System.getProperty("os.name", unknown),
                    "os_version", System.getProperty("os.version", unknown)
                    );

            MinecraftServer.getGlobalEventHandler()
                    .addListener(PlayerPacketEvent.class, e -> Metrics.PACKETS.labels("in").inc())
                    .addListener(PlayerPacketOutEvent.class, e -> Metrics.PACKETS.labels("out").inc())
                    .addListener(PlayerLoginEvent.class, e -> Metrics.ONLINE_PLAYERS.inc())
                    .addListener(PlayerDisconnectEvent.class, e -> Metrics.ONLINE_PLAYERS.dec());

            // Network usage
            if (NetworkUsage.executablesPresent()) {
                NetworkUsage.resetCounters();
                MinecraftServer.getSchedulerManager()
                        .scheduleTask(() -> NETWORK_IO.labels("out").set(NetworkUsage.getBytesSent()),
                                TaskSchedule.seconds(0), TaskSchedule.seconds(1));
                MinecraftServer.getSchedulerManager()
                        .scheduleTask(() -> NETWORK_IO.labels("in").set(NetworkUsage.getBytesReceived()),
                                TaskSchedule.seconds(0), TaskSchedule.seconds(1));
            } else {
                LOGGER.warn("No executables found for network metrics");
            }

            new HTTPServer(ConfigHandler.CONFIG.prometheus().port());
            new MemoryPoolsExports().register();
            new GarbageCollectorExports().register();
        } catch (IOException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
    }
}
