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
import android.opengl.Matrix;

import com.virat.openglviewer.opengl.FrameBuffer;
import com.virat.openglviewer.R;
import com.virat.openglviewer.pojo.OBJParser;
import com.virat.openglviewer.objects.FrameBufferObject;
import com.virat.openglviewer.objects.TextureObject;
import com.virat.openglviewer.programs.PerspectiveShaderProgram;
import com.virat.openglviewer.programs.TextureShaderProgram;
import com.virat.openglviewer.utils.Logger;
import com.virat.openglviewer.opengl.MatrixHelper;
import com.virat.openglviewer.opengl.TextureHelper;

import java.io.InputStream;

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
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.scaleM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

public class MyGLRenderer implements Renderer {
  public static final int ALL_BUFFERS = GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT;

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Default Values
  //////////////////////////////////////////////////////////////////////////////////////////////////
  public static final float Z_NEAR = 1f;
  public static final float Z_FAR = 40f;

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // State variables
  //////////////////////////////////////////////////////////////////////////////////////////////////

  private final Context context;
  private final int width;
  private final int height;

  private final float[] projectionMatrix = new float[16];
  private final float[] viewMatrix = new float[16];

  private volatile float angleX;
  private volatile float angleY;

  private TextureObject hulkObject;
  private TextureObject spideyObject;
  private FrameBufferObject frameBufferObject;

  private PerspectiveShaderProgram perspectiveShaderProgram;
  private TextureShaderProgram textureProgram;

  private int hulkTexture;
  private int spideyTexture;

  private FrameBuffer frameBuffer;

  // Perspective shader variables
  private float fenceRemovalStart = Z_FAR;
  private float fenceRemovalEnd = Z_FAR;

  private OnRenderListener onRenderListener;
  private boolean isFirstRenderSent;

  public static interface OnRenderListener {
    void onRender(Bitmap bitmap);
  }

  public MyGLRenderer(Context context, int width, int height) {
    this.context = context;
    this.width = width;
    this.height = height;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Renderer Implementation
  //////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
    glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

    // Create shader effects
    perspectiveShaderProgram = new PerspectiveShaderProgram(context);
    textureProgram = new TextureShaderProgram(context);

    // HULK
    hulkObject = loadObject(R.raw.hulk);
    final Bitmap slasherBitmap = createBitmap(context, R.drawable.hulk);
    hulkTexture = TextureHelper.loadTexture(slasherBitmap);

    // SPIDERMAN
    spideyObject = loadObject(R.raw.spiderman);
    final Bitmap dogBitmap = createBitmap(context, R.drawable.spiderman);
    spideyTexture = TextureHelper.loadTexture(dogBitmap);

    // Surface for rendering frame buffer in Ortho projection
    frameBufferObject = new FrameBufferObject((float)height / (float) width);
    frameBuffer = new FrameBuffer(width, height);
    frameBuffer.create();

    Logger.info("[objParser finished] textures loaded");
  }

  // Very limited loading of an .obj object
  private TextureObject loadObject(int resourceId) {
    OBJParser objParser = new OBJParser();
    try (InputStream inputStream = context.getResources().openRawResource(resourceId)) {
      return objParser.parse(inputStream).get(0);
    } catch (Exception e) {
      throw new RuntimeException("There was a problem loading an .obj model: " + e.getMessage());
    }
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

    moveCamera();

    renderHulk();
    renderSpidey();

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

    // reset the matrix so we render a non rotated texture
    float[] modelMatrix = new float[16];
    setIdentityM(modelMatrix, 0);

    final float[] orthoMatrix = new float[16];
    final float aspectRatio = (float) height / (float) width;
    orthoM(orthoMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);

    final float[] mvpMatrix = new float[16];
    multiplyMM(mvpMatrix, 0, orthoMatrix, 0, modelMatrix, 0);

    renderPerspectiveEffect(mvpMatrix);
  }

  /**
   * Position the camera (compute the View matrix).
   * NOTE: This must be called before rendering anything to the scene.
   */
  private void moveCamera() {
    float angle = angleX * 0.01f; // scale the angle, it's too fast

    float radius = 5f;
    float eyeX = (float) (radius * Math.cos(angle));
    float eyeZ = (float) (radius * Math.sin(angle));
    Matrix.setLookAtM(viewMatrix, 0,
      eyeX, 0f, eyeZ, // eye position
      0f, 0f, -2.5f, // position you want to look at
      0f, 1.0f, 0.0f // up vector
    );
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

  private void renderHulk() {
    // translate
    float[] mvpMatrix = getMvpMatrix(hulkObject,
      0, // rotation Y
      1f, // scale
      0.15f, 0f, -3f // translation

    );

    perspectiveShaderProgram.useProgram();
    perspectiveShaderProgram.setUniforms(mvpMatrix, hulkTexture, fenceRemovalStart, fenceRemovalEnd);
    hulkObject.bindData(perspectiveShaderProgram);
    hulkObject.draw();
  }

  private void renderSpidey() {
    // translate
    float[] mvpMatrix = getMvpMatrix(spideyObject,
      45, // rotation Y
      1f, // scale
      -0.45f, -0.35f, -1f // translation
    );

    perspectiveShaderProgram.useProgram();
    perspectiveShaderProgram.setUniforms(mvpMatrix, spideyTexture, fenceRemovalStart, fenceRemovalEnd);
    spideyObject.bindData(perspectiveShaderProgram);
    spideyObject.draw();
  }

  private float[] getMvpMatrix(TextureObject object, float rotationY, float scale,
    float translationX, float translationY, float translationZ) {

    final float[] modelMatrix = new float[16];
    setIdentityM(modelMatrix, 0);

    // Move it back a little bit
    translateM(modelMatrix, 0, translationX, translationY, translationZ);

    // Apply rotation (if any)
    rotateM(modelMatrix, 0, rotationY, 0f, 1f, 0f);

    scaleM(modelMatrix, 0, scale, scale, scale);

    // center the object
    translateM(modelMatrix, 0, //

      -(object.maxX() + object.minX()) / 2f, //
      -(object.maxY() + object.minY()) / 2f, //
      -(object.maxZ() + object.minZ()) / 2f //
    );

    final float[] mvpMatrix = new float[16];
    multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
    multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
    return mvpMatrix;
  }

  private Bitmap createBitmap(Context context, int resourceId) {
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inScaled = false;

    // Read in the resource
    return BitmapFactory.decodeResource(
      context.getResources(), resourceId, options);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Post Processing Render Effects (Second pass draw from FrameBuffer texture)
  //////////////////////////////////////////////////////////////////////////////////////////////////

  private void renderPerspectiveEffect(float[] mvpMatrix) {
    textureProgram.useProgram();
    textureProgram.setUniforms(mvpMatrix, frameBuffer.getColorTexture());
    frameBufferObject.bindData(textureProgram);
    frameBufferObject.draw();
  }
}