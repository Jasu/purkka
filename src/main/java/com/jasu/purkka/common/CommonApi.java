package com.jasu.purkka.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.jasu.purkka.http.Endpoint;
import com.jasu.purkka.http.HttpException;
import com.jasu.purkka.events.EventListeners;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.RegistryManager;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.IReverseTag;
import net.minecraftforge.registries.tags.ITagManager;
import net.minecraftforge.registries.tags.ITag;

public final class CommonApi {
  @Endpoint(path="registry")
  public List<String> registry() {
    List<String> result = new ArrayList<String>();
    for (ResourceLocation loc : RegistryManager.ACTIVE.registries.keySet()) {
      result.add(loc.toString());
    }
    return result;
  }
  
  @Endpoint(path="registry/%s")
  public List<String> registryItems(String name) throws HttpException {
    List<String> result = new ArrayList<String>();
    for (ResourceLocation loc : getRegistry(name).getKeys()) {
      result.add(loc.toString());
    }
    return result;
  }

  @Endpoint(path="registry/%s/tags")
  public List<String> registryTags(String name) throws HttpException {
    ITagManager<?> tm = getRegistry(name).tags();
    if (tm == null) {
      return new ArrayList();
    }
    return tm.getTagNames().map(TagKey<?>::location).map(Object::toString).collect(Collectors.toList());
  }

  @Endpoint(path="registry/%s/tags/%s")
  public List<String> registryTagContents(String name, String tagName) throws HttpException {
    return getTagContents(getRegistry(name), parseResourceLocation(tagName));
  }

  @Endpoint(path="event/%s/listeners")
  public List<EventListeners.ListenerInfo> eventListeners(String event) throws Exception {
    try {
    return EventListeners.listenerInfos(event);
    } catch (ClassNotFoundException e) {
      throw HttpException.notFound(e.toString());
    }
  }

  private static ResourceLocation parseResourceLocation(String name) throws HttpException {
    ResourceLocation result = ResourceLocation.tryParse(name);
    if (result == null) {
      throw HttpException.badRequest("Resource location \"" + name + "\" is invalid");
    }
    return result;
  }

  public <T> List<String> getTagContents(ForgeRegistry<T> registry, ResourceLocation tagName) throws HttpException {
    TagKey<T> key = registry.tags().createTagKey(tagName);
    ITag<T> tag = registry.tags().getTag(key);
    return tag.stream().map(registry::getKey).map(Object::toString).collect(Collectors.toList());
  }

  private static <T> ForgeRegistry<T> getRegistry(String name) throws HttpException {
    ForgeRegistry<T> result = RegistryManager.ACTIVE.getRegistry(parseResourceLocation(name));
    if (result == null) {
      throw HttpException.notFound("Registry \"" + name + "\" does not exist");
    }
    return result;
  }

  @Endpoint(path="items")
  public Map<String, ItemInfo> allItems() throws Exception {
    ForgeRegistry<Item> registry = getRegistry("item");
    return registryEntries(registry)
      .map(v -> new ItemInfo(v.stringKey(), v.value.getDescriptionId(), v.stringTags(), getFoodInfo(v.value)))
      .collect(Collectors.toMap(ItemInfo::getKey, Function.identity()));
  }

  public FoodInfo getFoodInfo(Item i) {
    FoodProperties fp = i.getFoodProperties();
    if (fp == null) {
      return null;
    }
    return new FoodInfo(fp.isFastFood(), fp.isMeat(), fp.canAlwaysEat(), !fp.getEffects().isEmpty(), fp.getNutrition(), fp.getSaturationModifier());
  }

  public record FoodInfo(boolean fastFood, boolean meat, boolean canAlwaysEat, boolean hasEffects, int nutrition, float saturation) { }

  public record ItemInfo(String key, String descriptionId, List<String> tags, FoodInfo foodInfo) {
    String getKey() { return key; }
  };

  public final class Resource<T> {
    public ResourceLocation key;
    public T value;
    public List<TagKey<T>> tags;

    public String stringKey() {
      return key.toString();
    }
    public List<String> stringTags() {
      return tags.stream().map(TagKey<T>::location).map(Object::toString).collect(Collectors.toList());
    }

    public Resource(ResourceLocation key, T value, List<TagKey<T>> tags) {
      this.key = key;
      this.value = value;
      this.tags = tags;
    }
  }

  public <T> Stream<Resource<T>> registryEntries(ForgeRegistry<T> reg) throws HttpException {
    ITagManager<T> tm = reg.tags();
    return reg.getEntries().stream().map(e -> new Resource<T>(e.getKey().location(),
                                                              e.getValue(),
                                                              getTags(tm, e.getValue())));
  }

  public <T> List<TagKey<T>> getTags(ITagManager<T> tags, T value) {
    if (tags == null) { return List.of(); }
    IReverseTag<T> rtag = tags.getReverseTag(value).orElse(null);
    if (rtag == null) { return List.of(); }
    return rtag.getTagKeys().collect(Collectors.toList());
  }

}
