package cc_ski_track.ski_tracker.Examples.Geometry;

public class Vec3 {
    public float x;
    public float y;
    public float z;
    public Vec3(float X, float Y, float Z){
        this.x = X;
        this.y = Y;
        this.z = Z;
    }
    public Vec3 minus(Vec3 v1){
        return new Vec3 (x - v1.x, y - v1.y, z - v1.z);
    }

    public Vec3 sum(Vec3 v1){
          return new Vec3 (x + v1.x, y + v1.y, z + v1.z);
    }

    public Vec3 div(float i){
        return new Vec3 (x/i, y/i, z/i);
    }

    public Vec3 cross(Vec3 v1){
        return new Vec3 (y * v1.z - v1.y * z, z * v1.x - x * v1.z, x * v1.y - y*v1.x);
    }

    public void normalize(){
        this.x = (float)(x/Math.sqrt((double)x*x + y*y + z*z));
        this.y = (float)(y/Math.sqrt((double)x*x + y*y + z*z));
        this.z = (float)(z/Math.sqrt((double)x*x + y*y + z*z));
    }


//    public boolean equals(Vec3 vec){
//        if (this.x == vec.x && this.y == vec.y && this.z == vec.z)
//            return true;
//        else
//            return false;
//    }
}