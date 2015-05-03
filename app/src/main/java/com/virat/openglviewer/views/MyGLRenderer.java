/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 ***/
package com.virat.openglviewer.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView.Renderer;

import com.virat.openglviewer.R;
import com.virat.openglviewer.objects.FrameBufferObject;
import com.virat.openglviewer.objects.TextureObject;
import com.virat.openglviewer.opengl.FrameBuffer;
import com.virat.openglviewer.opengl.MatrixHelper;
import com.virat.openglviewer.opengl.TextureHelper;
import com.virat.openglviewer.opengl.Transform;
import com.virat.openglviewer.pojos.Meshes;
import com.virat.openglviewer.programs.PerspectiveShaderProgram;
import com.virat.openglviewer.programs.TextureShaderProgram;
import com.virat.openglviewer.utils.Logger;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES30.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES30.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES30.GL_DEPTH_TEST;
import static android.opengl.GLES30.glClear;
import static android.opengl.GLES30.glClearColor;
import static android.opengl.GLES30.glEnable;
import static android.opengl.GLES30.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.orthoM;
import static android.opengl.Matrix.setIdentityM;

public class MyGLRenderer implements Renderer {
  public static final int ALL_BUFFERS = GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT;
  public static final float[] AXIS_X = new float[]{1f, 0f, 0f};
  public static final float[] AXIS_Y = new float[]{0f, 1f, 0f};
  public static final float[] AXIS_Z = new float[]{0f, 0f, 1f};
  public static final float Z_NEAR = 1f;
  public static final float Z_FAR = 40f;
  public static final float ZOOM_OUT = -3f;
  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Default Values
  //////////////////////////////////////////////////////////////////////////////////////////////////
  public static final int DEFAULT_OIL_PAINT_RADIUS = 4;
  public static final float DEFAULT_BOKEH_FOCAL_DEPTH = 10f;
  public static final float DEFAULT_BOKEH_MAX_BLUR = 1f;
  public static final float DEFAULT_SKETCH_INTENSITY = 1f;
  public static final float DEFAULT_FENCE_REMOVAL_START = Z_FAR;
  public static final float DEFAULT_FENCE_REMOVAL_END = Z_FAR;

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // State variables
  //////////////////////////////////////////////////////////////////////////////////////////////////
  private final Context context;
  private final Meshes meshes;
  private final int width;
  private final int height;

  private final float[] projectionMatrix = new float[16];

  private volatile float angleX;
  private volatile float angleY;
  private float touchX;
  private float touchY;

  private List<TextureObject> textureObjects = new ArrayList<>();

  private FrameBufferObject frameBufferObject;

  private PerspectiveShaderProgram perspectiveShaderProgram;
  private TextureShaderProgram textureProgram;

  private FrameBuffer frameBuffer;


  // Fence removal variables
  private float fenceRemovalStart = DEFAULT_FENCE_REMOVAL_START;
  private float fenceRemovalEnd = DEFAULT_FENCE_REMOVAL_END;

  private OnRenderListener onRenderListener;
  private boolean isFirstRenderSent;

  private Transform transform = new Transform();
  public float dx;
  public float dy;

  public interface OnRenderListener {
    void onRender(Bitmap bitmap);
  }

  public MyGLRenderer(Context context, Meshes meshes, int width, int height) {
    this.context = context;
    this.meshes = meshes;
    this.width = width;
    this.height = height;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Renderer Implementation
  //////////////////////////////////////////////////////////////////////////////////////////////////

  public void onClick(float touchX, float touchY) {
    this.touchX = touchX;
    this.touchY = touchY;
  }

  @Override
  public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
    glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

    // Create shader effects
    perspectiveShaderProgram = new PerspectiveShaderProgram(context);
    textureProgram = new TextureShaderProgram(context);

    float[] faces = meshes.subMeshes.get(0).faces;
    Bitmap bitmap = createBitmap(context, R.drawable.spiderman);
    int textureId = loadTexture(bitmap);
    textureObjects.add(new TextureObject(faces, textureId));

    Logger.info("number of textures: " + textureObjects.size());

    // Surface for rendering frame buffer in Ortho projection
    frameBufferObject = new FrameBufferObject((float)height / (float) width);
    frameBuffer = new FrameBuffer(width, height);
    frameBuffer.create();

    Logger.info("[objParser finished] textures loaded");
  }

  @Override
  public void onSurfaceChanged(GL10 glUnused, int width, int height) {
    // Set the OpenGL viewport to fill the entire surface.
    glViewport(0, 0, width, height);

    final float aspect = (float) width / (float) height;
    MatrixHelper.perspectiveM(projectionMatrix, 53.13f, aspect, Z_NEAR, Z_FAR);
  }

