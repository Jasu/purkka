package com.jasu.purkka.recipes;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import com.google.gson.JsonObject;

public final class DefaultRecipeConverter<R extends Recipe<?>> extends RecipeConverter<R> {
  public JsonObject getConverterInfo() {
    return type("default", layoutRest("ingredients"));
  }
  public String getName() {
    return "default";
  }
}
