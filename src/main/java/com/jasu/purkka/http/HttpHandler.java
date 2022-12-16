package com.jasu.purkka.http;

import java.lang.reflect.*;
import java.util.List;
import java.util.ArrayList;
import io.netty.handler.codec.http.FullHttpResponse;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class HttpHandler<Result>
{
  String method;
  String pathString;
  String[] path;
  MethodHandle fn;

  public HttpHandler(String method, String path, MethodHandle fn) {
    this.method = method;
    this.path = HttpUtils.splitPath(path, false);
    this.pathString = path;
    this.fn = fn;
  }

  public int match(String method, String[] path) {
    int l = path.length;
    if (l != this.path.length) {
      return 0;
    }
    for (int i = 0; i < l; ++i) {
      String part = this.path[i];
      if (part.equals("%s")) {
      } else if (part.equals("%d")) {
        if (!part.matches("-?[0-9]+")) {
          return 0;
        }
      } else if (!part.equals(path[i])) {
        return 0;
      }
    }
    if (!method.equals(this.method)) {
      return 1;
    }
    return 2;
  }

  public FullHttpResponse handle(String[] path) throws Throwable {
    List<Object> params = new ArrayList();
    for (int i = 0; i < this.path.length; ++i) {
      if (this.path[i].equals("%s")) {
        params.add(path[i]);
      } else if (this.path[i].equals("%d")) {
        params.add(Integer.parseInt(path[i]));
      }
    }
    return (FullHttpResponse)fn.invokeExact(params.toArray());
  }

  public static List<HttpHandler> parse(Object handlers) throws Exception {
    List<HttpHandler> result = new ArrayList();
    for (Method m : handlers.getClass().getMethods()) {
      Endpoint a = m.getAnnotation(Endpoint.class);
      if (a == null) {
        continue;
      }
      MethodHandle handle = MethodHandles.lookup().unreflect(m).bindTo(handlers).asSpreader(Object[].class, m.getParameterCount());
      MethodHandle resultWrapper;
      if (a.contentType().equals("")) {
        resultWrapper = MethodHandles.lookup().findStatic(HttpUtils.class, "makeJson200", MethodType.methodType(FullHttpResponse.class, Object.class));
        if (m.getReturnType() != Object.class) {
          resultWrapper = MethodHandles.explicitCastArguments(resultWrapper, MethodType.methodType(FullHttpResponse.class, m.getReturnType()));
        }
      } else {
        resultWrapper = MethodHandles.lookup().findStatic(HttpUtils.class, "make200", MethodType.methodType(FullHttpResponse.class, m.getReturnType()));
      }
      result.add(new HttpHandler(a.method(), a.path(), MethodHandles.filterReturnValue(handle, resultWrapper)));
    }
    return result;
  }
}
