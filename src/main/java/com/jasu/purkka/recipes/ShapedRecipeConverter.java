package com.jasu.purkka.recipes;

import net.minecraft.world.Container;
import com.google.gson.JsonObject;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import java.util.ArrayList;
import java.util.List;

public final class ShapedRecipeConverter<C extends ShapedRecipe> extends RecipeConverter<C> {
  public JsonObject getConverterInfo() {
    return type("shaped", layoutGrid("ingredients", 3, 3, 0));
  }
  public String getName() {
    return "shaped";
  }
  public boolean isComplete() {
    return true;
  }
  public List<Ingredient> getIngredients(C recipe) {
    List<Ingredient> result = new ArrayList<Ingredient>();
    int n = 0;
    List<Ingredient> ingredients = recipe.getIngredients();
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 3; ++j) {
        Ingredient ingredient;
        if (i < recipe.getHeight() && j < recipe.getWidth()) {
          ingredient = ingredients.get(n++);
        } else {
          ingredient = Ingredient.EMPTY;
        }
        result.add(ingredient.isEmpty() ? null : ingredient);
      }
    }
    return result;
  }
}
