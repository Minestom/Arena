package net.minestom.arena.feature;

import net.minestom.server.entity.ItemEntity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

record DropFeature() implements Feature {

    @Override
    public void hook(@NotNull EventNode<Event> node) {
        node.addListener(ItemDropEvent.class, event -> {
            ItemStack item = event.getItemStack();
            ItemEntity itemEntity = new ItemEntity(item);
            itemEntity.setPickupDelay(500, TimeUnit.MILLISECOND);
            itemEntity.setInstance(event.getInstance(), event.getPlayer().getPosition().add(.0, 1.5, .0));
            itemEntity.setGlowing(true);
            itemEntity.setVelocity(event.getPlayer().getPosition().direction().mul(6.0));
        });
    }
}
