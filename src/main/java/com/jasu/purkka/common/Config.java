package com.jasu.purkka.common;

import com.jasu.purkka.events.Matcher;
import com.jasu.purkka.events.Utils;
import java.util.List;
import java.util.stream.Stream;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.Event;

public final class Config {
  public final class HttpServerConfig {
    public HttpServerConfig(ForgeConfigSpec.Builder builder, int defaultPort) {
      builder.comment("Debug HTTP server configuration").push("httpServer");
      enabled = builder.define("serverEnabled", true);
      port = builder.defineInRange("serverPort", defaultPort, 1, 65535);
      builder.pop();
    }

    public final ForgeConfigSpec.BooleanValue enabled;
    public final ForgeConfigSpec.IntValue port;
  }

  public final class EventBlacklistConfig {
    public EventBlacklistConfig(ForgeConfigSpec.Builder builder,
                                List<String> defaultEventsScanned,
                                List<String> defaultBlacklist,
                                List<String> defaultClassBlacklist) {
      builder.comment("Mod event handler blacklisting configuration").push("eventHandlerBlacklist");
      confEventsScanned = builder
        .comment("These events will be checked for blacklisted values and cleared of those. The values must be names of classes deriving from Event.")
        .defineListAllowEmpty(List.of("eventsScanned"), () -> defaultEventsScanned, o -> true);
      confEventHandlerBlacklist = builder
        .comment("Methods listed here will be removed from events in eventsScanned.")
        .defineListAllowEmpty(List.of("eventsHandlerBlacklist"), () -> defaultBlacklist, o -> true);
      confEventHandlerClassBlacklist = builder
        .comment("Methods in classes listed here will be removed from events in eventsScanned.")
        .defineListAllowEmpty(List.of("eventHandlerClassBlacklist"), () -> defaultClassBlacklist, o -> true);
      builder.pop();
    }

    public boolean isEnabled() {
      return (confEventsScanned.get().isEmpty()
              || (confEventHandlerBlacklist.get().isEmpty()
                  && confEventHandlerClassBlacklist.get().isEmpty()));
    }

    public Stream<Class<?>> eventsScanned() {
      return confEventsScanned.get().stream().map(Object::toString).map(Utils::getClassUnsafe);
    }

    public Matcher[] matchers() {
      return Stream.concat(confEventHandlerBlacklist.get().stream().map(Object::toString).map(Matcher::makeWithMethod),
                    confEventHandlerClassBlacklist.get().stream().map(Object::toString).map(Matcher::makeWithoutMethod))
        .toArray(Matcher[]::new);
    }

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> confEventsScanned;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> confEventHandlerBlacklist;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> confEventHandlerClassBlacklist;
  }

  public HttpServerConfig httpServer;
  public EventBlacklistConfig eventBlacklist;
  public ForgeConfigSpec spec;

  public Config(int defaultPort,
                List<String> defaultEventsScanned,
                List<String> defaultBlacklist,
                List<String> defaultClassBlacklist)
  {
    ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    httpServer = new HttpServerConfig(builder, defaultPort);
    eventBlacklist = new EventBlacklistConfig(builder, defaultEventsScanned, defaultBlacklist, defaultClassBlacklist);
    spec = builder.build();
  }
}
