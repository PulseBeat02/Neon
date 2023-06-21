package org.cef.browser;

import com.jogamp.opengl.GL2;

import java.awt.Rectangle;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

class CefRenderer {
  private final boolean transparent_;
  private GL2 initialized_context_ = null;
  private final int[] texture_id_ = new int[1];
  private int view_width_ = 0;
  private int view_height_ = 0;
  private float spin_x_ = 0f;
  private float spin_y_ = 0f;
  private Rectangle popup_rect_ = new Rectangle(0, 0, 0, 0);
  private Rectangle original_popup_rect_ = new Rectangle(0, 0, 0, 0);
  private boolean use_draw_pixels_ = false;

  protected CefRenderer(final boolean transparent) {
    this.transparent_ = transparent;
  }

  protected boolean isTransparent() {
    return this.transparent_;
  }

  protected int getTextureID() {
    return this.texture_id_[0];
  }

  protected void initialize(final GL2 gl2) {
    if (this.initialized_context_ == gl2) {
      return;
    }

    this.initialized_context_ = gl2;

    if (!gl2.getContext().isHardwareRasterizer()) {
      // Workaround for Windows Remote Desktop which requires pot textures.
      System.out.println("opengl rendering may be slow as hardware rendering isn't available");
      this.use_draw_pixels_ = true;
      return;
    }

    gl2.glHint(gl2.GL_POLYGON_SMOOTH_HINT, gl2.GL_NICEST);

    gl2.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

    // Necessary for non-power-of-2 textures to render correctly.
    gl2.glPixelStorei(gl2.GL_UNPACK_ALIGNMENT, 1);

    // Create the texture.
    gl2.glGenTextures(1, this.texture_id_, 0);

    gl2.glBindTexture(gl2.GL_TEXTURE_2D, this.texture_id_[0]);
    gl2.glTexParameteri(gl2.GL_TEXTURE_2D, gl2.GL_TEXTURE_MIN_FILTER, gl2.GL_NEAREST);
    gl2.glTexParameteri(gl2.GL_TEXTURE_2D, gl2.GL_TEXTURE_MAG_FILTER, gl2.GL_NEAREST);
    gl2.glTexEnvf(gl2.GL_TEXTURE_ENV, gl2.GL_TEXTURE_ENV_MODE, gl2.GL_MODULATE);
  }

  protected void cleanup(final GL2 gl2) {
    if (this.texture_id_[0] != 0) {
      gl2.glDeleteTextures(1, this.texture_id_, 0);
    }
    this.view_width_ = this.view_height_ = 0;
  }

  protected void render(final GL2 gl2) {
    if (this.use_draw_pixels_ || this.view_width_ == 0 || this.view_height_ == 0) {
      return;
    }

    final float[] vertex_data = { // tu,   tv,     x,     y,    z
      0.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 1.0f, 1.0f, -1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f,
      0.0f, 0.0f, -1.0f, 1.0f, 0.0f
    };
    final FloatBuffer vertices = FloatBuffer.wrap(vertex_data);

    gl2.glClear(gl2.GL_COLOR_BUFFER_BIT | gl2.GL_DEPTH_BUFFER_BIT);

    gl2.glMatrixMode(gl2.GL_MODELVIEW);
    gl2.glLoadIdentity();

    // Match GL units to screen coordinates.
    gl2.glViewport(0, 0, this.view_width_, this.view_height_);
    gl2.glMatrixMode(gl2.GL_PROJECTION);
    gl2.glLoadIdentity();

    // Draw the background gradient.
    gl2.glPushAttrib(gl2.GL_ALL_ATTRIB_BITS);
    gl2.glBegin(gl2.GL_QUADS);
    gl2.glColor4f(1.0f, 0.0f, 0.0f, 1.0f); // red
    gl2.glVertex2f(-1.0f, -1.0f);
    gl2.glVertex2f(1.0f, -1.0f);
    gl2.glColor4f(0.0f, 0.0f, 1.0f, 1.0f); // blue
    gl2.glVertex2f(1.0f, 1.0f);
    gl2.glVertex2f(-1.0f, 1.0f);
    gl2.glEnd();
    gl2.glPopAttrib();

    // Rotate the view based on the mouse spin.
    if (this.spin_x_ != 0) {
      gl2.glRotatef(-this.spin_x_, 1.0f, 0.0f, 0.0f);
    }
    if (this.spin_y_ != 0) {
      gl2.glRotatef(-this.spin_y_, 0.0f, 1.0f, 0.0f);
    }

    if (this.transparent_) {
      // Alpha blending style. Texture values have premultiplied alpha.
      gl2.glBlendFunc(gl2.GL_ONE, gl2.GL_ONE_MINUS_SRC_ALPHA);

      // Enable alpha blending.
      gl2.glEnable(gl2.GL_BLEND);
    }

    // Enable 2D textures.
    gl2.glEnable(gl2.GL_TEXTURE_2D);

    // Draw the facets with the texture.
    assert (this.texture_id_[0] != 0);
    gl2.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    gl2.glBindTexture(gl2.GL_TEXTURE_2D, this.texture_id_[0]);
    gl2.glInterleavedArrays(gl2.GL_T2F_V3F, 0, vertices);
    gl2.glDrawArrays(gl2.GL_QUADS, 0, 4);

    // Disable 2D textures.
    gl2.glDisable(gl2.GL_TEXTURE_2D);

    if (this.transparent_) {
      // Disable alpha blending.
      gl2.glDisable(gl2.GL_BLEND);
    }
  }

