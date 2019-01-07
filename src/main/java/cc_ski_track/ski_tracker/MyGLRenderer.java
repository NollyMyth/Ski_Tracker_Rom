package cc_ski_track.ski_tracker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;



import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

import cc_ski_track.ski_tracker.TextureHelper;
import cc_ski_track.ski_tracker.Examples.Square;
import cc_ski_track.ski_tracker.Examples.Triangle;
import cc_ski_track.ski_tracker.Examples.Arthur;

public class MyGLRenderer implements GLSurfaceView.Renderer {
    private Square mSquare;
    //private Triangle mTriangle;
    private Arthur mArthur;
    private final float[] mMVPMatrix = new float[16];// mMVPMatrix="Model View Projection Matrix"
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private float[] mRotationMatrix = new float[16];
    public volatile float mAngle;
    /** TEST TEXTURE */
    private Context context;
    private int mTextureDataHandle;
    private int mProgramHandle;
    private int mTextureCoordinateHandle;
    /** FIN TEST TEXTURE */
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor((float)30/255, (float)144/255, 1.0f, 1.0f);
        //Initialisation et load du triangle
        //mTriangle = new Triangle();
        //Initialisation et load du carré
        mSquare = new Square();
        mArthur = new Arthur();
        this.context = context;
        mTextureDataHandle = TextureHelper.loadTexture(context,R.drawable.montagne_nb);
    }

    public void onDrawFrame(GLES20 unused) {
        float[] touche = new float[16];
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle,);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mTextureDataHandle);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Create a rotation transformation for the triangle

        Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *MUST BE FIRST* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(touche, 0, mMVPMatrix, 0, mRotationMatrix, 0);
        //mTriangle.draw();
        //mArthur.draw();
        mArthur.draw(touche);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height); //centre, largeur, hauteur de l'écran
        float ratio = (float) width / (float) height;
        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    //permet de compiler le code shader avant d'utiliser l'environnement OpenGL ES
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }


    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float angle) {
        mAngle = angle;
    }



}