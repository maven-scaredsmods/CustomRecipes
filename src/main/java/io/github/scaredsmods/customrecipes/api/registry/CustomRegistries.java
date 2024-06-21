package io.github.scaredsmods.customrecipes.api.registry;

import com.google.common.collect.Maps;
import com.mojang.serialization.Lifecycle;
import io.github.scaredsmods.customrecipes.CustomRecipes;
import io.github.scaredsmods.customrecipes.api.recipe.CustomRecipeType;
import io.github.scaredsmods.customrecipes.api.serializer.CustomRecipeSerializer;
import net.minecraft.Bootstrap;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.apache.commons.lang3.Validate;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

public class CustomRegistries {
    private static final Map<Identifier, Supplier<?>> DEFAULT_ENTRIES = Maps.<Identifier, Supplier<?>>newLinkedHashMap();

    public static final Registry<CustomRecipeType<?>> CUSTOM_RECIPE_TYPE = create(CustomRegistryKeys.CUSTOM_RECIPE_TYPE, registry -> CustomRecipeType.TEST_RECIPE);
    public static final Registry<CustomRecipeSerializer<?>> CUSTOM_RECIPE_SERIALIZER = create(CustomRegistryKeys.CUSTOM_RECIPE_SERIALIZER, registry -> CustomRecipeSerializer.TEST);

    private static final MutableRegistry<MutableRegistry<?>> ROOT = new SimpleRegistry<>(RegistryKey.ofRegistry(RegistryKeys.ROOT), Lifecycle.stable());
    public static final Registry<? extends Registry<?>> REGISTRIES = ROOT;


    private static <T> Registry<T> create(RegistryKey<? extends Registry<T>> key, Initializer<T> initializer) {
        return create(key, Lifecycle.stable(), initializer);
    }

    private static <T> Registry<T> createIntrusive(RegistryKey<? extends Registry<T>> key, Initializer<T> initializer) {
        return create(key, new SimpleRegistry<>(key, Lifecycle.stable(), true), initializer, Lifecycle.stable());
    }

    private static <T> DefaultedRegistry<T> create(RegistryKey<? extends Registry<T>> key, String defaultId, Initializer<T> initializer) {
        return create(key, defaultId, Lifecycle.stable(), initializer);
    }

    private static <T> DefaultedRegistry<T> createIntrusive(RegistryKey<? extends Registry<T>> key, String defaultId, Initializer<T> initializer) {
        return createIntrusive(key, defaultId, Lifecycle.stable(), initializer);
    }

    private static <T> Registry<T> create(RegistryKey<? extends Registry<T>> key, Lifecycle lifecycle, Initializer<T> initializer) {
        return create(key, new SimpleRegistry<>(key, lifecycle, false), initializer, lifecycle);
    }

    private static <T> DefaultedRegistry<T> create(
            RegistryKey<? extends Registry<T>> key, String defaultId, Lifecycle lifecycle, Initializer<T> initializer
    ) {
        return create(key, new SimpleDefaultedRegistry<>(defaultId, key, lifecycle, false), initializer, lifecycle);
    }

    private static <T> DefaultedRegistry<T> createIntrusive(
            RegistryKey<? extends Registry<T>> key, String defaultId, Lifecycle lifecycle, Initializer<T> initializer
    ) {
        return create(key, new SimpleDefaultedRegistry<>(defaultId, key, lifecycle, true), initializer, lifecycle);
    }

    private static <T, R extends MutableRegistry<T>> R create(
            RegistryKey<? extends Registry<T>> key, R registry, Initializer<T> initializer, Lifecycle lifecycle
    ) {

        return registry;
    }

    public static void bootstrap() {
        init();
        freezeRegistries();
        validate(REGISTRIES);
    }

    private static void init() {
        DEFAULT_ENTRIES.forEach((id, initializer) -> {
            if (initializer.get() == null) {
                CustomRecipes.LOGGER.error("Unable to bootstrap registry '{}'", id);
            }
        });
    }

    private static void freezeRegistries() {
        REGISTRIES.freeze();

        for (Registry<?> registry : REGISTRIES) {
            registry.freeze();
        }
    }

    private static <T extends Registry<?>> void validate(Registry<T> registries) {
        registries.forEach(registry -> {
            if (registry.getIds().isEmpty()) {
                Util.error("Registry '" + registries.getId((T)registry) + "' was empty after loading");
            }

            if (registry instanceof DefaultedRegistry) {
                Identifier identifier = ((DefaultedRegistry)registry).getDefaultId();
                Validate.notNull(registry.get(identifier), "Missing default of DefaultedMappedRegistry: " + identifier);
            }
        });
    }

    @FunctionalInterface
    interface Initializer<T> {
        T run(Registry<T> registry);
    }
}
