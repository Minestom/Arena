package net.minestom.arena.game.mobdrops;

import net.minestom.arena.Messenger;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.Random;
import java.util.UUID;

public class PickupEvent {

    public void itemPickUp(PickupItemEvent event) {
        if(!event.getEntity().getEntityType().equals(EntityType.PLAYER)) { return; }
        Player player = (Player) event.getEntity();
        if(event.getItemStack().equals(RandomDrop.healItem)) {
            if(player.getMaxHealth() - player.getHealth() < 5) {
                player.setHealth(player.getMaxHealth());
            }else{
                player.setHealth(player.getHealth() + 5);
            }
            Messenger.info(player, "You have been healed!");

        }else if(event.getItemStack().equals(RandomDrop.speedItem)){
            player.getAttribute(Attribute.MOVEMENT_SPEED).addModifier(new AttributeModifier(UUID.randomUUID().toString(), 0.03f, AttributeOperation.ADDITION));
            Messenger.info(player, "You have received a speed effect!");
        }else if(event.getItemStack().equals(RandomDrop.lightningItem)) {
            for(Entity entity : player.getInstance().getEntities()) {
                if(!entity.getEntityType().equals(EntityType.PLAYER)) {
                    entity.remove();
                    Entity lightning = new Entity(EntityType.LIGHTNING_BOLT);
                    lightning.setInstance(entity.getInstance(), entity.getPosition());
                    Messenger.info(player, "You have shocked 1 mob!");
                    return;
                }
            }
        }
    }
}
