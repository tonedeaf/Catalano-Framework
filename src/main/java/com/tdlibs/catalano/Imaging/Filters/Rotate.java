// catalano Imaging Library
// The catalano Framework
//
// Copyright © Diego catalano, 2012-2016
// diego.catalano at live.com
//
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

/**
 * Rotate image.
 * @author Diego catalano
 */
public class Rotate implements IApply, IApplyInPlace{
    
    /**
     * Interpolation algorithm.
     */
    public static enum Algorithm{

        /**
         * Bilinear interpolation.
         */
        BILINEAR,

        /**
         * Bicubic interpolation.
         */
        BICUBIC,

        /**
         * Nearest neighbor interpolation.
         */
        NEAREST_NEIGHBOR};
    private double angle;
    private boolean keepSize;
    private Algorithm algorithm;
    
    /**
     * Get angle.
     * @return Angle.
     */
    public double getAngle() {
        return angle;
    }

    /**
     * Set angle [0..360].
     * @param angle Angle.
     */
    public void setAngle(double angle) {
        this.angle = angle;
    }

    /**
     * Keep original size.
     * @return True if keep the original image size, otherwise false.
     */
    public boolean isKeepSize() {
        return keepSize;
    }

    /**
     * Set keep original size.
     * @param keepSize True if keep the original image size, otherwise false.
     */
    public void setKeepSize(boolean keepSize) {
        this.keepSize = keepSize;
    }

    /**
     * Get Interpolation algorithm.
     * @return Interpolation algorithm.
     */
    public Algorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * Set Interpolation algorithm.
     * @param algorithm Interpolation algorithm.
     */
    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }
    
    /**
     * Initialize a new instance of the Rotate class.
     * @param angle Angle.
     */
    public Rotate(double angle){
        this.angle = angle;
        this.algorithm = Algorithm.NEAREST_NEIGHBOR;
    }
    
    /**
     * Initialize a new instance of the Rotate class.
     * @param angle Angle.
     * @param keepSize Keep the original size.
     */
    public Rotate(double angle, boolean keepSize){
        this.angle = angle;
        this.keepSize = keepSize;
        this.algorithm = Algorithm.NEAREST_NEIGHBOR;
    }

    /**
     * Initialize a new instance of the Rotate class.
     * @param angle Angle.
     * @param algorithm Interpolation algorithm.
     */
    public Rotate(double angle, Algorithm algorithm) {
        this.angle = angle;
        this.algorithm = algorithm;
    }
    
    /**
     * Initialize a new instance of the Rotate class.
     * @param angle Angle.
     * @param keepSize Keep the original size.
     * @param algorithm Interpolation algorithm.
     */
    public Rotate(double angle, boolean keepSize, Algorithm algorithm) {
        this.angle = angle;
        this.keepSize = keepSize;
        this.algorithm = algorithm;
    }

    @Override
    public FastBitmap apply(FastBitmap fastBitmap) {
        switch(algorithm){
            case BILINEAR:
                return new RotateBilinear(angle, keepSize).apply(fastBitmap);
            case BICUBIC:
                return new RotateBicubic(angle, keepSize).apply(fastBitmap);
            default:
                return new RotateNearestNeighbor(angle, keepSize).apply(fastBitmap);
        }
    }
    
    
    @Override
    public void applyInPlace(FastBitmap fastBitmap){
        FastBitmap temp = apply(fastBitmap);
        fastBitmap.setImage(temp);
    }
}