package org.cef.browser;

import org.cef.CefClient;
import org.cef.OS;
import org.cef.handler.CefWindowHandler;
import org.cef.handler.CefWindowHandlerAdapter;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;

/**
 * This class represents a windowed rendered browser.
 * The visibility of this class is "package". To create a new
 * CefBrowser instance, please use CefBrowserFactory.
 */
class CefBrowserWr extends CefBrowser_N {
    private Canvas canvas_ = null;
    private Component component_ = null;
    private Rectangle content_rect_ = new Rectangle(0, 0, 0, 0);
    private long window_handle_ = 0;
    private boolean justCreated_ = false;
    private double scaleFactor_ = 1.0;
    private final Timer delayedUpdate_ = new Timer(100, new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (CefBrowserWr.this.isClosed()) {
                        return;
                    }

                    final boolean hasCreatedUI = CefBrowserWr.this.createBrowserIfRequired(true);

                    if (hasCreatedUI) {
                        CefBrowserWr.this.delayedUpdate_.restart();
                    } else {
                        // If on Mac, this is needed due to the quirk described below
                        // (in org.cef.browser.CefBrowserWr.CefBrowserWr(...).new JPanel()
                        // {...}.paint(Graphics)). If on Linux, this is needed to invoke an
                        // XMoveResizeWindow call shortly after the UI was created. That seems to be
                        // necessary to actually get a windowed renderer to display something.
                        if (OS.isMacintosh() || OS.isLinux()) {
                            CefBrowserWr.this.doUpdate();
                        }
                    }
                }
            });
        }
    });

    private final CefWindowHandlerAdapter win_handler_ = new CefWindowHandlerAdapter() {
        private Point lastPos = new Point(-1, -1);
        private final long[] nextClick = new long[MouseInfo.getNumberOfButtons()];
        private final int[] clickCnt = new int[MouseInfo.getNumberOfButtons()];

        @Override
        public Rectangle getRect(final CefBrowser browser) {
            synchronized (CefBrowserWr.this.content_rect_) {
                return CefBrowserWr.this.content_rect_;
            }
        }

        @Override
        public void onMouseEvent(final CefBrowser browser, int event, final int screenX,
                                 final int screenY, final int modifier, final int button) {
            final Point pt = new Point(screenX, screenY);
            if (event == MouseEvent.MOUSE_MOVED) {
                // Remove mouse-moved events if the position of the cursor hasn't
                // changed.
                if (pt.equals(this.lastPos)) {
                    return;
                }
                this.lastPos = pt;

                // Change mouse-moved event to mouse-dragged event if the left mouse
                // button is pressed.
                if ((modifier & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
                    event = MouseEvent.MOUSE_DRAGGED;
                }
            }

            final int finalEvent = event;

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    // Send mouse event to the root UI component instead to the browser UI.
                    // Otherwise no mouse-entered and no mouse-exited events would be fired.
                    final Component parent = SwingUtilities.getRoot(CefBrowserWr.this.component_);
                    if (parent == null) {
                        return;
                    }
                    SwingUtilities.convertPointFromScreen(pt, parent);

                    int clickCnt = 0;
                    final long now = new Date().getTime();
                    if (finalEvent == MouseEvent.MOUSE_WHEEL) {
                        final int scrollType = MouseWheelEvent.WHEEL_UNIT_SCROLL;
                        final int rotation = button > 0 ? 1 : -1;
                        CefBrowserWr.this.component_.dispatchEvent(new MouseWheelEvent(parent, finalEvent, now,
                                modifier, pt.x, pt.y, 0, false, scrollType, 3, rotation));
                    } else {
                        clickCnt = getClickCount(finalEvent, button);
                        CefBrowserWr.this.component_.dispatchEvent(new MouseEvent(parent, finalEvent, now, modifier,
                                pt.x, pt.y, screenX, screenY, clickCnt, false, button));
                    }

                    // Always fire a mouse-clicked event after a mouse-released event.
                    if (finalEvent == MouseEvent.MOUSE_RELEASED) {
                        CefBrowserWr.this.component_.dispatchEvent(
                                new MouseEvent(parent, MouseEvent.MOUSE_CLICKED, now, modifier,
                                        pt.x, pt.y, screenX, screenY, clickCnt, false, button));
                    }
                }
            });
        }

        public int getClickCount(final int event, final int button) {
            // avoid exceptions by using modulo
            final int idx = button % this.nextClick.length;

            switch (event) {
                case MouseEvent.MOUSE_PRESSED:
                    final long currTime = new Date().getTime();
                    if (currTime > this.nextClick[idx]) {
                        this.nextClick[idx] = currTime
                                + (Integer) Toolkit.getDefaultToolkit().getDesktopProperty(
                                "awt.multiClickInterval");
                        this.clickCnt[idx] = 1;
                    } else {
                        this.clickCnt[idx]++;
                    }
                    // FALL THRU
                case MouseEvent.MOUSE_RELEASED:
                    return this.clickCnt[idx];
                default:
                    return 0;
            }
        }
    };

    CefBrowserWr(final CefClient client, final String url, final CefRequestContext context) {
        this(client, url, context, null, null);
    }

    @SuppressWarnings("serial")
    private CefBrowserWr(final CefClient client, final String url, final CefRequestContext context,
                         final CefBrowserWr parent, final Point inspectAt) {
        super(client, url, context, parent, inspectAt);
        this.delayedUpdate_.setRepeats(false);

        // Disabling lightweight of popup menu is required because
        // otherwise it will be displayed behind the content of component_
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

        // We're using a JComponent instead of a Canvas now because the
        // JComponent has clipping informations, which aren't accessible for Canvas.
        this.component_ = new JPanel(new BorderLayout()) {
            private boolean removed_ = true;

            @Override
            public void setBounds(final int x, final int y, final int width, final int height) {
                super.setBounds(x, y, width, height);
                CefBrowserWr.this.wasResized((int) (width * CefBrowserWr.this.scaleFactor_), (int) (height * CefBrowserWr.this.scaleFactor_));
            }

            @Override
            public void setBounds(final Rectangle r) {
                this.setBounds(r.x, r.y, r.width, r.height);
            }

            @Override
            public void setSize(final int width, final int height) {
                super.setSize(width, height);
                CefBrowserWr.this.wasResized((int) (width * CefBrowserWr.this.scaleFactor_), (int) (height * CefBrowserWr.this.scaleFactor_));
            }

            @Override
            public void setSize(final Dimension d) {
                this.setSize(d.width, d.height);
            }

            @Override
            public void paint(final Graphics g) {
                // If the user resizes the UI component, the new size and clipping
                // informations are forwarded to the native code.
                // But on Mac the last resize information doesn't resize the native UI
                // accurately (sometimes the native UI is too small). An easy way to
                // solve this, is to send the last Update-Information again. Therefore
                // we're setting up a delayedUpdate timer which is reset each time
                // paint is called. This prevents the us of sending the UI update too
                // often.
                if (g instanceof Graphics2D) {
                    CefBrowserWr.this.scaleFactor_ = ((Graphics2D) g).getTransform().getScaleX();
                }
                CefBrowserWr.this.doUpdate();
                CefBrowserWr.this.delayedUpdate_.restart();
            }

            @Override
            public void addNotify() {
                super.addNotify();
                if (this.removed_) {
                    CefBrowserWr.this.setParent(getWindowHandle(this), CefBrowserWr.this.canvas_);
                    this.removed_ = false;
                }
            }

            @Override
            public void removeNotify() {
                if (!this.removed_) {
                    if (!CefBrowserWr.this.isClosed()) {
                        CefBrowserWr.this.setParent(0, null);
                    }
                    this.removed_ = true;
                }
                super.removeNotify();
            }
        };

        // On windows we have to use a Canvas because its a heavyweight component
        // and we need its native HWND as parent for the browser UI. The same
        // technique is used on Linux as well.
        if (OS.isWindows() || OS.isLinux()) {
            this.canvas_ = new Canvas();
            ((JPanel) this.component_).add(this.canvas_, BorderLayout.CENTER);
        }

        // Initial minimal size of the component. Otherwise the UI won't work
        // accordingly in panes like JSplitPane.
        this.component_.setMinimumSize(new Dimension(0, 0));
        this.component_.setFocusable(true);
        this.component_.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(final FocusEvent e) {
                CefBrowserWr.this.setFocus(false);
            }

            @Override
            public void focusGained(final FocusEvent e) {
                // Dismiss any Java menus that are currently displayed.
                MenuSelectionManager.defaultManager().clearSelectedPath();
                CefBrowserWr.this.setFocus(true);
            }
        });
        this.component_.addHierarchyBoundsListener(new HierarchyBoundsListener() {
            @Override
            public void ancestorResized(final HierarchyEvent e) {
                CefBrowserWr.this.doUpdate();
            }
            @Override
            public void ancestorMoved(final HierarchyEvent e) {
                CefBrowserWr.this.doUpdate();
                CefBrowserWr.this.notifyMoveOrResizeStarted();
            }
        });
        this.component_.addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(final HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                    CefBrowserWr.this.setWindowVisibility(e.getChanged().isVisible());
                }
            }
        });
    }

    @Override
    public void createImmediately() {
        this.justCreated_ = true;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Create the browser immediately. It will be parented to the Java
                // window once it becomes available.
                CefBrowserWr.this.createBrowserIfRequired(false);
            }
        });
    }

    @Override
    public Component getUIComponent() {
        return this.component_;
    }

    @Override
    public CefWindowHandler getWindowHandler() {
        return this.win_handler_;
    }

    @Override
    protected CefBrowser_N createDevToolsBrowser(final CefClient client, final String url,
                                                 final CefRequestContext context, final CefBrowser_N parent, final Point inspectAt) {
        return new CefBrowserWr(client, url, context, (CefBrowserWr) this, inspectAt);
    }

    private synchronized long getWindowHandle() {
        if (this.window_handle_ == 0 && OS.isMacintosh()) {
            this.window_handle_ = getWindowHandle(this.component_);
        }
        return this.window_handle_;
    }

    private static long getWindowHandle(final Component component) {
        if (OS.isMacintosh()) {
            try {
                final Class<?> cls = Class.forName("org.cef.browser.mac.CefBrowserWindowMac");
                final CefBrowserWindow browserWindow = (CefBrowserWindow) cls.newInstance();
                if (browserWindow != null) {
                    return browserWindow.getWindowHandle(component);
                }
            } catch (final ClassNotFoundException e) {
                e.printStackTrace();
            } catch (final InstantiationException e) {
                e.printStackTrace();
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private void doUpdate() {
        if (this.isClosed()) {
            return;
        }

        final Rectangle vr = ((JPanel) this.component_).getVisibleRect();
        final Rectangle clipping = new Rectangle((int) (vr.getX() * this.scaleFactor_),
                (int) (vr.getY() * this.scaleFactor_), (int) (vr.getWidth() * this.scaleFactor_),
                (int) (vr.getHeight() * this.scaleFactor_));

        if (OS.isMacintosh()) {
            Container parent = this.component_.getParent();
            final Point contentPos = this.component_.getLocation();
            while (parent != null) {
                final Container next = parent.getParent();
                if (next != null && next instanceof Window) {
                    break;
                }
                final Point parentPos = parent.getLocation();
                contentPos.translate(parentPos.x, parentPos.y);
                parent = next;
            }
            contentPos.translate(clipping.x, clipping.y);

            final Point browserPos = clipping.getLocation();
            browserPos.x *= -1;
            browserPos.y *= -1;

            synchronized (this.content_rect_) {
                this.content_rect_ = new Rectangle(contentPos, clipping.getSize());
                final Rectangle browserRect = new Rectangle(browserPos, this.component_.getSize());
                this.updateUI(this.content_rect_, browserRect);
            }
        } else {
            synchronized (this.content_rect_) {
                final Rectangle bounds = null != this.canvas_ ? this.canvas_.getBounds() : this.component_.getBounds();
                this.content_rect_ = new Rectangle((int) (bounds.getX() * this.scaleFactor_),
                        (int) (bounds.getY() * this.scaleFactor_),
                        (int) (bounds.getWidth() * this.scaleFactor_),
                        (int) (bounds.getHeight() * this.scaleFactor_));
                this.updateUI(clipping, this.content_rect_);
            }
        }
    }

    private boolean createBrowserIfRequired(final boolean hasParent) {
        if (this.isClosed()) {
            return false;
        }

        long windowHandle = 0;
        Component canvas = null;
        if (hasParent) {
            windowHandle = this.getWindowHandle();
            canvas = (OS.isWindows() || OS.isLinux()) ? this.canvas_ : this.component_;
        }

        if (this.getNativeRef("CefBrowser") == 0) {
            if (this.getParentBrowser() != null) {
                this.createDevTools(this.getParentBrowser(), this.getClient(), windowHandle, false, false, canvas,
                        this.getInspectAt());
                return true;
            } else {
                this.createBrowser(this.getClient(), windowHandle, this.getUrl(), false, false, canvas,
                        this.getRequestContext());
                return true;
            }
        } else if (hasParent && this.justCreated_) {
            this.setParent(windowHandle, canvas);
            this.setFocus(true);
            this.justCreated_ = false;
        }

        return false;
    }

    @Override
    public CompletableFuture<BufferedImage> createScreenshot(final boolean nativeResolution) {
        throw new UnsupportedOperationException("Unsupported for windowed rendering");
    }
}
