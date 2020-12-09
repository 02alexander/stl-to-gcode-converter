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
//import java.io.File;
import java.io.*;

// information om alla gcoder: https://marlinfw.org/meta/search/?q=M190

public class GCodeWriter {
    
    double bedTemperature; // temperaturen av plattan som den printar ut på
    double endTemperature; // temperaturen av munstycket
    
    // hur stor plattan är i x-, y- och z riktningarna
    double bedX;
    double bedY;
    double bedZ;
    
    double layerHeight;
    double lineWidth;
    double wireDiameter;
    int feedrate;
    double curE; // hur många millimeter av plasttråden den ska ha tryckt ut.
    ArrayList<ArrayList<Polygon>> layers;
    //Vector2 curPosition;
    Vector2 offset; // hörn av rektangeln som omger det printade objektet.
    
    public static double highest(double[] arr) {
        double curHighest = arr[0];
        for (int i = 0; i < arr.length; ++i) {
            if (arr[i] > curHighest) {
                curHighest = arr[i];
            }
        }
        return curHighest;
    }
    public static double lowest(double[] arr) {
        double curLowest = arr[0];
        for (int i = 0; i < arr.length; ++i) {
            if (arr[i] < curLowest) {
                curLowest = arr[i];
            }
        }
        return curLowest;
    }
    public GCodeWriter(double layerh, double linew, double wired, int feedr, double bx, double by, double bz) {
        layerHeight = layerh;
        lineWidth = linew;
        wireDiameter = wired;
        layers = new ArrayList<>();
        feedrate = feedr;
        bedX = bx;
        bedY = by;
        bedZ = bz;
        
        curE = 0.0;
        //curPosition = new Vector2(0.0, 0.0);
        offset = new Vector2(120.0, 120.0);
    }
    public void build(String filename) {
        String result = "M140 S50\n" + // sätter platt-temperaturen till 50C
                            "M105\n" + 
                            "M190 S50\n" + // vänta på att platt-temperaturen blir 50C
                            "M104 S200\n" + // sätt munstycke-temperaturn till 200C
                            "M105\n" +
                            "M109 S200\n" + // vänta på att munstycke-temperaturen blir 200C 
                            "M82\n" +
                            "G92 E0\n" +
                            "G28\n" + // flyttar till noll-positionen
                            "G1 Z2.0 F3000\n" +
                            "G1 X0.1 Y20 Z0.3 F5000.0\n" +
                            "G1 X0.1 Y200.0 Z0.3 F1500.0 E15\n" +
                            "G1 X0.4 Y200.0 Z0.3 F5000.0\n" +
                            "G1 X0.4 Y20 Z0.3 F1500.0 E30\n" +
                            "G92 E0\n" + // nollställer E
                            "G1 Z2.0 F3000\n" + // för att inte repa plattan
                            "G1 X5 Y20 Z0.3 F5000.0\n" +
                            "G92 E0\n" +
                            "G92 E0\n" +
                            "G1 F2700 E-5\n"+
                            "M107\n";// stänger av fläkten 


        //result += "G0 F6000";
        
        double curHeight = layerHeight;
        
        //calculateOffset();
        
        for (ArrayList<Polygon> layer : layers) {
            Vector2 startCord = layer.get(0).vertices.get(0).add(offset);
            
            if (Math.abs(curHeight-2.0*layerHeight) < 0.00001) {
                result += "M106 S85\n";
            }
            
            result += "G0 F6000 " + cordAsGCode(startCord) + "\n";
            result += "G0 F300 "+cordAsGCode(startCord) + " Z" + toStringPrecision(curHeight, 4) + "\n"; 
            
            if (curHeight == layerHeight) {
                result += "G1 F2700 E0\n";
            }   
            
            for (Polygon p : layer) {
                Polygon inner = p.innerPolygon(lineWidth/2.0).fixLoops().removeSharpTurns(Math.PI-0.1);
                result += asGCode(inner);
                for (Polygon line : fill(inner, 0.7)) {
                    result += asGCode(line);
                }
            }   
            
            curHeight += layerHeight;
        }
        
        result += "G1 F2700 E18.66126\n" +
                "M140 S0\n" +
                "M107\n" +
                "G91\n" +
                "G1 E-2 F2700\n" +
                "G1 E-2 Z0.2 F2400\n" +
                "G1 X5 Y5 F3000\n" +
                "G1 Z10\n" +
                "G90\n" +
                "\n" +
                "G1 X0 Y235\n" +
                "M106 S0\n" +
                "M104 S0\n" +
                "M140 S0\n" +
                "\n" +
                "M84 X Y E\n" +
                "\n" +
                "M82 ;absolute extrusion mode\n" +
                "M104 S0";
        
        //File output = new File(filename);
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write(result);
        writer.close();
        
        
        //System.out.println(result);
        
    }
    
