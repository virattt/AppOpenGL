package com.virat.openglviewer.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import com.virat.openglviewer.R;
import com.virat.openglviewer.pojos.Meshes;
import com.virat.openglviewer.pojos.OBJConverter;
import com.virat.openglviewer.utils.Logger;
import com.virat.openglviewer.views.MyGLRenderer;
import com.virat.openglviewer.views.MyGLSurfaceView;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Logger.plant(new Logger.AndroidTree()); // Hook up logging

    final MyGLSurfaceView surfaceView = setupSurfaceViewRenderer(getMeshes());

    // This is called on the very first render pass, when the scene
    // is rendered for the first time.
    surfaceView.setOnRenderListener(new MyGLRenderer.OnRenderListener() {
      @Override
      public void onRender(final Bitmap bitmap) {
        // Use the bitmap in the mode chooser
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            View progressView = findViewById(R.id.progressBar);
            progressView.animate().x(-progressView.getWidth()).setDuration(500).start();
          }
        });
      }
    });
  }

  private Meshes getMeshes() {
    try {
      InputStream obj = getResources().getAssets().open("spiderman.obj"); // hardcoded for testing
      InputStream mtl = getResources().getAssets().open("spiderman.mtl"); // hardcoded for testing
      OBJConverter converter = new OBJConverter();
      return new Meshes(converter.parse(obj, mtl));

    } catch (IOException e) {
      Logger.error(e.getMessage());
    }
    return null;
  }

  private MyGLSurfaceView setupSurfaceViewRenderer(Meshes meshes) {
    final MyGLSurfaceView surfaceView = (MyGLSurfaceView) findViewById(R.id.glSurfaceView);
    Rect size = new Rect();
    surfaceView.getWindowVisibleDisplayFrame(size);

    MyGLRenderer renderer = new MyGLRenderer(
        this, meshes, size.right - size.left, size.bottom - size.top
    );
    surfaceView.setRenderer(renderer);

    return surfaceView;
  }
}
