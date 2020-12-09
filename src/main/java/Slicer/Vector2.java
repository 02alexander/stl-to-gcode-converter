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
public class Vector2 {
    double x;
    double y;
    
    public Vector2(double ax, double ay) {
        x = ax;
        y = ay;
    }
    public Vector2(Vector2 other) {
        this.x = other.x;
        this.y = other.y;
    }
    public Vector2() {
        x = 0.0; x = 0.0;
    }
    public Vector2(Vector3 old) {
        x = old.x;
        y = old.y;
    }
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public String toString() {
        double r = 1000.0;
        return "(" + Double.toString(Math.round(x*r)/r) + ", " + Double.toString(Math.round(y*r)/r) + ")";
    }
    public Vector2 add(Vector2 other) {
        double nx = x + other.getX();
        double ny = y + other.getY();
        return new Vector2(nx, ny);
    }
    public Vector2 sub(Vector2 other) {
        double nx = x-other.getX();
        double ny = y-other.getY();
        return new Vector2(nx, ny);
    }
    public Vector2 scalarmul(double scalar) {
        return new Vector2(scalar*x, scalar*y);
    }
    public double length() {
        return Math.sqrt(x*x+y*y);
    }
    public double squaredLength() {
        return x*x+y*y;
    }
    public double dot(Vector2 other) {
        return x*other.x+y*other.y;
    }
    public boolean equals(Vector2 other, double tolerance) {
        return this.sub(other).length()<=tolerance;
    }
    public double signedAngle(Vector2 other) {
        double theta = this.getAngle();
        Vector2 rotatedv = other.rotate(-theta);
        return rotatedv.getAngle();
    }
    public Vector2 rotate(double angle) {
        double nx = Math.cos(angle)*getX()-Math.sin(angle)*getY();
        double ny = Math.sin(angle)*getX()+Math.cos(angle)*getY();
        return new Vector2( nx, ny);
    }
    public double getAngle() {
        return Math.atan2(y, x);
    }
}
