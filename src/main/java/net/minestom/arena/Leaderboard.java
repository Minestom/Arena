package net.minestom.arena;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.arena.utils.HologramUtils;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.hologram.Hologram;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

// TODO: Optimise to not sort every update
public class Leaderboard {
    private static final Path FILE_LOCATION = Path.of("leaderboard.dat");

    private final List<Hologram> holograms = new ArrayList<>();
    private final Instance instance;
    private final Pos spawnAt;
    private final Map<String, Integer> data;
    private final Component title;
    private final int top;
    private final Function<Map.Entry<String, Integer>, String> formatFunction;

    public Leaderboard(@NotNull Instance instance, @NotNull Pos spawnAt, Map<String, Integer> data,
                       @NotNull String title, int top, @NotNull Function<Map.Entry<String, Integer>, String> formatFunction) {

        this.instance = instance;
        this.spawnAt = spawnAt;
        this.data = new ConcurrentHashMap<>(data);
        this.title = Component.text(title, NamedTextColor.GOLD, TextDecoration.BOLD);
        this.top = top + 1; // +1 for the title
        this.formatFunction = formatFunction;
    }

    public void addEntry(String names, int score) {
        if (score <= data.getOrDefault(names, 0)) return;
        data.put(names, score);
        update();
    }

    void update() {
        for (Hologram hologram : holograms) {
            hologram.remove();
        }
        holograms.clear();

        final List<Component> lines = new ArrayList<>(List.of(title));
        data.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(top)
                .forEachOrdered(entry -> lines.add(0, Component.text(formatFunction.apply(entry))));

        while (lines.size() < top) {
            lines.add(0, Component.text("-"));
        }

        holograms.addAll(HologramUtils.spawnHolograms(instance, spawnAt, lines));
    }

    static Map<String, Integer> loadLeaderboardData(String arena) {
        if (!Files.exists(FILE_LOCATION)) return new HashMap<>();

        try (NBTReader reader = new NBTReader(FILE_LOCATION)) {
            NBTCompound tag = (NBTCompound) reader.read();
            NBTCompound arenaTag = tag.getCompound(arena);
            NBTList<NBTCompound> leaderboard = arenaTag.getList("leaderboard");

            HashMap<String, Integer> map = new HashMap<>();
            for (NBTCompound entry : leaderboard) {
                String names = entry.getString("names");
                int score = entry.getInt("score");

                map.put(names, score);
            }

            return map;
        } catch (IOException | NBTException e) {
            throw new RuntimeException(e);
        }
    }

    static void saveLeaderboard(String arena, Leaderboard leaderboard) {
        Set<Map.Entry<String, Integer>> data = leaderboard.data.entrySet();

        List<NBTCompound> entries = data.stream()
                .map(entry -> NBT.Compound(builder -> {
                    builder.setString("names", entry.getKey());
                    builder.setInt("score", entry.getValue());
                }))
                .toList();
        NBTList<NBTCompound> compounds = NBT.List(NBTType.TAG_Compound, entries);

        NBTCompound compound = NBT.Compound(root ->
                root.put(arena, NBT.Compound(builder ->
                        builder.put("leaderboard", compounds))
                )
        );

        try (NBTWriter writer = new NBTWriter(FILE_LOCATION)) {
            writer.writeNamed("", compound);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
