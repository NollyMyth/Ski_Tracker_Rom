package cc_ski_track.ski_tracker.Examples;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import cc_ski_track.ski_tracker.MyGLRenderer;
import cc_ski_track.ski_tracker.R;

import cc_ski_track.ski_tracker.Examples.Mountain;


public class Trace {
    private Context context;

    public List<Double> latitude = new ArrayList<>();
    public List<Double> longitude = new ArrayList<>();
    private List<Float> altitude = new ArrayList<>();
    private List<Float> distance = new ArrayList<>();
    private List<Integer> time = new ArrayList<>();
    private List<Integer> dt = new ArrayList<>();
    private List<Float> speed = new ArrayList<>();

    private final float X0 = 6.39249f;
    private final float Y0 = 45.10769f;
    private final float X1 = 6.54551f;
    private final float Y1 = 45.21548f;

    private double[] traceData;
    private static int Nc = 200;
    private static int Nv = 200;

    private final int[] vbo = new int[1];
    private double max_alt = 0;

    private void xmlReader(final int id){
        try{
            XmlPullParser xpp = context.getResources().getXml(id);
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT){
                if (xpp.getEventType() == XmlPullParser.START_TAG){
                    if(xpp.getName().equals("Latitude")){
                        xpp.next();
                        double lat = Float.parseFloat(xpp.getText());
                        latitude.add(lat);
                    }
                    else if(xpp.getName().equals("Longitude")){
                        xpp.next();
                        double lon = Float.parseFloat(xpp.getText());
                        longitude.add(lon);
                    }
                    else if(xpp.getName().equals("Altitude")){
                        xpp.next();
                        float alt = Float.parseFloat(xpp.getText());
                        if(alt>max_alt){
                            max_alt = alt;
                        }
                        altitude.add(alt);
                    }
                    else if(xpp.getName().equals("Time")){
                        xpp.next();
                        String s = xpp.getText();
                        String[] s_split = s.split("T"); //split date and time int 2 string
                        s_split[1] = s_split[1].substring(0,s_split[1].length()-1); //delete "Z" of the time
                        String[] times = s_split[1].split(":");
                        //Log.d("TIME :", time[0]+"h "+time[1]+"m "+time[2]+"s");
                        int t = Integer.parseInt(times[0])*3600+Integer.parseInt(times[1])*60+Integer.parseInt(times[2]);
                        time.add(t);
                    }
                }
                xpp.next();
            }
        }
        catch (Throwable t){
            Log.d("Request failed : ", t.toString());
        }
    }

    /**
     * Calculate each distance between 2 geospace coordinate
     *      - Fill the distance vector
     *      - Sum distance to return the full distance of the track in km
     * @return h: distance of the full track
     */
    private double maxDist(){
        double h = 0;
        for(int i = 0; i<longitude.size()-1; i++){
            double lo_1 = longitude.get(i);
            double la_1 = latitude.get(i);
            double al_1 = altitude.get(i);
            double lo_2 = longitude.get(i+1);
            double la_2 = latitude.get(i+1);
            double al_2 = altitude.get(i+1);
            // 1° displacement correspond to 111.6 km of displacement or we've seen and error of 20%
            double dis = Math.sqrt(Math.pow(la_1-la_2,2)+Math.pow(lo_1-lo_2,2))*89.28;
            double d = Math.sqrt(Math.pow(dis,2) + Math.pow((al_2-al_1)/1000,2)); // The we used pythagore
            h += d;
            distance.add((float)d*1000);
        }
        return h;
    }
    private void time_difference(){
        for(int i = 0; i<time.size()-1; i++){
            dt.add(time.get(i+1)-time.get(i));
        }
    }
    private float maxSpeed(){
        float max_v = 0;
        for (int i=0; i<dt.size(); i++){
            float v = distance.get(i)/(dt.get(i));
            speed.add(v);
            if(v>max_v){
                max_v = v;
            }
        }
        return max_v;
    }
    private float averageSpeed(){
        float s = 0;
        for(float v:speed){
            s += v;
        }
        return s/speed.size();
    }
    private void fillData(Bitmap map){
        int offset = 0;
        int size = map.getHeight();
        for (int i = 0; i<longitude.size(); i++){
            traceData[offset++]=2*((longitude.get(i)-X0)/(X1-X0))-1;
            traceData[offset++]=2*((latitude.get(i)-Y1)/(Y0-Y1))-1;
            traceData[offset++]=0.0f;
        }
    }

    private static int mMVPMatrixHandle;
    private static int vMatrixHandle;
    private static int mMatrixHandle;
    private final int mProgram;

    public Trace(Context c){
        this.context = c;
        xmlReader(R.xml.move);
        double max_d = maxDist();
        time_difference();
        float max_v = maxSpeed()*3.6f;
        float av_speed = averageSpeed()*3.6f;
        //
        Log.d("SIZE :", Integer.toString(altitude.size()));
        Log.d("SIZE :", Integer.toString(longitude.size()));
        Log.d("SIZE :", Integer.toString(latitude.size()));
        Log.d("Distance = ", Double.toString(max_d));
        Log.d("Distance SIZE = ", Integer.toString(distance.size()));
        Log.d("Max speed = ", Float.toString(max_v));
        Log.d("Average speed :", Float.toString(av_speed));
        //
        traceData = new double[longitude.size()*3];
        Bitmap map = BitmapFactory.decodeResource(context.getResources(),R.drawable.val);
        fillData(map);

        //----------------------------------------------------------------------------------
        final FloatBuffer traceVertexDataBuffer = ByteBuffer
                .allocateDirect(traceData.length * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        //traceVertexDataBuffer.put((float)traceData).position(0);
        // Generate vbo and ibo
        GLES20.glGenBuffers(1, vbo, 0);
        if (vbo[0] > 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, traceVertexDataBuffer.capacity() * 4,
                    traceVertexDataBuffer, GLES20.GL_STATIC_DRAW);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
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

    }
}
