// catalano Math Library
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

package com.tdlibs.catalano.Math.Distances;

/**
 * Jaccard distance.
 * @author Diego catalano
 */
public class JaccardDistance implements IDistance<double[]>{

    /**
     * Initializes a new instance of the JaccardDistance class.
     */
    public JaccardDistance() {}

    @Override
    public double Compute(double[] u, double[] v) {
        return Distance.JaccardDistance(u, v);
    }
}