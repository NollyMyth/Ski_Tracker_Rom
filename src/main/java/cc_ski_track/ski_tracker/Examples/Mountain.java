package cc_ski_track.ski_tracker.Examples;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import cc_ski_track.ski_tracker.MyGLRenderer;
import cc_ski_track.ski_tracker.R;
import cc_ski_track.ski_tracker.Examples.Trace;

public class Mountain implements Shader{

    private Context context;

    /**constants. */
    private static final int POSITION_DATA_SIZE_IN_ELEMENTS = 3;
    private static final int NORMAL_DATA_SIZE_IN_ELEMENTS = 3;
    private static final int COLOR_DATA_SIZE_IN_ELEMENTS = 4;
    private static final int BYTES_PER_FLOAT = 4;
    private static final int BYTES_PER_SHORT = 2;
    private final int floatsPerVertex = POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS
            + COLOR_DATA_SIZE_IN_ELEMENTS;
    private static final int STRIDE = (POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS + COLOR_DATA_SIZE_IN_ELEMENTS)
            * BYTES_PER_FLOAT;

    /** Map global variable */
    private static int Nc = 200;
    private static int Nv = 200;
    private final float[] heightMapVertexData = new float[Nc * Nv * floatsPerVertex];
    private static short drawOrders[] = new short[(Nc-1)*(Nv-1)*6];
    private final float[] lightPos = {0.0f,0.0f,-5.0f};
    private final int[] vertice = new int[9];
    /** VBO and IBO */
    private final int[] vbo = new int[1];
    private final int[] ibo = new int[1];
    // Set color with red, green, blue and alpha (opacity) values
    private float color[] = { 1.0f, 0.0f, 0.0f, 1.0f };

    /**
     * Generation of our map, basic plan with 1 vertices for each pixels of our height map
     * @param map hieght map of the "Mont Blanc" (generated with terrain.party)
     * @param normalmap normal map of our height map (generated with GIMP)
     */
    private void generateTerrain(Bitmap map, Bitmap normalmap) {
        //Build height map data
        int size = map.getWidth();
        int length = normalmap.getWidth();
//        String l = ":" + size;
//        Log.d("Taille", l);
        int offset = 0;
        for (int i = 0; i < Nv; i++) {
            for (int j = 0; j < Nc; j++) {
                // Position
                heightMapVertexData[offset++] = (-1 + (2 * (float) j / (Nc - 1))); // X coordinate
                heightMapVertexData[offset++] = 1.0f-1.5f*(float)getIntensity(i*size/Nc,j*size/Nv,map)/255; // Y coordinate
                heightMapVertexData[offset++] = (1.0f - (2 * (float) i / (Nv - 1)));  // Z coordinate

                // Normal
                float x = (float)(Color.red(normalmap.getPixel(i*length/Nc,j*length/Nv)))/255; // X coordinate
                float y = (float)(Color.green(normalmap.getPixel(i*length/Nc,j*length/Nv)))/255; // Y coordinate
                float z = (float)(Color.blue(normalmap.getPixel(i*length/Nc,j*length/Nv)))/255; // Z coordinate
                heightMapVertexData[offset++] = x;
                heightMapVertexData[offset++] = y;
                heightMapVertexData[offset++] = z;

                // Color
                float c = (x+y+z)/3;
                heightMapVertexData[offset++] = c;
                heightMapVertexData[offset++] = c;
                heightMapVertexData[offset++] = c;
                heightMapVertexData[offset++] = 1.0f;
            }
        }
        // Build height map draw order indexes
        int n = 0;
        for (int j = 0; j < Nv - 1; j++) {
            for (int i = 0; i < Nc - 1; i++) {
                // Romain le stremon
                drawOrders[n * 6] = (short) (i + j * Nc);
                drawOrders[n * 6 + 1] = (short) (Nc * j + i + 1);
                drawOrders[n * 6 + 2] = (short) (Nc * j + i + Nc + 1);
                drawOrders[n * 6 + 3] = (short) (Nc * j + i);
                drawOrders[n * 6 + 4] = (short) (Nc * j + i + Nc + 1);
                drawOrders[n * 6 + 5] = (short) (Nc * j + i + Nc);
                n++;
            }
        }
    }

    private void dessineTrace(List<Double> longitude, List<Double> latitude){
        for(int j = 0; j<longitude.size()-1; j++) {
            double longi = longitude.get(j);
            double lati = latitude.get(j);
            int vertice[] = calculCoord(longi,lati);
            for (int i = 0; i < 9; i++) {
                heightMapVertexData[vertice[0] * 10 + 6] = 1; //vertice de i
                heightMapVertexData[vertice[0] * 10 + 7] = 0;
                heightMapVertexData[vertice[0] * 10 + 8] = 0;
                i++;
            }
        }

    }
    private int getIntensity(int X, int Y,Bitmap icon) {
        int pixel = icon.getPixel(X, Y);
        int redBucket = Color.red(pixel);
        int greenBucket = Color.green(pixel);
        int blueBucket = Color.blue(pixel);
        int moyenne = (greenBucket+redBucket+blueBucket)/3;
        return (moyenne);
    }

