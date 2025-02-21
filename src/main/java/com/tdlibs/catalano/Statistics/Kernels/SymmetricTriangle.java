// catalano Statistics Library
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

package com.tdlibs.catalano.Statistics.Kernels;

/**
 * Symmetric Triangle Kernel.
 * @author Diego catalano
 */
public class SymmetricTriangle implements IMercerKernel<double[]>{
    
    private double gamma;

    /**
     * Get the kernel's gamma value.
     * @return The kernel's gamma value.
     */
    public double getGamma() {
        return gamma;
    }

    /**
     * Set the kernel's gamma value.
     * @param value The kernel's gamma value.
     */
    public void setGamma(double value) {
        this.gamma = value;
    }

    /**
     * Constructs a new Symmetric Tirangle Kernel.
     */
    public SymmetricTriangle() {}

    /**
     * Constructs a new Symmetric Tirangle Kernel.
     * @param gamma Gamma value.
     */
    public SymmetricTriangle(double gamma) {
        this.gamma = gamma;
    }

    @Override
    public double Function(double[] x, double[] y) {
        double norm = 0.0, d;
        for (int i = 0; i < x.length; i++)
        {
            d = x[i] - y[i];
            norm += d * d;
        }

        double z = 1.0 - gamma * Math.sqrt(norm);

        return (z > 0) ? z : 0;
    }
}