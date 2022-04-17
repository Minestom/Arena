package net.minestom.arena.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minestom.arena.mob.RandomMob;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.instance.RemoveEntityFromInstanceEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Arena {

    private static final ConcurrentHashMap<UUID, Arena> arenaList = new ConcurrentHashMap<>();
    private static final Tag<UUID> arenaTag = Tag.UUID("arena");

    private final @NotNull EventNode<InstanceEvent> node;
    private final @NotNull ArenaInstance arenaInstance;
    private int stage = 0;

    public void nextStage() {
        stage++;

        for (int i = 0; i < stage; i++) {
            EntityCreature creature = RandomMob.random(stage);

            creature.setInstance(arenaInstance, new Pos(0, 42, 0));
        }

        for (Player player : arenaInstance.getPlayers()) {
            player.showTitle(Title.title(Component.text("Stage " + stage), Component.empty()));
            player.sendMessage(Component.text("Stage " + stage));
        }

    }

    public Arena() {
        this.arenaInstance = new ArenaInstance();
        this.node = EventNode.event(
                "arena-" + arenaInstance.getUniqueId(),
                EventFilter.INSTANCE,
                event -> event.getInstance() == arenaInstance
        );

        // Register this
        arenaList.put(arenaInstance.getUniqueId(), this);
        MinecraftServer.getInstanceManager().registerInstance(this.arenaInstance);

        node.addListener(RemoveEntityFromInstanceEvent.class, (event) -> {
            // We don't care about entities, only players.
            if ((event.getEntity() instanceof Player)) return;

            // If a player leaves the instance, remove the tag from them.
            event.getEntity().removeTag(arenaTag);


            for (Player player : arenaInstance.getPlayers()) {
                // There is still a player in this instance which is not scheduled to be removed.
                if (player != event.getEntity()) {
                    return;
                }
            }

            // All players have left. We can remove this instance.
            MinecraftServer.getInstanceManager().unregisterInstance(arenaInstance);
            MinecraftServer.getGlobalEventHandler().removeChild(node);
        });

        node.addListener(EntityDeathEvent.class, (event) -> {
            for (Entity entity : this.arenaInstance.getEntities()) {
                if (!(entity instanceof Player)) {
                    // TODO give money;
                    return; // round hasn't ended yet
                }
            }

            nextStage();
        });

        MinecraftServer.getGlobalEventHandler().addChild(node);
    }

    public void start() {
        nextStage();
    }

    public void join(@NotNull Player player) {
        player.setInstance(arenaInstance, new Pos(0, 41, 0));
        player.setTag(arenaTag, arenaInstance.getUniqueId());
    }

    public static Arena getArena(Player player) {
        if (!player.hasTag(arenaTag)) return null;

        return arenaList.get(player.getTag(arenaTag));
    }

}
