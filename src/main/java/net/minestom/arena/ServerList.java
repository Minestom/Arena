package net.minestom.arena;

import net.kyori.adventure.text.Component;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.ping.ResponseData;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;

public final class ServerList {
    public static void hook(EventNode<Event> eventNode) {
        String favicon = "";
        try {
            final InputStream faviconStream = Main.class.getResourceAsStream("/favicon.png");
            assert faviconStream != null;
            BufferedImage image = ImageIO.read(faviconStream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            favicon = "data:image/png;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
            outputStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String finalFavicon = favicon;
        eventNode.addListener(ServerListPingEvent.class, event -> {
            ResponseData responseData = event.getResponseData();
            responseData.setDescription(Component.text("Minestom Arena").color(Messenger.ORANGE_COLOR));
            responseData.setFavicon(finalFavicon);
        });
    }
}
