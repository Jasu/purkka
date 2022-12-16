package com.jasu.purkka.server;

import com.jasu.purkka.recipes.RecipeConverter;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.List;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jasu.purkka.http.Endpoint;
import com.jasu.purkka.http.HttpException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;

public final class ServerApi {
  MinecraftServer server;

  public ServerApi() {
    server = ServerLifecycleHooks.getCurrentServer();
  }

  @Endpoint(path="recipe-types")
  public JsonObject getRecipeTypes() {
    return RecipeConverter.getConverterInfos();
  }

  @Endpoint(path="recipes")
  public JsonArray getAllRecipes() {
    JsonArray result = new JsonArray();
    RecipeManager rm = server.getRecipeManager();
    for (Recipe<?> r : rm.getRecipes()) {
      result.add(recipeToJson(r));
    }
    return result;
  }

  @Endpoint(path="loot-table-ids")
  public String[] getLootTableIds() {
    return server.getLootTables().getIds().stream().map(Object::toString).toArray(String[]::new);
    // return loot.getIds().stream().map(loot::get).map(this::parseLootTable).collect(Collectors.toList());
  }

  // public JsonObject parseLootTable(LootTable t) {
  //   JsonObject result = new JsonObject();
  //   result.addProperty("id", t.getLootTableId().toString());
  //   JsonArray pools = new JsonArray();
  //   for (LootPool p : t.pools) {
  //     JsonObject pool = new JsonObject();
  //     pool.addProperty("name", pool.getName());
  //     pool.addProperty("rolls", pool.getRolls().getInt());
  //     pool.addProperty("extra_rolls", pool.getExtraRolls().getInt());
  //     pool.entries
  //     pools.add(pool);
  //   }
  //   result.add("pools", pools);
  //   return result;
  // }

  <T extends Recipe<?>> JsonObject recipeToJson(T recipe) {
    return RecipeConverter.getConverter(recipe).toJson(recipe);
  }
}
