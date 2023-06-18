// Copyright (c) 2013 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

package org.cef.browser;

import org.cef.CefClient;
import org.cef.browser.CefRequestContext;
import org.cef.callback.CefDragData;
import org.cef.callback.CefNativeAdapter;
import org.cef.callback.CefPdfPrintCallback;
import org.cef.callback.CefRunFileDialogCallback;
import org.cef.callback.CefStringVisitor;
import org.cef.handler.CefClientHandler;
import org.cef.handler.CefDialogHandler.FileDialogMode;
import org.cef.handler.CefRenderHandler;
import org.cef.handler.CefWindowHandler;
import org.cef.misc.CefPdfPrintSettings;
import org.cef.network.CefRequest;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.SwingUtilities;

/**
 * This class represents all methods which are connected to the
 * native counterpart CEF.
 * The visibility of this class is "package". To create a new
 * CefBrowser instance, please use CefBrowserFactory.
 */
public abstract class CefBrowser_N extends CefNativeAdapter implements CefBrowser {
    private volatile boolean isPending_ = false;
    private final CefClient client_;
    private final String url_;
    private final CefRequestContext request_context_;
    private volatile CefBrowser_N parent_ = null;
    private volatile Point inspectAt_ = null;
    private volatile CefBrowser_N devTools_ = null;
    private boolean closeAllowed_ = false;
    private volatile boolean isClosed_ = false;
    private volatile boolean isClosing_ = false;

    protected CefBrowser_N(final CefClient client, final String url, final CefRequestContext context,
                           final CefBrowser_N parent, final Point inspectAt) {
        this.client_ = client;
        this.url_ = url;
        this.request_context_ = context;
        this.parent_ = parent;
        this.inspectAt_ = inspectAt;
    }

    protected String getUrl() {
        return this.url_;
    }

    protected CefRequestContext getRequestContext() {
        return this.request_context_;
    }

    protected CefBrowser_N getParentBrowser() {
        return this.parent_;
    }

    protected Point getInspectAt() {
        return this.inspectAt_;
    }

    protected boolean isClosed() {
        return this.isClosed_;
    }

    @Override
    public CefClient getClient() {
        return this.client_;
    }

    @Override
    public CefRenderHandler getRenderHandler() {
        return null;
    }

    @Override
    public CefWindowHandler getWindowHandler() {
        return null;
    }

    @Override
    public synchronized void setCloseAllowed() {
        this.closeAllowed_ = true;
    }

