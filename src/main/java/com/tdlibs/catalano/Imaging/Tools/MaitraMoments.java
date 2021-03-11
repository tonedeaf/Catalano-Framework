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

package com.tdlibs.catalano.Imaging.Tools;

import com.tdlibs.catalano.Imaging.FastBitmap;

/**
 * Maitra moments.
 * 
 * References: Maitra, Sidhartha. "Moment invariants." Proceedings of the IEEE 67.4 (1979): 697-699.
 * 
 * @author Diego catalano
 */
public class MaitraMoments {
    
    private boolean normalize;

    /**
     * Initialize a new instance of the MaitraMoments class.
     */
    public MaitraMoments() {}

    /**
     * Initialize a new instance of the MaitraMoments class.
     * @param normalize Normalize by log.
     */
    public MaitraMoments(boolean normalize) {
        this.normalize = normalize;
    }
    
    /**
     * Compute Maitra moments.
     * @param fastBitmap Image.
     * @return 6 Moments.
     */
    public double[] Compute(FastBitmap fastBitmap){
        double[] result = new double[6];
        
        HuMoments hu = new HuMoments();
        double[] m = hu.Compute(fastBitmap);
        
        double u00 = ImageMoments.getNormalizedCentralMoment(fastBitmap, 0, 0);
        
        result[0] = Math.sqrt(m[1]) / m[0];
        result[1] = (m[2] * u00) / (m[1] * m[0]);
        result[2] = m[3] / m[2];
        result[3] = Math.sqrt(m[4]) / m[3];
        result[4] = m[5] / (m[3] * m[0]);
        result[5] = m[6] / m[4];
        
        //Normalize by log
        if(normalize){
            for (int i = 0; i < result.length; i++) {
                result[i] = Math.signum(result[i]) * Math.log10(Math.abs(result[i]) + 1);
            }
        }
        
        return result;
    }
    
}