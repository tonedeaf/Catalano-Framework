// catalano Imaging Library
// The catalano Framework
//
// Copyright © Diego catalano, 2012-2016
// diego.catalano at live.com
//
// Copyright © Arlington, 2013
// Copyright © Saulo, 2013
// scsm at ecmp.poli.br
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

import com.tdlibs.catalano.Core.DoublePoint;
import com.tdlibs.catalano.Imaging.FastBitmap;

/**
 * Image Moments.
 * <para>Moment invariants are properties of connected regions in binary images that are invariant to translation, rotation and scale.
 * They are useful because they define a simply calculated set of region properties that can be used for shape classification and part recognition.</para>
 * 
 * @see HuMoments
 * @see ZernikeMoments
 * @author Diego catalano
 */
public final class ImageMoments {

    /**
     * Don't let anyone instantiate this class.
     */
    private ImageMoments() {}
    
    /**
     * Compute Raw moment.
     * @param fastBitmap Image.
     * @param p Order p.
     * @param q Order q.
     * @return Raw moment.
     */
    public static double getRawMoment(FastBitmap fastBitmap, int p, int q) {
        
        int width = fastBitmap.getWidth();
        int height = fastBitmap.getHeight();
        
        double m = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                m += Math.pow(i, p) * Math.pow(j, q) * fastBitmap.getGray(i, j);
            }
        }
        return m;
    }

    /**
     * Compute central moment.
     * @param fastBitmap Image.
     * @param p Order p.
     * @param q Order q.
     * @return Central moment.
     */
    public static double getCentralMoment(FastBitmap fastBitmap, int p, int q) {
        DoublePoint centroid = getCentroid(fastBitmap);
        return getCentralMoment(fastBitmap, p, q, centroid);
    }
    
    /**
     * Compute central moment.
     * @param fastBitmap Image.
     * @param p Order p.
     * @param q Order q.
     * @param centroid Centroid.
     * @return Central moment.
     */
    public static double getCentralMoment(FastBitmap fastBitmap, int p, int q, DoublePoint centroid) {
        
        int width = fastBitmap.getWidth();
        int height = fastBitmap.getHeight();
        
        double mc = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                mc += Math.pow((i - centroid.x), p) * Math.pow((j - centroid.y), q) * fastBitmap.getGray(i, j);
            }
        }
        return mc;
    }
    
    /**
     * Compute centroid components.
     * @param fastBitmap Image.
     * @return Centroid.
     */
    public static DoublePoint getCentroid(FastBitmap fastBitmap){
        double m00 = ImageMoments.getRawMoment(fastBitmap, 0, 0);
        return getCentroid(fastBitmap, m00);
    }
    
    /**
     * Compute centroid components.
     * @param fastBitmap Image.
     * @param m00 M00.
     * @return Centroid.
     */
    public static DoublePoint getCentroid(FastBitmap fastBitmap, double m00){
        double m10 = ImageMoments.getRawMoment(fastBitmap, 1, 0);
        double m01 = ImageMoments.getRawMoment(fastBitmap, 0, 1);
        double x0 = m10 / m00;
        double y0 = m01 / m00;
        return new DoublePoint(x0, y0);
    }

    /**
     * Compute Covariance XY.
     * @param fastBitmap Image.
     * @param p Order p.
     * @param q Order q.
     * @return Covariance.
     */
    public static double getCovarianceXY(FastBitmap fastBitmap, int p, int q) {
        double mc00 = ImageMoments.getCentralMoment(fastBitmap, 0, 0);
        double mc11 = ImageMoments.getCentralMoment(fastBitmap, 1, 1);
        return mc11 / mc00;
    }

    /**
     * Compute variance X.
     * @param fastBitmap Image.
     * @param p Order p.
     * @param q Order q.
     * @return Variance X.
     */
    public static double getVarianceX(FastBitmap fastBitmap, int p, int q) {
        double mc00 = ImageMoments.getCentralMoment(fastBitmap, 0, 0);
        double mc20 = ImageMoments.getCentralMoment(fastBitmap, 2, 0);
        return mc20 / mc00;
    }

    /**
     * Compute variance Y.
     * @param fastBitmap Image.
     * @param p Order p.
     * @param q Order q.
     * @return Variace Y.
     */
    public static double getVarianceY(FastBitmap fastBitmap, int p, int q) {
        double mc00 = ImageMoments.getCentralMoment(fastBitmap, 0, 0);
        double mc02 = ImageMoments.getCentralMoment(fastBitmap, 0, 2);
        return mc02 / mc00;
    }
    
    /**
     * Compute orientation in radians.
     * @param fastBitmap Image.
     * @return Orientation from the image.
     */
    public static double getOrientation(FastBitmap fastBitmap){
        DoublePoint centroid = getCentroid(fastBitmap);
        double cm11 = ImageMoments.getCentralMoment(fastBitmap, 1, 1, centroid);
        double cm20 = ImageMoments.getCentralMoment(fastBitmap, 2, 0, centroid);
        double cm02 = ImageMoments.getCentralMoment(fastBitmap, 0, 2, centroid);
        
        return 0.5 * Math.atan2(cm20 - cm02, 2 * cm11);
    }
    
    /**
     * Compute projection skewness.
     * @param fastBitmap Image.
     * @return Projection skewness.
     */
    public static DoublePoint getProjectionSkewness(FastBitmap fastBitmap){
        DoublePoint centroid = getCentroid(fastBitmap);
        return getProjectionSkewness(fastBitmap, centroid);
    }
    
    /**
     * Compute projection skewness.
     * @param fastBitmap Image.
     * @param centroid Centroid.
     * @return Projection skewness.
     */
    public static DoublePoint getProjectionSkewness(FastBitmap fastBitmap, DoublePoint centroid){
        double u30 = ImageMoments.getCentralMoment(fastBitmap, 3, 0, centroid);
        double u03 = ImageMoments.getCentralMoment(fastBitmap, 0, 3, centroid);
        double u20 = ImageMoments.getCentralMoment(fastBitmap, 2, 0, centroid);
        double u02 = ImageMoments.getCentralMoment(fastBitmap, 0, 2, centroid);
        
        double skx = u30 / Math.pow(u20, 1.5);
        double sky = u03 / Math.pow(u02, 1.5);
        
        return new DoublePoint(skx, sky);
    }
    
    /**
     * Compute projection kurtosis.
     * @param fastBitmap Image.
     * @return Projection kurtorsis.
     */
    public static DoublePoint getProjectionKurtosis(FastBitmap fastBitmap){
        DoublePoint centroid = getCentroid(fastBitmap);
        return getProjectionKurtosis(fastBitmap, centroid);
    }
    
    /**
     * Compute projection kurtosis.
     * @param fastBitmap Image.
     * @param centroid Centroid.
     * @return Projection kurtorsis.
     */
    public static DoublePoint getProjectionKurtosis(FastBitmap fastBitmap, DoublePoint centroid){
        double u40 = ImageMoments.getCentralMoment(fastBitmap, 4, 0, centroid);
        double u20 = ImageMoments.getCentralMoment(fastBitmap, 2, 0, centroid);
        double u04 = ImageMoments.getCentralMoment(fastBitmap, 0, 4, centroid);
        double u02 = ImageMoments.getCentralMoment(fastBitmap, 0, 2, centroid);
        
        double skx = (u40 / Math.pow(u20, 2)) - 3;
        double sky = (u04 / Math.pow(u02, 2)) - 3;
        
        return new DoublePoint(skx, sky);
    }
    
    /**
     * Normalized Central Moment.
     * @param fastBitmap Image.
     * @param p Order p.
     * @param q Order q.
     * @return Normalized central moment.
     */
    public static double getNormalizedCentralMoment(FastBitmap fastBitmap, int p, int q) {
        double m00 = ImageMoments.getCentralMoment(fastBitmap, 0, 0);
        DoublePoint centroid = getCentroid(fastBitmap, m00);
        return getNormalizedCentralMoment(fastBitmap, p, q, centroid, m00);
    }
    
    /**
     * Normalized Central Moment.
     * @param fastBitmap Image.
     * @param p Order p.
     * @param q Order q.
     * @param centroid Centroid.
     * @param m00 M00 moment value.
     * @return Normalized central moment.
     */
    public static double getNormalizedCentralMoment(FastBitmap fastBitmap, int p, int q, DoublePoint centroid, double m00) {
        double gama = ((p + q) / 2) + 1;
        double mpq = ImageMoments.getCentralMoment(fastBitmap, p, q, centroid);
        double m00gama = Math.pow(m00, gama);
        if(m00gama == 0) return 0;
        return mpq / m00gama;
    }
    
    /**
     * Abo-Zaid Normalized Central Moment.
     * @param fastBitmap Image.
     * @param p Order p.
     * @param q Order q.
     * @return Abo-Zaid normalized central moment.
     */
    public static double getZaidNormalizedCentralMoment(FastBitmap fastBitmap, int p, int q){
        
        double mpq = ImageMoments.getCentralMoment(fastBitmap, p, q);
        double m00 = ImageMoments.getCentralMoment(fastBitmap, 0, 0);
        double m20 = ImageMoments.getCentralMoment(fastBitmap, 2, 0);
        double m02 = ImageMoments.getCentralMoment(fastBitmap, 0, 2);
        
        return mpq * (1 / m00)
                   * Math.pow(m00 / (m20 + m02), (p+q) / 2);
        
    }
}