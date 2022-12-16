package com.jasu.purkka.events;
import net.minecraftforge.eventbus.ASMEventHandler;
import net.minecraftforge.eventbus.api.IEventListener;
import net.minecraftforge.eventbus.api.EventPriority;

public class Matcher {
  String startsWithStatic;
  String startsWithNonStatic;
  String methodName = null;

  public Matcher(String className) {
    className = Utils.normalizeNestedClassName(className);
    startsWithStatic = "ASM: class " + className + " ";
    startsWithNonStatic = "ASM: " + className + "@";
  }

  public Matcher(String className, String methodName) {
    className = Utils.normalizeNestedClassName(className);
    startsWithStatic = "ASM: class " + className + " ";
    startsWithNonStatic = "ASM: " + className + "@";
    this.methodName = methodName;
  }

  public static Matcher makeWithMethod(String str) {
    int clsEnd = str.lastIndexOf('.');
    return new Matcher(str.substring(clsEnd + 1), Utils.normalizeNestedClassName(str.substring(0, clsEnd)));
  }

  public static Matcher makeWithoutMethod(String str) {
    return new Matcher(Utils.normalizeNestedClassName(str));
  }

  public boolean match(IEventListener eh) {
    if (!(eh instanceof ASMEventHandler)) {
      return false;
    }
    String str = eh.toString();
    if (str.startsWith(startsWithStatic)) {
      return true;
    } else if (!str.startsWith(startsWithNonStatic)) {
      return false;
    }
    if (methodName == null) {
      return true;
    }
    int methodBegin = str.indexOf(' ', 5) + 1;
    int methodEnd = str.indexOf('(', methodBegin);
    return str.substring(methodBegin, methodEnd) == methodName;
  }
}
