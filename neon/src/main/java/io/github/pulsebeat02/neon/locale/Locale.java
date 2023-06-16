package io.github.pulsebeat02.neon.locale;

import static io.github.pulsebeat02.neon.locale.LocaleParent.*;

public interface Locale extends LocaleParent {

  NullComponent<Sender> INVALID_RESOLUTION = error("neon.command.browser.invalid-resolution");
  NullComponent<Sender> INVALID_SENDER = error("neon.command.invalid-sender");
  UniComponent<Sender, String> INVALID_DITHER_ALGORITHM = error("neon.command.browser.invalid-dither", null);
  UniComponent<Sender, String> SET_DITHER_ALGORITHM = info("neon.command.browser.dither", null);
  BiComponent<Sender, Integer, Integer> SET_RESOLUTION = info("neon.command.browser.resolution", null, null);
  BiComponent<Sender, Integer, Integer> SET_BLOCK_DIMENSION = info("neon.command.browser.block-dimension", null, null);
}
