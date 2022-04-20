package net.minestom.arena.game.mob;

import net.kyori.adventure.sound.Sound;
import net.minestom.arena.Messenger;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.sound.SoundEvent;

public class NextStageNPC extends Entity {
    private final MobArena arena;
    private final EventNode<InstanceEvent> node = EventNode.type("next-stage-npc", EventFilter.INSTANCE);;

    public NextStageNPC(MobArena arena) {
        super(EntityType.VILLAGER);
        this.arena = arena;

        node.addListener(PlayerEntityInteractEvent.class, event -> {
           Player player = event.getPlayer();

           if (!arena.hasContinued(player)) {
               player.openInventory(new MobShopInventory(player, arena));
               player.playSound(Sound.sound(SoundEvent.ENTITY_VILLAGER_YES, Sound.Source.NEUTRAL, 1, 1), this);
           } else {
               Messenger.warn(player, "You already continued");
               player.playSound(Sound.sound(SoundEvent.ENTITY_VILLAGER_NO, Sound.Source.NEUTRAL, 1, 1), this);
           }
        });
        arena.instance().eventNode().addChild(node);
    }

    @Override
    public void remove() {
        super.remove();
        arena.instance().eventNode().removeChild(node);
    }
}
