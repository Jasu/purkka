package com.jasu.purkka.http;

import com.google.gson.Gson;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.QueryStringDecoder;

public final class HttpUtils
{
  public static Gson GSON = new Gson();
  public record Error(int code, String status, String message) { } 

  public static FullHttpResponse makeResponse(HttpResponseStatus status, ByteBuffer content, String contentType) {
    ByteBuf buf = Unpooled.copiedBuffer(content);
    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buf);
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
    HttpUtil.setContentLength(response, buf.readableBytes());
    return response;
  }

  public static FullHttpResponse makeResponse(HttpResponseStatus status, byte[] content, String contentType) {
    return makeResponse(status, ByteBuffer.wrap(content), contentType);
  }

  public static FullHttpResponse makeResponse(HttpResponseStatus status, String content, String contentType) {
    return makeResponse(status, StandardCharsets.UTF_8.encode(content), contentType);
  }

  public static FullHttpResponse makeJson(HttpResponseStatus status, Object content) {
    return makeResponse(status, GSON.toJson(content), "application/json");
  }

  public static FullHttpResponse makeJson200(Object content) {
    return makeJson(HttpResponseStatus.OK, content);
  }

  public static FullHttpResponse exceptionToResponse(Throwable exception) {
    if (!(exception instanceof HttpException)) {
      exception = HttpException.internalServerError(exception);
    }
    return makeJson(((HttpException)exception).status, ((HttpException)exception).json());
  }

  public static FullHttpResponse make200(String contentType, String content) {
    return makeResponse(HttpResponseStatus.OK, content, contentType);
  }

  public static FullHttpResponse make200(String contentType, ByteBuffer content) {
    return makeResponse(HttpResponseStatus.OK, content, contentType);
  }

  public static FullHttpResponse make200(String contentType, byte[] content) {
    return makeResponse(HttpResponseStatus.OK, content, contentType);
  }

  public static String[] splitPath(String path, boolean isEncoded) {
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    if (path.equals("")) {
      return new String[]{};
    } else {
      String[] split = path.split("/");
      if (!isEncoded) {
        return split;
      }
      return List.of(split).stream().map(QueryStringDecoder::decodeComponent).toArray(String[]::new);
    }
  }
}
