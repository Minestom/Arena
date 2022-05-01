package net.minestom.arena.game.mob;

import de.articdive.jnoise.JNoise;
import de.articdive.jnoise.modules.octavation.OctavationModule;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.minestom.arena.Icons;
import net.minestom.arena.Lobby;
import net.minestom.arena.Messenger;
import net.minestom.arena.feature.Feature;
import net.minestom.arena.feature.Features;
import net.minestom.arena.game.SingleInstanceArena;
import net.minestom.arena.group.Group;
import net.minestom.arena.utils.FullbrightDimension;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.instance.RemoveEntityFromInstanceEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public final class MobArena implements SingleInstanceArena {
    private static final Tag<Integer> MELEE_TAG = Tag.Integer("melee").defaultValue(0);
    private static final Tag<Integer> ARMOR_TAG = Tag.Integer("armor").defaultValue(0);
    private static final Tag<Boolean> BOW_TAG = Tag.Boolean("bow").defaultValue(false);
    private static final Tag<Boolean> WAND_TAG = Tag.Boolean("wand").defaultValue(false);
    private static final AttributeModifier ATTACK_SPEED_MODIFIER = new AttributeModifier("mob-arena", 100f, AttributeOperation.ADDITION);

    private static final MobGenerator[] MOB_GENERATORS = {
            (stage, needed) -> Stream.generate(() -> new ZombieMob(stage))
                    .limit(ThreadLocalRandom.current().nextInt(needed + 1))
                    .toList(),
            (stage, needed) -> Stream.generate(() -> new SpiderMob(stage))
                    .limit(ThreadLocalRandom.current().nextInt(needed / 2 + 1))
                    .toList(),
            (stage, needed) -> Stream.generate(() -> new SkeletonMob(stage))
                    .limit(ThreadLocalRandom.current().nextInt(needed / 2 + 1))
                    .toList()
    };
    public static final ArenaClass[] CLASSES = {
            new ArenaClass("Knight", "Starter class with mediocre attack and defense.", Icons.SWORD, Material.STONE_SWORD, new Kit(
                    new ItemStack[] { ItemStack.of(Material.STONE_SWORD).withTag(MELEE_TAG, 2) },
                    null,
                    ItemStack.of(Material.CHAINMAIL_CHESTPLATE).withTag(ARMOR_TAG, 4),
                    null,
                    null
            ), 5),
            new ArenaClass("Archer", "Easily deal (and take) high damage using your bow.", Icons.BOW, Material.BOW, new Kit(
                    new ItemStack[] { ItemStack.of(Material.BOW).withTag(BOW_TAG, true), ItemStack.of(Material.ARROW) },
                    null,
                    ItemStack.of(Material.LEATHER_CHESTPLATE).withTag(ARMOR_TAG, 3),
                    null,
                    null
            ), 10),
            new ArenaClass("Tank", "Very beefy, helps your teammates safely deal damage.", Icons.SHIELD, Material.IRON_CHESTPLATE, new Kit(
                    new ItemStack[] { ItemStack.of(Material.WOODEN_SWORD).withTag(MELEE_TAG, 1) },
                    ItemStack.of(Material.CHAINMAIL_HELMET).withTag(ARMOR_TAG, 2),
                    ItemStack.of(Material.IRON_CHESTPLATE).withTag(ARMOR_TAG, 4),
                    ItemStack.of(Material.CHAINMAIL_LEGGINGS).withTag(ARMOR_TAG, 3),
                    ItemStack.of(Material.IRON_BOOTS).withTag(ARMOR_TAG, 1)
            ), 15),
            new ArenaClass("Healer", "Support your teammates, but you better stay at a safe distance.", Icons.POTION, Material.POTION, new Kit(
                    new ItemStack[] { ItemStack.of(Material.BLAZE_POWDER).withTag(WAND_TAG, true) },
                    null,
                    null,
                    ItemStack.of(Material.LEATHER_LEGGINGS).withTag(ARMOR_TAG, 2),
                    null
            ), 20),
            new ArenaClass("Berserker", "For when knight doesn't deal enough damage.", Icons.AXE, Material.STONE_AXE, new Kit(
                    new ItemStack[] { ItemStack.of(Material.STONE_AXE).withTag(MELEE_TAG, 4) },
                    null,
                    null,
                    null,
                    null
            ), 25)
    };

    private static final int SPAWN_RADIUS = 10;
    private static final int HEIGHT = 16;

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
                final Point start = unit.absoluteStart();
                for (int x = 0; x < unit.size().x(); x++) {
                    for (int z = 0; z < unit.size().z(); z++) {
                        Point bottom = start.add(x, 0, z);
                        synchronized (noise) { // Synchronization is necessary for JNoise
                            // Ensure flat terrain in the fighting area
                            final double modifier = MathUtils.clamp((bottom.distance(Pos.ZERO.withY(bottom.y())) - 75) / 50, 0, 1);
                            double height = noise.getNoise(bottom.x(), bottom.z()) * modifier;
                            height = (height > 0 ? height * 4 : height) * 8 + HEIGHT;
                            unit.modifier().fill(bottom, bottom.add(1, 0, 1).withY(height), Block.SAND);
                        }
                    }
                }
            });

            int x = SPAWN_RADIUS;
            int y = 0;
            int xChange = 1 - (SPAWN_RADIUS << 1);
            int yChange = 0;
            int radiusError = 0;

            while (x >= y) {
                for (int i = -x; i <= x; i++) {
                    setBlock(i, 15, y, Block.RED_SAND);
                    setBlock(i, 15, -y, Block.RED_SAND);
                }
                for (int i = -y; i <= y; i++) {
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
    private final BossBar bossBar;
    private final Instance arenaInstance = new MobArenaInstance();
    private final Set<Player> continued = new HashSet<>();
    private final Map<Player, ArenaClass> playerClasses = new ConcurrentHashMap<>();

    private int stage = 0;
    private int coins = 0;

    public MobArena(Group group) {
        this.group = group;
        group.setDisplay(new MobArenaSidebarDisplay(this));

        // Show boss bar
        bossBar = BossBar.bossBar(Component.text("Loading..."), 1, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
        group.audience().showBossBar(bossBar);

        // Remove attack indicator
        for (Player member : group.members()) {
            member.getAttribute(Attribute.ATTACK_SPEED).addModifier(ATTACK_SPEED_MODIFIER);
        }

        arenaInstance.eventNode().addListener(EntityDeathEvent.class, event -> {
            coins++;
            group.display().update();

            for (Entity entity : arenaInstance.getEntities()) {
                if (entity instanceof EntityCreature creature && !(creature.isDead())) {
                    // -1 for the mob that is dying right now
                    final int mobsLeft = (int) arenaInstance.getEntities().stream().filter(e -> e instanceof ArenaMob).count() - 1;
                    final int mobCount = (int) (stage * 1.5);
                    final String mobOrMobs = " mob" + (mobCount == 1 ? "" : "s");

                    bossBar.name(Component.text("Kill mobs! " + mobsLeft + mobOrMobs + " remaining"));
                    bossBar.progress((float) mobsLeft / mobCount);
                    bossBar.color(BossBar.Color.RED);

                    return; // Round hasn't ended yet
                }
            }

            // Revive dead players
            for (Player deadPlayer : deadPlayers()) {
                deadPlayer.setInstance(arenaInstance, spawnPosition(deadPlayer));

                deadPlayer.getAttribute(Attribute.ATTACK_SPEED).addModifier(ATTACK_SPEED_MODIFIER);

                deadPlayer.showBossBar(bossBar);
            }

            final int playerCount = arenaInstance.getPlayers().size();
            final String playerOrPlayers = "player" + (playerCount == 1 ? "" : "s");

            bossBar.name(Component.text("Stage cleared! Waiting for " + playerCount + " more " + playerOrPlayers + " to continue"));
            bossBar.progress(0);
            bossBar.color(BossBar.Color.GREEN);

            group.audience().playSound(Sound.sound(SoundEvent.UI_TOAST_CHALLENGE_COMPLETE, Sound.Source.MASTER, 0.5f, 1), Sound.Emitter.self());
            Messenger.info(group.audience(), "Stage " + stage + " cleared! Talk to the NPC to continue to the next stage");
            new NextStageNPC().setInstance(arenaInstance, new Pos(0.5, HEIGHT, 0.5));
        }).addListener(PickupItemEvent.class, event -> {
            if (event.getEntity() instanceof Player player) {
                player.getInventory().addItemStack(event.getItemStack());
            } else {
                // Don't allow other mobs to pick up items
                event.setCancelled(true);
            }
        }).addListener(PlayerDeathEvent.class, event -> {
            final Player player = event.getPlayer();

            player.setInstance(Lobby.INSTANCE);

            event.setChatMessage(null);
            Messenger.info(player, "You died. Your last stage was " + stage);
        }).addListener(RemoveEntityFromInstanceEvent.class, event -> {
            // We don't care about entities, only players.
            if (!(event.getEntity() instanceof Player player)) return;

            // Re-add attack indicator
            player.getAttribute(Attribute.ATTACK_SPEED).removeModifier(ATTACK_SPEED_MODIFIER);

            // Hide boss bar
            player.hideBossBar(bossBar);

            Messenger.info(player, "You left the arena. Your last stage was " + stage);
        }).addListener(PlayerEntityInteractEvent.class, event -> {
            Player player = event.getPlayer();
            Entity target = event.getTarget();

            if (!(target instanceof NextStageNPC)) return;
            if (!hasContinued(player)) {
                player.openInventory(new NextStageInventory(player, this));
                player.playSound(Sound.sound(SoundEvent.ENTITY_VILLAGER_YES, Sound.Source.NEUTRAL, 1, 1), target);
            } else {
                Messenger.warn(player, "You already continued");
                player.playSound(Sound.sound(SoundEvent.ENTITY_VILLAGER_NO, Sound.Source.NEUTRAL, 1, 1), target);
            }
        });

        // TODO: Cancel armor unequip
    }

    public void continueToNextStage(Player player) {
        if (!continued.add(player)) return;

        final int continuedCount = continued.size();
        final int haveToContinue = arenaInstance.getPlayers().size();
        final int untilStart = haveToContinue - continuedCount;

        if (untilStart <= 0) {
            Messenger.info(group.audience(), player.getUsername() + " has continued. Starting the next wave.");

            bossBar.name(Component.text("Wave starting..."));
            bossBar.progress(1);
            bossBar.color(BossBar.Color.BLUE);

            Messenger.countdown(group().audience(), 3)
                    .thenRun(this::nextStage)
                    .thenRun(continued::clear);
        } else {
            Messenger.info(group.audience(), player.getUsername() + " has continued. " + untilStart + " more players must continue to start the next wave.");

            final String playerOrPlayers = "player" + (untilStart == 1 ? "" : "s");
            bossBar.name(Component.text("Stage cleared! Waiting for " + untilStart + " more " + playerOrPlayers + " to continue"));
            bossBar.progress((float) continuedCount / haveToContinue);
            bossBar.color(BossBar.Color.GREEN);
        }
    }

    public void nextStage() {
        stage++;
        int mobCount = (int) (stage * 1.5);
        for (Entity entity : arenaInstance.getEntities()) {
            if (entity instanceof NextStageNPC) {
                entity.remove();
                break;
            }
        }

        for (Player member : group.members()) {
            member.setHealth(member.getHealth() + 4); // Heal 2 hearts
            playerClass(member).apply(member);
        }

        List<ArenaMob> mobs = generateMobs(stage, mobCount);
        for (ArenaMob mob : mobs) {
            mob.setInstance(arenaInstance, Vec.ONE
                    .rotateAroundY(ThreadLocalRandom.current().nextDouble(2 * Math.PI))
                    .mul(SPAWN_RADIUS, 0, SPAWN_RADIUS)
                    .asPosition()
                    .add(0, HEIGHT, 0)
            );
        }

        final String mobOrMobs = " mob" + (mobCount == 1 ? "" : "s");

        arenaInstance.showTitle(Title.title(
                Component.text("Stage " + stage, NamedTextColor.GREEN),
                Component.text(mobCount + mobOrMobs)
        ));

        arenaInstance.playSound(Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_PLING, Sound.Source.MASTER, 1f, 2f));
        Messenger.info(arenaInstance, "Stage " + stage + " has begun! Kill all the mobs to proceed to the next stage");

        bossBar.name(Component.text("Kill mobs! " + mobCount + mobOrMobs + " remaining"));
        bossBar.progress(1f);
        bossBar.color(BossBar.Color.RED);
    }

    public boolean hasContinued(Player player) {
        return continued.contains(player);
    }

    public int coins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public ArenaClass playerClass(Player player) {
        return playerClasses.getOrDefault(player, CLASSES[0]); // Knight class is default
    }

    public void setPlayerClass(Player player, ArenaClass arenaClass) {
        playerClasses.put(player, arenaClass);
    }

    private Set<Player> deadPlayers() {
        Set<Player> deadPlayers = new HashSet<>(group.members());
        deadPlayers.removeAll(arenaInstance.getPlayers());

        return deadPlayers;
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
        return new Pos(0.5, HEIGHT, 0.5);
    }

    @Override
    public @NotNull List<Feature> features() {
        return List.of(Features.combat(false, (attacker, victim) -> {
            float damage = 1;
            if (attacker instanceof LivingEntity livingEntity) {
                damage = livingEntity.getAttributeValue(Attribute.ATTACK_DAMAGE);
            }

            if (attacker instanceof Player player) {
                final int tier = player.getItemInMainHand().getTag(MELEE_TAG);
                final float multi = 0.1f * tier; // 0 (no weapon)

                damage *= 1 + multi;
            }

            if (victim instanceof Player player) {
                int armorPoints = player.getHelmet().getTag(ARMOR_TAG) +
                        player.getChestplate().getTag(ARMOR_TAG) +
                        player.getLeggings().getTag(ARMOR_TAG) +
                        player.getBoots().getTag(ARMOR_TAG);

                // Armor point = 4% damage reduction
                final float multi = -0.04f * armorPoints;

                damage *= 1 + multi;
            }

            return damage;
        }, victim -> {
            if (victim instanceof Player) return 500;
            else return 100;
        }), Features.drop(item -> !item.getTag(Kit.KIT_ITEM_TAG)));
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
