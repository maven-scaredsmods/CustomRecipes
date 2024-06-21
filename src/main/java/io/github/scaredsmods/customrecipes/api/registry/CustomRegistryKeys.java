package io.github.scaredsmods.customrecipes.api.registry;

import io.github.scaredsmods.customrecipes.api.recipe.CustomRecipeType;
import io.github.scaredsmods.customrecipes.api.serializer.CustomRecipeSerializer;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class CustomRegistryKeys {

    public static final RegistryKey<Registry<CustomRecipeType<?>>> CUSTOM_RECIPE_TYPE = of("custom_recipe_type");
    public static final RegistryKey<Registry<CustomRecipeSerializer<?>>>CUSTOM_RECIPE_SERIALIZER = of("custom_recipe_serializer");

    private static <T> RegistryKey<Registry<T>> of(String id) {
        return RegistryKey.ofRegistry(new Identifier(id));
    }
}