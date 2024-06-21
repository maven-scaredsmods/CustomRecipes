package io.github.scaredsmods.customrecipes.api.recipe;

import com.mojang.serialization.Codec;
import io.github.scaredsmods.customrecipes.api.registry.CustomRegistries;
import io.github.scaredsmods.customrecipes.api.serializer.CustomRecipeSerializer;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.List;

public interface CustomRecipe <C extends Inventory> {

    Codec<CustomRecipe<?>> CODEC = CustomRegistries.CUSTOM_RECIPE_SERIALIZER.getCodec().dispatch(CustomRecipe::getSerializer, CustomRecipeSerializer::codec);

    boolean matches(C inventory, World world);
    ItemStack craftFirstItemStack(C inventory, DynamicRegistryManager registryManager);
    ItemStack craftSecondItemStack(C inventory, DynamicRegistryManager registryManager);

    boolean fits(int width, int height);
    ItemStack getFirstOutput(DynamicRegistryManager registryManager);
    ItemStack getSecondOutput(DynamicRegistryManager registryManager);

    default DefaultedList<ItemStack> getRemainder(C inventory) {
        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);

        for(int i = 0; i < defaultedList.size(); ++i) {
            Item item = inventory.getStack(i).getItem();
            if (item.hasRecipeRemainder()) {
                defaultedList.set(i, new ItemStack(item.getRecipeRemainder()));
            }
        }

        return defaultedList;
    }
    default DefaultedList<Ingredient> getIngredients() {
        return DefaultedList.of();
    }

    default boolean isIgnoredInRecipeBook() {
        return false;
    }

    default boolean showNotification() {
        return true;
    }

    default String getGroup() {
        return "";
    }

    default ItemStack createIcon() {
        return new ItemStack(Blocks.CRAFTING_TABLE);
    }


    CustomRecipeSerializer<?> getSerializer();

    CustomRecipeType<?> getType();

    default boolean isEmpty() {
        DefaultedList<Ingredient> defaultedList = this.getIngredients();
        return defaultedList.isEmpty() || defaultedList.stream().anyMatch((ingredient) -> {
            return ingredient.getMatchingStacks().length == 0;
        });
    }

}
