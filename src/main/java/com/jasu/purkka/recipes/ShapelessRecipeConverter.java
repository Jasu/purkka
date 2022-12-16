package com.jasu.purkka.recipes;

import com.google.gson.JsonObject;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public final class ShapelessRecipeConverter<C extends ShapelessRecipe> extends RecipeConverter<C> {
  public JsonObject getConverterInfo() {
    return type("shapeless", layoutRest("ingredients"));
  }
  public String getName() {
    return "shapeless";
  }
}
