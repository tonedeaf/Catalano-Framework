// catalano Genetic Library
// The catalano Framework
//
// Copyright © Diego catalano, 2012-2019
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

package com.tdlibs.catalano.Evolutionary.Genetic.Selection;

import com.tdlibs.catalano.Core.ArraysUtil;
import com.tdlibs.catalano.Evolutionary.Genetic.Chromosome.IChromosome;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Roulette Wheel Selection.
 * @author Diego catalano
 */
public class RouletteWheelSelection implements ISelection{
    
    private long seed;

    /**
     * Initializes a new instance of the RouletteWheelSelection class.
     */
    public RouletteWheelSelection() {}

    /**
     * Initializes a new instance of the RouletteWheelSelection class.
     * @param seed Random seed.
     */
    public RouletteWheelSelection(long seed) {
        this.seed = seed;
    }

    @Override
    public int[] Compute(List<IChromosome> chromosomes) {
        
        //Scale fitness [0..1]
        int[] index = new int[2];
        double[] fitness = new double[chromosomes.size()];
        
        double max = 0;
        for (int i = 0; i < fitness.length; i++) {
            fitness[i] = chromosomes.get(i).getFitness();
            max = Math.max(max, fitness[i]);
        }
        
        for (int i = 0; i < fitness.length; i++) {
            fitness[i] /= max;
        }
        
        //Sort the fitness values
        int[] oriIndex = ArraysUtil.Argsort(fitness, true);
        Arrays.sort(fitness);
        
        //Select the chromossomes
        Random r = new Random();
        if(seed != 0) r.setSeed(seed);
        
        for (int i = 0; i < index.length; i++) {
            double v = r.nextDouble();
            for (int j = 0; j < fitness.length; j++) {
                if(v >= fitness[j]){
                    index[i] = oriIndex[j];
                }
            }
        }
        
        return index;
        
    }
}