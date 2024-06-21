package io.github.scaredsmods.customrecipes.api.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.scaredsmods.customrecipes.api.serializer.CustomRecipeSerializer;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;

import java.util.List;

public class TestRecipe implements CustomRecipe<SimpleInventory> {
    private final List<Ingredient> recipeItems;

    private final ItemStack output_1;
    private final ItemStack output_2;

    public TestRecipe(List<Ingredient> recipeItems, ItemStack output1, ItemStack output2) {
        this.recipeItems = recipeItems;
        output_1 = output1;
        output_2 = output2;
    }

    @Override
    public boolean matches(SimpleInventory inventory, World world) {
        if(world.isClient()) {
            return false;
        }

        return recipeItems.get(0).test(inventory.getStack(0));
    }

    @Override
    public ItemStack craftFirstItemStack(SimpleInventory inventory, DynamicRegistryManager registryManager) {
        return output_1;
    }

    @Override
    public ItemStack craftSecondItemStack(SimpleInventory inventory, DynamicRegistryManager registryManager) {
        return output_2;
    }



    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getFirstOutput(DynamicRegistryManager registryManager) {
        return output_1;
    }

    @Override
    public ItemStack getSecondOutput(DynamicRegistryManager registryManager) {
        return output_2;
    }


    @Override
    public DefaultedList<Ingredient> getIngredients() {
        DefaultedList<Ingredient> list = DefaultedList.ofSize(this.recipeItems.size());
        list.addAll(recipeItems);
        return list;
    }

    @Override
    public CustomRecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public CustomRecipeType<?> getType() {
        return Type.INSTANCE;
    }
    public static class Type implements CustomRecipeType<TestRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "test_recipe";
    }
    public static class Serializer implements CustomRecipeSerializer<TestRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final String ID = "test_recipe";

        public static final Codec<TestRecipe> CODEC = RecordCodecBuilder.create(in -> in.group(
                validateAmount(Ingredient.DISALLOW_EMPTY_CODEC, 9).fieldOf("ingredients").forGetter(TestRecipe::getIngredients),
                ItemStack.RECIPE_RESULT_CODEC.fieldOf("output_1").forGetter(r -> r.output_1),
                ItemStack.RECIPE_RESULT_CODEC.fieldOf("output_2").forGetter(r -> r.output_2)
        ).apply(in, TestRecipe::new));

        private static Codec<List<Ingredient>> validateAmount(Codec<Ingredient> delegate, int max) {
            return Codecs.validate(Codecs.validate(
                    delegate.listOf(), list -> list.size() > max ? DataResult.error(() -> "Recipe has too many ingredients!") : DataResult.success(list)
            ), list -> list.isEmpty() ? DataResult.error(() -> "Recipe has no ingredients!") : DataResult.success(list));
        }
        @Override
        public Codec<TestRecipe> codec() {
            return CODEC;
        }

        @Override
        public TestRecipe read(Identifier id, PacketByteBuf buf) {
            DefaultedList<Ingredient> inputs = DefaultedList.ofSize(buf.readInt(), Ingredient.EMPTY);

            for(int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromPacket(buf));
            }

            ItemStack output1 = buf.readItemStack();
            ItemStack output2 = buf.readItemStack();

            return new TestRecipe(inputs, output1, output2);
        }




        @Override
        public void write(PacketByteBuf buf, TestRecipe recipe) {
            buf.writeInt(recipe.getIngredients().size());

            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredient.write(buf);
            }

            buf.writeItemStack(recipe.output_1);
            buf.writeItemStack(recipe.output_2);
        }

    }
}