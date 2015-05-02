package com.virat.openglviewer.opengl;

import android.graphics.Bitmap;
import android.opengl.GLES30;

import com.virat.openglviewer.utils.Logger;

import java.nio.ByteBuffer;

import static android.opengl.GLES20.GL_UNSIGNED_SHORT_5_6_5;
import static android.opengl.GLES20.glDeleteFramebuffers;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glReadPixels;
import static android.opengl.GLES20.glTexImage2D;
import static android.opengl.GLES30.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES30.GL_COLOR_ATTACHMENT0;
import static android.opengl.GLES30.GL_DEPTH_ATTACHMENT;
import static android.opengl.GLES30.GL_DEPTH_COMPONENT;
import static android.opengl.GLES30.GL_FRAMEBUFFER;
import static android.opengl.GLES30.GL_LINEAR;
import static android.opengl.GLES30.GL_NEAREST;
import static android.opengl.GLES30.GL_RGB;
import static android.opengl.GLES30.GL_TEXTURE_2D;
import static android.opengl.GLES30.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES30.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES30.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES30.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES30.glBindFramebuffer;
import static android.opengl.GLES30.glBindTexture;
import static android.opengl.GLES30.glCheckFramebufferStatus;
import static android.opengl.GLES30.glFramebufferTexture2D;
import static android.opengl.GLES30.glGenFramebuffers;
import static android.opengl.GLES30.glGenTextures;
import static android.opengl.GLES30.glTexParameteri;

public class FrameBuffer {
  public static final int COLOR_TEXTURE = 0;
  public static final int DEPTH_TEXTURE = 1;
  private final int[] textures = new int[2];
  private final int[] frameBuffers = new int[1];

  private final int texWidth, texHeight;

  public FrameBuffer(int textureWidth, int textureHeight) {
    this.texWidth = textureWidth;
    this.texHeight = textureHeight;
  }

  public void create() {
    // generate the framebuffer and texture object names
    glGenFramebuffers(1, frameBuffers, 0);
    glGenTextures(2, textures, 0);

    // bind color texture and load the texture mip level 0 texels are RGB565
    // no texels need to specified as we are going to draw into the texture
    glBindTexture(GL_TEXTURE_2D, textures[COLOR_TEXTURE]);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, texWidth, texHeight, 0,
      GL_RGB, GL_UNSIGNED_SHORT_5_6_5, null);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

    // bind depth texture and load the texture mip level 0
    // no texels need to specified as we are going to draw into
    // the texture
    glBindTexture(GL_TEXTURE_2D, textures[DEPTH_TEXTURE]);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, texWidth, texHeight, 0,
        GL_DEPTH_COMPONENT, GLES30.GL_FLOAT, null);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

    // unbind the texture
    glBindTexture(GL_TEXTURE_2D, 0);
  }

  public void bind() {
    // bind the framebuffer
    glBindFramebuffer(GL_FRAMEBUFFER, frameBuffers[0]);

    // specify texture as color attachment
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
      GL_TEXTURE_2D, textures[COLOR_TEXTURE],
      0);

    // specify texture as depth attachment
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
      GL_TEXTURE_2D, textures[DEPTH_TEXTURE],
      0);

    checkStatus();
  }

  public void delete() {
    glDeleteFramebuffers(1, frameBuffers, 0);
    glDeleteTextures(2, textures, 0);
  }

  public int getColorTexture() {
    return textures[COLOR_TEXTURE];
  }

  public int getDepthTexture() {
    return textures[DEPTH_TEXTURE];
  }

  public Bitmap renderScreenshot(int screenWidth, int screenHeight) {

    // Grab a square portion, cropped from the middle
    final int x = 0;
    final int y = (int) ((screenHeight - screenWidth) / 2.0f);
    final int width = screenWidth;
    final int height = screenWidth;

    final int pixelSizeBytes = 2; // unsigned short 5_6_5
    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(width * height * pixelSizeBytes);

    glReadPixels(x, y, width, height, GL_RGB, GL_UNSIGNED_SHORT_5_6_5, byteBuffer);

    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
    bitmap.copyPixelsFromBuffer(byteBuffer);

    return bitmap;
  }

  /**
   * See here for more info:
   * https://www.khronos.org/opengles/sdk/docs/man/xhtml/glCheckFramebufferStatus.xml
   */
  private void checkStatus() {
    int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    switch (status) {
      case GLES30.GL_FRAMEBUFFER_COMPLETE:
        Logger.debug("Frame buffer success!!!");
        break;

      /**
       * Not all framebuffer attachment points are framebuffer attachment complete.
       * This means that at least one attachment point with a renderbuffer or texture
       * attached has its attached object no longer in existence or has an attached image
       * with a width or height of zero, or the color attachment point has a non-color-renderable
       * image attached, or the depth attachment point has a non-depth-renderable image attached,
       * or the stencil attachment point has a non-stencil-renderable image attached.
       *
       * Color-renderable formats include GL_RGBA4, GL_RGB5_A1, and GL_RGB565.
       * GL_DEPTH_COMPONENT16 is the only depth-renderable format.
       * GL_STENCIL_INDEX8 is the only stencil-renderable format.
       */
      case GLES30.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
        Logger.error("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
        break;
      case GLES30.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
        Logger.error("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
        break;
      case GLES30.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
        Logger.error("GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS");
        break;
      case GLES30.GL_FRAMEBUFFER_UNSUPPORTED:
        Logger.error("GL_FRAMEBUFFER_UNSUPPORTED");
        break;
      default:
        Logger.error("Unknown error loading frame buffer: " + status);
    }
  }

}
