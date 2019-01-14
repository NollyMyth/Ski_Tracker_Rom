package cc_ski_track.ski_tracker;

public interface Shader {
     String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
//            "uniform mat4 uMVMatrix;" +
            "attribute vec4 vPosition;" +
            "attribute vec4 vNormal;" +
            "varying vec4 vColor;" +
            "const vec4 lightPosition = vec4(0.0, -2.0, 0.0, 1.0);" +

            "void main() {" +
//            "vec3 modelViewVertex = vec3(u_MVMatrix * vPosition);" +
//            "vec3 modelViewNormal = vec3(u_MVMatrix * vec4(vNormal, 0.0));" +
//            "float distance = length(lightPosition - modelViewVertex);" +
//            "vec4 lightVector = normalize(lightPosition - modelViewVertex);" +

            "vec4 lightVector = normalize(lightPosition - vPosition);" +
            "float lightDiffuse = max(dot(lightVector,vNormal),0.1);" +
//            "lightDiffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));" +
            // the matrix must be included as a modifier of gl_Position
            // Note that the uMVPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
             "vColor = vColor * lightDiffuse;" +
             "gl_Position = uMVPMatrix*vPosition;" +
            "}";


     String fragmentShaderCode =
            "precision mediump float;" +
//            "uniform vec4 vColor;" +
            "varying vec4 vColor;" +
            "void main() {" +
//            "gl_FragColor = vColor*lightDiffuse;" +
                    "gl_FragColor = vColor;" +
            "}";
}
