package net.minestom.arena.game;

import net.minestom.arena.Lobby;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Arena {

    private static final ConcurrentHashMap<UUID, Arena> arenaList = new ConcurrentHashMap<>();
    private static final Tag<UUID> arenaTag = Tag.UUID("arena");

    private final @NotNull ArenaInstance arenaInstance;

    public Arena() {
        this.arenaInstance = new ArenaInstance();

        arenaList.put(arenaInstance.getUniqueId(), this);
        MinecraftServer.getInstanceManager().registerInstance(this.arenaInstance);
    }

    public void join(@NotNull Player player) {
        player.setInstance(arenaInstance);
        player.setTag(arenaTag, arenaInstance.getUniqueId());
    }

    public void leave(@NotNull Player player) {
        player.setInstance(Lobby.INSTANCE);
        player.removeTag(arenaTag);

        if (arenaInstance.getPlayers().isEmpty()) {
            // All players have left. We can remove this instance.
            MinecraftServer.getInstanceManager().unregisterInstance(arenaInstance);
        }
    }

    public static Arena getArena(Player player) {
        if (!player.hasTag(arenaTag)) return null;

        return arenaList.get(player.getTag(arenaTag));
    }

}
