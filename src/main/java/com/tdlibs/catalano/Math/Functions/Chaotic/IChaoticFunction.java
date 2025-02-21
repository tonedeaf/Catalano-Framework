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

package com.tdlibs.catalano.Math.Functions.Chaotic;

/**
 * Common interface for to chaotic functions.
 * @author Diego catalano
 */
public interface IChaoticFunction {
    
    /**
     * Generate a value from the chaotic function.
     * @param x Value.
     * @return Value.
     */
    public double Generate(double x);
    
    /**
     * Generate a map from the chaotic function.
     * @param initialState Initial state (value).
     * @param iterations Number of iterations.
     * @return Chaotic map.
     */
    public double[] Generate(double initialState, int iterations);
}
