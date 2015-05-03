package com.virat.openglviewer.utils;

import com.virat.openglviewer.pojos.Meshes;
import com.virat.openglviewer.pojos.OBJConverter;
import com.virat.openglviewer.pojos.OBJLoader;

import junit.framework.TestCase;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class OBJLoaderTest extends TestCase{

  @Test
  public void test_loadMeshes_shouldReturn_nonNullMeshes() {
    // Given
    OBJLoader loader = new OBJLoader(new OBJConverter());

    // When
    Meshes meshes = loader.loadMeshes(new File("/Users/viratsingh/Downloads/AppOpenGL/app/src/main/res/raw/spiderman_obj"));

    // Then
    assertTrue(meshes != null);
  }
}