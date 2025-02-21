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
import com.tdlibs.catalano.Imaging.IApply;
import com.tdlibs.catalano.Imaging.IApplyInPlace;
import com.tdlibs.catalano.Imaging.Shapes.IntRectangle;

/**
 * Crop an image.
 * <p>The filter crops an image providing a new image, which contains only the specified rectangle of the original image.</p>
 * 
 * <p><li>Supported types: Grayscale, RGB.
 * <br><li>Coordinate System: Matrix.
 * 
 * @author Diego catalano
 */
public class Crop implements IApply, IApplyInPlace{
    
    private int x;
    private int y;
    private int width;
    private int height;

    /**
     * Get Initial X axis coordinate.
     * @return X axis coordinate.
     */
    public int getX() {
        return x;
    }

    /**
     * Set Initial X axis coordinate.
     * @param x X axis coordinate.
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Get Initial Y axis coordinate.
     * @return Y axis coordinate.
     */
    public int getY() {
        return y;
    }

    /**
     * Set Initial Y axis coordinate.
     * @param y Y axis coordinate.
     */
    public void setY(int y) {
        this.y = y;
    }
    
    /**
     * Set Initial X and Y axis coordinate.
     * @param x X axis coordinate.
     * @param y Y axis coordinate.
     */
    public void setPosition(int x, int y){
        this.x = x;
        this.y = y;
    }

    /*
     * Get Height.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Set Height.
     * @param height Height.
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Get Width.
     * @return Width.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Set Width.
     * @param width Width.
     */
    public void setWidth(int width) {
        this.width = width;
    }
    
    /**
     * Set Width and Height.
     * @param height Height.
     * @param width Width.
     */
    public void setSize(int height, int width){
        this.height = height;
        this.width = width;
    }
    
    /**
     * Initialize a new instance of the Crop class.
     * @param rectangle Rectangle.
     */
    public Crop(IntRectangle rectangle){
        this(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }
    
    /**
     * Initialize a new instance of the Crop class.
     * @param x start x position.
     * @param y start y position.
     * @param width new width.
     * @param height new height.
     */
    public Crop(int x, int y, int width, int height){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public FastBitmap apply(FastBitmap fastBitmap) {
        
        if((this.x + height > fastBitmap.getHeight()) ||
                this.y + width > fastBitmap.getWidth()){
            throw new IllegalArgumentException("The size is higher than original image.");
        }
        
        FastBitmap l = new FastBitmap(width, height, fastBitmap.getColorSpace());
        
        if (fastBitmap.isGrayscale()) {
            if(fastBitmap.getCoordinateSystem() == FastBitmap.CoordinateSystem.Matrix){
                for (int r = 0; r < height; r++) {
                    for (int c = 0; c < width; c++) {
                        l.setGray(r, c, fastBitmap.getGray(r + this.x, c + this.y));
                    }
                }
            }
            else{
                for (int r = 0; r < height; r++) {
                    for (int c = 0; c < width; c++) {
                        l.setGray(c, r, fastBitmap.getGray(r + this.y, c + this.x));
                    }
                }
            }
        }
        else{
            if(fastBitmap.getCoordinateSystem() == FastBitmap.CoordinateSystem.Matrix){
                for (int r = 0; r < height; r++) {
                    for (int c = 0; c < width; c++) {
                        l.setRed(r, c, fastBitmap.getRed(r + this.x, c + this.y));
                        l.setGreen(r, c, fastBitmap.getGreen(r + this.x, c + this.y));
                        l.setBlue(r, c, fastBitmap.getBlue(r + this.x, c + this.y));
                    }
                }
            }
            else{
                for (int r = 0; r < height; r++) {
                    for (int c = 0; c < width; c++) {
                        l.setRed(c, r, fastBitmap.getRed(r + this.y, c + this.x));
                        l.setGreen(c, r, fastBitmap.getGreen(r + this.y, c + this.x));
                        l.setBlue(c, r, fastBitmap.getBlue(r + this.y, c + this.x));
                    }
                }
            }
        }
        return l;
    }
    
    /**
     * Apply filter to a FastBitmap.
     * @param fastBitmap FastBitmap
     */
    @Override
    public void applyInPlace(FastBitmap fastBitmap){
        FastBitmap temp = apply(fastBitmap);
        fastBitmap.setImage(temp);
    }
}