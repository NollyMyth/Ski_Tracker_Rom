package cc_ski_track.ski_tracker;

public interface Shader {
     String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
//            "uniform mat4 uMVMatrix;" +
            "attribute vec4 vPosition;" +
            "attribute vec4 vNormal;" +
            "uniform vec4 vColor;" +
            "varying vec4 aColor;" +
            "const vec4 lightPosition = vec4(0.0, 0.0, 5.0, 1.0);" +

            "void main() {" +
            "vec4 lightVector = lightPosition - vPosition;" +
            "float lightDiffuse = max(dot(lightVector,vNormal),0.01);" +
               "    if (lightDiffuse > 0.0) {\n" +
               "        aColor = vColor*lightDiffuse;\n" +
               "    }\n" +
               "    else {\n" +
               "        aColor = vec4(0.0, 0.0, 0.0, 1.0);\n" +
               "    }" +
//          "aColor = vColor;" +
                    // the matrix must be included as a modifier of gl_Position
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
             "gl_Position = uMVPMatrix*vPosition;" +
            "}";


     String fragmentShaderCode =
            "precision mediump float;" +
            "varying vec4 aColor;" +
            "void main() {" +
               "gl_FragColor = aColor;" +
            "}";
}
