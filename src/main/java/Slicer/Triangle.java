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

import java.util.*;

public class Triangle {
    Vector3 origin;
    Vector3 edge1;
    Vector3 edge2;
    public boolean isIntersecting(float planeHeight) {
        float c = planeHeight;
        return isIntersectingEdge(origin, edge1, c) || isIntersectingEdge(origin, edge2, c) || isIntersectingEdge(edge1.add(origin), edge2.sub(edge1), c);
    }
    boolean isIntersectingEdge(Vector3 orig, Vector3 edge, float planeHeight) {
        float c = planeHeight;
        return (orig.getZ() > c && orig.getZ()+edge.getZ() < c) || (orig.getZ() < c && orig.getZ()+edge.getZ() > c);
    }
    Vector3 intersectionPoint(Vector3 linePoint, Vector3 dirVec, float planeHeight) {
        float c = planeHeight;
        double t = ((double)c-linePoint.getZ())/dirVec.getZ();
        return linePoint.add(dirVec.scalarmul(t));
    }
    // ger tillbaka alla punkter där 
    public ArrayList<Vector3> intersectionPoints(float planeHeight) {
        ArrayList<Vector3> v = new ArrayList<Vector3>();
        if(isIntersectingEdge(origin, edge1, planeHeight)) {
            v.add(intersectionPoint(origin, edge1, planeHeight));
        } 
        if(isIntersectingEdge(origin, edge2, planeHeight)) {
            v.add(intersectionPoint(origin, edge2, planeHeight));
        } 
        if(isIntersectingEdge(edge1.add(origin), edge2.sub(edge1), planeHeight)) {
            v.add(intersectionPoint(edge1.add(origin), edge2.sub(edge1), planeHeight));
        }
        return v;
    }
    public String toString() {
        return "("+origin.toString()+", "+origin.add(edge1).toString()+", "+origin.add(edge2).toString()+")";
    }
    Triangle(Vector3 p1, Vector3 p2, Vector3 p3) {
        origin = p1;
        edge1 = p2.sub(p1);
        edge2 = p3.sub(p1);
    }
    public double highestPoint() {
        double curHighest = origin.getZ();
        if (origin.add(edge1).getZ() > curHighest) {
            curHighest = origin.add(edge1).getZ();
        }
        if (origin.add(edge2).getZ() > curHighest) {
            curHighest = origin.add(edge2).getZ();
        }
        return curHighest;
    }
    // i=0,1,2 beroende på vilket hörn.
    public Vector3 getV(int i) {
        if (i == 0) {
            return origin;
        } else if (i == 1) {
            return origin.add(edge1);
        } else {
            return origin.add(edge2);
        }
    }
}
