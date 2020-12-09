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

public class Slicer {
    ArrayList<Triangle> triangles;
    double height;
    public Slicer(ArrayList<Triangle> t) {
        triangles = t;
        double curHighest = 0.0;
        for (int i = 0; i < triangles.size(); ++i) {
            double h = triangles.get(i).highestPoint();
            if (h > curHighest) {
                curHighest = h;
            }
        }
        height = curHighest;
    }
    
    // ger tillbaka en list av alla par av punkter där det valda planet korsar trianglarna.
    public ArrayList<ArrayList<Vector3>> sliceLayer(float layerHeight) {
        ArrayList<ArrayList<Vector3>> intersectionPairs = new ArrayList<>();
        for (int i = 0; i < triangles.size(); ++i) {
            if (triangles.get(i).isIntersecting(layerHeight)) {
                if (triangles.get(i).intersectionPoints(layerHeight).size()==0) {
                    System.out.println("Warning, no intersection: "+triangles.get(i).toString());
                }
                intersectionPairs.add(triangles.get(i).intersectionPoints(layerHeight));
            }
        }
        return intersectionPairs;
    }
    
    // ger tillbaka en lista av polygoner av skärningssnittet.
    // tolerance är hur stort avståndet mellan punkter kan vara för att de ska ränkas som lika.
    public ArrayList<Polygon> connectEdges(ArrayList<ArrayList<Vector3>> intersectionPairs, double tolerance) {
        ArrayList<ArrayList<Vector3>> intersectionPairsCpy = new ArrayList<>(intersectionPairs);
        ArrayList<Polygon> polygons = new ArrayList<>();
        while (intersectionPairsCpy.size()!=0) {  // varje iteration innebär en polygon
            ArrayList<Vector2> polygon = new ArrayList<>();
            Vector3 prevVertex = intersectionPairsCpy.get(intersectionPairsCpy.size()-1).get(0);
            Vector3 firstVertex = prevVertex;

            // söker efter en vertex som är identisk med prevVertex.
            while (true) {
                for (int i = 0; i < intersectionPairsCpy.size(); ++i) {
                    if (intersectionPairsCpy.get(i).get(0).equals(prevVertex, tolerance)) {
                        prevVertex = intersectionPairsCpy.get(i).get(1);
                        intersectionPairsCpy.remove(i);
                        polygon.add(new Vector2(prevVertex));
                        break;
                    } else if (intersectionPairsCpy.get(i).get(1).equals(prevVertex, tolerance)) {
                        prevVertex = intersectionPairsCpy.get(i).get(0);
                        polygon.add(new Vector2(prevVertex));
                        intersectionPairsCpy.remove(i);
                        break;
                    }
                }
                if (prevVertex.equals(firstVertex, tolerance)) {
                    break;
                }
            }
            
            
            polygons.add(new Polygon(polygon));
        }
        return polygons;
    }
}
