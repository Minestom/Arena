package net.minestom.arena;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.network.packet.client.play.ClientPongPacket;
import net.minestom.server.network.packet.server.play.PingPacket;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public final class PingCommand extends Command {
    public PingCommand() {
        super("ping");

        setDefaultExecutor((sender, context) -> {
            final Player player = (Player) sender;
            final int id = ThreadLocalRandom.current().nextInt();
            final AtomicLong now = new AtomicLong();
            
            player.sendPacket(new PingPacket(id));
            MinecraftServer.getGlobalEventHandler().addListener(EventListener.builder(PlayerPacketOutEvent.class)
                    .filter(event -> event.getPlayer().equals(player)
                            && event.getPacket() instanceof PingPacket pingPacket
                            && pingPacket.id() == id)
                    .expireCount(1)
                    .handler(event -> now.set(System.currentTimeMillis()))
                    .build()
            );
            player.eventNode().addListener(EventListener.builder(PlayerPacketEvent.class)
                    .filter(event -> event.getPacket() instanceof ClientPongPacket pongPacket && pongPacket.id() == id)
                    .expireCount(1)
                    .handler(event ->
                            Messenger.info(player, "Your ping is " + (System.currentTimeMillis() - now.get()) + "ms"))
                    .build()
            );
        });
    }
}
