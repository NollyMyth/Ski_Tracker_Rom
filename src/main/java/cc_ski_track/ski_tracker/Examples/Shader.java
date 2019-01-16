package cc_ski_track.ski_tracker.Examples;

public interface Shader {
    String vertex_shader =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec3 vNormal;"+
                    "attribute vec4 vColor;" +
                    "uniform mat4 V;"+
                    "uniform mat4 M;"+
                    "varying vec4 color;"+
                   // "out vec4 normal;"+
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  color = vColor;"+
                    "  gl_Position = uMVPMatrix*vPosition;" +
                    // Normal of the the vertex, in camera space
                    "}";

    String fragment_shader =
            "precision mediump float;" +
                    "varying vec4 color;" +
                    //"in vec4 normal;"+
                    "void main() {" +
                    //"  float cosTheta = clamp( dot( transformedVertexNormal,light ), 0,1 );"+
                    "  gl_FragColor = color;"+//cosTheta;" +
                    "}";

}
