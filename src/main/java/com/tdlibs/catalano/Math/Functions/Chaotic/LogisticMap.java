// catalano Math Library
// The catalano Framework
//
// Copyright © Diego catalano, 2012-2016
// diego.catalano at live.com
//
// Copyright © Seyedali Mirjalili, 2018
// ali.mirjalili at gmail.com
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

package com.tdlibs.catalano.Math.Functions.Chaotic;

/**
 * Logistic map.
 * @author Diego catalano
 */
public class LogisticMap implements IChaoticFunction{
    
    private double r;

    /**
     * Initialize a new instance of the LogisticMap class.
     */
    public LogisticMap() {
        this(3.6);
    }

    /**
     * Initialize a new instance of the LogisticMap class.
     * @param r Parameter u.
     */
    public LogisticMap(double r) {
        this.r = r;
    }

    @Override
    public double Generate(double x) {
        return r * x * (1 - x);
    }
    
    @Override
    public double[] Generate(double initialState, int iterations) {
        double[] map = new double[iterations];
        map[0] = initialState;
        
        for (int i = 1; i < iterations; i++) {
            map[i] = Generate(map[i - 1]);
        }
        
        return map;
    }
}