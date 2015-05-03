package com.virat.openglviewer.pojos;

import java.util.Arrays;

public class Mesh {
  public final Material material;
  public final float[] faces;

  public Mesh(Material material, float[] faces) {
    this.material = material;
    this.faces = faces;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Mesh mesh = (Mesh) o;

    if (!Arrays.equals(faces, mesh.faces)) {
      return false;
    }
    if (!material.equals(mesh.material)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = material.hashCode();
    result = 31 * result + Arrays.hashCode(faces);
    return result;
  }
}
