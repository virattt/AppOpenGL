package com.virat.openglviewer.objects;

// TODO: rename this class to something that makes more sense
public class FrameBufferObject extends TextureObject {

  public FrameBufferObject(float aspectRatio) {
    super(
        // x, y, z, S, T
        new float[]{
            // left triangle
            -1f, -aspectRatio, 0f, 0f, 0f, //
            1f, aspectRatio, 0f, 1f, 1f, //
            -1f, aspectRatio, 0f, 0f, 1f, //

            // right triangle
            -1f, -aspectRatio, 0f, 0f, 0f, //
            1f, -aspectRatio, 0f, 1f, 0f, //
            1f, aspectRatio, 0f, 1f, 1f //
        }
    );
  }
}
