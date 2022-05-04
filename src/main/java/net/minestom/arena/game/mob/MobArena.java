package net.minestom.arena.game.mob;

import de.articdive.jnoise.JNoise;
import de.articdive.jnoise.modules.octavation.OctavationModule;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import net.minestom.arena.Icons;
import net.minestom.arena.Lobby;
import net.minestom.arena.Messenger;
import net.minestom.arena.feature.Feature;
import net.minestom.arena.feature.Features;
import net.minestom.arena.game.SingleInstanceArena;
import net.minestom.arena.group.Group;
import net.minestom.arena.utils.CountDownFuture;
import net.minestom.arena.utils.FullbrightDimension;
import net.minestom.arena.utils.ItemUtils;
import net.minestom.arena.utils.VoidFuture;
import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.arrow.ArrowMeta;
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
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public final class MobArena extends SingleInstanceArena {
    private static final Tag<Integer> MELEE_TAG = Tag.Integer("melee").defaultValue(0);
    private static final Tag<Integer> ARMOR_TAG = Tag.Integer("armor").defaultValue(0);
    private static final Tag<Boolean> BOW_TAG = Tag.Boolean("bow").defaultValue(false);
    private static final Tag<Boolean> WAND_TAG = Tag.Boolean("wand").defaultValue(false);
    private static final AttributeModifier ATTACK_SPEED_MODIFIER = new AttributeModifier("mobarena-attack-speed", 100f, AttributeOperation.ADDITION);
    private static final AttributeModifier HEALTHCARE_MODIFIER = new AttributeModifier("mobarena-healthcare", 4f, AttributeOperation.ADDITION);
    private static final AttributeModifier COMBAT_TRAINING_MODIFIER = new AttributeModifier("mobarena-combat-training", 0.1f, AttributeOperation.MULTIPLY_TOTAL);

    private static final ItemStack WAND = ItemUtils.stripItalics(ItemStack.builder(Material.BLAZE_ROD)
            .displayName(Component.text("Wand"))
            .set(WAND_TAG, true)
            .build());

    private static final List<MobGenerator> MOB_GENERATORS = List.of(
            (stage, needed) -> Stream.generate(() -> new ZombieMob(stage))
                    .limit(ThreadLocalRandom.current().nextInt(needed + 1))
                    .toList(),
            (stage, needed) -> Stream.generate(() -> new SpiderMob(stage))
                    .limit(ThreadLocalRandom.current().nextInt(needed / 2 + 1))
                    .toList(),
            (stage, needed) -> Stream.generate(() -> new SkeletonMob(stage))
                    .limit(ThreadLocalRandom.current().nextInt(needed / 2 + 1))
                    .toList()
    );

    private static final ArenaClass KNIGHT_CLASS = new ArenaClass("Knight", "Starter class with mediocre attack and defense.",
            Icons.SWORD, TextColor.color(0xbebebe), Material.STONE_SWORD, new Kit(
            List.of(ItemStack.of(Material.STONE_SWORD).withTag(MELEE_TAG, 2)),
            null,
            ItemStack.of(Material.CHAINMAIL_CHESTPLATE).withTag(ARMOR_TAG, 4),
            null,
            null
    ), 5);
    public static final List<ArenaClass> CLASSES = List.of(
            KNIGHT_CLASS,
            new ArenaClass("Archer", "Easily deal (and take) high damage using your bow.",
                    Icons.BOW, TextColor.color(0xf9ff87), Material.BOW, new Kit(
                            List.of(ItemStack.of(Material.BOW).withTag(BOW_TAG, true), ItemStack.of(Material.ARROW)),
                            null,
                            ItemStack.of(Material.LEATHER_CHESTPLATE).withTag(ARMOR_TAG, 3),
                            null,
                            null
                    ), 10),
            new ArenaClass("Tank", "Very beefy, helps your teammates safely deal damage.",
                    Icons.SHIELD, TextColor.color(0x6b8ebe), Material.IRON_CHESTPLATE, new Kit(
                            List.of(ItemStack.of(Material.WOODEN_SWORD).withTag(MELEE_TAG, 1)),
                            ItemStack.of(Material.CHAINMAIL_HELMET).withTag(ARMOR_TAG, 2),
                            ItemStack.of(Material.IRON_CHESTPLATE).withTag(ARMOR_TAG, 4),
                            ItemStack.of(Material.CHAINMAIL_LEGGINGS).withTag(ARMOR_TAG, 3),
                            ItemStack.of(Material.IRON_BOOTS).withTag(ARMOR_TAG, 1)
                    ), 15),
            new ArenaClass("Mage", "Fight enemies from far away using your long ranged magic missiles.",
                    Icons.POTION, TextColor.color(0x3cbea5), Material.BLAZE_ROD, new Kit(
                            List.of(WAND),
                            null,
                            null,
                            ItemStack.of(Material.LEATHER_LEGGINGS).withTag(ARMOR_TAG, 2),
                            null
                    ), 20),
            new ArenaClass("Berserker", "For when knight doesn't deal enough damage.",
                    Icons.AXE, TextColor.color(0xbe6464), Material.STONE_AXE, new Kit(
                            List.of(ItemStack.of(Material.STONE_AXE).withTag(MELEE_TAG, 5)),
                            null,
                            null,
                            null,
                            ItemStack.of(Material.GOLDEN_BOOTS).withTag(ARMOR_TAG, 2)
                    ), 25)
    );

    private static final ArenaUpgrade ALLOYING_UPGRADE = new ArenaUpgrade("Alloying", "Increase armor effectiveness by 25%.",
            TextColor.color(0xf9ff87), Material.LAVA_BUCKET, null, 10);
    public static final List<ArenaUpgrade> UPGRADES = List.of(
            new ArenaUpgrade("Improved Healthcare", "Increases max health by two hearts.",
                    TextColor.color(0x63ff52), Material.POTION,
                    player -> {
                        // Since this upgrade includes healing, check if they have the modifier first
                        // before healing them another two hearts
                        if (!player.getAttribute(Attribute.MAX_HEALTH).getModifiers().contains(HEALTHCARE_MODIFIER)) {
                            player.getAttribute(Attribute.MAX_HEALTH).addModifier(HEALTHCARE_MODIFIER);
                            player.setHealth(player.getHealth() + HEALTHCARE_MODIFIER.getAmount());
                        }
                    }, 10),
            new ArenaUpgrade("Combat Training", "All physical attacks deal 10% more damage.",
                    TextColor.color(0xff5c3c), Material.IRON_SWORD,
                    player -> player.getAttribute(Attribute.ATTACK_DAMAGE)
                            .addModifier(COMBAT_TRAINING_MODIFIER), 10),
            ALLOYING_UPGRADE
    );

    private static final int SPAWN_RADIUS = 10;
    private static final int HEIGHT = 16;
    private boolean stageInProgress;
    private CountDownFuture stageMobCDF;

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
    private final Map<Player, ArenaClass> playerClasses = new HashMap<>();
    private final Set<ArenaUpgrade> upgrades = new HashSet<>();

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
            addCoins(1);
            if (event.getEntity() instanceof ArenaMob) {
                stageMobCDF.countDown();
            }

            final int mobsLeft = (int) stageMobCDF.getCount();

            if (mobsLeft > 0) {
                final int mobCount = stageMobCDF.getInitialCount();
                final String mobOrMobs = " mob" + (mobCount == 1 ? "" : "s");

                bossBar.name(Component.text("Kill mobs! " + mobsLeft + mobOrMobs + " remaining"));
                bossBar.progress((float) mobsLeft / mobCount);
                bossBar.color(BossBar.Color.RED);
            } else {
                onStageCleared();
            }
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

    private void endGame() {
        for (Player member : group().members()) {
            member.setInstance(Lobby.INSTANCE);
            Messenger.info(group().audience(), "You left the arena. Your last stage was " + stage);
        }
    }

    private void onStageCleared() {
        if (getState() == State.ENDING) endGame();
        stageInProgress = false;
        // Revive dead players
        for (Player deadPlayer : deadPlayers()) {
            deadPlayer.setInstance(arenaInstance, spawnPosition(deadPlayer));

            deadPlayer.getAttribute(Attribute.ATTACK_SPEED).addModifier(ATTACK_SPEED_MODIFIER);

            deadPlayer.showBossBar(bossBar);
        }

        for (ArenaUpgrade upgrade : upgrades) {
            if (upgrade.consumer() != null)
                for (Player player : arenaInstance.getPlayers()) {
                    upgrade.consumer().accept(player);
                }
        }

        final int playerCount = arenaInstance.getPlayers().size();
        final String playerOrPlayers = "player" + (playerCount == 1 ? "" : "s");

        bossBar.name(Component.text("Stage cleared! Waiting for " + playerCount + " more " + playerOrPlayers + " to continue"));
        bossBar.progress(0);
        bossBar.color(BossBar.Color.GREEN);

        group.audience().playSound(Sound.sound(SoundEvent.UI_TOAST_CHALLENGE_COMPLETE, Sound.Source.MASTER, 0.5f, 1), Sound.Emitter.self());
        Messenger.info(group.audience(), "Stage " + stage + " cleared! Talk to the NPC to continue to the next stage");
        new NextStageNPC().setInstance(arenaInstance, new Pos(0.5, HEIGHT, 0.5));
    }

    public void continueToNextStage(Player player) {
        if (!continued.add(player)) return;
        // Don't allow to start a new stage if shutdown is scheduled
        if (getState() == State.ENDING) return;

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
        stageInProgress = true;
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

        stageMobCDF = new CountDownFuture(mobCount);
    }

    public boolean hasContinued(Player player) {
        return continued.contains(player);
    }

    public int coins() {
        return coins;
    }

    public boolean takeCoins(int coins) {
        if (coins() > coins) {
            setCoins(coins() - coins);
            return true;
        }

        return false;
    }

    public void addCoins(int coins) {
        setCoins(coins() + coins);
    }

    private void setCoins(int coins) {
        this.coins = coins;
        group.display().update();
    }

    public ArenaClass playerClass(Player player) {
        return playerClasses.getOrDefault(player, KNIGHT_CLASS); // Knight class is default
    }

    public void setPlayerClass(Player player, ArenaClass arenaClass) {
        playerClasses.put(player, arenaClass);
        arenaClass.apply(player);
    }

    public boolean hasUpgrade(ArenaUpgrade upgrade) {
        return upgrades.contains(upgrade);
    }

    public void addUpgrade(ArenaUpgrade upgrade) {
        upgrades.add(upgrade);
        if (upgrade.consumer() != null)
            for (Player player : arenaInstance.getPlayers()) {
                upgrade.consumer().accept(player);
            }
    }

    private Set<Player> deadPlayers() {
        Set<Player> deadPlayers = new HashSet<>(group.members());
        deadPlayers.removeAll(arenaInstance.getPlayers());

        return deadPlayers;
    }

    @Override
    protected VoidFuture onStart() {
        nextStage();
        return VoidFuture.completedFuture();
    }

    @Override
    protected VoidFuture handleOnStop(boolean normalEnding) {
        if (normalEnding) {
            endGame();
        } else {
            if (stageInProgress) {
                final Duration halfTime = getEndTimeout().dividedBy(2);
                Messenger.info(group().audience(), "New objective! Clear stage within " + halfTime.toString());
                //todo extend messenger to provide nice countdowns
                final VoidFuture future = new VoidFuture();
                new VoidFuture().thenRun(halfTime, ignored -> endGame()).thenRun(future::complete);
                return future;
            } else {
                endGame();
            }
        }
        return VoidFuture.completedFuture();
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
        return List.of(Features.bow((entity, power) -> {
            final EntityProjectile projectile = new EntityProjectile(entity, EntityType.ARROW);
            final ArrowMeta meta = (ArrowMeta) projectile.getEntityMeta();
            meta.setCritical(power >= 0.9);
            projectile.scheduleRemove(Duration.of(100, TimeUnit.SERVER_TICK));

            return projectile;
        }), Features.combat(false, (attacker, victim) -> {
            float damage = 1;
            if (attacker instanceof LivingEntity livingEntity) {
                damage = livingEntity.getAttributeValue(Attribute.ATTACK_DAMAGE);
            } else if (attacker instanceof EntityProjectile projectile && projectile.getShooter() instanceof Player player) {
                final float movementSpeed = (float) (projectile.getVelocity().length() / MinecraftServer.TICK_PER_SECOND);
                damage = movementSpeed * player.getAttributeValue(Attribute.ATTACK_DAMAGE);
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
                final float multi = -0.04f * armorPoints * (hasUpgrade(ALLOYING_UPGRADE) ? 1.25f : 1);

                damage *= 1 + multi;
            }

            return damage;
        }, victim -> {
            if (victim instanceof Player) return 500;
            else return 100;
        }), Features.drop(item ->
                !item.getTag(Kit.KIT_ITEM_TAG)
        ), Features.functionalItem(item -> item.getTag(WAND_TAG), player -> {
            Instance instance = player.getInstance();
            AtomicReference<Pos> atomicPos = new AtomicReference<>(player.getPosition().add(0, player.getEyeHeight(), 0));
            AtomicInteger atomicAge = new AtomicInteger();

            MinecraftServer.getSchedulerManager().submitTask(() -> {
                if (instance == null) return TaskSchedule.stop();

                final Pos playerEyes = player.getPosition().add(0, player.getEyeHeight(), 0);
                final Pos pos = atomicPos.getAndUpdate(p -> p.withLookAt(playerEyes.add(playerEyes.direction().mul(30)))
                        .add(p.direction()));
                final int age = atomicAge.getAndIncrement();

                if (instance.getNearbyEntities(pos, 2)
                        .stream()
                        .anyMatch(entity -> entity instanceof ArenaMob) ||
                        !instance.getBlock(pos).isAir() ||
                        !instance.getWorldBorder().isInside(pos) ||
                        age >= 30) {

                    arenaInstance.sendGroupedPacket(ParticleCreator.createParticlePacket(
                            Particle.EXPLOSION, pos.x(), pos.y(), pos.z(),
                            0.5f, 0.5f, 0.5f, 5
                    ));
                    arenaInstance.playSound(
                            Sound.sound(SoundEvent.ENTITY_GENERIC_EXPLODE, Sound.Source.NEUTRAL, 1, 1),
                            pos.x(), pos.y(), pos.z()
                    );
                    for (Entity entity : instance.getNearbyEntities(pos, 5)) {
                        if (entity instanceof LivingEntity livingEntity && !(entity instanceof Player)) {
                            livingEntity.damage(DamageType.fromPlayer(player), 7);
                        }
                    }

                    return TaskSchedule.stop();
                }

                arenaInstance.sendGroupedPacket(ParticleCreator.createParticlePacket(
                        Particle.FIREWORK, true, pos.x(), pos.y(), pos.z(),
                        0.3f, 0.3f, 0.3f, 0.01f, 50, null
                ));
                arenaInstance.playSound(
                        Sound.sound(SoundEvent.ENTITY_AXOLOTL_SWIM, Sound.Source.NEUTRAL, 1, 1),
                        pos.x(), pos.y(), pos.z()
                );

                return TaskSchedule.tick(1);
            });

        }, 1500));
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
