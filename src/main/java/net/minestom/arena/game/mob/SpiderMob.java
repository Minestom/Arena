package net.minestom.arena.game.mob;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.ai.goal.RangedAttackGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.LazyPacket;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

final class SpiderMob extends ArenaMob {
    public SpiderMob(int stage) {
        super(EntityType.SPIDER, stage);

        RangedAttackGoal attackGoal = new RangedAttackGoal(
                this, Duration.of(10, TimeUnit.SECOND),
                16, 12, false, 1.3, 0);

        attackGoal.setProjectileGenerator(WebProjectile::new);

        addAIGroup(
                List.of(attackGoal),
                List.of(new ClosestEntityTarget(this, 32, Player.class))
        );
    }

    private static class WebProjectile extends EntityProjectile {
        public WebProjectile(@Nullable Entity shooter) {
            super(shooter, EntityType.ARMOR_STAND);
            ArmorStandMeta meta = (ArmorStandMeta) getEntityMeta();
            meta.setHasNoBasePlate(true);
            meta.setHasArms(true);
            meta.setSmall(true);
            meta.setRightArmRotation(new Vec(135, 90, 0));
            meta.setInvisible(true);
            getViewersAsAudience().playSound(Sound.sound(SoundEvent.ENTITY_SPIDER_STEP, Sound.Source.HOSTILE, 1, 1), shooter);

            eventNode().addListener(ProjectileCollideWithEntityEvent.class, event -> {
                final Entity target = event.getTarget();
                if (!(target instanceof Player)) event.setCancelled(true);
                else collide(event.getEntity(), target.getPosition());
            });
            eventNode().addListener(ProjectileCollideWithBlockEvent.class, event -> collide(event.getEntity(), event.getCollisionPosition()));
        }

        private @NotNull EntityEquipmentPacket getEquipmentsPacket() {
            return new EntityEquipmentPacket(this.getEntityId(), Map.of(
                    EquipmentSlot.MAIN_HAND, ItemStack.of(Material.COBWEB),
                    EquipmentSlot.OFF_HAND, ItemStack.AIR,
                    EquipmentSlot.BOOTS, ItemStack.AIR,
                    EquipmentSlot.LEGGINGS, ItemStack.AIR,
                    EquipmentSlot.CHESTPLATE, ItemStack.AIR,
                    EquipmentSlot.HELMET, ItemStack.AIR));
        }

        @Override
        public void updateNewViewer(@NotNull Player player) {
            super.updateNewViewer(player);
            player.sendPacket(new LazyPacket(this::getEquipmentsPacket));
        }

        private static void collide(Entity projectile, Pos pos) {
            final Instance instance = projectile.getInstance();
            if (instance == null) return;

            final Random random = ThreadLocalRandom.current();
            final List<Pos> cobwebs = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                Pos spawnAt = pos.add(
                        random.nextInt(-1, 1),
                        random.nextInt(0, 2),
                        random.nextInt(-1, 1)
                );

                if (instance.getBlock(spawnAt).isAir()) {
                    instance.setBlock(spawnAt, Block.COBWEB);
                    cobwebs.add(spawnAt);
                }
            }

            projectile.remove();

            instance.scheduler().buildTask(() -> {
                if (!instance.isRegistered()) return;

                for (Pos cobweb : cobwebs) {
                    instance.setBlock(cobweb, Block.AIR);
                }
            }).delay(5, TimeUnit.SECOND).schedule();
        }
    }
}
