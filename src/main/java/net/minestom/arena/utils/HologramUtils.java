package net.minestom.arena.utils;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.hologram.Hologram;
import net.minestom.server.instance.Instance;

import java.util.ArrayList;
import java.util.List;

public final class HologramUtils {
    private HologramUtils() {}

	public static List<Hologram> spawnHolograms(Instance instance, Pos position, List<Component> lines) {
		return spawnHolograms(instance, position, lines.toArray(Component[]::new));
	}
	
	public static List<Hologram> spawnHolograms(Instance instance, Pos position, Component... lines) {
		List<Hologram> holograms = new ArrayList<>();
		for (Component line : lines) {
			if (!line.equals(Component.empty()))
				holograms.add(new Hologram(instance, position, line, true, true));
			
			position = position.add(0, 0.25, 0);
		}
		
		return holograms;
	}
}
