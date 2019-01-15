package cc_ski_track.ski_tracker.Examples;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;
import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import cc_ski_track.ski_tracker.MyGLRenderer;
import cc_ski_track.ski_tracker.R;
import cc_ski_track.ski_tracker.Shader;
import cc_ski_track.ski_tracker.Examples.Geometry.Vec3;

public class Mountain implements Shader {
    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    private FloatBuffer normalBuffer;
    private int mMVMatrixHandle;
    // Map Mountain
    private static int Nc = 200; //largeur
    private static int Nv = 200; //hauteur
    private static float coords[] = new float[Nc*Nv*3];
    private static short drawOrders[] = new short[(Nc-1)*(Nv-1)*6];

    // Map geometry
    private static ArrayList<Vec3> verticies = new ArrayList<>();
    private static float normals[] = new float[3*Nc*Nv];

    // number of coordinates per vertex in this array
    private static final int COORDS_PER_VERTEX = 3;
    // Set color with red, green, blue and alpha (opacity) values
    private float color[] = { 1.0f, 1.0f, 1.0f, 1.0f };

    // Use to access and set the view transformation
    private static int mMVPMatrixHandle;

    private final int mProgram;

    /** Fonction qui crée un tableau avec les coordonnées des vertices pour une Bitmap définie
     * @param icon
     */
    private void generateTerrain(Bitmap icon) {
        int taille = icon.getWidth();
        int k = 0;
        for (int i = 0; i < Nv; i++) {
            for (int j = 0; j < Nc; j++) {
                coords[k*3]=(-1 + (2*(float)j/(Nc-1)));                                       //Coordonnée en X
                coords[k*3 + 1]=(1 - (2*(float)i/(Nv-1)));                                    //Coordonnée en Y
                coords[k*3 + 2]= -2*(float)getIntensity(i*taille/Nc,j*taille/Nv,icon)/255;  //Coordonnée en Z
                verticies.add(new Vec3(coords[k * 3],coords[k * 3 + 1],coords[k * 3 + 2]));
                k++;
            }
        }
        k=0;
        for (int i = 0; i < Nv; i++) {
            for (int j = 0; j < Nc; j++) {
                normalMap(i, j, k);
                k++;
            }
        }
        int n = 0;
        for (int j = 0; j < Nv-1; j++) {
            for (int i = 0; i < Nc-1; i++) {
                // Romain le stremon
                drawOrders[n * 6]     = (short) (i + j * Nc);
                drawOrders[n * 6 + 1] = (short) (Nc * j + i + 1);
                drawOrders[n * 6 + 2] = (short) (Nc * j + i + Nc + 1);
                drawOrders[n * 6 + 3] = (short) (Nc * j + i);
                drawOrders[n * 6 + 4] = (short) (Nc * j + i + Nc + 1);
                drawOrders[n * 6 + 5] = (short) (Nc * j + i + Nc);
                n++;
            }
        }
    }

    /** Fonction qui la normale d'un vertex     */
    private void normalMap(int i, int j, int n){
        // On prend que les vertices centraux
        if((i+j*Nc)>Nc && (i+j*Nc)<(Nv-1)*Nc-1 && (i+j*Nc)%Nc!=0 && (i+j*Nc)%Nc!=3) {
            //vecteur entre vertex de gauche et de droite
            Vec3 x = verticies.get(i - 1 + j * Nc).minus(verticies.get(i + 1 + j * Nc));
            //vecteur entre vertex du haut et du bas
            Vec3 y = verticies.get(i + (j - 1) * Nc).minus(verticies.get(i + (j + 1) * Nc));
            // on calcule la normale suivant x
            Vec3 nx = x.cross(new Vec3(0, 1, 0));
            // on calcule la normale suivant x
            Vec3 ny = y.cross(new Vec3(1, 0, 0));
            // on fait la somme des deux
            Vec3 vecNormal = nx.sum(ny);
            vecNormal.normalize();
            // on stocke les données dans un tableau pour le shader
            normals[n * 3]     = vecNormal.x;
            normals[n * 3 + 1] = vecNormal.y;
            normals[n * 3 + 2] = vecNormal.z;

            if(n%38974 == 0) {
                String co =  n  + " \nx.x:" + x.x + "; x.y:" + x.y + "; x.z:" +x.z + "\n" +
                         "y.x:" + y.x + "; y.y:" + y.y + "; y.z:" +y.z + "\n" +
                         "nx.x:" + nx.x + "; nx.y:" + nx.y + "; nx.z:" +nx.z + "\n" +
                         "ny.x:" + ny.x + "; ny.y:" + ny.y + "; ny.z:" +ny.z + "\n" +
                         "vecNormal.x: " + vecNormal.x + "; vecNormal.y: " + vecNormal.y + "; vecNormal.z: " +vecNormal.z + "\n" +
                         "normals[n*3]:" + normals[n * 3] + "; normals[n*3 + 1]:" + normals[n * 3 + 1] + "; normals[n*3+2]:" +normals[n * 3 + 2] + "\n";
                Log.d("NORMALE", co);
            }
        }
        else {
            normals[n * 3] = 0;
            normals[n * 3 + 1] = 0;
            normals[n * 3 + 2] = 1;
        }
    }

    /** Fonction qui prend l'intensité moyenne du pixel (x,y) d'une Bitmap
     * qui sera ensuite utilisé comme hauteur */
    private int getIntensity(int X, int Y,Bitmap icon) {
        int pixel = icon.getPixel(X, Y);
        int redBucket = Color.red(pixel);
        int greenBucket = Color.green(pixel);
        int blueBucket = Color.blue(pixel);
        int moyenne = (greenBucket+redBucket+blueBucket)/3;
        return (moyenne);
    }


    /***********************************************************************************************/
    public Mountain(Context context){

        Context c = context;
        Bitmap icon = BitmapFactory.decodeResource(c.getResources(), R.drawable.valmeinier);
        generateTerrain(icon);
//        generateNormal();

        // initialize NORMAL byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4); GLES20.glGetError();//  4 bytes per float
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer(); GLES20.glGetError();
        vertexBuffer.put(coords); GLES20.glGetError();
        vertexBuffer.position(0); GLES20.glGetError();

        // initialize VERTEX byte buffer for shape normals
        ByteBuffer normalbuffer = ByteBuffer.allocateDirect(normals.length * 4); GLES20.glGetError();//  4 bytes per float
        bb.order(ByteOrder.nativeOrder());
        normalBuffer = normalbuffer.asFloatBuffer(); GLES20.glGetError();
        normalBuffer.put(normals); GLES20.glGetError();
        normalBuffer.position(0); GLES20.glGetError();

        // initialize byte buffer for the ORDER list
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrders.length * 2); GLES20.glGetError();//  2 bytes per short
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();GLES20.glGetError();
        drawListBuffer.put(drawOrders);GLES20.glGetError();
        drawListBuffer.position(0);GLES20.glGetError();



        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, Shader.vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,Shader.fragmentShaderCode);

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();
        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);
        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);
        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);
    }

    private static int mPositionHandle;
    private static int mColorHandle;
    private static int mNormalHandle;

    private static final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    public void draw(float[] mvpMatrix) { // pass in the calculated transformation matrix

        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to vertex shader's vNormal member
        mNormalHandle = GLES20.glGetAttribLocation(mProgram, "vNormal");
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, normalBuffer);

//        mMVMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVMatrix");
//        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mvpMatrix, 0);


        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        // Draw the triangles

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrders.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer ); GLES20.glGetError();

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }


}
