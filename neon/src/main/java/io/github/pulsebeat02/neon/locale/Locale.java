/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.neon.locale;

import static io.github.pulsebeat02.neon.locale.LocaleParent.*;
import static io.github.pulsebeat02.neon.locale.LocaleParent.info;

public interface Locale extends LocaleParent {
  NullComponent<Sender> INVALID_RESOLUTION =
      error("neon.command.browser.configure.invalid-resolution");
  NullComponent<Sender> INVALID_ENTITY_SELECTION =
      error("neon.command.browser.configure.invalid-entity-selection");
  NullComponent<Sender> INVALID_SENDER = error("neon.command.invalid-sender");
  NullComponent<Sender> INVALID_HOMEPAGE_URL = error("neon.command.browser.invalid-homepage-url");
  NullComponent<Sender> INVALID_BROWSER_SETTING = error("neon.command.browser.invalid-setting");
  NullComponent<Sender> INVALID_LOCATION = error("neon.command.browser.invalid-location");
  NullComponent<Sender> DESTROY_BROWSER = info("neon.command.browser.destroy");
  UniComponent<Sender, String> PACKET_HANDLER = info("neon.packethandler", null);
  UniComponent<Sender, String> INVALID_DITHER_ALGORITHM =
      error("neon.command.browser.configure.invalid-dither", null);
  UniComponent<Sender, String> LOAD_BROWSER = info("neon.command.browser.load", null);
  TriComponent<Sender, String, String, String> CONFIGURE_BROWSER_MAP =
      info("neon.command.browser.configure.map", null, null, null);
  TriComponent<Sender, String, String, String> CONFIGURE_BROWSER_ENTITY =
      info("neon.command.browser.configure.entity", null, null, null);
}
