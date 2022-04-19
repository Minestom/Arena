package net.minestom.arena.game.mobdrops;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DropEvents {

    public void onDeath(EntityDeathEvent event) {

        if(event.getEntity().getEntityType() == EntityType.PLAYER) {
            Player player = (Player) event.getEntity();
            player.getAttribute(Attribute.MOVEMENT_SPEED).getModifiers().clear();
        }else {
            new RandomDrop().getDrop().setInstance(event.getInstance(), event.getEntity().getPosition());
        }
    }

    public void itemPickUp(PickupItemEvent event) {
        if(event.getEntity().getEntityType() != EntityType.PLAYER) { return; }
        Player player = (Player) event.getEntity();
        if(event.getItemStack().getMaterial() == Material.GOLDEN_APPLE) {
            if(player.getMaxHealth() - player.getHealth() < 5) {
                player.setHealth(player.getMaxHealth());
            }else{
                player.setHealth(player.getHealth() + 5);
            }
            player.sendMessage("§aYou have been §6§lhealed§a!");

        }else if(event.getItemStack().getMaterial() == Material.FEATHER){
            int size = player.getAttribute(Attribute.MOVEMENT_SPEED).getModifiers().size();
            player.getAttribute(Attribute.MOVEMENT_SPEED).addModifier(new AttributeModifier(UUID.randomUUID().toString(), 0.03f, AttributeOperation.ADDITION));

            player.sendMessage("§aYou have received a §6§lspeed§a effect.");
        }else if(event.getItemStack().getMaterial() == Material.BLAZE_ROD) {
            for(Entity loopEntity : player.getInstance().getEntities()) {
                if(loopEntity.getEntityType() != EntityType.PLAYER && loopEntity instanceof LivingEntity) {
                    Entity lightning = new Entity(EntityType.LIGHTNING_BOLT);
                    LivingEntity entity = (LivingEntity) loopEntity;
                    entity.damage(DamageType.fromEntity(lightning), 5f);
                    lightning.setInstance(player.getInstance(), entity.getPosition());
                    entity.setCustomName(generateHealthBar(entity.getMaxHealth(), entity.getHealth()));
                }
            }
            player.sendMessage("§aYou §6§lshocked§a the mobs.");
        }
    }

    private static final int BLOCK_LENGTH = 6;
    private static final String EMPTY_BLOCK_CHAR = "□";
    private static final String FULL_BLOCK_CHAR = "■";

    @Contract(pure = true)
    private static @NotNull Component generateHealthBar(float maxHealth, float minHealth) {
        // Converts the health percentage into a number from 0-{blockLength} -- only 0 if the mob's health is 0
        final int charHealth = (int) Math.ceil((minHealth / maxHealth) * BLOCK_LENGTH);
        return Component.text()
                .append(Component.text("[", NamedTextColor.DARK_GRAY))
                .append(Component.text(FULL_BLOCK_CHAR.repeat(charHealth) + EMPTY_BLOCK_CHAR.repeat(BLOCK_LENGTH - charHealth), NamedTextColor.RED))
                .append(Component.text("]", NamedTextColor.DARK_GRAY))
                .build();
    }
}
