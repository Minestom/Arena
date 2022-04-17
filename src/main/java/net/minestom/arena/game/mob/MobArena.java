package net.minestom.arena.game.mob;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minestom.arena.combat.CombatEvent;
import net.minestom.arena.game.Arena;
import net.minestom.arena.mob.RandomMob;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.instance.RemoveEntityFromInstanceEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class MobArena extends Arena {

    private static final Tag<UUID> arenaTag = Tag.UUID("arena");

    private int stage = 0;

    public void nextStage() {
        stage++;

        for (int i = 0; i < stage; i++) {
            EntityCreature creature = RandomMob.random(stage);

            creature.setInstance(arenaInstance, new Pos(0, 42, 0));
        }

        arenaInstance.showTitle(Title.title(Component.text("Stage " + stage), Component.empty()));
        arenaInstance.sendMessage(Component.text("Stage " + stage));

    }

    public MobArena() {

        super(new MobArenaInstance());

        // Register this arena
        MinecraftServer.getInstanceManager().registerInstance(this.arenaInstance);

        CombatEvent.hook(arenaInstance.eventNode(), false);

        arenaInstance.eventNode().addListener(RemoveEntityFromInstanceEvent.class, (event) -> {
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
        });

        arenaInstance.eventNode().addListener(EntityDeathEvent.class, (event) -> {
            for (Entity entity : this.arenaInstance.getEntities()) {
                if (entity instanceof EntityCreature) {
                    // TODO give money;
                    return; // round hasn't ended yet
                }
            }

            nextStage();
        });
    }

    @Override
    public void start() {
        nextStage();
    }

    @Override
    public void join(@NotNull Player player) {
        player.setInstance(arenaInstance, new Pos(0, 41, 0));
        player.setTag(arenaTag, arenaInstance.getUniqueId());
    }

}
