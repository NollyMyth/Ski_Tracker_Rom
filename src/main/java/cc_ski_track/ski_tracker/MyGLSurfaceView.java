package cc_ski_track.ski_tracker;
/*A GLSurfaceView is a specialized view where you can draw OpenGL ES graphics.
 *It does not do much by itself. The actual drawing of objects is controlled
 *in the GLSurfaceView.Renderer that you set on this view. */

import android.opengl.GLSurfaceView;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;
import android.util.Log;

class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;
//    private Context context;
    private long startTime;
    private int clickCount;
    private long duration;
    static final int MAX_DURATION = 200;

    public MyGLSurfaceView(Context context){
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new MyGLRenderer(context);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);
        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public MyGLSurfaceView(Context context, AttributeSet attribs){
        super(context, attribs);
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new MyGLRenderer(context);

        // Set the Renderer for drawing on the GLSurfaceView
        //setRenderer(mRenderer);
        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    @Override
    public boolean onTouchEvent(MotionEvent e){
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();
        float zx;
        float zy;
        switch (e.getAction()){
            case MotionEvent.ACTION_MOVE:
                float dx = -x + mPreviousX;
                float dy = -y + mPreviousY;
                // up vertical move
                if (dy > 0 && dy > dx) {
                    mRenderer.setAngle_v(
                            mRenderer.getAngle_v() +
                                    (-0.1f * (dx + dy) * TOUCH_SCALE_FACTOR));
                }
                // down vertical move
                if (dy < 0 && dy < dx) {
                    mRenderer.setAngle_v(
                            mRenderer.getAngle_v() +
                                    (0.1f * (dx - dy) * TOUCH_SCALE_FACTOR));
                }
                // right horizontal move
                if (dx > 0 && dx > dy) {
                    mRenderer.setAngle_h(
                            mRenderer.getAngle_h() +
                                    (-0.1f * (dx + dy) * TOUCH_SCALE_FACTOR));
                }
                // left horizontal move
                if (dx < 0 && dx < dy) {
                    mRenderer.setAngle_h(
                            mRenderer.getAngle_h() +
                                    (-0.1f * (dx + dy) * TOUCH_SCALE_FACTOR));
                }
                requestRender();
                break;

            case MotionEvent.ACTION_UP:
                startTime = System.currentTimeMillis();
                clickCount++;
                break;

            case MotionEvent.ACTION_DOWN:
                long time = System.currentTimeMillis()-startTime;
                clickCount++;
                if(clickCount == 2){
                    if (time <= MAX_DURATION){
                        zx=e.getX();
                        zy=e.getY();
                        mRenderer.setZoom();
                        Log.d("DOUBLE TAP", " temps: " + time);
                    }
                }

                clickCount = 0;
                break;

        }
        mPreviousX = x;
        mPreviousY = y;
        return true;
    }
}