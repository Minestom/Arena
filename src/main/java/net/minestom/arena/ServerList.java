package net.minestom.arena;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.arena.config.ConfigHandler;
import net.minestom.arena.config.ConfigurationChangedEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.ping.ResponseData;

import java.io.InputStream;
import java.util.Base64;

@SuppressWarnings("SpellCheckingInspection")
final class ServerList {
    private static final String FAVICON;
    private static Component motd;

    static {
        String favicon = null;
        try (InputStream stream = Main.class.getResourceAsStream("/favicon.png")) {
            if (stream != null)
                favicon = "data:image/png;base64," + Base64.getEncoder().encodeToString(stream.readAllBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        FAVICON = favicon;
        parseMotd();
    }

    public static void hook(EventNode<Event> eventNode) {
        eventNode.addListener(ServerListPingEvent.class, event -> {
            final ResponseData responseData = event.getResponseData();
            responseData.setDescription(motd);
            if (FAVICON != null)
                responseData.setFavicon(FAVICON);
            responseData.setMaxPlayer(100);
            responseData.addEntries(MinecraftServer.getConnectionManager().getOnlinePlayers());
        }).addListener(ConfigurationChangedEvent.class, e -> parseMotd());
    }

    private static void parseMotd() {
        final MiniMessage miniMessage = MiniMessage.miniMessage();
        motd = miniMessage.deserialize(ConfigHandler.CONFIG.server().motd().get(0))
                .append(Component.newline())
                .append(miniMessage.deserialize(ConfigHandler.CONFIG.server().motd().get(1)));
    }
}
