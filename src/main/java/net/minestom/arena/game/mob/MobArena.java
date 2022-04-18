package net.minestom.arena.game.mob;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.minestom.arena.Lobby;
import net.minestom.arena.Messenger;
import net.minestom.arena.feature.Feature;
import net.minestom.arena.feature.Features;
import net.minestom.arena.game.SingleInstanceArena;
import net.minestom.arena.group.Group;
import net.minestom.arena.utils.FullbrightDimension;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;

public final class MobArena implements SingleInstanceArena {
    private static final List<BiFunction<Integer, EventNode<InstanceEvent>, EntityCreature>> MOB_GENERATION_LAMBDAS = List.of(
            ZombieMob::new
    );

    public static final class MobArenaInstance extends InstanceContainer {
        public MobArenaInstance() {
            super(UUID.randomUUID(), FullbrightDimension.INSTANCE);
            setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.SAND));
        }
    }

    private final Group group;
    private final Instance arenaInstance = new MobArenaInstance();

    private int stage = 0;

    public void nextStage() {
        stage++;
        for (int i = 0; i < stage; i++) {
            EntityCreature creature = findMob(stage, arenaInstance.eventNode());
            creature.setInstance(arenaInstance, new Pos(0, 42, 0));
        }
        arenaInstance.showTitle(Title.title(
                Component.text("Stage " + stage, NamedTextColor.GREEN),
                Component.text(stage + " mob" + (stage == 1 ? "" : "s") + ".")
        ));

        arenaInstance.playSound(Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_PLING, Sound.Source.MASTER, 1f, 1f));

        Messenger.info(arenaInstance, "Stage " + stage);
    }

    public MobArena(Group group) {
        this.group = group;
        arenaInstance.eventNode().addListener(EntityDeathEvent.class, (event) -> {
            for (Entity entity : this.arenaInstance.getEntities()) {
                if (entity instanceof EntityCreature creature && !(creature.isDead())) {
                    // TODO give money;
                    return; // round hasn't ended yet
                }
            }
            nextStage();
        });

        arenaInstance.eventNode().addListener(PlayerDeathEvent.class, event -> {
            event.getPlayer().setInstance(Lobby.INSTANCE);
            Messenger.info(event.getPlayer(), "You died. Your last stage was " + stage);
        });
    }

    @Override
    public void start() {
        nextStage();
    }

    @Override
    public @NotNull Group group() {
        return group;
    }

    @Override
    public @NotNull Instance instance() {
        return arenaInstance;
    }

    @Override
    public @NotNull Pos spawnPosition(@NotNull Player player) {
        return new Pos(0, 41, 0);
    }

    @Override
    public @NotNull List<Feature> features() {
        return List.of(Features.combat());
    }

    static EntityCreature findMob(int level, EventNode<InstanceEvent> node) {
        BiFunction<Integer, EventNode<InstanceEvent>, EntityCreature> randomMobGenerator = MOB_GENERATION_LAMBDAS.get(
                ThreadLocalRandom.current().nextInt(MOB_GENERATION_LAMBDAS.size()) % MOB_GENERATION_LAMBDAS.size()
        );
        return randomMobGenerator.apply(level, node);
    }
}
