package com.virat.openglviewer.utils;

public class ArrayUtils {

  public static int[] toIntArray(String[] strings, int startIndex) {
    int size = strings.length - startIndex;
    int[] integers = new int[size];
    for (int i = startIndex, count = 0; count < size; i++, count++) {
      integers[count] = Integer.parseInt(strings[i]);
    }
    return integers;
  }

  public static float[] toFloatArray(String[] strings, int startIndex) {
    int size = strings.length - startIndex;
    float[] floats = new float[size];
    for (int i = startIndex, count = 0; count < size; i++, count++) {
      floats[count] = Float.parseFloat(strings[i]);
    }
    return floats;
  }

}
