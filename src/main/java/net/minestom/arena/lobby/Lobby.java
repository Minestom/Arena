package net.minestom.arena.lobby;

import net.minestom.arena.group.Group;
import net.minestom.arena.utils.FullbrightDimension;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.instance.AddEntityToInstanceEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.AnvilLoader;
import net.minestom.server.instance.Instance;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.nio.file.Path;

public final class Lobby {
    public static final Instance INSTANCE;

    static  {
        final Instance instance = MinecraftServer.getInstanceManager().createInstanceContainer(
                FullbrightDimension.INSTANCE, new AnvilLoader(Path.of("lobby")));

        Map.create(instance, new Pos(2, 18, 9));
        instance.setTimeRate(0);
        for (NPC npc : NPC.spawnNPCs(instance)) {
            instance.eventNode().addListener(EntityAttackEvent.class, npc::handle)
                    .addListener(PlayerEntityInteractEvent.class, npc::handle);
        }

        instance.eventNode().addListener(AddEntityToInstanceEvent.class, event -> {
            if (!(event.getEntity() instanceof Player player)) return;

            if (player.getInstance() != null) player.scheduler().scheduleNextTick(() -> onArenaFinish(player));
            else onFirstSpawn(player);
        }).addListener(ItemDropEvent.class, event -> event.setCancelled(true));

        INSTANCE = instance;
    }

    private static void onFirstSpawn(Player player) {
        player.sendPackets(Map.packets());

        final Group group = Group.findGroup(player);
        group.setDisplay(new LobbySidebarDisplay(group));
    }

    private static void onArenaFinish(Player player) {
        player.refreshCommands();
        player.getInventory().clear();
        player.teleport(new Pos(0.5, 16, 0.5));
        player.tagHandler().updateContent(NBTCompound.EMPTY);
    }
}
