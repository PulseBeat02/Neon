package io.github.pulsebeat02.neon.browser;

import io.github.pulsebeat02.neon.Neon;
import io.github.pulsebeat02.neon.config.BrowserConfiguration;
import java.awt.Component;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefPdfPrintCallback;
import org.cef.callback.CefRunFileDialogCallback;
import org.cef.callback.CefStringVisitor;
import org.cef.handler.CefDialogHandler.FileDialogMode;
import org.cef.handler.CefRenderHandler;
import org.cef.handler.CefWindowHandler;
import org.cef.misc.CefPdfPrintSettings;
import org.cef.network.CefRequest;
import org.jetbrains.annotations.NotNull;

public final class MinecraftBrowser implements CefBrowser {

  private final @NotNull CefBrowser internalBrowser;
  private @NotNull final Neon neon;
  private @NotNull final CefClient client;

  private @NotNull final CefRenderHandler renderer;

  public MinecraftBrowser(@NotNull final Neon neon, @NotNull final BrowserSettings settings)
      throws UnsupportedPlatformException,
          CefInitializationException,
          IOException,
          InterruptedException {
    this.neon = neon;
    @NotNull final CefApp app = this.createApp();
    this.client = app.createClient();
    this.internalBrowser = this.createBrowser();
    this.renderer = new MinecraftBrowserRenderer(neon, settings);
    this.setFocus(true);
  }

  private @NotNull CefApp createApp()
      throws UnsupportedPlatformException,
          CefInitializationException,
          IOException,
          InterruptedException {
    final CefAppBuilder builder = new CefAppBuilder();
    builder.setProgressHandler(new CefProgressHandler(this.neon));
    builder.addJcefArgs("--disable-gpu");
    builder.addJcefArgs("--disable-gpu-compositing");
    return builder.build();
  }

  private @NotNull CefBrowser createBrowser() {
    final BrowserConfiguration configuration = this.neon.getConfiguration();
    final String url = configuration.getHomePageUrl();
    return this.client.createBrowser(url, true, true);
  }

  @Override
  public void createImmediately() {
    this.internalBrowser.createImmediately();
  }

  @Override
  public @NotNull Component getUIComponent() {
    return this.internalBrowser.getUIComponent();
  }

  @Override
  public @NotNull CefClient getClient() {
    return this.internalBrowser.getClient();
  }

  @Override
  public @NotNull CefRenderHandler getRenderHandler() {
    return this.renderer;
  }

  @Override
  public @NotNull CefWindowHandler getWindowHandler() {
    return this.internalBrowser.getWindowHandler();
  }

  @Override
  public boolean canGoBack() {
    return this.internalBrowser.canGoBack();
  }

  @Override
  public void goBack() {
    this.internalBrowser.goBack();
  }

  @Override
  public boolean canGoForward() {
    return this.internalBrowser.canGoForward();
  }

  @Override
  public void goForward() {
    this.internalBrowser.goForward();
  }

  @Override
  public boolean isLoading() {
    return this.internalBrowser.isLoading();
  }

  @Override
  public void reload() {
    this.internalBrowser.reload();
  }

  @Override
  public void reloadIgnoreCache() {
    this.internalBrowser.reloadIgnoreCache();
  }

  @Override
  public void stopLoad() {
    this.internalBrowser.stopLoad();
  }

  @Override
  public int getIdentifier() {
    return this.internalBrowser.getIdentifier();
  }

  @Override
  public @NotNull CefFrame getMainFrame() {
    return this.internalBrowser.getMainFrame();
  }

  @Override
  public @NotNull CefFrame getFocusedFrame() {
    return this.internalBrowser.getFocusedFrame();
  }

  @Override
  public @NotNull CefFrame getFrame(final long identifier) {
    return this.internalBrowser.getFrame(identifier);
  }

  @Override
  public @NotNull CefFrame getFrame(final String name) {
    return this.internalBrowser.getFrame(name);
  }

  @Override
  public @NotNull Vector<Long> getFrameIdentifiers() {
    return this.internalBrowser.getFrameIdentifiers();
  }

