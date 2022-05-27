package net.minestom.arena;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.GarbageCollectorExports;
import io.prometheus.client.hotspot.MemoryPoolsExports;
import net.minestom.arena.config.ConfigHandler;
import net.minestom.server.MinecraftServer;

import java.io.IOException;

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
    public static final Counter PACKETS = Counter.build().name("packets").help("Number of packets by direction")
            .labelNames("direction").register();

    public static void init() {
        try {
            new HTTPServer(ConfigHandler.CONFIG.prometheus().port());
            new MemoryPoolsExports().register();
            new GarbageCollectorExports().register();
        } catch (IOException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
    }
}
