package net.minestom.arena;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.arena.config.ConfigHandler;
import net.minestom.arena.config.ConfigurationReloadedEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.ping.ResponseData;

import java.io.InputStream;
import java.util.Base64;
import java.util.List;

final class ServerList {
    private static final String FAVICON = favicon();
    private static Component motd = motd();

    public static void hook(EventNode<Event> eventNode) {
        eventNode.addListener(ServerListPingEvent.class, event -> {
            final ResponseData responseData = event.getResponseData();
            responseData.setDescription(motd);
            if (FAVICON != null)
                responseData.setFavicon(FAVICON);
            responseData.setMaxPlayer(100);
            responseData.addEntries(MinecraftServer.getConnectionManager().getOnlinePlayers());
        }).addListener(ConfigurationReloadedEvent.class, e -> motd = motd());
    }

    private static String favicon() {
        String favicon = null;
        try (InputStream stream = Main.class.getResourceAsStream("/favicon.png")) {
            if (stream != null)
                favicon = "data:image/png;base64," + Base64.getEncoder().encodeToString(stream.readAllBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return favicon;
    }

    private static Component motd() {
        final MiniMessage miniMessage = MiniMessage.miniMessage();
        final List<String> motd = ConfigHandler.CONFIG.server().motd();
        return motd.stream()
            .map(miniMessage::deserialize)
            .reduce(Component.empty(), (a, b) -> a.append(b).appendNewline());
    }
}
