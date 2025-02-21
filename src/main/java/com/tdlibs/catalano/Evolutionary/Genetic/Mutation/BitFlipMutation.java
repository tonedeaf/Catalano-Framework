// catalano Genetic Library
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

package com.tdlibs.catalano.Evolutionary.Genetic.Mutation;

import com.tdlibs.catalano.Evolutionary.Genetic.Chromosome.BinaryChromosome;
import java.util.Random;

/**
 * Bit Flip Mutation.
 * 
 * Support: Binary Chromosome.
 * 
 * @author Diego catalano
 */
public class BitFlipMutation implements IMutation<BinaryChromosome>{
    
    private float probability = 0.01f;

    public float getProbability() {
        return probability;
    }

    public void setProbability(float probability) {
        this.probability = probability;
    }

    /**
     * Initializes a new instance of the BitFlipMutation class.
     */
    public BitFlipMutation() {}
    
    /**
     * Initializes a new instance of the BitFlipMutation class.
     * @param probability Probability for each gene.
     */
    public BitFlipMutation(float probability) {
        this.probability = probability;
    }

    @Override
    public BinaryChromosome Compute(BinaryChromosome chromosome) {
        Random r = new Random();
        
        String bin = chromosome.toBinary();
        String newBin = "";
        for (int i = 0; i < bin.length(); i++) {
            char v = bin.charAt(i);
            if(r.nextFloat() <= probability){
                if(v == '0') newBin += "1";
                if(v == '1') newBin += "0";
            } else{
                newBin += v;
            }
        }
        
        return new BinaryChromosome(chromosome.getLength(), newBin);
    }
    
}