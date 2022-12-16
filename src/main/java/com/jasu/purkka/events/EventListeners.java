package com.jasu.purkka.events;

import java.util.List;
import java.util.stream.Collectors;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.ASMEventHandler;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.IEventListener;
import net.minecraftforge.eventbus.api.EventListenerHelper;
import org.objectweb.asm.Type;

public class EventListeners {
  public static int forgeBusId() throws NoSuchFieldException, IllegalAccessException  {
    return (int)Utils.readPrivate(MinecraftForge.EVENT_BUS, "busID");
  }

  public static List<ListenerInfo> listenerInfos(String event) throws Exception {
    return listenerInfos(Utils.getClass(event));
  }

  public static List<ListenerInfo> listenerInfos(Class<?> event) throws Exception {
    return List.of(EventListenerHelper.getListenerList(event).getListeners(forgeBusId()))
      .stream().map(EventListeners::toListenerInfo)
      .collect(Collectors.toList());
  }

  public static ListenerInfo toListenerInfo(IEventListener listener) {
    FnInfo fnInfo = null;
    if (listener instanceof ASMEventHandler) {
      ASMEventHandler alistener = (ASMEventHandler)listener;
      String name = alistener.toString().substring(5);
      String clsName;
      int endIdx = 0;
      boolean isStatic = name.startsWith("class ");
      if (isStatic) {
        endIdx = name.indexOf(' ', 6);
        clsName = name.substring(6, endIdx);
      } else {
        endIdx = name.indexOf(' ');
        int atIdx = name.indexOf('@');
        atIdx = atIdx < 0 ? endIdx : atIdx;
        clsName = atIdx < 0 ? name : name.substring(0, atIdx);
      }
      int parenIdx = name.indexOf('(', endIdx);
      String fnName = name.substring(endIdx + 1, parenIdx);
      String descriptor = name.substring(parenIdx);
      String returnType = Type.getReturnType(descriptor).getClassName();
      List<String> args = List.of(Type.getArgumentTypes(descriptor)).stream().map(Type::getClassName).collect(Collectors.toList());

      fnInfo = new FnInfo(isStatic, clsName, fnName, returnType, args);
    }
    return new ListenerInfo(listener.getClass().toString(), listener.toString(), fnInfo);
  }

  public record FnInfo(boolean is_static, String cls, String method, String return_type, List<String> param_types) { }
  public record ListenerInfo(String type, String string, FnInfo method) { }
}
