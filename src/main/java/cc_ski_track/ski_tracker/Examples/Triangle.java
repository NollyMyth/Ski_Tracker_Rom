package cc_ski_track.ski_tracker.Examples.Geometry;


public class Triangle {
    public Vec3 v1;
    public Vec3 v2;
    public Vec3 v3;
    public Vec3 normale;
    public Triangle(Vec3 v1, Vec3 v2, Vec3 v3){
        this.v1=v1;
        this.v2=v2;
        this.v3=v3;
    }
    public void setNormale(Vec3 normale){
        this.normale = normale;
    }

}