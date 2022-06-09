package net.minestom.arena.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.minestom.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConfigHandler {
    public volatile static Config CONFIG;
    private static boolean reload = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigHandler.class);

    static {
        loadConfig();
    }

    public static void loadConfig() {
        Config old = CONFIG;
        Config config = null;
        try (JsonReader reader = new JsonReader(new FileReader("config.json"))) {
            config = new GsonBuilder()
                    .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
                    .create()
                    .fromJson(reader, Config.class);
        } catch (IOException ignored) {}

        CONFIG = config == null ? new Config() : config;

        if (reload) {
            MinecraftServer.getGlobalEventHandler().call(new ConfigurationChangeEvent(old, CONFIG));
            LOGGER.info("Configuration reloaded!");
        } else {
            reload = true;
        }
    }

    private ConfigHandler() {}

    private static class RecordTypeAdapterFactory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            final Class<? super T> clazz = type.getRawType();
            final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

            if (!clazz.isRecord())
                return null;

            return new TypeAdapter<>() {
                @Override
                public void write(JsonWriter out, T value) throws IOException {
                    delegate.write(out, value);
                }

                @SuppressWarnings("unchecked")
                @Override
                public T read(JsonReader reader) throws IOException {
                    if (reader.peek() == JsonToken.NULL) {
                        reader.nextNull();
                        return null;
                    } else {
                        final RecordComponent[] recordComponents = clazz.getRecordComponents();
                        final Map<String, TypeToken<?>> typeMap = new HashMap<>();
                        final Map<String, Object> argsMap = new HashMap<>();

                        for (RecordComponent component : recordComponents)
                            typeMap.put(component.getName(), TypeToken.get(component.getGenericType()));

                        reader.beginObject();
                        while (reader.hasNext()) {
                            String name = reader.nextName();
                            argsMap.put(name, gson.getAdapter(typeMap.get(name)).read(reader));
                        }
                        reader.endObject();

                        final List<Object> args = new ArrayList<>();
                        final List<Class<?>> argTypes = new ArrayList<>();
                        for (RecordComponent component : recordComponents) {
                            args.add(argsMap.get(component.getName()));
                            argTypes.add(component.getType());
                        }

                        try {
                            Constructor<? super T> constructor = clazz.getDeclaredConstructor(argTypes.toArray(Class<?>[]::new));
                            constructor.setAccessible(true);
                            return (T) constructor.newInstance(args.toArray(Object[]::new));
                        } catch (ReflectiveOperationException e) {
                            return null;
                        }
                    }
                }
            };
        }
    }
}
