package net.minestom.arena.feature;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.ItemUpdateStateEvent;
import net.minestom.server.event.player.PlayerItemAnimationEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * @param projectileGenerator Uses the return value as the entity to shoot (in lambda arg 1 is shooter, arg 2 is power)
 */
record BowFeature(@NotNull BiFunction<Entity, Double, EntityProjectile> projectileGenerator) implements Feature {
    private static final Tag<Long> CHARGE_SINCE_TAG = Tag.Long("bow_charge_since").defaultValue(Long.MAX_VALUE);

    @Override
    public void hook(@NotNull EventNode<InstanceEvent> node) {
        node.addListener(EventListener.builder(PlayerItemAnimationEvent.class)
                .handler(event -> event.getPlayer().setTag(CHARGE_SINCE_TAG, System.currentTimeMillis()))
                .filter(event -> event.getItemAnimationType() == PlayerItemAnimationEvent.ItemAnimationType.BOW)
                .build()
        ).addListener(EventListener.builder(ItemUpdateStateEvent.class)
                .handler(event -> {
                    final Player player = event.getPlayer();
                    final double chargedFor = (System.currentTimeMillis() - player.getTag(CHARGE_SINCE_TAG)) / 1000D;
                    final double power = MathUtils.clamp((chargedFor * chargedFor + 2 * chargedFor) / 2D, 0, 1);

                    if (power > 0.2) {
                        final EntityProjectile projectile = projectileGenerator.apply(player, power);
                        final Pos position = player.getPosition().add(0, player.getEyeHeight(), 0);

                        projectile.setInstance(Objects.requireNonNull(player.getInstance()), position);

                        Vec direction = projectile.getPosition().direction();
                        projectile.shoot(position.add(direction).sub(0, 0.2, 0), power * 3, 1.0);
                    }

                    // Restore arrow
                    player.getInventory().update();
                })
                .filter(event -> event.getItemStack().material() == Material.BOW)
                .build());
    }
}
