package com.virat.openglviewer.pojo;


import com.virat.openglviewer.objects.TextureObject;
import com.virat.openglviewer.utils.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class OBJParser {
  private int vertex_count = 0;
  private int texture_count = 0;
  private int face_count = 0;

  private float[] maxVertexVals;
  private float[] minVertexVals;

  private float[][] vertices;
  private float[][] textures;

  public OBJParser() {
    maxVertexVals = new float[]{Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE};
    minVertexVals = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
  }

  public List<TextureObject> parse(InputStream inputStream) throws IOException {
    Logger.info("Entered parse...");
    List<TextureObject> objectList = new ArrayList<>();

    int vertexIndex = 0;
    int textureIndex = 0;

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
      String line;

      // Read the info (for debugging purposes?)
      inputStream.mark(inputStream.available());
      getOBJinfo(reader);
      inputStream.reset();

      Logger.info("Starting while loop...");
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("v ")) {
          String[] vertex_array = line.substring(2).split("\\s+");

          if (line.startsWith("v  ")) {
            vertex_array = line.substring(3).split("\\s+");
          }

          vertices[vertexIndex][0] = Float.parseFloat(vertex_array[0]);
          vertices[vertexIndex][1] = Float.parseFloat(vertex_array[1]);
          vertices[vertexIndex][2] = Float.parseFloat(vertex_array[2]);

          vertexIndex++;
        } else if (line.startsWith("vt")) {
          String[] texture_array = line.substring(3).split("\\s+");

          textures[textureIndex][0] = Float.parseFloat(texture_array[0]);
          textures[textureIndex][1] = 1f - Float.parseFloat(texture_array[1]);
          textureIndex++;
        } else if (line.startsWith("usemtl")) {
          Logger.info("Completed vertex/texture parsing");

          ArrayList<String> faceList = new ArrayList<>();
          while ((line = reader.readLine()) != null) {
            if (line.startsWith("usemtl")) {
              reader.reset();
              break;
            }

            if (line.startsWith("f ")) {
              String face = line.substring(2);
              faceList.add(face);
              reader.mark(face_count);
            }
          }
          float[] vertexData = parseFaces(faceList);
          objectList.add(new TextureObject(vertexData, minVertexVals, maxVertexVals));
        }
      }
    } catch (FileNotFoundException e) {
      Logger.error("FileNotFoundException: " + e);
    } catch (IOException e) {
      Logger.error("IOException: " + e);
    }

    Logger.info("Simple Object list size: %s", objectList.size());
    return objectList;
  }

  /**
   * Takes a list of face indices (String) and converts/returns the list as a float[] array.
   * The array is ordered as [x, y, z, S, T, x, y, z, S, T,...]
   *
   * @param faceList
   */
  private float[] parseFaces(ArrayList<String> faceList) throws IOException {
    Logger.info("Entered parseFaces...");
    ArrayList<Tuple> faceIndices = new ArrayList<>();

    for (String face : faceList) {  // eg. face = "1/2/3 4/5/6 7/8/9"
      String[] face_coordinates = face.split("\\s+"); // eg. face_coordinates = ["1/2/3",
      // "4/5/6", "7/8/9"]

      // Faces are defined by vertex/texture/normal indices
      // For example 1/2/3:
      //  1 - The vertex index of the face
      //  2 - The texture index
      //  3 - The normal index
      for (String coordinate : face_coordinates) { // eg. coordinate = "1/2/3"
        String[] vertexTextureNormal;

        // Check for either one / or two //
        if (coordinate.contains("//")) {
          vertexTextureNormal = coordinate.split("//");
        } else {
          vertexTextureNormal = coordinate.split("/");
        }


        int vertexIndex = Integer.parseInt(vertexTextureNormal[0]);
        int textureIndex = Integer.parseInt(vertexTextureNormal[1]);

        faceIndices.add(new Tuple(vertexIndex, textureIndex));
      }
    }

    float[] faces = new float[faceIndices.size() * 5];
    int stride = 0;

    for (int i = 0; i < faceIndices.size(); i++) {
      Tuple tuple = faceIndices.get(i);

      float[] vertice = vertices[tuple.vertexIndex - 1];
      float[] texture = textures[tuple.textureIndex - 1];

      faces[stride++] = vertice[0];
      faces[stride++] = vertice[1];
      faces[stride++] = vertice[2];

      faces[stride++] = texture[0];
      faces[stride++] = texture[1];

      maxVertexVals[0] = vertice[0] > maxVertexVals[0] ? vertice[0] : maxVertexVals[0];
      maxVertexVals[1] = vertice[1] > maxVertexVals[1] ? vertice[1] : maxVertexVals[1];
      maxVertexVals[2] = vertice[2] > maxVertexVals[2] ? vertice[2] : maxVertexVals[2];

      minVertexVals[0] = vertice[0] < minVertexVals[0] ? vertice[0] : minVertexVals[0];
      minVertexVals[1] = vertice[1] < minVertexVals[1] ? vertice[1] : minVertexVals[1];
      minVertexVals[2] = vertice[2] < minVertexVals[2] ? vertice[2] : minVertexVals[2];
    }

    return faces;
  }

  class Tuple {
    final int vertexIndex;
    final int textureIndex;

    Tuple(int vertexIndex, int textureIndex) {
      this.vertexIndex = vertexIndex;
      this.textureIndex = textureIndex;
    }
  }

  private void getOBJinfo(BufferedReader reader) {

    try {
      String line;

      while ((line = reader.readLine()) != null) {
        if (line.startsWith("v ")) {
          ++vertex_count;
        } else if (line.startsWith("vt")) {
          ++texture_count;
        } else if (line.startsWith("f")) {
          ++face_count;
        }
      }

      vertices = new float[vertex_count][3];
      textures = new float[texture_count][2];

    } catch (IOException e) {
      Logger.error("IOException: " + e);
    }

    Logger.debug("File has: %d vertices, %d texture coords, and %d faces", vertex_count,
      texture_count, face_count);
  }
}
