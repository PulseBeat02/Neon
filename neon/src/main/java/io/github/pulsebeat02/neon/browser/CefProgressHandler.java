package io.github.pulsebeat02.neon.browser;

import io.github.pulsebeat02.neon.Neon;
import me.friwi.jcefmaven.EnumProgress;
import me.friwi.jcefmaven.IProgressHandler;
import org.jetbrains.annotations.NotNull;

public final class CefProgressHandler implements IProgressHandler {

  private final Neon neon;

  public CefProgressHandler(@NotNull final Neon neon) {
    this.neon = neon;
  }

  @Override
  public void handleProgress(final EnumProgress state, final float percent) {
    this.neon.logConsole(String.format("CEF Download Progress: %s (%.2f%%)", state, percent));
  }
}
