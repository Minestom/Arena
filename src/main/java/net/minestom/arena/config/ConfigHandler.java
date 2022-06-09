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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.RecordComponent;
import java.util.*;

public final class ConfigHandler {
    public volatile static Config CONFIG;
    private static boolean reload = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigHandler.class);
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
            .create();
    private static final File configFile = new File("config.json");

    static {
        loadConfig();
    }

    public static void loadConfig() {
        Config old = CONFIG;

        if (configFile.exists()) {
            try (JsonReader reader = new JsonReader(new FileReader(configFile))) {
                CONFIG = gson.fromJson(reader, Config.class);
            } catch (IOException exception) {
                LOGGER.error("Failed to load configuration file, using defaults.", exception);
                loadDefaults();
            }
        } else {
            loadDefaults();
            try {
                final FileWriter writer = new FileWriter(configFile);
                gson.toJson(CONFIG, writer);
                writer.flush();
                writer.close();
            } catch (IOException exception) {
                LOGGER.error("Failed to write default configuration.", exception);
            }
        }

        if (reload) {
            MinecraftServer.getGlobalEventHandler().call(new ConfigurationReloadedEvent(old, CONFIG));
            LOGGER.info("Configuration reloaded!");
        } else {
            reload = true;
        }
    }

    private static void loadDefaults() {
        CONFIG = gson.fromJson("{}", Config.class);
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

                        Arrays.stream(recordComponents).filter(x -> !argsMap.containsKey(x.getName())).forEach(x -> {
                            final String name = x.getName();
                            final Class<?> argClazz = x.getType();
                            final Default def = x.getAnnotation(Default.class);
                            if (def == null) {
                                argsMap.put(name, instantiateWithDefaults(argClazz));
                                return;
                            }
                            try {
                                if (argClazz == String.class) {
                                    argsMap.put(name, def.value());
                                } else {
                                    argsMap.put(name, gson.getAdapter(typeMap.get(name)).fromJson(def.value()));
                                }
                            } catch (IOException ignored) {}
                        });

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

                private Object instantiateWithDefaults(Class<?> clazz) {
                    final List<Object> args = new ArrayList<>();
                    final Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
                    for (Parameter param : constructor.getParameters()) {
                        final Class<?> paramClazz = param.getType();
                        final Default def = param.getAnnotation(Default.class);
                        if (def == null) {
                            args.add(instantiateWithDefaults(paramClazz));
                            continue;
                        }
                        try {
                            if (paramClazz == String.class) {
                                args.add(def.value());
                            } else {
                                args.add(gson.getAdapter(TypeToken.get(param.getType())).fromJson(def.value()));
                            }
                        } catch (IOException ignored) {
                            args.add(null);
                        }
                    }
                    try {
                        return constructor.newInstance(args.toArray(Object[]::new));
                    } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                        return null;
                    }
                }
            };
        }
    }
}
