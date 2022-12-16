# Developer utility mod

Mod containing miscellaneous debugging helpers for modded Minecraft (on Forge).

  - Expose Minecraft registries over a JSON API, with a local HTTP server
    running in Minecraft
  - View event handlers installed on events in Forge event bus (including handlers
    registered with ASM binding)
  - Expose recipes for read access, in a simpleish format over JSON.
    Currently supports shaped and shapeless crafting recipes. Other crafting
    recipes might partially work, but only the primary ingredients and outputs
    would be shown, without metadata about the position of ingredients, and
    without any information on catalysts etc. Fluid recipes do not work at all.

## License

This mod is licensed under... no idea. It's probably in violation of all copyright and
trademark laws imaginable, like most Minecraft mods.

  - It depends on Forge MCP, which is LGPL with exceptions.
  - It makes runtime modifications to Minecraft which is all-rights-reserved and then some.
  - It makes runtime modifications to other Minecraft mods, which are all-rights-reserved.
  - It packages a part of Netty in its own JAR, and technically modifies it during shadow 
    JAR process (to avoid clashes with other mods packaging Netty). However, only
    `netty-codec-http` is included, so I will need to go through the Apache NOTICE.txt and
    figure out which subdependencies I am actually shipping to be able to include a correct
    NOTICE.txt. Additioally, I should distribute a copy of the license with the binary,
    but mod sites provide downloads for single JARs. Putting the license inside that would
    not make things noticably better.
