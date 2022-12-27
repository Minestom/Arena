package net.minestom.arena;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.arena.game.ArenaCommand;
import net.minestom.arena.group.Group;
import net.minestom.arena.utils.FullbrightDimension;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.entity.metadata.other.ItemFrameMeta;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.instance.AddEntityToInstanceEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.AnvilLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.MapMeta;
import net.minestom.server.map.framebuffers.LargeGraphics2DFramebuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Lobby extends InstanceContainer {
    public static final Lobby INSTANCE = new Lobby();
    public static final ServerPacket[] MAP_PACKETS;

    static {
        MinecraftServer.getInstanceManager().registerInstance(INSTANCE);

        for (NPC npc : spawnNPCs()) {
            INSTANCE.eventNode().addListener(EntityAttackEvent.class, npc::handle)
                    .addListener(PlayerEntityInteractEvent.class, npc::handle);
        }

        createMaps();
        try {
            final LargeGraphics2DFramebuffer framebuffer = new LargeGraphics2DFramebuffer(5 * 128, 3 * 128);
            final InputStream imageStream = Lobby.class.getResourceAsStream("/minestom.png");
            assert imageStream != null;
            BufferedImage image = ImageIO.read(imageStream);
            framebuffer.getRenderer().drawRenderedImage(image, AffineTransform.getScaleInstance(1.0, 1.0));
            MAP_PACKETS = mapPackets(framebuffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Lobby() {
        super(UUID.randomUUID(), FullbrightDimension.INSTANCE);
        setChunkLoader(new AnvilLoader(Path.of("lobby")));
        setTimeRate(0);

        eventNode().addListener(AddEntityToInstanceEvent.class, event -> {
            final Entity entity = event.getEntity();
            if (entity instanceof Player player) {
                final Instance instance = player.getInstance();
                if (instance != null) player.scheduler().scheduleNextTick(() -> onArenaFinish(player));
                else onFirstSpawn(player);
            }
        }).addListener(ItemDropEvent.class, event -> event.setCancelled(true));
    }

    void onFirstSpawn(Player player) {
        player.sendPackets(Lobby.MAP_PACKETS);

        final Group group = Group.findGroup(player);
        group.setDisplay(new LobbySidebarDisplay(group));
    }

    void onArenaFinish(Player player) {
        player.refreshCommands();
        player.getInventory().clear();
        player.teleport(new Pos(0.5, 16, 0.5));
        player.tagHandler().updateContent(NBTCompound.EMPTY);
    }

    /**
     * Creates the maps on the board in the lobby
     */
    private static void createMaps() {
        final int maxX = 2;
        final int maxY = 18;
        final int z = 9;
        for (int i = 0; i < 15; i++) {
            final int x = maxX - i % 5;
            final int y = maxY - i / 5;
            final int id = i;

            Entity itemFrame = new Entity(EntityType.ITEM_FRAME);
            ItemFrameMeta meta = (ItemFrameMeta) itemFrame.getEntityMeta();
            itemFrame.setInstance(INSTANCE, new Pos(x, y, z, 180, 0));
            meta.setNotifyAboutChanges(false);
            meta.setOrientation(ItemFrameMeta.Orientation.NORTH);
            meta.setInvisible(true);
            meta.setItem(ItemStack.builder(Material.FILLED_MAP)
                    .meta(MapMeta.class, builder -> builder.mapId(id))
                    .build());
            meta.setNotifyAboutChanges(true);
        }
    }

    /**
     * Creates packets for maps that will display an image on the board in the lobby
     */
    private static ServerPacket[] mapPackets(LargeGraphics2DFramebuffer framebuffer) {
        ServerPacket[] packets = new ServerPacket[15];
        for (int i = 0; i < 15; i++) {
            final int x = i % 5;
            final int y = i / 5;
            packets[i] = framebuffer.createSubView(x * 128, y * 128).preparePacket(i);
        }
        return packets;
    }

    private static List<NPC> spawnNPCs() {
        try {
            final Map<String, PlayerSkin> skins = new HashMap<>();
            final Gson gson = new Gson();
            final JsonObject root = gson.fromJson(new String(Lobby.class.getResourceAsStream("/skins.json")
                    .readAllBytes()), JsonObject.class);

            for (JsonElement skin : root.getAsJsonArray("skins")) {
                final JsonObject object = skin.getAsJsonObject();
                final String owner = object.get("owner").getAsString();
                final String value = object.get("value").getAsString();
                final String signature = object.get("signature").getAsString();
                skins.put(owner, new PlayerSkin(value, signature));
            }

            return List.of(
                new NPC("Discord", skins.get("Discord"), INSTANCE, new Pos(8.5, 15, 8.5),
                        player -> Messenger.info(player, Component.text("Click here to join the Discord server")
                                .clickEvent(ClickEvent.openUrl("https://discord.gg/minestom")))),
                new NPC("Website", skins.get("Website"), INSTANCE, new Pos(-7.5, 15, 8.5),
                        player -> Messenger.info(player, Component.text("Click here to go to the Minestom website")
                                .clickEvent(ClickEvent.openUrl("https://minestom.net")))),
                new NPC("GitHub", skins.get("GitHub"), INSTANCE, new Pos(8.5, 15, -7.5),
                        player -> Messenger.info(player, Component.text("Click here to go to the Arena GitHub repository")
                                .clickEvent(ClickEvent.openUrl("https://github.com/Minestom/Arena")))),
                new NPC("Play", skins.get("Play"), INSTANCE, new Pos(-7.5, 15, -7.5), ArenaCommand::open)
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
