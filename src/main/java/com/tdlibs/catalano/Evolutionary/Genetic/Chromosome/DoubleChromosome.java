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

package com.tdlibs.catalano.Evolutionary.Genetic.Chromosome;

import com.tdlibs.catalano.Math.Tools;
import java.util.Arrays;
import java.util.Random;

/**
 * Double Chromosome.
 * @author Diego catalano
 */
public class DoubleChromosome extends ChromosomeBase{
    
    private final int size;
    private double minValue;
    private double maxValue;
    
    private double[] values;

    /**
     * Get minimum value.
     * @return Minimum value.
     */
    public double getMinValue() {
        return minValue;
    }

    /**
     * Get maximum value.
     * @return Maximum value.
     */
    public double getMaxValue() {
        return maxValue;
    }

    /**
     * Get values.
     * @return Values.
     */
    public double[] getData() {
        return values;
    }
    
    /**
     * Initializes a new instance of the DoubleChromosome class.
     * @param values Values.
     * @param minValue Minimum value.
     * @param maxValue Maximum value.
     */
    public DoubleChromosome(double[] values, double minValue, double maxValue){
        this.size = values.length;
        this.values = values;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    /**
     * Initializes a new instance of the DoubleChromosome class.
     * @param size Size of the chromosome.
     * @param minValue Minimum value.
     * @param maxValue Maximum value.
     */
    public DoubleChromosome(int size, double minValue, double maxValue) {
        this.size = size;
        this.minValue = minValue;
        this.maxValue = maxValue;
        Generate();
    }

    @Override
    public Object getGene(int index) {
        return values[index];
    }

    @Override
    public void setGene(int index, Object gene) {
        values[index] = (Double)gene;
    }

    @Override
    public void Generate() {
        
        Random rand = new Random();
        
        values = new double[size];
        for (int i = 0; i < size; i++) {
            values[i] = Tools.Scale(0, 1, minValue, maxValue, rand.nextDouble());
        }
        
    }

    @Override
    public IChromosome CreateNew() {
        return new DoubleChromosome(size, minValue, maxValue);
    }

    @Override
    public IChromosome Clone() {
        DoubleChromosome c = new DoubleChromosome(Arrays.copyOf(values, values.length), minValue, maxValue);
        c.fitness = this.fitness;
        return c;
    }

    @Override
    public int getLength() {
        return size;
    }

    @Override
    public String toString() {
        
        String str = "";
        for (int i = 0; i < size; i++) {
            str += String.valueOf(values[i]);
        }
        
        return str;
        
    }
    
}