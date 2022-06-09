package net.minestom.arena.config;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.Nullable;

public record ConfigurationChangeEvent(@Nullable Config previousConfig, Config currentConfig) implements Event {
}
