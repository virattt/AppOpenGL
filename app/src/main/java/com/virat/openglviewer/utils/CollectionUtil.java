package com.virat.openglviewer.utils;

import java.util.List;

public class CollectionUtil {

  public static float[] flatten(List<float[]> list) {

    int numFloats = 0;
    for (float[] floats : list) {
      numFloats += floats.length;
    }

    float[] flattened = new float[numFloats];

    int count = 0;
    for (float[] array : list) {
      for (float t : array) {
        flattened[count++] = t;
      }
    }

    return flattened;
  }
}
