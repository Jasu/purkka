package com.jasu.purkka.http;

import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpException extends Exception {
  public HttpResponseStatus status;
  public String info;

  public HttpException(HttpResponseStatus status) {
    super(status.toString());
    this.status = status;
  }

  public HttpException(HttpResponseStatus status, String info) {
    super(info == null ? status.toString() : status.toString() + " " + info);
    this.status = status;
    this.info = info;
  }

  public record JsonObject(int code, String status, String info) { }

  public JsonObject json() {
    return new JsonObject(status.code(), status.toString(), info);
  }

  public static HttpException badRequest(String message) {
    return new HttpException(HttpResponseStatus.BAD_REQUEST, message);
  }
  public static HttpException badRequest() {
    return badRequest(null);
  }
  public static HttpException notFound(String message) {
    return new HttpException(HttpResponseStatus.NOT_FOUND, message);
  }
  public static HttpException notFound() {
    return notFound(null);
  }
  public static HttpException methodNotAllowed(String message) {
    return new HttpException(HttpResponseStatus.METHOD_NOT_ALLOWED, message);
  }
  public static HttpException methodNotAllowed() {
    return methodNotAllowed(null);
  }
  public static HttpException internalServerError(String message) {
    return new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR, message);
  }
  public static HttpException internalServerError(Throwable exc) {
    return internalServerError(exc.toString());
  }
  public static HttpException internalServerError() {
    return internalServerError((String)null);
  }
}
