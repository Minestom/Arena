package net.minestom.arena.feature;

import net.minestom.server.entity.ItemEntity;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

record DropFeature(Predicate<ItemStack> allowPredicate) implements Feature {
    @Override
    public void hook(@NotNull EventNode<InstanceEvent> node) {
        node.addListener(ItemDropEvent.class, event -> {
            ItemStack item = event.getItemStack();

            if (!allowPredicate.test(item)) {
                event.setCancelled(true);
                return;
            }

            ItemEntity itemEntity = new ItemEntity(item);
            itemEntity.setPickupDelay(40, TimeUnit.SERVER_TICK);
            itemEntity.setInstance(event.getInstance(), event.getPlayer().getPosition().add(0, 1.5, 0));
            itemEntity.setGlowing(true);
            itemEntity.setVelocity(event.getPlayer().getPosition().direction().mul(6));
        });
    }
}
