package io.github.scaredsmods.customrecipes.api.serializer;

import com.mojang.serialization.Codec;
import io.github.scaredsmods.customrecipes.api.recipe.CustomRecipe;
import io.github.scaredsmods.customrecipes.api.registry.CustomRegistries;
import io.github.scaredsmods.customrecipes.api.recipe.TestRecipe;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public interface CustomRecipeSerializer <T extends CustomRecipe<?>> {

    CustomRecipeSerializer<TestRecipe> TEST = register("test", new TestRecipe.Serializer());


    Codec<T> codec();

    T read(Identifier id, PacketByteBuf buf);

    void write(PacketByteBuf buf, T recipe);

    static <S extends CustomRecipeSerializer<T>, T extends CustomRecipe<?>> S register(String id, S serializer) {
        return Registry.register(CustomRegistries.CUSTOM_RECIPE_SERIALIZER, id, serializer);
    }
}
