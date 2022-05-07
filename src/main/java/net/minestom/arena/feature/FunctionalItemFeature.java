package net.minestom.arena.feature;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

record FunctionalItemFeature(Predicate<ItemStack> trigger, Consumer<Player> consumer, long cooldown) implements Feature {
    @Override
    public void hook(@NotNull EventNode<InstanceEvent> node) {
        final UUID random = UUID.randomUUID();
        final Tag<Long> lastUseTag = Tag.Long("item_" + random + "_last_use").defaultValue(0L);

        node.addListener(EventListener.builder(PlayerHandAnimationEvent.class)
                .handler(event -> {
                    final Player player = event.getPlayer();
                    final long lastUse = player.getTag(lastUseTag);
                    final long now = System.currentTimeMillis();

                    if (now - lastUse >= cooldown) {
                        player.setTag(lastUseTag, now);
                        consumer.accept(player);
                    }
                })
                .filter(event -> trigger.test(event.getPlayer().getItemInHand(event.getHand())))
                .filter(event -> event.getPlayer().getOpenInventory() == null)
                .build()
        );
    }
}
