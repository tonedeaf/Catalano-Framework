// catalano Machine Learning Library
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

package com.tdlibs.catalano.MachineLearning.Performance;

import com.tdlibs.catalano.MachineLearning.Dataset.DatasetRegression;
import com.tdlibs.catalano.MachineLearning.Regression.IRegression;
import com.tdlibs.catalano.MachineLearning.Regression.RegressionMeasure;

/**
 * Interface common for regression performance.
 * @author Diego catalano
 */
public interface IRegressionValidation {
    
    /**
     * Run the validation.
     * @param regression Regression.
     * @param input Input.
     * @param output Output.
     * @return Regression measure.
     */
    RegressionMeasure Run(IRegression regression, double[][] input, double[] output);
    
    /**
     * Run the validation.
     * @param regression Regression.
     * @param dataset Dataset.
     * @return Regression measure.
     */
    RegressionMeasure Run(IRegression regression, DatasetRegression dataset);
}