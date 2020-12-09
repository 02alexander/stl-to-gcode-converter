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

public class Polygon {
    ArrayList<Vector2> vertices;
    
    //ArrayList<ArrayList<ArrayList<int>>> 
    
    public Polygon(ArrayList<Vector2> v) {
        vertices = v;
    }
    
    public ArrayList<Vector2> getVertices() {
        return vertices;
    }
    
    // används för att ta reda på vilket håll som är innåt.
    // om den är positiv så ska man rotera positivt för att komma innåt
    // och om den är negativ så ska man rotera negativt för att komma innåt.
    public double orientation() {
        double totalAngleSum = 0.0;
        Vector2 prevPoint = vertices.get(1);
        Vector2 prevprevPoint = vertices.get(0);
        for (int j = 2; j < vertices.size()+2; ++j) {
            int i = j%vertices.size();
            Vector2 v1 = prevPoint.sub(prevprevPoint);
            Vector2 v2 = vertices.get(i).sub(prevPoint);
            totalAngleSum += v1.signedAngle(v2);
            prevprevPoint = prevPoint;
            prevPoint = vertices.get(i);

        }
        return totalAngleSum;
    }
    
    public Polygon innerPolygon(double margin) {
        ArrayList<Vector2> result = new ArrayList<>();
        double orientation = Math.signum(orientation());
        Vector2 prevPoint = vertices.get(1);
        Vector2 prevprevPoint = vertices.get(0);
        for (int j = 2; j < vertices.size()+2; ++j) {
            int i = j%vertices.size();
            Vector2 v1 = prevPoint.sub(prevprevPoint);
            Vector2 v2 = vertices.get(i).sub(prevPoint);
            
            Vector2 innerPoint = innerCorner(v1, v2, orientation, margin).add(prevPoint);
            if (Math.abs(innerPoint.x) > 10.0 || Math.abs(innerPoint.y) > 10.0) {
                System.out.println("length="+v1.length());
                System.out.println(innerPoint.toString());
                System.out.println("theta="+v1.signedAngle(v2));
                System.out.println(prevprevPoint.toString()+" "+prevPoint.toString()+" "+vertices.get(i).toString());
            }
            result.add(innerPoint);

            prevprevPoint = prevPoint;
            prevPoint = vertices.get(i);

        }
        return new Polygon(result);
    }
    
    public Polygon reverse() {
        ArrayList<Vector2> reversed = new ArrayList<>();
        for (int i = this.vertices.size()-1; i >= 0; --i) {
            reversed.add(this.vertices.get(i));
        }
        return new Polygon(reversed);
    }
    
    public ArrayList<Polygon> intersectionLines(Vector2 dirVec, Vector2 linePoint) {
        ArrayList<Polygon> polygons = new ArrayList<>();
        
        Vector2 prevVertex = vertices.get(0);
        ArrayList<Vector2> intersectionPoints = new ArrayList<>();
        ArrayList<Double> projlen = new ArrayList<>();
        
        for (int j = 1; j < vertices.size()+1; ++j) {
            int i = j%vertices.size();
            
            if (isIntersecting(vertices.get(i), prevVertex, dirVec, linePoint)) {
                intersectionPoints.add(intersectionPoint(vertices.get(i), prevVertex, dirVec, linePoint));
                projlen.add(signedProjectionLength(vertices.get(i), prevVertex, dirVec, linePoint));
            }
            
            prevVertex = vertices.get(i);
        }
        ArrayList<Vector2> curPolygon = new ArrayList<>();
        while (!projlen.isEmpty()) {
            int lowestIdx = 0;
            double lowest = projlen.get(0);
            for (int i = 1; i < projlen.size(); ++i) {
                if (projlen.get(i) < lowest) {
                    lowestIdx = i;
                }
            }
            
            curPolygon.add(intersectionPoints.get(lowestIdx));
            intersectionPoints.remove(lowestIdx);
            projlen.remove(lowestIdx);
            
            if (curPolygon.size() == 2) {
                polygons.add(new Polygon(curPolygon));
                curPolygon = new ArrayList<>();
            }
        }
        
        return polygons;
    }
    
    // u är vektorn som kommer efter v.
    // ger tillbaka riktningen av vektorn som bildar det inre hörnet.
    public static Vector2 innerCorner(Vector2 v, Vector2 u, double orientation, double margin) {
        // normaliserar vektor v
        v = v.scalarmul(1.0/v.length());
        double theta = v.signedAngle(u)*orientation;
        return v.rotate( orientation*(Math.PI+theta)/2.0 ).scalarmul( margin/Math.abs(Math.sin( (Math.PI-theta)/2.0 )) );
    }
    
    static boolean isIntersecting(Vector2 a, Vector2 b, Vector2 dirVec, Vector2 linePoint) {
        a = a.sub(linePoint);
        b = b.sub(linePoint);
        
        double theta = dirVec.getAngle();
        a = a.rotate(-theta);
        b = b.rotate(-theta);
        return (a.y > 0 && b.y <= 0) || (b.y > 0 && a.y <= 0);
    }
    
