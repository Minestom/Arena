package net.minestom.arena.game.mob;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.minestom.arena.Items;
import net.minestom.arena.Lobby;
import net.minestom.arena.Messenger;
import net.minestom.arena.feature.Feature;
import net.minestom.arena.feature.Features;
import net.minestom.arena.game.SingleInstanceArena;
import net.minestom.arena.group.Group;
import net.minestom.arena.utils.FullbrightDimension;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MobArena implements SingleInstanceArena {
    private static final MobGenerator[] MOB_GENERATORS = {
            (stage, needed) -> Stream.generate(() -> new ZombieMob(stage))
                    .limit(ThreadLocalRandom.current().nextInt(needed + 1))
                    .collect(Collectors.toList()),
            (stage, needed) -> Stream.generate(() -> new SpiderMob(stage))
                    .limit(ThreadLocalRandom.current().nextInt(needed / 2 + 1))
                    .collect(Collectors.toList())
    };

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
        List<ArenaMob> mobs = generateMobs(stage, stage);
        for (ArenaMob mob : mobs) {
            mob.setInstance(arenaInstance, Vec.ONE
                    .rotateAroundY(ThreadLocalRandom.current().nextDouble(2 * Math.PI))
                    .mul(10, 0, 10)
                    .asPosition()
                    .add(0, 40, 0)
            );
        }

        arenaInstance.showTitle(Title.title(
                Component.text("Stage " + stage, NamedTextColor.GREEN),
                Component.text(stage + " mob" + (stage == 1 ? "" : "s"))
        ));

        arenaInstance.playSound(Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_PLING, Sound.Source.MASTER, 1f, 1f));

        Messenger.info(arenaInstance, "Stage " + stage);
    }

    public MobArena(Group group) {
        this.group = group;
        arenaInstance.eventNode().addListener(EntityDeathEvent.class, event -> {
            ItemEntity item = new ItemEntity(Items.COIN);
            item.setInstance(arenaInstance, event.getEntity().getPosition());

            for (Entity entity : arenaInstance.getEntities()) {
                if (entity instanceof EntityCreature creature && !(creature.isDead())) {
                    return; // Round hasn't ended yet
                }
            }
            nextStage();
        }).addListener(PickupItemEvent.class, event -> {
            if (event.getEntity() instanceof Player player) {
                player.getInventory().addItemStack(event.getItemStack());
            } else {
                // Don't allow other mobs to pick up coins
                event.setCancelled(true);
            }
        }).addListener(PlayerDeathEvent.class, event -> {
            event.getPlayer().setInstance(Lobby.INSTANCE);

            event.setChatMessage(null);
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
    public int stage() {
        return stage;
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

    private static List<ArenaMob> generateMobs(int stage, int needed) {
        List<ArenaMob> mobs = new ArrayList<>();
        while (needed > 0) {
            for (MobGenerator generator : MOB_GENERATORS) {
                if (needed <= 0) {
                    return mobs;
                }

                List<? extends ArenaMob> generatedMobs = generator.generate(stage, needed);
                mobs.addAll(generatedMobs);
                needed -= generatedMobs.size();
            }
        }

        return mobs;
    }
}