    @Override
    public synchronized boolean doClose() {
        if (this.closeAllowed_) {
            // Allow the close to proceed.
            return false;
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Trigger close of the parent window.
                final Component parent = SwingUtilities.getRoot(CefBrowser_N.this.getUIComponent());
                if (parent != null) {
                    parent.dispatchEvent(
                            new WindowEvent((Window) parent, WindowEvent.WINDOW_CLOSING));
                }
            }
        });

        // Cancel the close.
        return true;
    }

    @Override
    public synchronized void onBeforeClose() {
        this.isClosed_ = true;
        if (this.request_context_ != null) {
            this.request_context_.dispose();
        }
        if (this.parent_ != null) {
            this.parent_.closeDevTools();
            this.parent_.devTools_ = null;
            this.parent_ = null;
        }
    }

    @Override
    public CefBrowser getDevTools() {
        return this.getDevTools(null);
    }

    @Override
    public synchronized CefBrowser getDevTools(final Point inspectAt) {
        if (this.devTools_ == null) {
            this.devTools_ = this.createDevToolsBrowser(this.client_, this.url_, this.request_context_, this, inspectAt);
        }
        return this.devTools_;
    }

    protected abstract CefBrowser_N createDevToolsBrowser(CefClient client, String url,
            CefRequestContext context, CefBrowser_N parent, Point inspectAt);

    /**
     * Create a new browser.
     */
    protected void createBrowser(final CefClientHandler clientHandler, final long windowHandle, final String url,
                                 final boolean osr, final boolean transparent, final Component canvas, final CefRequestContext context) {
        if (this.getNativeRef("CefBrowser") == 0 && !this.isPending_) {
            try {
                this.N_CreateBrowser(
                        clientHandler, windowHandle, url, osr, transparent, canvas, context);
            } catch (final UnsatisfiedLinkError err) {
                err.printStackTrace();
            }
        }
    }

    /**
     * Called async from the (native) main UI thread.
     */
    private void notifyBrowserCreated() {
        this.isPending_ = true;
    }

    /**
     * Create a new browser as dev tools
     */
    protected final void createDevTools(final CefBrowser_N parent, final CefClientHandler clientHandler,
                                        final long windowHandle, final boolean osr, final boolean transparent, final Component canvas,
                                        final Point inspectAt) {
        if (this.getNativeRef("CefBrowser") == 0 && !this.isPending_) {
            try {
                this.isPending_ = this.N_CreateDevTools(
                        parent, clientHandler, windowHandle, osr, transparent, canvas, inspectAt);
            } catch (final UnsatisfiedLinkError err) {
                err.printStackTrace();
            }
        }
    }

    /**
     * Returns the native window handle for the specified native surface handle.
     */
    protected final long getWindowHandle(final long surfaceHandle) {
        try {
            return this.N_GetWindowHandle(surfaceHandle);
        } catch (final UnsatisfiedLinkError err) {
            err.printStackTrace();
        }
        return 0;
    }

    @Override
    protected void finalize() throws Throwable {
        this.close(true);
        super.finalize();
    }

    @Override
    public boolean canGoBack() {
        try {
            return this.N_CanGoBack();
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
        return false;
    }

    @Override
    public void goBack() {
        try {
            this.N_GoBack();
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    @Override
    public boolean canGoForward() {
        try {
            return this.N_CanGoForward();
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
        return false;
    }

    @Override
    public void goForward() {
        try {
            this.N_GoForward();
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    @Override
    public boolean isLoading() {
        try {
            return this.N_IsLoading();
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
        return false;
    }

    @Override
    public void reload() {
        try {
            this.N_Reload();
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    @Override
    public void reloadIgnoreCache() {
        try {
            this.N_ReloadIgnoreCache();
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    @Override
    public void stopLoad() {
        try {
            this.N_StopLoad();
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    @Override
    public int getIdentifier() {
        try {
            return this.N_GetIdentifier();
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
            return -1;
        }
    }

    @Override
    public CefFrame getMainFrame() {
        try {
            return this.N_GetMainFrame();
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
            return null;
        }
    }

    @Override
    public CefFrame getFocusedFrame() {
        try {
            return this.N_GetFocusedFrame();
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
            return null;
        }
    }

    @Override
    public CefFrame getFrame(final long identifier) {
        try {
            return this.N_GetFrame(identifier);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
            return null;
        }
    }

    @Override
    public CefFrame getFrame(final String name) {
        try {
            return this.N_GetFrame2(name);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
            return null;
        }
    }

    @Override
    public Vector<Long> getFrameIdentifiers() {
        try {
            return this.N_GetFrameIdentifiers();
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
            return null;
        }
    }

    @Override
    public Vector<String> getFrameNames() {
        try {
            return this.N_GetFrameNames();
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
            return null;
        }
    }

    @Override
    public int getFrameCount() {
        try {
            return this.N_GetFrameCount();
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
            return -1;
        }
    }

    @Override
    public boolean isPopup() {
        try {
            return this.N_IsPopup();
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean hasDocument() {
        try {
            return this.N_HasDocument();
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
        return false;
    }

    @Override
    public void viewSource() {
        try {
            this.N_ViewSource();
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    @Override
    public void getSource(final CefStringVisitor visitor) {
        try {
            this.N_GetSource(visitor);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    @Override
    public void getText(final CefStringVisitor visitor) {
        try {
            this.N_GetText(visitor);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    @Override
    public void loadRequest(final CefRequest request) {
        try {
            this.N_LoadRequest(request);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    @Override
    public void loadURL(final String url) {
        try {
            this.N_LoadURL(url);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    @Override
    public void executeJavaScript(final String code, final String url, final int line) {
        try {
            this.N_ExecuteJavaScript(code, url, line);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    @Override
    public String getURL() {
        try {
            return this.N_GetURL();
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
        return "";
    }

    @Override
    public void close(final boolean force) {
        if (this.isClosing_ || this.isClosed_) {
            return;
        }
        if (force) {
            this.isClosing_ = true;
        }

        try {
            this.N_Close(force);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    @Override
    public void setFocus(final boolean enable) {
        try {
            this.N_SetFocus(enable);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    @Override
    public void setWindowVisibility(final boolean visible) {
        try {
            this.N_SetWindowVisibility(visible);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    @Override
    public double getZoomLevel() {
        try {
            return this.N_GetZoomLevel();
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public void setZoomLevel(final double zoomLevel) {
        try {
            this.N_SetZoomLevel(zoomLevel);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    @Override
    public void runFileDialog(final FileDialogMode mode, final String title, final String defaultFilePath,
                              final Vector<String> acceptFilters, final int selectedAcceptFilter,
                              final CefRunFileDialogCallback callback) {
        try {
            this.N_RunFileDialog(
                    mode, title, defaultFilePath, acceptFilters, selectedAcceptFilter, callback);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    @Override
    public void startDownload(final String url) {
        try {
            this.N_StartDownload(url);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    @Override
    public void print() {
        try {
            this.N_Print();
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    @Override
    public void printToPDF(
            final String path, final CefPdfPrintSettings settings, final CefPdfPrintCallback callback) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("path was null or empty");
        }
        try {
            this.N_PrintToPDF(path, settings, callback);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    @Override
    public void find(final String searchText, final boolean forward, final boolean matchCase, final boolean findNext) {
        try {
            this.N_Find(searchText, forward, matchCase, findNext);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    @Override
    public void stopFinding(final boolean clearSelection) {
        try {
            this.N_StopFinding(clearSelection);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    protected final void closeDevTools() {
        try {
            this.N_CloseDevTools();
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    @Override
    public void replaceMisspelling(final String word) {
        try {
            this.N_ReplaceMisspelling(word);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    /**
     * Notify that the browser was resized.
     * @param width The new width of the browser
     * @param height The new height of the browser
     */
    protected final void wasResized(final int width, final int height) {
        try {
            this.N_WasResized(width, height);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    /**
     * Invalidate the UI.
     */
    public final void invalidate() {
        try {
            this.N_Invalidate();
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    /**
     * Send a key event.
     * @param e The event to send.
     */
    protected final void sendKeyEvent(final KeyEvent e) {
        try {
            this.N_SendKeyEvent(e);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    /**
     * Send a mouse event.
     * @param e The event to send.
     */
    protected final void sendMouseEvent(final MouseEvent e) {
        try {
            this.N_SendMouseEvent(e);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    /**
     * Send a mouse wheel event.
     * @param e The event to send.
     */
    protected final void sendMouseWheelEvent(final MouseWheelEvent e) {
        try {
            this.N_SendMouseWheelEvent(e);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    /**
     * Call this method when the user drags the mouse into the web view (before
     * calling DragTargetDragOver/DragTargetLeave/DragTargetDrop).
     * |drag_data| should not contain file contents as this type of data is not
     * allowed to be dragged into the web view. File contents can be removed using
     * CefDragData::ResetFileContents (for example, if |drag_data| comes from
     * CefRenderHandler::StartDragging).
     * This method is only used when window rendering is disabled.
     */
    protected final void dragTargetDragEnter(
            final CefDragData dragData, final Point pos, final int modifiers, final int allowedOps) {
        try {
            this.N_DragTargetDragEnter(dragData, pos, modifiers, allowedOps);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    /**
     * Call this method each time the mouse is moved across the web view during
     * a drag operation (after calling DragTargetDragEnter and before calling
     * DragTargetDragLeave/DragTargetDrop).
     * This method is only used when window rendering is disabled.
     */
    protected final void dragTargetDragOver(final Point pos, final int modifiers, final int allowedOps) {
        try {
            this.N_DragTargetDragOver(pos, modifiers, allowedOps);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    /**
     * Call this method when the user drags the mouse out of the web view (after
     * calling DragTargetDragEnter).
     * This method is only used when window rendering is disabled.
     */
    protected final void dragTargetDragLeave() {
        try {
            this.N_DragTargetDragLeave();
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    /**
     * Call this method when the user completes the drag operation by dropping
     * the object onto the web view (after calling DragTargetDragEnter).
     * The object being dropped is |drag_data|, given as an argument to
     * the previous DragTargetDragEnter call.
     * This method is only used when window rendering is disabled.
     */
    protected final void dragTargetDrop(final Point pos, final int modifiers) {
        try {
            this.N_DragTargetDrop(pos, modifiers);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    /**
     * Call this method when the drag operation started by a
     * CefRenderHandler.startDragging call has ended either in a drop or
     * by being cancelled. |x| and |y| are mouse coordinates relative to the
     * upper-left corner of the view. If the web view is both the drag source
     * and the drag target then all DragTarget* methods should be called before
     * DragSource* methods.
     * This method is only used when window rendering is disabled.
     */
    protected final void dragSourceEndedAt(final Point pos, final int operation) {
        try {
            this.N_DragSourceEndedAt(pos, operation);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    /**
     * Call this method when the drag operation started by a
     * CefRenderHandler.startDragging call has completed. This method may be
     * called immediately without first calling DragSourceEndedAt to cancel a
     * drag operation. If the web view is both the drag source and the drag
     * target then all DragTarget* methods should be called before DragSource*
     * methods.
     * This method is only used when window rendering is disabled.
     */
    protected final void dragSourceSystemDragEnded() {
        try {
            this.N_DragSourceSystemDragEnded();
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    protected final void updateUI(final Rectangle contentRect, final Rectangle browserRect) {
        try {
            this.N_UpdateUI(contentRect, browserRect);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    protected final void setParent(final long windowHandle, final Component canvas) {
        if (this.isClosing_ || this.isClosed_) {
            return;
        }

        try {
            this.N_SetParent(windowHandle, canvas);
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    /**
     * Call this method if the browser frame was moved.
     * This fixes positioning of select popups and dismissal on window move/resize.
     */
    protected final void notifyMoveOrResizeStarted() {
        try {
            this.N_NotifyMoveOrResizeStarted();
        } catch (final UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    private final native boolean N_CreateBrowser(CefClientHandler clientHandler, long windowHandle,
            String url, boolean osr, boolean transparent, Component canvas,
            CefRequestContext context);
    private final native boolean N_CreateDevTools(CefBrowser parent, CefClientHandler clientHandler,
            long windowHandle, boolean osr, boolean transparent, Component canvas, Point inspectAt);
    private final native long N_GetWindowHandle(long surfaceHandle);
    private final native boolean N_CanGoBack();
    private final native void N_GoBack();
    private final native boolean N_CanGoForward();
    private final native void N_GoForward();
    private final native boolean N_IsLoading();
    private final native void N_Reload();
    private final native void N_ReloadIgnoreCache();
    private final native void N_StopLoad();
    private final native int N_GetIdentifier();
    private final native CefFrame N_GetMainFrame();
    private final native CefFrame N_GetFocusedFrame();
    private final native CefFrame N_GetFrame(long identifier);
    private final native CefFrame N_GetFrame2(String name);
    private final native Vector<Long> N_GetFrameIdentifiers();
    private final native Vector<String> N_GetFrameNames();
    private final native int N_GetFrameCount();
    private final native boolean N_IsPopup();
    private final native boolean N_HasDocument();
    private final native void N_ViewSource();
    private final native void N_GetSource(CefStringVisitor visitor);
    private final native void N_GetText(CefStringVisitor visitor);
    private final native void N_LoadRequest(CefRequest request);
    private final native void N_LoadURL(String url);
    private final native void N_ExecuteJavaScript(String code, String url, int line);
    private final native String N_GetURL();
    private final native void N_Close(boolean force);
    private final native void N_SetFocus(boolean enable);
    private final native void N_SetWindowVisibility(boolean visible);
    private final native double N_GetZoomLevel();
    private final native void N_SetZoomLevel(double zoomLevel);
    private final native void N_RunFileDialog(FileDialogMode mode, String title,
            String defaultFilePath, Vector<String> acceptFilters, int selectedAcceptFilter,
            CefRunFileDialogCallback callback);
    private final native void N_StartDownload(String url);
    private final native void N_Print();
    private final native void N_PrintToPDF(
            String path, CefPdfPrintSettings settings, CefPdfPrintCallback callback);
    private final native void N_Find(
            String searchText, boolean forward, boolean matchCase, boolean findNext);
    private final native void N_StopFinding(boolean clearSelection);
    private final native void N_CloseDevTools();
    private final native void N_ReplaceMisspelling(String word);
    private final native void N_WasResized(int width, int height);
    private final native void N_Invalidate();
    private final native void N_SendKeyEvent(KeyEvent e);
    private final native void N_SendMouseEvent(MouseEvent e);
    private final native void N_SendMouseWheelEvent(MouseWheelEvent e);
    private final native void N_DragTargetDragEnter(
            CefDragData dragData, Point pos, int modifiers, int allowed_ops);
    private final native void N_DragTargetDragOver(Point pos, int modifiers, int allowed_ops);
    private final native void N_DragTargetDragLeave();
    private final native void N_DragTargetDrop(Point pos, int modifiers);
    private final native void N_DragSourceEndedAt(Point pos, int operation);
    private final native void N_DragSourceSystemDragEnded();
    private final native void N_UpdateUI(Rectangle contentRect, Rectangle browserRect);
    private final native void N_SetParent(long windowHandle, Component canvas);
    private final native void N_NotifyMoveOrResizeStarted();
}
