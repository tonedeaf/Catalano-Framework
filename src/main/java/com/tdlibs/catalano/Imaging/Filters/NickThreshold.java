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
import com.tdlibs.catalano.Imaging.Filters.Integral.IntegralMean;
import com.tdlibs.catalano.Imaging.Filters.Integral.IntegralVariance;
import com.tdlibs.catalano.Imaging.IApplyInPlace;
import com.tdlibs.catalano.Math.Matrix;

/**
 * Nick Threshold.
 * References: http://www.math-info.univ-paris5.fr/~vincent/articles/DRR_nick_binarization_09.pdf
 * @author Diego catalano
 */
public class NickThreshold implements IApplyInPlace{
    
    private int radius = 15;
    private double k = -0.2D;

    /**
     * Get Radius.
     * @return Radius.
     */
    public int getRadius() {
        return radius;
    }

    /**
     * Set Radius.
     * @param radius Radius.
     */
    public void setRadius(int radius) {
        this.radius = radius;
    }

    /**
     * Get parameter K.
     * @return K value.
     */
    public double getK() {
        return k;
    }

    /**
     * Set parameter K.
     * @param k K value.
     */
    public void setK(double k) {
        this.k = k;
    }

    /**
     * Initialize a new instance of the NickThreshold class.
     */
    public NickThreshold() {}
    
    /**
     * Initialize a new instance of the NickThreshold class.
     * @param radius Radius.
     */
    public NickThreshold(int radius){
        this.radius = radius;
    }

    /**
     * Initialize a new instance of the NickThreshold class.
     * @param radius Radius.
     * @param k Parameter K.
     */
    public NickThreshold(int radius, double k) {
        this.radius = radius;
        this.k = k;
    }

    @Override
    public void applyInPlace(FastBitmap fastBitmap) {
        
        if (fastBitmap.isGrayscale()){
            
            FastBitmap mean = new FastBitmap(fastBitmap);
            FastBitmap var = new FastBitmap(fastBitmap);
            
            IntegralMean im = new IntegralMean(radius);
            im.applyInPlace(mean);
            
            IntegralVariance iv = new IntegralVariance(radius);
            iv.applyInPlace(var);
            
            int size = fastBitmap.getSize();
            for (int i = 0; i < size; i++) {
                float P = fastBitmap.getGray(i);
                float mP = mean.getGray(i);
                float vP = var.getGray(i);
                int g = (P > (mP + k * Math.sqrt(vP + mP*mP))) ? 255 : 0;

                fastBitmap.setGray(i, g);
            }
            
        }
        else{
            throw new IllegalArgumentException("Nick Threshold only works in grayscale images.");
        }
    }
}