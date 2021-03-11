/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tdlibs.catalano.Vision.Detection;

import com.tdlibs.catalano.Imaging.FastBitmap;
import com.tdlibs.catalano.Imaging.Shapes.IntRectangle;
import java.util.List;

/**
 *
 * @author Diego
 */
public interface IObjectDetector {
    
    List<IntRectangle> DetectedObjects();
    List<IntRectangle> ProcessFrame(FastBitmap fastBitmap);
    
}
