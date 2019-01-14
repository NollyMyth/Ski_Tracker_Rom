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
import cc_ski_track.ski_tracker.Examples.Geometry.Triangle;
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
    private static ArrayList<Vec3> vNormale = new ArrayList<>();
    private static float normals[] = new float[3*Nc*Nv];

    // number of coordinates per vertex in this array
    private static final int COORDS_PER_VERTEX = 3;
    // Set color with red, green, blue and alpha (opacity) values
    private float color[] = { 1.0f, 0.0f, 0.0f, 1.0f };

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
        for (int i =0; i<Nc*Nv; i++){
            vNormale.add(new Vec3(0,0,0));
        }
        int n = 0;
        for (int j = 0; j < Nv-1; j++) {
            for (int i = 0; i < Nc-1; i++) {
                normalMap(i,j);
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

    /** Fonction qui ajoute calcul les normales de du triangle (i,j) puis crée une ArrayList
     * pour donner à chaque vertex une normale.
     * @param i
     * @param j
     */
    private void normalMap(int i, int j){
        // Triangle 1
        Triangle t1 = new Triangle(verticies.get(i + j * Nc),
                                   verticies.get(Nc * j + i + 1),
                                   verticies.get(Nc * j + i + Nc + 1));
        Vec3 edge1 = t1.v2.minus(t1.v1);
        Vec3 edge2 = t1.v3.minus(t1.v1);
        Vec3 n1 = edge1.cross(edge2);
        n1.normalize();
        vNormale.get(i + j * Nc).sum(n1);
        vNormale.get(Nc * j + i + 1).sum(n1);
        vNormale.get(Nc * j + i + Nc + 1).sum(n1);
        // Triangle 2
        Triangle t2 = new Triangle(verticies.get(Nc * j + i),
                                   verticies.get(Nc * j + i + Nc + 1),
                                   verticies.get(Nc * j + i + Nc));
        Vec3 edge_1 = t2.v2.minus(t2.v1);
        Vec3 edge_2 = t2.v3.minus(t2.v1);
        Vec3 n2 = edge_1.cross(edge_2);
        n2.normalize();
        vNormale.get(Nc * j + i).sum(n2);
        vNormale.get(Nc * j + i + Nc + 1).sum(n2);
        vNormale.get(Nc * j + i + Nc).sum(n2);
    }

    /** Fonction qui prend l'intensité moyenne du pixel (x,y) d'une Bitmap */
    private int getIntensity(int X, int Y,Bitmap icon) {
        int pixel = icon.getPixel(X, Y);
        int taille = icon.getWidth();
        int redBucket = Color.red(pixel);
        int greenBucket = Color.green(pixel);
        int blueBucket = Color.blue(pixel);
        int moyenne = (greenBucket+redBucket+blueBucket)/3;
//        String co = "moyenne "+ moyenne + ", taille " + taille;
//        Log.d("PIXEL", co);
        return (moyenne);
    }

    /** Fonction qui crée un tableau de NORMALES à envoyer au Shader*/
    private void generateNormal(){
        int l = 0;
        for(Vec3 n:vNormale){
            n.normalize();
            normals[l*3]     = n.x;
            normals[l*3 + 1] = n.y;
            normals[l*3 + 2] = n.z;
            l++;
        }
    }

    /***********************************************************************************************/
    public Mountain(Context context){

        Context c = context;
        Bitmap icon = BitmapFactory.decodeResource(c.getResources(), R.drawable.montblanc);
        generateTerrain(icon);
        generateNormal();

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4); GLES20.glGetError();//  4 bytes per float
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer(); GLES20.glGetError();
        vertexBuffer.put(coords); GLES20.glGetError();
        vertexBuffer.position(0); GLES20.glGetError();

        // initialize vertex byte buffer for shape normals
        ByteBuffer normalbuffer = ByteBuffer.allocateDirect(normals.length * 4); GLES20.glGetError();//  4 bytes per float
        bb.order(ByteOrder.nativeOrder());
        normalBuffer = normalbuffer.asFloatBuffer(); GLES20.glGetError();
        normalBuffer.put(normals); GLES20.glGetError();
        normalBuffer.position(0); GLES20.glGetError();

        // initialize byte buffer for the draw list
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

        // get handle to vertex shader's vPosition member
        mNormalHandle = GLES20.glGetAttribLocation(mProgram, "vNormal");
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

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