    private int[] calculCoord(double longi, double lati){
        //Coordonnées du point haut droite
        double longi1 = 6.545519;
        double lati1 = 45.215488;
        //Coordonnées du point bas gauche
        double longi0 = 6.392493;
        double lati0 = 45.107691;
        //
        double j = (longi - longi0)/(longi1 - longi0)*200;
        double i = 200-(lati - lati1)/(lati0 - lati1)*200;
        if ( i<0 && i>200 && j<0 && j>200){
            Log.d("ERREUR","!!!!"+ i + j);
        }
        vertice[0]= (int)i     + (int)j*Nc;
        vertice[1]= (int)(i-2) + (int)j*Nc;
        vertice[2]= (int)(i-1) + (int)j*Nc;
        vertice[3]= (int)i     + (int)(j-2)*Nc;
        vertice[4]= (int)i     + (int)(j-1)*Nc;
        vertice[5]= (int)(i+2) + (int)j*Nc;
        vertice[6]= (int)(i+1) + (int)j*Nc;
        vertice[7]= (int)i     + (int)(j+2)*Nc;
        vertice[8]= (int)i     + (int)(j+1)*Nc;
        return vertice;
    }

    private static int mMVPMatrixHandle;
    private static int vMatrixHandle;
    private static int mMatrixHandle;
    private final int mProgram;
    /***********************************************************************************************/
    public Mountain(Context c){
        this.context = c;
        Trace matrace = new Trace(c);
        /* Read à height map picture from our resources to generate our map */
        Bitmap map = BitmapFactory.decodeResource(context.getResources(), R.drawable.val);
        Bitmap normalmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.normal);
        generateTerrain(map, normalmap);
        dessineTrace(matrace.longitude,matrace.latitude);
        // initialize vertex byte buffer for shape coordinates, normals and colors
        final FloatBuffer heightMapVertexDataBuffer = ByteBuffer
                .allocateDirect(heightMapVertexData.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        heightMapVertexDataBuffer.put(heightMapVertexData).position(0);
        // initialize byte buffer for the draw list
        final ShortBuffer heightMapIndexDataBuffer = ByteBuffer
                .allocateDirect(drawOrders.length * BYTES_PER_SHORT).order(ByteOrder.nativeOrder())
                .asShortBuffer();
        heightMapIndexDataBuffer.put(drawOrders).position(0);

        // Generate vbo and ibo
        GLES20.glGenBuffers(1, vbo, 0);
        GLES20.glGenBuffers(1, ibo, 0);
        if (vbo[0] > 0 && ibo[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, heightMapVertexDataBuffer.capacity() * BYTES_PER_FLOAT,
                    heightMapVertexDataBuffer, GLES20.GL_STATIC_DRAW);

            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, heightMapIndexDataBuffer.capacity()
                    * BYTES_PER_SHORT, heightMapIndexDataBuffer, GLES20.GL_STATIC_DRAW);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        } else {
            Log.d("ERREUR","!!!!");
        }

        // Initialized vertex and fragment shader
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, Shader.vertex_shader);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,Shader.fragment_shader);
        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();
        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);
        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);
        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);
    }

    /** OpenGL handles to our program attributes. */
    private int positionAttribute;
    private int normalAttribute;
    private int colorAttribute;
    public void draw(float[] mvpMatrix, float[] vMatrix, float[] mMatrix) { // pass in the calculated transformation matrix
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);
        // get handle to vertex shader's vPosition member
        positionAttribute = GLES20.glGetAttribLocation(mProgram, "vPosition");
        normalAttribute = GLES20.glGetAttribLocation(mProgram,"vNormal");
        colorAttribute = GLES20.glGetAttribLocation(mProgram, "vColor");
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        GLES20.glDepthMask(true);
        if (vbo[0] > 0 && ibo[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);

            // Bind Attributes
            GLES20.glVertexAttribPointer(positionAttribute, POSITION_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                    STRIDE, 0); // Prepare the triangle coordinate data
            GLES20.glEnableVertexAttribArray(positionAttribute); // Enable a handle to the triangle vertices

            GLES20.glVertexAttribPointer(normalAttribute, NORMAL_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                    STRIDE, POSITION_DATA_SIZE_IN_ELEMENTS * BYTES_PER_FLOAT);
            GLES20.glEnableVertexAttribArray(normalAttribute);

            GLES20.glVertexAttribPointer(colorAttribute, COLOR_DATA_SIZE_IN_ELEMENTS, GLES20.GL_FLOAT, false,
                    STRIDE, (POSITION_DATA_SIZE_IN_ELEMENTS + NORMAL_DATA_SIZE_IN_ELEMENTS) * BYTES_PER_FLOAT);
            GLES20.glEnableVertexAttribArray(colorAttribute);

            // Draw
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrders.length, GLES20.GL_UNSIGNED_SHORT, 0);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        }

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        vMatrixHandle = GLES20.glGetUniformLocation(mProgram, "V");
        GLES20.glUniformMatrix4fv(vMatrixHandle, 1, false, vMatrix, 0);
        mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "M");
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMatrix, 0);
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionAttribute);
        GLES20.glDisableVertexAttribArray(normalAttribute);
        GLES20.glDisableVertexAttribArray(colorAttribute);

    }
}