    public static boolean isSectionsIntersecting(Vector2 a, Vector2 b, Vector2 c, Vector2 d) {
        double x = signedProjectionLength(a, b, d.sub(c), c);
        a = a.sub(c);
        b = b.sub(c);
        d = d.sub(c);
        double theta = d.getAngle();
        d = d.rotate(-theta);
        a = a.rotate(-theta);
        b = b.rotate(-theta);
        double l = Math.min(d.x, 0.0);
        double h = Math.max(d.x, 0.0);
        return x >= l && x <= h && ((a.y >= 0 && b.y <= 0) || (b.y >= 0 && a.y <= 0) );
    }
    
    // ger tillbaka positiv eller negativ beroende på  vilken sida om linePoint.
    // samma riktning som dirVec ger positiv. motsatt riktning ger negativ.
    public static double signedProjectionLength(Vector2 a, Vector2 b, Vector2 dirVec, Vector2 linePoint) {
        a = a.sub(linePoint);
        b = b.sub(linePoint);
        
        double theta = dirVec.getAngle();
        a = a.rotate(-theta);
        b = b.rotate(-theta);
        
        Vector2 v = b.sub(a);
        double t = -a.y/v.y;
        return t*v.x+a.x;
    }
    
    // ibland när den gör det inre polygonet så hamnar en del i fel "ordning" 
    // vilket orsakar skarpa svängar.
    // Denna funktion tar bort all sådana svängar.
    public Polygon removeSharpTurns(double angleTolerance) {
        ArrayList<Vector2> result = new ArrayList<>();
        Vector2 v1 = vertices.get(0);
        Vector2 v2 = vertices.get(1);
        for (int j = 2; j < vertices.size()+2; ++j) {
            int i = j%vertices.size();
            Vector2 v3 = vertices.get(i);
            
            Vector2 u = v2.sub(v1);
            Vector2 v = v3.sub(v2);
            double theta = u.signedAngle(v);
            if (Math.abs(theta) <= angleTolerance) {
                result.add(v2);
            }
            
            v1 = v2;
            v2 = v3;
        }
        return new Polygon(result);
    }
    
    // Om man har en trapets där toppen är väldigt liten så kan det bildas loopar
    // som denna funktion rättar till genom att sätta skärningspunkten som den nya punkten
    
    public Polygon fixLoops() {
        ArrayList<Vector2> newVertices = new ArrayList<>(); 
        
        Vector2 v1 = vertices.get(0);
        Vector2 v2 = vertices.get(1);
        Vector2 v3 = vertices.get(2);
        boolean removeLast = false;
        
        for (int j = 3; j < vertices.size()+3; ++j) {
            int i = j % vertices.size();
            Vector2 v4 = vertices.get(i);
            if (isSectionsIntersecting(v1, v2, v3, v4) && vertices.size() > 3) {
                System.out.println(v1.toString()+" "+v2.toString()+" "+v3.toString()+" "+v4.toString());
                //System.out.println(signedProjectionLength(v1, v2, v4.sub(v3), v3));
                Vector2 d = v4;
                d = d.sub(v3);
                d = d.rotate(-d.getAngle());
                double l = Math.min(d.x, 0.0);
                double h = Math.max(d.x, 0.0);
                //System.out.println(" "+l+", "+h);
                System.out.println(d.toString());
                if (newVertices.size()==0) {
                    removeLast = true;
                } else {
                    newVertices.remove(newVertices.size()-1);
                }
                newVertices.add(intersectionPoint(v1, v2, v3.sub(v4), v4));
                //System.out.println(intersectionPoint(v1, v2, v3.sub(v4), v4).toString());
            } else  {
                newVertices.add(v3);
            }
            
            v1 = v2;
            v2 = v3;
            v3 = v4;
        }
        
        if (removeLast) {
            newVertices.remove(newVertices.size()-1);
        }
        
        return new Polygon(newVertices);
    }
    
    public static Vector2 intersectionPoint(Vector2 a, Vector2 b, Vector2 dirVec, Vector2 linePoint) {
        
        double x = signedProjectionLength(a, b, dirVec, linePoint);
        
        Vector2 dirVecnorm = dirVec.scalarmul(1.0/dirVec.length());
        return linePoint.add(dirVecnorm.scalarmul(x));
    }
    
    // gör om hörn vars vinkel är mindre än tolerance till en lång linje.
    public Polygon clean(double tolerance) {
        assert tolerance > 0.0;
        
        ArrayList<Vector2> newVertices = new ArrayList<>();
        Vector2 prevprevVertex = vertices.get(0);
        Vector2 prevVertex = vertices.get(1);
        for (int j = 2; j < vertices.size()+2;++j) {
            int i = j % vertices.size();
            Vector2 v = prevVertex.sub(prevprevVertex);
            Vector2 u = vertices.get(i).sub(prevVertex);
            if (Math.abs(v.signedAngle(u)) >= tolerance) {
                newVertices.add(prevVertex);
            }
            prevprevVertex = prevVertex;
            prevVertex = vertices.get(i);
        }
        return new Polygon(newVertices);
    }
    
    public String toString() {
        String result = new String();
        for (Vector2 vertex : vertices) {
            result += vertex.toString() + "\n";
        }
        return result;
    }
}
