package com.virat.openglviewer.pojos;

public class Material {
  public final String diffuseTextureMap; // map_Kd (should be an image file name)

  public Material(String diffuseTextureMap) {
    this.diffuseTextureMap = diffuseTextureMap;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Material material = (Material) o;

    if (!diffuseTextureMap.equals(material.diffuseTextureMap)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return diffuseTextureMap.hashCode();
  }
}
