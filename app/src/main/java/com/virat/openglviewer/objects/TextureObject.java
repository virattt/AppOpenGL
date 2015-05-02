/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 ***/
package com.virat.openglviewer.objects;

import com.virat.openglviewer.data.VertexArray;
import com.virat.openglviewer.programs.ShaderProgram;

import static android.opengl.GLES30.GL_TRIANGLES;
import static android.opengl.GLES30.glDrawArrays;

public class TextureObject {
  private static final int BYTES_PER_FLOAT = 4;

  protected static final int POSITION_COMPONENT_COUNT = 3;
  protected static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
  protected static final int TOTAL_COMPONENT_COUNT = POSITION_COMPONENT_COUNT
      + TEXTURE_COORDINATES_COMPONENT_COUNT;
  protected static final int STRIDE = (TOTAL_COMPONENT_COUNT) * BYTES_PER_FLOAT;

  protected final VertexArray vertexArray;
  private final float[] minVertexVals;
  private final float[] maxVertexVals;

  public TextureObject(float[] data) {
    vertexArray = new VertexArray(data);
    minVertexVals = new float[3]; // TODO: something better?
    maxVertexVals = new float[3];
  }

  public TextureObject(float[] data, float[] minVertexVals, float[] maxVertexVals) {
    this.minVertexVals = minVertexVals;
    this.maxVertexVals = maxVertexVals;
    vertexArray = new VertexArray(data);
  }

  public void bindData(ShaderProgram textureProgram) {
    vertexArray.setVertexAttribPointer(
        0,
        textureProgram.getPositionAttributeLocation(),
        POSITION_COMPONENT_COUNT,
        STRIDE);

    vertexArray.setVertexAttribPointer(
        POSITION_COMPONENT_COUNT,
        textureProgram.getTextureCoordinatesAttributeLocation(),
        TEXTURE_COORDINATES_COMPONENT_COUNT,
        STRIDE);
  }

  public float minX() {
    return minVertexVals[0];
  }

  public float minY() {
    return minVertexVals[1];
  }

  public float minZ() {
    return minVertexVals[2];
  }

  public float maxX() {
    return maxVertexVals[0];
  }

  public float maxY() {
    return maxVertexVals[1];
  }

  public float maxZ() {
    return maxVertexVals[2];
  }

  public void draw() {
    glDrawArrays(GL_TRIANGLES, 0, vertexArray.length() / TOTAL_COMPONENT_COUNT);
  }
}
