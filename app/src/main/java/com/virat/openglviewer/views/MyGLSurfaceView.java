package com.virat.openglviewer.views;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MyGLSurfaceView extends GLSurfaceView {
  private static final float TOUCH_SCALE_FACTOR = 180.0f / 320;

  private  MyGLRenderer renderer;
  private float previousX;
  private float previousY;
  private long downTime;

  public MyGLSurfaceView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setEGLContextClientVersion(2);
  }

  public void setRenderer(MyGLRenderer renderer) {
    this.renderer = renderer;
    super.setRenderer(renderer);
    setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    // MotionEvent reports input details from the touch screen
    // and other input controls. In this case, you are only
    // interested in events where the touch position changed.
    float x = event.getX();
    float y = event.getY();

    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        downTime = event.getEventTime();
        break;
      case MotionEvent.ACTION_MOVE:
        float dx = x - previousX;
        float dy = y - previousY;

        renderer.dy += (int) ((dy) * TOUCH_SCALE_FACTOR);
        renderer.dx += (int) ((dx) * TOUCH_SCALE_FACTOR);
        requestRender();

        requestRender();
        break;

      case MotionEvent.ACTION_UP:
        if (event.getEventTime() - downTime <= 250) {
          renderer.onClick(x, y);
          requestRender();

          callOnClick();
        }
        break;
    }
    previousX = x;
    previousY = y;

    return true;
  }

  public void setOnRenderListener(MyGLRenderer.OnRenderListener onRenderListener) {
    renderer.setOnRenderListener(onRenderListener);
  }
}
