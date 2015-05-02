package com.virat.openglviewer.programs;

import android.content.Context;

import com.virat.openglviewer.R;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;

public class PerspectiveShaderProgram extends ShaderProgram {
  private static final String U_RANGE_START = "u_rangeStart";
  private static final String U_RANGE_END = "u_rangeEnd";

  // Uniform locations
  private final int uMatrixLocation;
  private final int uTextureUnitLocation;
  private final int uRangeStartLocation;
  private final int uRangeEndLocation;

  // Attribute locations
  private final int aPositionLocation;
  private final int aTextureCoordinatesLocation;

  public PerspectiveShaderProgram(Context context) {
    super(context, R.raw.texture_vertex_shader,
      R.raw.perspective_shader);

    // Retrieve uniform locations for the shader program.
    uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
    uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);
    uRangeStartLocation = glGetUniformLocation(program, U_RANGE_START);
    uRangeEndLocation = glGetUniformLocation(program, U_RANGE_END);

    // Retrieve attribute locations for the shader program.
    aPositionLocation = glGetAttribLocation(program, A_POSITION);
    aTextureCoordinatesLocation = glGetAttribLocation(program, A_TEXTURE_COORDINATES);
  }

  public void setUniforms(float[] matrix, int textureId, float rangeStart, float rangeEnd) {
    // Pass the matrix into the shader program.
    glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);

    // Set the range for fence removal
    glUniform1f(uRangeStartLocation, rangeStart);
    glUniform1f(uRangeEndLocation, rangeEnd);

    // Set the active texture unit to texture unit 0.
    glActiveTexture(GL_TEXTURE0);

    // Bind the texture to this unit.
    glBindTexture(GL_TEXTURE_2D, textureId);

    // Tell the texture uniform sampler to use this texture in the shader by
    // telling it to read from texture unit 0.
    glUniform1i(uTextureUnitLocation, 0);
  }

  @Override
  public int getPositionAttributeLocation() {
    return aPositionLocation;
  }

  @Override
  public int getTextureCoordinatesAttributeLocation() {
    return aTextureCoordinatesLocation;
  }
}
