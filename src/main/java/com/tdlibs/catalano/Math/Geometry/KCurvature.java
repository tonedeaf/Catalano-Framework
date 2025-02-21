// catalano Math Library
// The catalano Framework
//
// Copyright © Diego catalano, 2012-2016
// diego.catalano at live.com
//
// Copyright © César Souza, 2009-2013
// cesarsouza at gmail.com
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

package com.tdlibs.catalano.Math.Geometry;

import com.tdlibs.catalano.Core.DoublePoint;
import com.tdlibs.catalano.Core.DoubleRange;
import com.tdlibs.catalano.Core.IntPoint;
import com.tdlibs.catalano.Math.Tools;
import java.util.ArrayList;
import java.util.List;

/**
 * K-curvatures algorithm for local contour extrema detection.
 * @author Diego catalano
 */
public class KCurvature {
    
    public int k;
    public DoubleRange theta;
    private int suppression;

    /**
     * Gets the number K of previous and posterior points to consider when find local extremum points.
     * @return Number of previous and posterior points.
     */
    public int getK() {
        return k;
    }

    /**
     * Sets the number K of previous and posterior points to consider when find local extremum points.
     * @param k Number of previous and posterior points.
     */
    public void setK(int k) {
        this.k = k;
    }

    /**
     * Gets the theta angle range (in degrees) used to define extremum points.
     * @return Theta angle.
     */
    public DoubleRange getTheta() {
        return theta;
    }

    /**
     * Sets the theta angle range (in degrees) used to define extremum points.
     * @param theta Theta angle.
     */
    public void setTheta(DoubleRange theta) {
        this.theta = theta;
    }

    /**
     * Gets the suppression radius to use during non-minimum suppression. 
     * @return Suppression radius.
     */
    public int getSuppression() {
        return suppression;
    }

    /**
     * Sets the suppression radius to use during non-minimum suppression. 
     * @param suppression Suppression radius.
     */
    public void setSuppression(int suppression) {
        this.suppression = suppression;
    }

    /**
     * Initializes a new instance of the KCurvature class.
     * @param k K.
     * @param theta Theta.
     */
    public KCurvature(int k, DoubleRange theta) {
        this.k = k;
        this.theta = theta;
    }
    
    /**
     * Finds local extremum points in the contour.
     * @param contour A list of integer points defining the contour.
     * @return Peaks. 
     */
    public List<IntPoint> FindPeaks(List<IntPoint> contour){
        double[] map = new double[contour.size()];
        
        for (int i = 0; i < contour.size(); i++){
            IntPoint a,b,c;
            int ai = Tools.Mod(i + k, contour.size());
            int ci = Tools.Mod(i - k, contour.size());
            
            a = contour.get(ai);
            b = contour.get(i);
            c = contour.get(ci);
            
            DoublePoint ab = new DoublePoint(b.x - a.x, b.y - a.y);
            DoublePoint cb = new DoublePoint(b.x - c.x, b.y - c.y);

            double angba = Math.atan2(ab.y, ab.x);
            double angbc = Math.atan2(cb.y, cb.x);
            double rslt = angba - angbc;

            if (rslt < 0) rslt = 2 * Math.PI + rslt;

            double rs = (rslt * 180) / Math.PI;
            
            if (theta.isInside(rs)) map[i] = rs;
        }
            
        // Non-Minima Suppression
        int r = suppression;
        List<IntPoint> peaks = new ArrayList<IntPoint>();
        for (int i = 0; i < map.length; i++){
            double current = map[i];
            if (current == 0) continue;

            boolean isMinimum = true;

            for (int j = -r; j < r && isMinimum; j++){
                int index = Tools.Mod(i+j, map.length);

                double candidate = map[index];

                if (candidate == 0)
                    continue;

                if (candidate < current)
                    isMinimum = false;
                else map[index] = 0;
            }
            if (isMinimum) peaks.add(contour.get(i));
        }
        return peaks;
    }
}
