package com.jasu.purkka;

import java.util.List;
import com.jasu.purkka.common.Config;
import com.jasu.purkka.common.PurkkaInstance;
import com.jasu.purkka.server.ServerApi;
import com.jasu.purkka.client.ClientApi;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Purkka.MODID)
public class Purkka
{
  public static final String MODID = "purkka";
  public static final Logger LOGGER = LogUtils.getLogger();

  public static final Config SERVER_CONFIG = new Config(8010, List.of(), List.of(), List.of());
  public static final Config CLIENT_CONFIG = new Config(8011,
                                                        List.of("net.minecraftforge.client.event.RenderGuiEvent.Pre",
                                                                "net.minecraftforge.client.event.RenderGuiEvent.Post",
                                                                "net.minecraftforge.client.event.RenderGuiOverlayEvent.Pre",
                                                                "net.minecraftforge.client.event.RenderGuiOverlayEvent.Post"),
                                                  List.of(), List.of());

  public static PurkkaInstance CLIENT_INSTANCE = new PurkkaInstance(CLIENT_CONFIG, ClientApi::new);
  public static PurkkaInstance SERVER_INSTANCE = new PurkkaInstance(SERVER_CONFIG, ServerApi::new);

  public Purkka() {
    FMLJavaModLoadingContext.get().getModEventBus().addListener(Purkka::onClientSetup);
    ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG.spec);
    ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_CONFIG.spec);
    MinecraftForge.EVENT_BUS.register(this);
  }

  public static void onClientSetup(FMLClientSetupEvent event) {
    CLIENT_INSTANCE.start(LOGGER);
  }

  @SubscribeEvent
  public void onServerStarting(final ServerStartingEvent event) {
    SERVER_INSTANCE.start(LOGGER);
  }

  @SubscribeEvent
  public void onGameClose(final GameShuttingDownEvent event) {
    SERVER_INSTANCE.stop();
    CLIENT_INSTANCE.stop();
  }
}
