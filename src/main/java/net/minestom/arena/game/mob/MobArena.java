package net.minestom.arena.game.mob;

import de.articdive.jnoise.JNoise;
import de.articdive.jnoise.modules.octavation.OctavationModule;
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
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.instance.RemoveEntityFromInstanceEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
                    .collect(Collectors.toList()),
            (stage, needed) -> Stream.generate(() -> new SkeletonMob(stage))
                    .limit(ThreadLocalRandom.current().nextInt(needed / 2 + 1))
                    .collect(Collectors.toList())
    };

    private static final int spawnRadius = 10;

    public static final class MobArenaInstance extends InstanceContainer {
        private final JNoise noise = JNoise.newBuilder()
                .fastSimplex()
                .setFrequency(0.0025)
                .addModule(OctavationModule.newBuilder()
                        .setOctaves(6)
                        .build())
                .build();

        public MobArenaInstance() {
            super(UUID.randomUUID(), FullbrightDimension.INSTANCE);
            getWorldBorder().setDiameter(100);
            setGenerator(unit -> {
                unit.modifier().fill(new Vec(-10, 16, -10), new Vec(10, 16, 10), Block.SMOOTH_QUARTZ);

                final Point start = unit.absoluteStart();
                for (int x = 0; x < unit.size().x(); x++) {
                    for (int z = 0; z < unit.size().z(); z++) {
                        Point bottom = start.add(x, 0, z);

                        synchronized (noise) { // Synchronization is necessary for JNoise
                            // Ensure flat terrain in the fighting area
                            final double modifier = MathUtils.clamp((bottom.distance(Pos.ZERO.withY(bottom.y())) - 75) / 50, 0, 1);
                            double height = noise.getNoise(bottom.x(), bottom.z()) * modifier;
                            height = (height > 0 ? height * 4 : height) * 8 + 16;
                            unit.modifier().fill(bottom, bottom.add(1, 0, 1).withY(height), Block.SAND);
                        }
                    }
                }
            });

            int x = spawnRadius;
            int y = 0;
            int xChange = 1 - (spawnRadius << 1);
            int yChange = 0;
            int radiusError = 0;

            while (x >= y) {
                for (int i = -x; i <= x; i++) {
                    setBlock(i, 15, y, Block.RED_SAND);
                    setBlock(i, 15, -y, Block.RED_SAND);
                }
                for (int i = -y; i <=  y; i++) {
                    setBlock(i, 15, x, Block.RED_SAND);
                    setBlock(i, 15, -x, Block.RED_SAND);
                }

                y++;
                radiusError += yChange;
                yChange += 2;
                if (((radiusError << 1) + xChange) > 0) {
                    x--;
                    radiusError += xChange;
                    xChange += 2;
                }
            }
        }
    }

    private final Group group;
    private final Instance arenaInstance = new MobArenaInstance();
    private final Set<Player> continued = new HashSet<>();
    private final Map<Player, Integer> weaponTiers = new ConcurrentHashMap<>();
    private final Map<Player, Integer> armorTiers = new ConcurrentHashMap<>();

    private int stage = 0;

    public MobArena(Group group) {
        this.group = group;
        arenaInstance.eventNode().addListener(EntityDeathEvent.class, event -> {
            ItemEntity item = new ItemEntity(Items.COIN);
            item.setGlowing(true);
            item.setInstance(arenaInstance, event.getEntity().getPosition());

            for (Entity entity : arenaInstance.getEntities()) {
                if (entity instanceof EntityCreature creature && !(creature.isDead())) {
                    return; // Round hasn't ended yet
                }
            }

            group.audience().playSound(Sound.sound(SoundEvent.UI_TOAST_CHALLENGE_COMPLETE, Sound.Source.MASTER, 0.5f, 1), Sound.Emitter.self());
            Messenger.info(group.audience(), "Stage " + stage + " cleared! Talk to the NPC to continue to the next stage");
            new NextStageNPC(this).setInstance(arenaInstance, new Pos(0, 16, 0));
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
        }).addListener(RemoveEntityFromInstanceEvent.class, event -> {
            // We don't care about entities, only players.
            if (!(event.getEntity() instanceof Player player)) return;

            Messenger.info(player, "You left the arena. Your last stage was " + stage);
        });

        // TODO: Cancel armor unequip
    }

    public void continueToNextStage(Player player) {
        continued.add(player);

        if (continued.size() >= group().members().size()) {
            Messenger.countdown(group().audience(), 5, this::nextStage);
            continued.clear();
        }
    }

    public void nextStage() {
        stage++;
        for (Entity entity : arenaInstance.getEntities()) {
            if (entity instanceof NextStageNPC) {
                entity.remove();
            }
        }

        List<ArenaMob> mobs = generateMobs(stage, stage);
        for (ArenaMob mob : mobs) {
            mob.setInstance(arenaInstance, Vec.ONE
                    .rotateAroundY(ThreadLocalRandom.current().nextDouble(2 * Math.PI))
                    .mul(spawnRadius, 0, spawnRadius)
                    .asPosition()
                    .add(0, 16, 0)
            );
        }

        arenaInstance.showTitle(Title.title(
                Component.text("Stage " + stage, NamedTextColor.GREEN),
                Component.text(stage + " mob" + (stage == 1 ? "" : "s"))
        ));

        arenaInstance.playSound(Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_PLING, Sound.Source.MASTER, 1f, 2f));
        Messenger.info(arenaInstance, "Stage " + stage + " has begun! Kill all the mobs to proceed to the next stage");
    }

    public boolean hasContinued(Player player) {
        return continued.contains(player);
    }

    public int currentWeaponTier(Player player) {
        return weaponTiers.getOrDefault(player, -1);
    }

    public int currentArmorTier(Player player) {
        return armorTiers.getOrDefault(player, -1);
    }

    public void setWeaponTier(Player player, int tier) {
        weaponTiers.put(player, tier);
    }

    public void setArmorTier(Player player, int tier) {
        armorTiers.put(player, tier);
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
        return new Pos(0, 16, 0);
    }

    @Override
    public @NotNull List<Feature> features() {
        return List.of(Features.combat(false, (attacker, victim) -> {
            if (attacker instanceof Player player) {
                final boolean isSword = player.getItemInMainHand()
                        .material()
                        .name()
                        .contains("sword");
                final float multi = 0.5f * (weaponTiers.getOrDefault(player, -1) + 1);

                return isSword ? 1 + multi : 1;
            } else if (victim instanceof Player player) {
                final boolean hasArmor = !player.getChestplate().isAir();
                final float multi = -0.1f * (armorTiers.getOrDefault(player, -1) + 1);

                return hasArmor ? 1 + multi : 1;
            }

            return 1;
        }), Features.drop());
    }

    private static @NotNull List<ArenaMob> generateMobs(int stage, int needed) {
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
