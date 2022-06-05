package net.minestom.arena;

import com.sun.management.OperatingSystemMXBean;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Info;
import io.prometheus.client.Summary;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.GarbageCollectorExports;
import io.prometheus.client.hotspot.MemoryPoolsExports;
import net.minestom.arena.config.ConfigHandler;
import net.minestom.arena.utils.NetworkUsage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.EntitySpawnEvent;
import net.minestom.server.event.instance.RemoveEntityFromInstanceEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.timer.TaskSchedule;

import java.io.IOException;
import java.lang.management.ManagementFactory;

public final class Metrics {
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
    private static final Gauge CPU_USAGE = Gauge.build().name("cpu").help("CPU Usage").register();


    public static void init() {
        try {
            final String unknown = "unknown";
            final OperatingSystemMXBean systemMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            GENERIC_INFO.info(
                    "java_version", System.getProperty("java.version", unknown),
                    "java_vendor", System.getProperty("java.vendor", unknown),
                    "os_arch", System.getProperty("os.arch", unknown),
                    "os_name", System.getProperty("os.name", unknown),
                    "os_version", System.getProperty("os.version", unknown),
                    "available_processors", ""+systemMXBean.getAvailableProcessors()
            );

            // Packets & players
            MinecraftServer.getGlobalEventHandler()
                    .addListener(PlayerPacketEvent.class, e -> Metrics.PACKETS.labels("in").inc())
                    .addListener(PlayerPacketOutEvent.class, e -> Metrics.PACKETS.labels("out").inc())
                    .addListener(PlayerLoginEvent.class, e -> Metrics.ONLINE_PLAYERS.inc())
                    .addListener(PlayerDisconnectEvent.class, e -> Metrics.ONLINE_PLAYERS.dec())
                    .addListener(EntitySpawnEvent.class, e -> {
                        if (!(e.getEntity() instanceof Player)) Metrics.ENTITIES.inc();
                    }).addListener(RemoveEntityFromInstanceEvent.class, e -> {
                        if (!(e.getEntity() instanceof Player)) Metrics.ENTITIES.dec();
                    });

            // Network usage
            if (NetworkUsage.checkEnabledOrExtract()) {
                NetworkUsage.resetCounters();
                MinecraftServer.getSchedulerManager()
                        .scheduleTask(() -> {
                                    NETWORK_IO.labels("out").set(NetworkUsage.getBytesSent());
                                    NETWORK_IO.labels("in").set(NetworkUsage.getBytesReceived());
                                }, TaskSchedule.seconds(0), TaskSchedule.seconds(2));
            }

            // CPU
            MinecraftServer.getSchedulerManager()
                    .scheduleTask(() -> {
                        CPU_USAGE.set(systemMXBean.getProcessCpuLoad());
                    }, TaskSchedule.seconds(0), TaskSchedule.seconds(2));

            new HTTPServer(ConfigHandler.CONFIG.prometheus().port());
            new MemoryPoolsExports().register();
            new GarbageCollectorExports().register();
        } catch (IOException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
    }
}
