package com.jasu.purkka.events;

import java.lang.reflect.Field;

public class Utils {
  static <T> T readPrivate(Object o, String field) throws NoSuchFieldException, IllegalAccessException {
    Field f = o.getClass().getDeclaredField(field);
    f.setAccessible(true);
    return (T)f.get(o);
  }

  public static String normalizeNestedClassName(String name) {
    return name.replaceAll("([A-Z][^.]*)[.]", "$1\\$");
  }
  public static Class<?> getClass(String name) throws ClassNotFoundException {
    return Class.forName(normalizeNestedClassName(name));
  }
  public static Class<?> getClassUnsafe(String name) {
    try {
      return getClass(name);
    } catch (Exception e) {
      return null;
    }
  }
}
