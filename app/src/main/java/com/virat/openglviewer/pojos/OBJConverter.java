package com.virat.openglviewer.pojos;


import com.virat.openglviewer.utils.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.virat.openglviewer.utils.CollectionUtil.flatten;


public class OBJConverter {
  public final float[] maxVertexVals;
  public final float[] minVertexVals;

  // Vertices of the form (x, y, z)
  // This makes each float[] of size 3
  private List<float[]> vertices;

  // Texture coordinates of the form (u, v)
  // This makes each float[] of size 2
  private List<float[]> texCoords;

  // Meshes, mapped by material name
  List<Mesh> meshes;
  private Map<String, List<float[]>> faceMap;

  public OBJConverter() {
    vertices = new ArrayList<>();
    texCoords = new ArrayList<>();
    meshes = new ArrayList<>();

    maxVertexVals = new float[]{Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE};
    minVertexVals = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
  }

  /**
   * Parses an .obj file into its corresponding Meshes.
   */
  public List<Mesh> parse(InputStream obj, InputStream mtl) throws FileNotFoundException {
    vertices.clear();
    texCoords.clear();
    meshes.clear();

//    Logger.debug("Parsing .obj file: " + obj.getAbsolutePath());
    String mtlFileName = parseObj(obj);

//    File mtl = new File(obj.getParentFile(), mtlFileName);
//    Logger.debug("Parsing .mtl file: " + mtl.getAbsolutePath());
    parseMtl(mtl);

    return meshes;
  }

  String parseObj(InputStream stream) {
    String mtlFileName = null;

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
      String line;
      String currentMaterial = null;

      faceMap = new HashMap<>();

      while ((line = reader.readLine()) != null) {
        // Split by whitespace (one or more)
        String[] split = line.split(" +");

        String identifier = split[0];
        switch (identifier) {
          case "v":
            float[] vertex = toFloatArray(split, 1);
            captureMinMaxVertexValues(vertex);
            vertices.add(vertex);
            break;
          case "vt":
            texCoords.add(toFloatArray(split, 1));
            break;
          case "usemtl":
            currentMaterial = split[1];
            faceMap.put(currentMaterial, new ArrayList<float[]>());
            break;
          case "f":
            faceMap.get(currentMaterial).add(buildFace(split));
            break;
          case "mtllib":
            mtlFileName = split[1];
            break;
        }
      }

    } catch (IOException e) {
      throw new RuntimeException("Error parsing obj file: " + e.getMessage());
    }

    return mtlFileName;
  }

  void captureMinMaxVertexValues(float[] vertex) {
    maxVertexVals[0] = vertex[0] > maxVertexVals[0] ? vertex[0] : maxVertexVals[0];
    maxVertexVals[1] = vertex[1] > maxVertexVals[1] ? vertex[1] : maxVertexVals[1];
    maxVertexVals[2] = vertex[2] > maxVertexVals[2] ? vertex[2] : maxVertexVals[2];

    minVertexVals[0] = vertex[0] < minVertexVals[0] ? vertex[0] : minVertexVals[0];
    minVertexVals[1] = vertex[1] < minVertexVals[1] ? vertex[1] : minVertexVals[1];
    minVertexVals[2] = vertex[2] < minVertexVals[2] ? vertex[2] : minVertexVals[2];
  }

  void parseMtl(InputStream stream) {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
      String line;

      List<float[]> currentFaces = null;

      while ((line = reader.readLine()) != null) {
        // Split by whitespace (one or more)
        String[] split = line.split(" +");
        String identifier = split[0];

        if ("newmtl".equals(identifier)) {
          currentFaces = faceMap.get(split[1]);
        } else if ("map_Kd".equals(identifier)) {
          meshes.add(new Mesh(new Material(split[1]), flatten(currentFaces)));
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Error parsing mtl file: " + e.getMessage());
    }
  }

  /**
   * Faces are defined using lists of vertex, texture and normal indices.
   * A valid vertex index starts from 1 and matches the corresponding vertex
   * elements of a previously defined vertex list. Each face can contain three
   * or more vertices.
   * <p>
   * There are several flavors of this:
   * <p>
   * f v1 v2 v3
   * f v1/vt1 v2/vt2 v3/vt3
   * f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3
   * <p>
   * As texture coordinates are optional, one can define geometry without them:
   * <p>
   * f v1//vn1 v2//vn2 v3//vn3
   * <p>
   * Right now we will assume the format:
   * <p>
   * f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3
   * <p>
   * which means each face is a triangle.
   */
  float[] buildFace(String[] faceDef) {
    // v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3
    // split by space gives:
    //   [0] =  v1/vt1/vn1
    //   [1] =  v2/vt2/vn2
    //   [2] =  v3/vt3/vn3
    int[] i1 = toIntArray(faceDef[1].split("/")); // [v1, vt1, vn1]
    int[] i2 = toIntArray(faceDef[2].split("/")); // [v2, vt2, vn2]
    int[] i3 = toIntArray(faceDef[3].split("/")); // [v3, vt3, vn3]

    // (x, y, z)
    float[] v1 = vertices.get(i1[0] - 1);
    float[] v2 = vertices.get(i2[0] - 1);
    float[] v3 = vertices.get(i3[0] - 1);

    // (s, t)
    float[] vt1 = texCoords.get(i1[1] - 1);
    float[] vt2 = texCoords.get(i2[1] - 1);
    float[] vt3 = texCoords.get(i3[1] - 1);

    return new float[]
      {
        v1[0], v1[1], v1[2], vt1[0], 1f - vt1[1], // (x1, y1, z1, s1, t1)
        v2[0], v2[1], v2[2], vt2[0], 1f - vt2[1], // (x2, y2, z2, s2, t2)
        v3[0], v3[1], v3[2], vt3[0], 1f - vt3[1], // (x3, y3, z3, s3, t3)
      };
  }

  static int[] toIntArray(String[] strings) {
    return toIntArray(strings, 0);
  }

  static float[] toFloatArray(String[] strings) {
    return toFloatArray(strings, 0);
  }

  static int[] toIntArray(String[] strings, int startIndex) {
    int size = strings.length - startIndex;
    int[] integers = new int[size];
    for (int i = startIndex, count = 0; count < size; i++, count++) {
      integers[count] = Integer.parseInt(strings[i]);
    }
    return integers;
  }

  static float[] toFloatArray(String[] strings, int startIndex) {
    int size = strings.length - startIndex;
    float[] floats = new float[size];
    for (int i = startIndex, count = 0; count < size; i++, count++) {
      floats[count] = Float.parseFloat(strings[i]);
    }
    return floats;
  }

}
