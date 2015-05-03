package com.virat.openglviewer.pojos;

import java.util.List;

public class Meshes {

  public final List<Mesh> subMeshes;

  public Meshes(List<Mesh> subMeshes) {
    this.subMeshes = subMeshes;
  }

  public Extents getExtents() {
    float[] max = new float[] {Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE};
    float[] min = new float[] {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};

    for (Mesh mesh : subMeshes) {
      int x = 0;
      int y = 1;
      int z = 2;
      int offset = 5;

      if (mesh.faces.length > 0) {
        while (z < mesh.faces.length) {
          max[0] = mesh.faces[x] > max[0] ? mesh.faces[x] : max[0];
          max[1] = mesh.faces[y] > max[1] ? mesh.faces[y] : max[1];
          max[2] = mesh.faces[z] > max[2] ? mesh.faces[z] : max[2];

          min[0] = mesh.faces[x] < min[0] ? mesh.faces[x] : min[0];
          min[1] = mesh.faces[y] < min[1] ? mesh.faces[y] : min[1];
          min[2] = mesh.faces[z] < min[2] ? mesh.faces[z] : min[2];

          x += offset;
          y += offset;
          z += offset;
        }
      }
    }

    return new Extents(max, min);
  }

  public static class Extents {
    public final float[] max;
    public final float[] min;

    private Extents(float[] max, float[] min) {
      this.max = max;
      this.min = min;
    }
  }
}
