package com.virat.openglviewer.views;

import android.content.Context;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MyGLSurfaceView extends GLSurfaceView {
  private static final float TOUCH_SCALE_FACTOR = 180.0f / 320;

  private final MyGLRenderer mRenderer;
  private float mPreviousX;
  private float mPreviousY;
  private long downTime;

  public MyGLSurfaceView(Context context, AttributeSet attrs) {
    super(context, attrs);

    Rect size = new Rect();
    if (!isInEditMode()) {
      getWindowVisibleDisplayFrame(size);
    }

    setEGLContextClientVersion(2);

    mRenderer = new MyGLRenderer(context, size.right - size.left, size.bottom - size.top);
    setRenderer(mRenderer);
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
        float dx = x - mPreviousX;
        float dy = y - mPreviousY;

        mRenderer.setAngleX(mRenderer.getAngleX() + ((dx) * TOUCH_SCALE_FACTOR));  // = 180.0f / 320
        mRenderer.setAngleY(mRenderer.getAngleY() + ((dy) * TOUCH_SCALE_FACTOR));  // = 180.0f / 320
        requestRender();
        break;
    }
    mPreviousX = x;
    mPreviousY = y;

    return true;
  }

  public void setOnRenderListener(MyGLRenderer.OnRenderListener onRenderListener) {
    mRenderer.setOnRenderListener(onRenderListener);
  }
}
