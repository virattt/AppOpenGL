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
  private final int textureId;

  public TextureObject(float[] data) {
    this(data, 0);
  }

  public TextureObject(float[] data, int textureId) {
    vertexArray = new VertexArray(data);
    this.textureId = textureId;
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

  public int getTextureId() {
    return textureId;
  }

  public void draw() {
    glDrawArrays(GL_TRIANGLES, 0, vertexArray.length() / TOTAL_COMPONENT_COUNT);
  }
}