    public String asGCode(Polygon path) {
        ArrayList<Vector2> vertices = path.getVertices();
        String result = "G0 F"+Integer.toString(feedrate)+" "+cordAsGCode(vertices.get(0).add(offset))+"\n";
        
        Vector2 prevVertex = vertices.get(0);
        for (int j = 1; j < vertices.size()+1; ++j) {
            int i = j % vertices.size();

            if (vertices.size() == 2 && i == 0) {
                continue;
            }
            
            String E = "E"+toStringPrecision(curE+requiredE(vertices.get(i).sub(prevVertex).length()), 4);
            String XY = cordAsGCode(vertices.get(i).add(offset));
            String F = "F"+Integer.toString(feedrate);
            result += "G1 "+F+" "+XY+" "+E+"\n";
            curE += requiredE(vertices.get(i).sub(prevVertex).length());
            prevVertex = vertices.get(i);
        }
        return result;
    }
    
    // ger tillbaka alla linjer som behöver dras för att fylla shape (exlusive den yttersta kanten som man ofta vill ha)
    public ArrayList<Polygon> fill(Polygon shape, double theta) {
        ArrayList<Polygon> result = new ArrayList<>();
        //Polygon inner = shape.innerPolygon(lineWidth/2.0);
        Vector2 dirVec = new Vector2(Math.sin(theta), Math.cos(theta));
        
        double[] cordsX = new double[shape.getVertices().size()];
        double[] cordsY = new double[shape.getVertices().size()];
        
        ArrayList<Vector2> vertices = shape.getVertices();
        for (int i = 0; i < cordsX.length; ++i) {
            cordsX[i] = vertices.get(i).x;
            cordsY[i] = vertices.get(i).y;
        }
        
        double minX = lowest(cordsX);
        double maxX = highest(cordsX);
        double minY = lowest(cordsY);
        double maxY = highest(cordsY);
        
        double stepX = lineWidth/Math.cos(theta);
        double stepY = lineWidth/Math.sin(theta);
        
        //shape = shape.reverse();
        
        boolean backward = false;
        double curX = minX-0.001;
        while (curX <= maxX && maxX - minX > stepX) {
            ArrayList<Polygon> lines = shape.intersectionLines(dirVec, new Vector2(curX, minY));
            if (backward) {
                for (int i = 0; i < lines.size(); ++i) {
                    Polygon p = lines.get(i).reverse();
                    lines.set(i, p);
                }
            }
            result.addAll(lines);
            //result.addAll(shape.intersectionLines(dirVec, new Vector2(curX, minY)));
            curX += stepX;
            backward = !backward;
        }
        
        backward = false;
        double curY = minY-0.001;
        while (curY <= maxY && maxY-minY > stepY) {
            ArrayList<Polygon> lines = shape.intersectionLines(dirVec, new Vector2(minX, curY));
            if (backward) {
                for (int i = 0; i < lines.size(); ++i) {
                    Polygon p = lines.get(i).reverse();
                    lines.set(i, p);
                }
            }
            result.addAll(lines);
            //result.addAll(shape.intersectionLines(dirVec, new Vector2(minX, curY)));
            curY += stepY;
            backward = !backward;
        }
        return result;
    }
    
    // kalkylerar vart mitten av formen är.
    public void calculateOffset() {
        int nVertices = 0;
        for (ArrayList<Polygon> layer : layers) {
            for (Polygon poly : layer) {
                nVertices += poly.getVertices().size();
            }
        }
        
        double[] cordsX = new double[nVertices];
        double[] cordsY = new double[nVertices];
        
        /*for (ArrayList<Polygon> layer : layers) {
            for (Polygon poly : layer) {
                for (Vector2 vertex : poly.vertices) {
                    
                }
            }
        }*/
        
        double highestX = highest(cordsX);
        double lowestX = lowest(cordsX);
        double highestY = highest(cordsY);
        double lowestY = lowest(cordsY);
        
        double middleX = (highestX-lowestX)/2.0;
        double middleY = (highestY-lowestY)/2.0;
        
        offset = new Vector2( bedX/2.0-middleX, bedY/2.0-middleY);
    }
    
    public void addLayer(ArrayList<Polygon> polygons) {
        layers.add(polygons);
    }
    
    public double requiredE(double dist) {
        double wireArea = Math.PI*wireDiameter*wireDiameter/2.0;
        double lineArea = layerHeight*lineWidth * 2.0; // 2.0 får den att funka för någon mystiskt anledning
        return dist*(lineArea/wireArea);
    }
    
    static String toStringPrecision(double x, int n) {
        String s = Double.toString(x);
        int commaIdx = s.indexOf('.');
        return s.substring(0, Math.min(commaIdx+n+1, s.length()));
    }
    
    static String cordAsGCode(Vector2 cord) {
        return "X"+toStringPrecision(cord.x, 4)+" "+"Y"+toStringPrecision(cord.y, 4);
    }
}

