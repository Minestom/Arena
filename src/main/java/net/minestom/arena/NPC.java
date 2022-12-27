package net.minestom.arena;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.*;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.metadata.PlayerMeta;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.PlayerInfoPacket;
import net.minestom.server.network.packet.server.play.PlayerInfoPacket.AddPlayer.Property;
import net.minestom.server.network.packet.server.play.PlayerInfoPacket.RemovePlayer;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static net.minestom.server.network.packet.server.play.PlayerInfoPacket.Action.ADD_PLAYER;
import static net.minestom.server.network.packet.server.play.PlayerInfoPacket.Action.REMOVE_PLAYER;
import static net.minestom.server.network.packet.server.play.PlayerInfoPacket.AddPlayer;

// https://gist.github.com/iam4722202468/36630043ca89e786bb6318e296f822f8
final class NPC extends EntityCreature {
    private final String name;
    private final PlayerSkin skin;
    private final Consumer<Player> onClick;

    NPC(@NotNull String name, @NotNull PlayerSkin skin, @NotNull Instance instance,
        @NotNull Point spawn, @NotNull Consumer<Player> onClick) {

        super(EntityType.PLAYER);
        this.name = name;
        this.skin = skin;
        this.onClick = onClick;

        final PlayerMeta meta = (PlayerMeta) getEntityMeta();
        meta.setNotifyAboutChanges(false);
        meta.setCapeEnabled(false);
        meta.setJacketEnabled(true);
        meta.setLeftSleeveEnabled(true);
        meta.setRightSleeveEnabled(true);
        meta.setLeftLegEnabled(true);
        meta.setRightLegEnabled(true);
        meta.setHatEnabled(true);
        meta.setNotifyAboutChanges(true);

        addAIGroup(
            List.of(new LookAtPlayerGoal(this)),
            List.of(new ClosestEntityTarget(this, 15, entity -> entity instanceof Player))
        );

        setInstance(instance, spawn);
    }

    public void handle(@NotNull EntityAttackEvent event) {
        if (event.getTarget() != this) return;
        if (!(event.getEntity() instanceof Player player)) return;

        player.playSound(Sound.sound()
                .type(SoundEvent.BLOCK_NOTE_BLOCK_PLING)
                .pitch(2)
                .build(), event.getTarget());
        onClick.accept(player);
    }

    public void handle(@NotNull PlayerEntityInteractEvent event) {
        if (event.getTarget() != this) return;
        if (event.getHand() != Player.Hand.MAIN) return; // Prevent duplicating event

        event.getEntity().playSound(Sound.sound()
                .type(SoundEvent.BLOCK_NOTE_BLOCK_PLING)
                .pitch(2)
                .build(), event.getTarget());
        onClick.accept(event.getEntity());
    }

    @Override
    public void updateNewViewer(@NotNull Player player) {
        // Required to spawn player
        final List<Property> properties = List.of(new Property("textures", skin.textures(), skin.signature()));
        player.sendPacket(new PlayerInfoPacket(ADD_PLAYER, new AddPlayer(getUuid(), name, properties,
                GameMode.SURVIVAL, 0, null, null)));

        // Remove from tab list after 1 second, seems not to load skin if 1 or 2 ticks
        MinecraftServer.getSchedulerManager().scheduleTask(
                () -> player.sendPacket(new PlayerInfoPacket(REMOVE_PLAYER, new RemovePlayer(getUuid()))),
                TaskSchedule.seconds(1), TaskSchedule.stop());

        super.updateNewViewer(player);
    }

    private static final class LookAtPlayerGoal extends GoalSelector {
        private Entity target;

        public LookAtPlayerGoal(EntityCreature entityCreature) {
            super(entityCreature);
        }

        @Override
        public boolean shouldStart() {
            target = findTarget();
            return target != null;
        }

        @Override
        public void start() {}

        @Override
        public void tick(long time) {
            if (entityCreature.getDistanceSquared(target) > 225 ||
                    entityCreature.getInstance() != target.getInstance()) {
                target = null;
                return;
            }

            entityCreature.lookAt(target);
        }

        @Override
        public boolean shouldEnd() {
            return target == null;
        }

        @Override
        public void end() {}
    }
}
