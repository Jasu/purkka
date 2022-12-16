package com.jasu.purkka.recipes;

import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.MultiItemValue;

public abstract class RecipeConverter<R extends Recipe<?>> {
  public static JsonObject layoutElement(String type, String name, int start) {
    JsonObject result = new JsonObject();
    result.addProperty("type", type);
    result.addProperty("name", name);
    result.addProperty("start", start);
    return result;
  }
  public static JsonObject layoutGrid(String name, int width, int height, int start) {
    JsonObject result = layoutElement("grid", name, start);
    result.addProperty("width", width);
    result.addProperty("height", height);
    return result;
  }
  public static JsonObject layoutSingle(String name, int index) {
    return layoutElement("single", name, index);
  }
  public static JsonObject layoutSingle(String name) {
    return layoutSingle(name, 0);
  }
  public static JsonObject layoutRest(String name, int start) {
    return layoutElement("rest", name, start);
  }
  public static JsonObject layoutRest(String name) {
    return layoutElement("rest", name, 0);
  }
  public static JsonObject type(String name, List<JsonObject> inputs, List<JsonObject> outputs) {
    JsonObject result = new JsonObject();
    result.addProperty("name", name);
    result.add("inputs", GSON.toJsonTree(inputs));
    result.add("aux", new JsonArray());
    result.add("outputs", GSON.toJsonTree(outputs));
    return result;
  }
  public static JsonObject type(String name, List<JsonObject> inputs) {
    return type(name, inputs, List.of(layoutRest("output")));
  }

  public static JsonObject type(String name, JsonObject input) {
    return type(name, List.of(input), List.of(layoutRest("output")));
  }
  public abstract String getName();
  public abstract JsonObject getConverterInfo();

  static Gson GSON = new Gson();
  static RecipeConverter[] CONVERTERS = {
    new ShapedRecipeConverter(),
    new ShapelessRecipeConverter(),
    new DefaultRecipeConverter(),
  };


  public final boolean isSpecial(R recipe) {
    return recipe.isSpecial();
  }

  public List<ItemStack> getOutputs(R recipe) {
    return List.of(recipe.getResultItem());
  }

  public List<Ingredient> getIngredients(R recipe) {
    return recipe.getIngredients();
  }

  public JsonObject toJson(R recipe) {
    JsonObject result = new JsonObject();
    result.addProperty("type", getName());
    result.addProperty("id", recipe.getId().toString());
    result.addProperty("special", recipe.isSpecial());
    JsonArray inputs = new JsonArray();
    for (Ingredient i : getIngredients(recipe)) {
      inputs.add(i == null ? null : i.toJson());
    }
    result.add("inputs", inputs);
    JsonArray outputs = new JsonArray();
    for (ItemStack i : getOutputs(recipe)) {
      outputs.add(i == null ? null : (new MultiItemValue(List.of(i))).serialize());
    }
    result.add("outputs", outputs);
    return result;
  }

  public static JsonObject getConverterInfos() {
    JsonObject result = new JsonObject();
    for (RecipeConverter c : CONVERTERS) {
      result.add(c.getName(), c.getConverterInfo());
    }
    return result;
  }

  public static <T extends Recipe<?>> RecipeConverter<T> getConverter(T recipe) {
    if (recipe instanceof ShapedRecipe) {
      return new ShapedRecipeConverter();
    } else if (recipe instanceof ShapelessRecipe) {
      return new ShapelessRecipeConverter();
    }
    return new DefaultRecipeConverter();
  }
}
