package com.virat.openglviewer.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import com.virat.openglviewer.R;
import com.virat.openglviewer.utils.Logger;
import com.virat.openglviewer.views.MyGLRenderer;
import com.virat.openglviewer.views.MyGLSurfaceView;

public class MainActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Logger.plant(new Logger.AndroidTree()); // Hook up logging

    final MyGLSurfaceView surfaceView = (MyGLSurfaceView) findViewById(R.id.glSurfaceView);

    // This is called on the very first render pass, when the scene
    // is rendered for the first time.
    surfaceView.setOnRenderListener(new MyGLRenderer.OnRenderListener() {
      @Override
      public void onRender(final Bitmap bitmap) {
        // Use the bitmap in the mode chooser
        runOnUiThread(new Runnable() {
          @Override
          public void run() {

            View view = findViewById(R.id.progressBar);
            view.animate().x(-view.getWidth()).setDuration(500).start();
          }
        });
      }
    });
  }
}
