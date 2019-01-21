package cc_ski_track.ski_tracker;
/** This class controls what gets drawn on the GLSurfaceView with which it is associated.
 * There are three methods in a renderer that are called by the Android system in order to
 * figure out what and how to draw on a GLSurfaceView:*/

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

import cc_ski_track.ski_tracker.Examples.Mountain;
import cc_ski_track.ski_tracker.Examples.Square;
import cc_ski_track.ski_tracker.Examples.Trace;

public class MyGLRenderer implements GLSurfaceView.Renderer {
    private Context context;
    private Square my_square;
    private Mountain my_map;
    private Trace my_trace;
    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private float[] mRotationMatrix = new float[16];
    private float[] mRotationMatrix2 = new float[16];

    /*******************************************************************
     * Methods
     *******************************************************************/
    /**
     * Called once to set up the view's OpenGL ES environment.
     * @param unused
     * @param config
     */
    public void onSurfaceCreated(GL10 unused, EGLConfig config){
        // Set the background frame color
        GLES20.glClearColor(0.4666666667f, 0.709803922f, 0.996078431f, 1.0f);
        // Intialize shapes
        my_square = new Square();
        my_map = new Mountain(context);
        //my_trace = new Trace(context);
    }
    /**
     * Called for each redraw of the view.
     * @param
     */
    public void onDrawFrame(GL10 unused) {
        float[] scratch = new float[16];
        // Redraw background color
        GLES20.glClearDepthf(2f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glLineWidth(10f);
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0 , -2.5f, -4.5f,
                0f, 0f, 0f, 0f, -1.0f, 0.0f);
        Matrix.translateM(mViewMatrix,0,0.0f,zoom*-0.5f,zoom*-1.5f);
        // Create a rotation transformation for the triangle
        //long time = SystemClock.uptimeMillis()%4000L;
        //float angle = 0.090f * ((int) time);
        Matrix.setRotateM(mRotationMatrix, 0, Angle_h, 0.0f, -1.0f, 0.0f);
        Matrix.setRotateM(mRotationMatrix2, 0, Angle_v,
            1.0f*(float)Math.cos(Math.toRadians(Angle_h)), 0.0f, -1.0f*(float)Math.sin(Math.toRadians(Angle_h)));
        //Combine the rotation matrix with the projection and camera view
        //Note that the mMVPMatrix factor *must be first* in order
        //for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mViewMatrix, 0, mRotationMatrix, 0);
        Matrix.multiplyMM(scratch, 0, scratch, 0, mRotationMatrix2, 0);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, scratch, 0);
        //GLES20.glClearDepthf(-2000f);
        //GLES20.glCullFace(GLES20.GL_FRONT);
        // Draw shapes
        my_map.draw(mMVPMatrix,scratch,mProjectionMatrix);
    }
    /**
     * Called if the geometry of the view changes, for example when the device's screen orientation changes.
     * @param unused
     * @param width
     * @param height
     */
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float)width/height;
        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    /********************************************************************************************
     * Load shaders for any shapes
     * @param type
     * @param shaderCode
     * @return
     */
    public static int loadShader(int type, String shaderCode){
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
     * Constructeur
     * @param context
     */
    public MyGLRenderer(Context context){
        this.context = context;
    }
    /**********************************************************************************************
     * Getter and Setter
     */
    private volatile float Angle_h = 90f;
    public float getAngle_h(){
        return Angle_h;
    }
    public void setAngle_h(float angle){
        Angle_h = angle;
    }
    private volatile float Angle_v = 0f;
    public float getAngle_v(){
        return Angle_v;
    }
    public void setAngle_v(float angle){
        if (angle>=90) {
            Angle_v = 90;
        }
        else if (angle<-50){
            Angle_v = -50;
        }
        else
            Angle_v = angle;
            Log.d("ANGLE", ": " + angle);
    }

    private volatile float zoom = 0.0f;
    public void setZoom(){
        if (zoom == 0.0f)
            zoom = 1.0f;
        else
            zoom = 0.0f;
    }
}