  protected void onPopupSize(final Rectangle rect) {
    if (rect.width <= 0 || rect.height <= 0) {
      return;
    }
    this.original_popup_rect_ = rect;
    this.popup_rect_ = this.getPopupRectInWebView(this.original_popup_rect_);
  }

  protected Rectangle getPopupRect() {
    return (Rectangle) this.popup_rect_.clone();
  }

  protected Rectangle getPopupRectInWebView(final Rectangle original_rect) {
    final Rectangle rc = original_rect;
    // if x or y are negative, move them to 0.
    if (rc.x < 0) {
      rc.x = 0;
    }
    if (rc.y < 0) {
      rc.y = 0;
    }
    // if popup goes outside the view, try to reposition origin
    if (rc.x + rc.width > this.view_width_) {
      rc.x = this.view_width_ - rc.width;
    }
    if (rc.y + rc.height > this.view_height_) {
      rc.y = this.view_height_ - rc.height;
    }
    // if x or y became negative, move them to 0 again.
    if (rc.x < 0) {
      rc.x = 0;
    }
    if (rc.y < 0) {
      rc.y = 0;
    }
    return rc;
  }

  protected void clearPopupRects() {
    this.popup_rect_.setBounds(0, 0, 0, 0);
    this.original_popup_rect_.setBounds(0, 0, 0, 0);
  }

  @SuppressWarnings("static-access")
  protected void onPaint(
      final GL2 gl2,
      final boolean popup,
      final Rectangle[] dirtyRects,
      final ByteBuffer buffer,
      final int width,
      final int height) {
    this.initialize(gl2);

    if (this.use_draw_pixels_) {
      gl2.glRasterPos2f(-1, 1);
      gl2.glPixelZoom(1, -1);
      gl2.glDrawPixels(width, height, GL2.GL_BGRA, GL2.GL_UNSIGNED_BYTE, buffer);
      return;
    }

    if (this.transparent_) {
      // Enable alpha blending.
      gl2.glEnable(gl2.GL_BLEND);
    }

    // Enable 2D textures.
    gl2.glEnable(gl2.GL_TEXTURE_2D);

    assert (this.texture_id_[0] != 0);
    gl2.glBindTexture(gl2.GL_TEXTURE_2D, this.texture_id_[0]);

    if (!popup) {
      final int old_width = this.view_width_;
      final int old_height = this.view_height_;

      this.view_width_ = width;
      this.view_height_ = height;

      gl2.glPixelStorei(gl2.GL_UNPACK_ROW_LENGTH, this.view_width_);

      if (old_width != this.view_width_ || old_height != this.view_height_) {
        // Update/resize the whole texture.
        gl2.glPixelStorei(gl2.GL_UNPACK_SKIP_PIXELS, 0);
        gl2.glPixelStorei(gl2.GL_UNPACK_SKIP_ROWS, 0);
        gl2.glTexImage2D(
            gl2.GL_TEXTURE_2D,
            0,
            gl2.GL_RGBA,
            this.view_width_,
            this.view_height_,
            0,
            gl2.GL_BGRA,
            gl2.GL_UNSIGNED_INT_8_8_8_8_REV,
            buffer);
      } else {
        // Update just the dirty rectangles.
        for (final Rectangle rect : dirtyRects) {
          gl2.glPixelStorei(gl2.GL_UNPACK_SKIP_PIXELS, rect.x);
          gl2.glPixelStorei(gl2.GL_UNPACK_SKIP_ROWS, rect.y);
          gl2.glTexSubImage2D(
              gl2.GL_TEXTURE_2D,
              0,
              rect.x,
              rect.y,
              rect.width,
              rect.height,
              gl2.GL_BGRA,
              gl2.GL_UNSIGNED_INT_8_8_8_8_REV,
              buffer);
        }
      }
    } else if (this.popup_rect_.width > 0 && this.popup_rect_.height > 0) {
      int skip_pixels = 0, x = this.popup_rect_.x;
      int skip_rows = 0, y = this.popup_rect_.y;
      int w = width;
      int h = height;

      // Adjust the popup to fit inside the view.
      if (x < 0) {
        skip_pixels = -x;
        x = 0;
      }
      if (y < 0) {
        skip_rows = -y;
        y = 0;
      }
      if (x + w > this.view_width_) {
        w -= x + w - this.view_width_;
      }
      if (y + h > this.view_height_) {
        h -= y + h - this.view_height_;
      }

      // Update the popup rectangle.
      gl2.glPixelStorei(gl2.GL_UNPACK_ROW_LENGTH, width);
      gl2.glPixelStorei(gl2.GL_UNPACK_SKIP_PIXELS, skip_pixels);
      gl2.glPixelStorei(gl2.GL_UNPACK_SKIP_ROWS, skip_rows);
      gl2.glTexSubImage2D(
          gl2.GL_TEXTURE_2D, 0, x, y, w, h, gl2.GL_BGRA, gl2.GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
    }

    // Disable 2D textures.
    gl2.glDisable(gl2.GL_TEXTURE_2D);

    if (this.transparent_) {
      // Disable alpha blending.
      gl2.glDisable(gl2.GL_BLEND);
    }
  }

  protected void setSpin(final float spinX, final float spinY) {
    this.spin_x_ = spinX;
    this.spin_y_ = spinY;
  }

  protected void incrementSpin(final float spinDX, final float spinDY) {
    this.spin_x_ -= spinDX;
    this.spin_y_ -= spinDY;
  }
}
