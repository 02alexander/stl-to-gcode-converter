/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Slicer;

/**
 *
 * @author alexander
 */
public class Vector3 {
    double x;
    double y;
    double z;
    public Vector3(double ax, double ay, double az) {
        x = ax;
        y = ay;
        z = az;
    }
    public Vector3() {
        x = 0.0; x = 0.0; z = 0.0;
    }
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getZ() {
        return z;
    }
    public String toString() {
        double r = 1000.0;
        return "(" + Double.toString(Math.round(x*r)/r) + ", " + Double.toString(Math.round(y*r)/r) + ", " + Double.toString(Math.round(z*r)/r) + ")";
    }
    public Vector3 add(Vector3 other) {
        double nx = x + other.getX();
        double ny = y + other.getY();
        double nz = z + other.getZ();
        return new Vector3(nx, ny, nz);
    }
    public Vector3 sub(Vector3 other) {
        double nx = x-other.getX();
        double ny = y-other.getY();
        double nz = z-other.getZ();
        return new Vector3(nx, ny, nz);
    }
    public Vector3 scalarmul(double scalar) {
        return new Vector3(scalar*x, scalar*y, scalar*z);
    }
    public double length() {
        return Math.sqrt(x*x+y*y+z*z);
    }
    public double squaredLength() {
        return x*x+y*y+z*z;
    }
    public double dot(Vector3 other) {
        return x*other.x+y*other.y+z*other.z;
    }
    public boolean equals(Vector3 other, double tolerance) {
        return this.sub(other).length()<=tolerance;
    }
    public double signedAngleZ(Vector3 other) {
        double theta = this.getAngleZ();
        Vector3 rotatedv = other.rotateZ(-theta);
        return rotatedv.getAngleZ();
    }
    public Vector3 rotateZ(double angle) {
        double nx = Math.cos(angle)*getX()-Math.sin(angle)*getY();
        double ny = Math.sin(angle)*getX()+Math.cos(angle)*getY();
        return new Vector3( nx, ny, getZ() );
    }
    public double getAngleZ() {
        return Math.atan2(y, x);
    }
}
