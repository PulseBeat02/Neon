package io.github.pulsebeat02.neon.locale;

import static io.github.pulsebeat02.neon.locale.LocaleParent.*;

public interface Locale extends LocaleParent {

  NullComponent<Sender> INVALID_RESOLUTION =
      error("neon.command.browser.configure.invalid-resolution");
  NullComponent<Sender> INVALID_ENTITY_SELECTION =
      error("neon.command.browser.configure.invalid-entity-selection");
  NullComponent<Sender> INVALID_SENDER = error("neon.command.invalid-sender");
  NullComponent<Sender> INVALID_HOMEPAGE_URL = error("neon.command.browser.invalid-homepage-url");
  NullComponent<Sender> INVALID_BROWSER_SETTING = error("neon.command.browser.invalid-setting");
  UniComponent<Sender, String> INVALID_DITHER_ALGORITHM =
      error("neon.command.browser.configure.invalid-dither", null);
  UniComponent<Sender, String> SET_HOMEPAGE_URL = info("neon.command.browser.homepage-url", null);

  TriComponent<Sender, String, String, String> CONFIGURE_BROWSER_MAP =
      info("neon.command.browser.configure.map", null, null, null);

  TriComponent<Sender, String, String, String> CONFIGURE_BROWSER_ENTITY =
      info("neon.command.browser.configure.entity", null, null, null);
}
