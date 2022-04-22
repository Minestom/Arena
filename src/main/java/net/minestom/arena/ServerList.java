package net.minestom.arena;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.ping.ResponseData;
import net.minestom.server.ping.ServerListPingType;

import java.io.InputStream;
import java.util.Base64;

public final class ServerList {
    private static final String FAVICON;

    static {
        String favicon = null;
        try (InputStream stream = Main.class.getResourceAsStream("/favicon.png")) {
            if (stream != null)
                favicon = "data:image/png;base64," + Base64.getEncoder().encodeToString(stream.readAllBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        FAVICON = favicon;
    }

    public static void hook(EventNode<Event> eventNode) {
        eventNode.addListener(ServerListPingEvent.class, event -> {
            final ResponseData responseData = event.getResponseData();
            final boolean rgb = event.getPingType() == ServerListPingType.MODERN_FULL_RGB;

            responseData.setDescription(Component.text("Minestom Arena").color(rgb ? Messenger.ORANGE_COLOR : NamedTextColor.GOLD));
            if (FAVICON != null)
                responseData.setFavicon(FAVICON);
            responseData.setMaxPlayer(100);
            responseData.addEntries(MinecraftServer.getConnectionManager().getOnlinePlayers());
        });
    }
}
