package com.jasu.purkka.http;

import java.util.ArrayList;
import java.util.List;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;

public class HttpServer
{
  private int port;
  private Logger logger;
  private NioEventLoopGroup selectGroup;
  private NioEventLoopGroup acceptGroup;
  private ArrayList<HttpHandler> handlers;
  private ChannelFuture channel;

  public HttpServer(int port, Logger logger)
  {
    this.port = port;
    this.logger = logger;
    selectGroup = new NioEventLoopGroup();
    acceptGroup = new NioEventLoopGroup();
    handlers = new ArrayList();
  }

  public void start() throws Exception
  {
    ServerBootstrap b = new ServerBootstrap();
    b.group(selectGroup, acceptGroup)
      .channel(NioServerSocketChannel.class)
      .childHandler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline p = ch.pipeline();
            p.addLast("request-decoder", new HttpRequestDecoder());
            p.addLast("http-aggregator", new HttpObjectAggregator(1024*1024));
            p.addLast("response-encoder", new HttpResponseEncoder());
            p.addLast("handler", new SimpleChannelInboundHandler<FullHttpRequest>() {
                @Override
                protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
                  try {
                    String method = request.method().name();
                    logger.info(method + " " + request.uri());
                    QueryStringDecoder dec = new QueryStringDecoder(request.uri());
                    String[] path = HttpUtils.splitPath(dec.rawPath(), true);
                    if (path.length == 0) {
                        ctx.writeAndFlush(HttpUtils.makeJson200(getEndpoints()));
                        return;
                    }

                    int matchResult = 0;
                    for (HttpHandler h : handlers) {
                        matchResult = Integer.max(matchResult, h.match(method, path));
                        if (matchResult == 2) {
                        ctx.writeAndFlush(h.handle(path));
                        return;
                        }
                    }
                    if (matchResult == 1) {
                        throw HttpException.methodNotAllowed(method + " " + dec.rawPath());
                    }
                    throw HttpException.notFound(dec.rawPath());
                  } catch (Throwable e) {
                    ctx.writeAndFlush(HttpUtils.exceptionToResponse(e));
                    logger.info("Exception when handling request: " + e.toString());
                  }
                }
              });
          }
        });
    b.option(ChannelOption.SO_BACKLOG, 128);
    b.childOption(ChannelOption.SO_KEEPALIVE, true);
    channel = b.bind(port).sync();
  }
  public void stop()
  {
    try
    {
      selectGroup.shutdownGracefully();
      selectGroup = null;
      acceptGroup.shutdownGracefully();
      acceptGroup = null;
      channel.channel().closeFuture().sync();
      channel = null;
    }
    catch (InterruptedException ignored)
    {
    }
  }

  public void tryAddHandler(Object handler) {
    try {
      addHandler(handler);
    } catch (Exception e) {
      logger.error(e.toString());
    }
  }
  public void addHandler(Object handler) throws Exception {
    this.handlers.addAll(HttpHandler.parse(handler));
  }

  public class EndpointsResponse {
    public record Endpoint(String method, String path) { }
    public List<Endpoint> endpoints;
    public EndpointsResponse(List<HttpHandler> handlers) {
      endpoints = new ArrayList();
      for (HttpHandler handler : handlers) {
        endpoints.add(new Endpoint(handler.method, handler.pathString));
      }
    }
  }

  public EndpointsResponse getEndpoints() {
    return new EndpointsResponse(handlers);
  }
}
