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
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public final class MobArena implements Arena {

    private int stage = 0;
    private final Instance arenaInstance = new MobArenaInstance();

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

        // Register this arena
        MinecraftServer.getInstanceManager().registerInstance(this.arenaInstance);

        CombatEvent.hook(arenaInstance.eventNode(), false);

        arenaInstance.eventNode().addListener(EntityDeathEvent.class, (event) -> {
            for (Entity entity : this.arenaInstance.getEntities()) {
                if (entity instanceof EntityCreature creature && !(creature.isDead())) {
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
    public @NotNull Instance getArenaInstance() {
        return arenaInstance;
    }

    @Override
    public CompletableFuture<Void> join(@NotNull Player player) {
        CompletableFuture<Void> future = player.setInstance(arenaInstance, new Pos(0, 41, 0));
        player.setTag(arenaTag, arenaInstance.getUniqueId());
        return future;
    }

}