  @Override
  public @NotNull Vector<String> getFrameNames() {
    return this.internalBrowser.getFrameNames();
  }

  @Override
  public int getFrameCount() {
    return this.internalBrowser.getFrameCount();
  }

  @Override
  public boolean isPopup() {
    return this.internalBrowser.isPopup();
  }

  @Override
  public boolean hasDocument() {
    return this.internalBrowser.hasDocument();
  }

  @Override
  public void viewSource() {
    this.internalBrowser.viewSource();
  }

  @Override
  public void getSource(@NotNull final CefStringVisitor visitor) {
    this.internalBrowser.getSource(visitor);
  }

  @Override
  public void getText(@NotNull final CefStringVisitor visitor) {
    this.internalBrowser.getText(visitor);
  }

  @Override
  public void loadRequest(@NotNull final CefRequest request) {
    this.internalBrowser.loadRequest(request);
  }

  @Override
  public void loadURL(@NotNull final String url) {
    this.internalBrowser.loadURL(url);
  }

  @Override
  public void executeJavaScript(
      @NotNull final String code, @NotNull final String url, final int line) {
    this.internalBrowser.executeJavaScript(code, url, line);
  }

  @Override
  public @NotNull String getURL() {
    return this.internalBrowser.getURL();
  }

  @Override
  public void close(final boolean force) {
    this.internalBrowser.close(force);
  }

  @Override
  public void setCloseAllowed() {
    this.internalBrowser.setCloseAllowed();
  }

  @Override
  public boolean doClose() {
    return this.internalBrowser.doClose();
  }

  @Override
  public void onBeforeClose() {
    this.internalBrowser.onBeforeClose();
  }

  @Override
  public void setFocus(final boolean enable) {
    this.internalBrowser.setFocus(enable);
  }

  @Override
  public void setWindowVisibility(final boolean visible) {
    this.internalBrowser.setWindowVisibility(visible);
  }

  @Override
  public double getZoomLevel() {
    return this.internalBrowser.getZoomLevel();
  }

  @Override
  public void setZoomLevel(final double zoomLevel) {
    this.internalBrowser.setZoomLevel(zoomLevel);
  }

  @Override
  public void runFileDialog(
      @NotNull final FileDialogMode mode,
      @NotNull final String title,
      @NotNull final String defaultFilePath,
      @NotNull final Vector<String> acceptFilters,
      final int selectedAcceptFilter,
      @NotNull final CefRunFileDialogCallback callback) {
    this.internalBrowser.runFileDialog(
        mode, title, defaultFilePath, acceptFilters, selectedAcceptFilter, callback);
  }

  @Override
  public void startDownload(@NotNull final String url) {
    this.internalBrowser.startDownload(url);
  }

  @Override
  public void print() {
    this.internalBrowser.print();
  }

  @Override
  public void printToPDF(
      @NotNull final String path,
      @NotNull final CefPdfPrintSettings settings,
      @NotNull final CefPdfPrintCallback callback) {
    this.internalBrowser.printToPDF(path, settings, callback);
  }

  @Override
  public void find(
      @NotNull final String searchText,
      final boolean forward,
      final boolean matchCase,
      final boolean findNext) {
    this.internalBrowser.find(searchText, forward, matchCase, findNext);
  }

  @Override
  public void stopFinding(final boolean clearSelection) {
    this.internalBrowser.stopFinding(clearSelection);
  }

  @Override
  public CefBrowser getDevTools() {
    return this.internalBrowser.getDevTools();
  }

  @Override
  public CefBrowser getDevTools(@NotNull final Point inspectAt) {
    return this.internalBrowser.getDevTools(inspectAt);
  }

  @Override
  public void replaceMisspelling(@NotNull final String word) {
    this.internalBrowser.replaceMisspelling(word);
  }

  @Override
  public @NotNull CompletableFuture<BufferedImage> createScreenshot(
      final boolean nativeResolution) {
    return this.internalBrowser.createScreenshot(nativeResolution);
  }
}
