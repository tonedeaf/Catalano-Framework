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
 * ANOVA (ANalysis Of VAriance) Kernel.
 * <para>The ANOVA kernel is a graph kernel, which can be
 * computed using dynamic programming tables.</para>
 * @author Diego catalano
 */
public class Anova implements IMercerKernel<double[]>{
    
    private int n;  // input vector length
    private int p; // length of subsequence
    private double[][][] K; // value cache

    /**
     * Constructs a new ANOVA Kernel.
     * @param vectorLength Length of the input vector.
     * @param subsequenceLength Length of the subsequences for the ANOVA decomposition.
     */
    public Anova(int vectorLength, int subsequenceLength){
        this.n = vectorLength;
        this.p = subsequenceLength;
        this.K = new double[vectorLength][vectorLength][subsequenceLength];
    }

    @Override
    public double Function(double[] x, double[] y) {
        // Evaluate the kernel by dynamic programming
        for (int k = 0; k < p; k++)
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    K[i][j][k] = kernel(x, i, y, j, k);

        // Get the final result
        return K[n - 1][n - 1][p - 1];
    }
    
    private double kernel(double[] x, int ni, double[] y, int mi, int pi) {
        double a;

        if (ni == 0 || mi == 0)
        {
            a = 0;
        }
        else
        {
            // Retrieve the value from the cache
            a = K[ni - 1][mi - 1][pi];
        }


        // Compute a linear kernel
        double k = x[ni] * y[mi];


        if (pi == 0)
        {
            return a + k;
        }
        else if (ni == 0 || mi == 0)
        {
            return a;
        }
        else
        {
            // Retrieve the value from the cache
            return a + k * K[ni - 1][mi - 1][pi - 1];
        }
    }
}