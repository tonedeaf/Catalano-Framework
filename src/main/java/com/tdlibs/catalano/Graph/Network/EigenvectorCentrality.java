/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tdlibs.catalano.Graph.Network;

import com.tdlibs.catalano.Graph.AdjacencyMatrix;
import com.tdlibs.catalano.Math.Matrix;

/**
 *
 * @author Diego
 */
public class EigenvectorCentrality {

    public EigenvectorCentrality() {}
    
    public double[] Compute(AdjacencyMatrix matrix){
        
        double[] dc = new DegreeCentrality().Compute(matrix);
        
        return Matrix.MultiplyByTranspose(matrix.getData(), dc);
        
        
    }
    
}
