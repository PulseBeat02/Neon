/*
 * MIT License
 *
 * Copyright (c) 2024 Brandon Li
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
  NullComponent<Sender> INIT_STATIC = info("neon.enable.static");
  NullComponent<Sender> INIT_CONFIG = info("neon.enable.config");
  NullComponent<Sender> INIT_CMDS = info("neon.enable.commands");
  NullComponent<Sender> INIT_BSTATS = info("neon.enable.stats");
  NullComponent<Sender> INIT_EVENTS = info("neon.enable.events");
  NullComponent<Sender> DISABLE_BROWSER = info("neon.disable.browser");
  NullComponent<Sender> DISABLE_CONFIG = info("neon.disable.config");
  NullComponent<Sender> DISABLE_STATIC = info("neon.disable.static");
  NullComponent<Sender> ERR_RES = error("neon.command.browser.configure.error.resolution");
  NullComponent<Sender> ERR_ENTITY = error("neon.command.browser.configure.error.entity");
  NullComponent<Sender> ERR_SENDER = error("neon.command.error.sender");
  NullComponent<Sender> ERR_URL = error("neon.command.browser.error.url");
  NullComponent<Sender> ERR_BROWSER = error("neon.command.browser.error.browser");
  NullComponent<Sender> ERR_LOC = error("neon.command.browser.error.location");
  NullComponent<Sender> INFO_DESTROY_BROWSER = info("neon.command.browser.info.destroy");
  NullComponent<Sender> INFO_SELENIUM = info("neon.command.browser.info.install");
  UniComponent<Sender, String> INIT_HANDLER = info("neon.enable.packet-handler", null);
  UniComponent<Sender, String> ERR_DITHER =
      error("neon.command.browser.configure.error.dither", null);
  UniComponent<Sender, String> INFO_BROWSER_LOAD = info("neon.command.browser.info.load", null);
  TriComponent<Sender, String, String, String> INFO_BROWSER_MAP =
      info("neon.command.browser.configure.info.map", null, null, null);
  TriComponent<Sender, String, String, String> INFO_BROWSER_ENTITY =
      info("neon.command.browser.configure.info.entity", null, null, null);
}