  @Override
  public void onDrawFrame(GL10 glUnused) {
    Logger.info("onDrawFrame called");

    /////////////////////////////////////////////////////////////////
    // Render the scene to a FrameBuffer
    /////////////////////////////////////////////////////////////////
    frameBuffer.bind();
    glClear(ALL_BUFFERS);
    glEnable(GL_DEPTH_TEST);

    float[] mvpMatrix = new float[16];
    multiplyMM(mvpMatrix, 0, projectionMatrix, 0, getModelMatrix(), 0);
    renderObjects(mvpMatrix);

    if (onRenderListener != null && !isFirstRenderSent) {
      isFirstRenderSent = true;
      onRenderListener.onRender(frameBuffer.renderScreenshot(width, height));
    }

    /////////////////////////////////////////////////////////////////
    // Run post processing effect to window framebuffer
    /////////////////////////////////////////////////////////////////
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    glClear(ALL_BUFFERS);
    glEnable(GL_DEPTH_TEST);

    // reset the matrix so we render a non rotated texture_mtl
    float[] modelMatrix = new float[16];
    setIdentityM(modelMatrix, 0);

    final float[] orthoMatrix = new float[16];
    final float aspectRatio = (float) height / (float) width;
    orthoM(orthoMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);

    mvpMatrix = new float[16];
    multiplyMM(mvpMatrix, 0, orthoMatrix, 0, modelMatrix, 0);

    renderPerspectiveEffect(mvpMatrix);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Public Methods
  //////////////////////////////////////////////////////////////////////////////////////////////////


  public void setOnRenderListener(OnRenderListener onRenderListener) {
    this.onRenderListener = onRenderListener;
  }

  public float getAngleX() {
    return angleX;
  }

  public void setAngleX(float angle) {
    angleX = angle;
  }

  public float getAngleY() {
    return angleY;
  }

  public void setAngleY(float angle) {
    angleY = angle;
  }


  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Private Methods
  //////////////////////////////////////////////////////////////////////////////////////////////////

  private int loadTexture(Bitmap bitmap) {
    return TextureHelper.loadTexture(bitmap);
  }

  private void renderObjects(float[] mvpMatrix) {
    if (textureObjects != null) {
      for (TextureObject textureObject : textureObjects) {
        if (textureObject != null) {
          perspectiveShaderProgram.useProgram();
          perspectiveShaderProgram.setUniforms(mvpMatrix, textureObject.getTextureId(), fenceRemovalStart, fenceRemovalEnd);
          textureObject.bindData(perspectiveShaderProgram);
          textureObject.draw();
        } else {
          Logger.info("textureObject is null");
        }
      }
    } else {
      Logger.info("textureObjects is null");
    }
  }

  private float[] getModelMatrix() {
    float[] min = meshes.getExtents().min;
    float[] max = meshes.getExtents().max;

    float xWidth = (max[0] - min[0]) / 2f;
    float yWidth = (max[1] - min[1]) / 2f;
    float zWidth = (max[2] - min[2]) / 2f;

    // Find max width out of x, y, z widths and multiply by a constant to zoom object out
    float maxWidth = (Math.max(xWidth, Math.max(yWidth, zWidth))) * ZOOM_OUT;
    Logger.info("extents maxWidth: " + maxWidth);

    transform.position = new float[]{ 0f, 0f, maxWidth };
    transform.preTranslate(new float[] {
        -(max[0] + min[0]) / 2f, //
        -(max[1] + min[1]) / 2f, //
        -(max[2] + min[2]) / 2f //
    });
    transform.rotate(AXIS_Y, dx, Transform.Space.WORLD);
    transform.rotate(AXIS_X, dy, Transform.Space.WORLD);

    dx = 0;
    dy = 0;

    return transform.localToWorldMatrix();
  }

  private Bitmap createBitmap(Context context, int resourceId) {
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inScaled = false;

    // Read in the resource
    return BitmapFactory.decodeResource(
        context.getResources(), resourceId, options);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Post Processing Render Effects (Second pass draw from FrameBuffer texture_mtl)
  //////////////////////////////////////////////////////////////////////////////////////////////////

  private void renderPerspectiveEffect(float[] mvpMatrix) {
    textureProgram.useProgram();
    textureProgram.setUniforms(mvpMatrix, frameBuffer.getColorTexture());
    frameBufferObject.bindData(textureProgram);
    frameBufferObject.draw();
  }

}