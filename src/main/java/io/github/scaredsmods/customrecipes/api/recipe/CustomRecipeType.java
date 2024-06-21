package io.github.scaredsmods.customrecipes.api.recipe;

import io.github.scaredsmods.customrecipes.CustomRecipes;
import io.github.scaredsmods.customrecipes.api.registry.CustomRegistries;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public interface CustomRecipeType<T extends CustomRecipe<?>> {

    CustomRecipeType<TestRecipe> TEST_RECIPE = register("test_recipe");

    static <T extends CustomRecipe<?>> CustomRecipeType<T> register(final String id) {
        return Registry.register(CustomRegistries.CUSTOM_RECIPE_TYPE, new Identifier(id), new CustomRecipeType<T>() {
            public String toString() {
                return id;
            }
        });
    }



}

