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
import java.nio.*;
import java.io.*;
import java.nio.file.Files;

public class STLReader {
    public static ArrayList<Triangle> readSTL(String fileName) {
        File file = new File(fileName);
        byte[] buf = Files.readAllBytes(file.toPath());
        
        //           Konverterar från Little-endian till Big-endian;
        int nfaces = (buf[80+3]<<24)&0xff000000 | (buf[80+2]<<16)&0xff0000 | (buf[80+1]<<8)&0xff00 | buf[80+0]&0xff;
        ArrayList<Triangle> triangles = new ArrayList<Triangle>();
        
        int offset = 84;
        
        for (int face = 0; face < nfaces; ++face) {
            Vector3[] points = new Vector3[4];
            for (int vec = 0; vec < 4; ++vec) {
                float[] cords = new float[3];
                for (int i = 0; i < 3; ++i) {
                    int d = (buf[offset+3]<<24)&0xff000000 | (buf[offset+2]<<16)&0xff0000 | (buf[offset+1]<<8)&0xff00 | buf[offset+0]&0xff;
                    cords[i] = Float.intBitsToFloat(d);
                    offset += 4;
                }
                points[vec] = new Vector3(cords[0], cords[1], cords[2]);
            }
            triangles.add(new Triangle(points[1], points[2], points[3]));
            offset += 2;
        }
        return triangles;
    }
}
