package net.minestom.arena.game.mob;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minestom.arena.combat.CombatEvent;
import net.minestom.arena.game.SingleInstanceArena;
import net.minestom.arena.mob.RandomMob;
import net.minestom.arena.utils.FullbrightDimension;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class MobArena implements SingleInstanceArena {
    public static final class MobArenaInstance extends InstanceContainer {
        public MobArenaInstance() {
            super(UUID.randomUUID(), FullbrightDimension.INSTANCE);
            setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.SAND));
        }
    }

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
    public @NotNull Instance instance() {
        return arenaInstance;
    }

    @Override
    public @NotNull Pos spawnPosition() {
        return new Pos(0, 41, 0);
    }
}
