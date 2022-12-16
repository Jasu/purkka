package com.jasu.purkka.common;

import org.slf4j.Logger;
import java.util.function.Supplier;
import com.jasu.purkka.http.HttpServer;
import com.jasu.purkka.events.EventUnsubscriber;

public final class PurkkaInstance {
  Config config;
  Supplier<Object> apiSupplier;
  HttpServer server;
  EventUnsubscriber unsubscriber;

  public PurkkaInstance(Config config, Supplier<Object> apiSupplier) {
    this.config = config;
    this.apiSupplier = apiSupplier;
  }

  public void start(Logger logger) {
    if (this.config.httpServer.enabled.get()) {
      server = new HttpServer(config.httpServer.port.get(), logger);
      server.tryAddHandler(new CommonApi());
      server.tryAddHandler(apiSupplier.get());
      try {
        server.start();
      } catch (Exception e) {
        logger.error("Starting server failed: " + e.toString());
        server = null;
      }
    }
  }

  public void stop() {
    if (server != null) {
      server.stop();
      server = null;
    }
  }
}
