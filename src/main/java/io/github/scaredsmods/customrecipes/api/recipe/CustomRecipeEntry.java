package io.github.scaredsmods.customrecipes.api.recipe;


import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;

public record CustomRecipeEntry<T extends CustomRecipe<?>>(Identifier id, T value) {
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else {
            if (o instanceof CustomRecipeEntry<?> recipeEntry && this.id.equals(recipeEntry.id)) {
                return true;
            }

            return false;
        }
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public String toString() {
        return this.id.toString();
    }
}
