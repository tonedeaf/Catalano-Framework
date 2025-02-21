// catalano Imaging Library
// The catalano Framework
//
// Copyright © Diego catalano, 2012-2016
// diego.catalano at live.com
//
//    This library is free software; you can redistribute it and/or
//    modify it under the terms of the GNU Lesser General Public
//    License as published by the Free Software Foundation; either
//    version 2.1 of the License, or (at your option) any later version.
//
//    This library is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//    Lesser General Public License for more details.
//
//    You should have received a copy of the GNU Lesser General Public
//    License along with this library; if not, write to the Free Software
//    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//

package com.tdlibs.catalano.Imaging.Filters;

import com.tdlibs.catalano.Imaging.FastBitmap;
import com.tdlibs.catalano.Imaging.IApplyInPlace;

/**
 * Binary Dilatation operator from Mathematical Morphology.
 * The filter assigns maximum value of surrounding pixels to each pixel of the result image. Surrounding pixels, which should be processed, are specified by structuring element: 1 - to process the neighbor, 0 - to skip it.
 * The filter especially useful for binary image processing, where it allows to grow separate objects or join objects.
 * 
 * <p><li>Supported types: Grayscale.
 * <br><li>Coordinate System: Matrix.
 * 
 * @author Diego catalano
 */
public class BinaryDilatation implements IApplyInPlace{
    
    private int[][] kernel;
    private int radius = 0;

    /**
     * Initialize a new instance of the Binary Dilatation class.
     */
    public BinaryDilatation() {
        this.radius = 1;
    }
    
    /**
     * Initialize a new instance of the Binary Dilatation class.
     * @param se Structure element.
     */
    public BinaryDilatation(int[][] se) {
        this.kernel = se;
    }

    /**
     * Initialize a new instance of the Binary Dilatation class.
     * @param radius Radius.
     */
    public BinaryDilatation(int radius) {
        radius = radius < 1 ? 1 : radius;
        this.radius = radius;
    }
    
    @Override
    public void applyInPlace(FastBitmap fastBitmap){
        if(fastBitmap.isGrayscale()){
            if (radius != 0) {
                ApplyInPlace(fastBitmap, radius);
            }
            else{
                ApplyInPlace(fastBitmap, kernel);
            }
        }
        else{
            throw new IllegalArgumentException("Binary Dilatation only works in grayscale images.");
        }
    }
    
    private void ApplyInPlace(FastBitmap fastBitmap, int radius){
        
        FastBitmap copy = new FastBitmap(fastBitmap);
        
        int width = fastBitmap.getWidth();
        int height = fastBitmap.getHeight();
        int l;
        
        int Xline,Yline;
        int lines = CalcLines(radius);
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                l = copy.getGray(x, y);
                if (l == 255) {
                    for (int i = 0; i < lines; i++) {
                        Xline = x + (i-radius);
                        for (int j = 0; j < lines; j++) {
                            Yline = y + (j-radius);
                            if ((Xline >= 0) && (Xline < height) && (Yline >=0) && (Yline < width)) {
                                fastBitmap.setGray(Xline, Yline, 255);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void ApplyInPlace(FastBitmap fastBitmap, int[][] kernel){
        
        FastBitmap copy = new FastBitmap(fastBitmap);
        
        int width = fastBitmap.getWidth();
        int height = fastBitmap.getHeight();
        int l;
        
        int Xline,Yline;
        int lines = CalcLines(kernel);
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                l = copy.getGray(x, y);
                if (l == 255) {
                    for (int i = 0; i < kernel.length; i++) {
                        Xline = x + (i-lines);
                        for (int j = 0; j < kernel[0].length; j++) {
                            Yline = y + (j-lines);
                            if ((Xline >= 0) && (Xline < height) && (Yline >=0) && (Yline < width)) {
                                if (kernel[i][j] == 1) {
                                    fastBitmap.setGray(Xline, Yline, 255);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private int CalcLines(int[][] se){
            int lines = (se[0].length - 1)/2;
            return lines;
    }
    
    private int CalcLines(int radius){
        return radius * 2 + 1;
    }
}