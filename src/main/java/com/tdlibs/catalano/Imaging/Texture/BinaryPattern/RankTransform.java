// catalano Imaging Library
// The catalano Framework
//
// Copyright © Diego catalano, 2012-2016
// diego.catalano at live.com
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

package com.tdlibs.catalano.Imaging.Texture.BinaryPattern;

import com.tdlibs.catalano.Imaging.FastBitmap;
import com.tdlibs.catalano.Imaging.Tools.ImageHistogram;

/**
 * Rank Transform (RT) is a type of feature used for classification in computer vision.
 * RT was first described in 1994. It has since been found to be a powerful feature for texture classification.
 * 
 * @author Diego catalano
 */
public class RankTransform implements IBinaryPattern{

    /**
     * Initialize a new instance of the RankTransform class.
     */
    public RankTransform() {}

    @Override
    public ImageHistogram ComputeFeatures(FastBitmap fastBitmap) {
        if (!fastBitmap.isGrayscale())
            throw new IllegalArgumentException("Rank Transform works only with grayscale images.");
        
        int width = fastBitmap.getWidth() - 1;
        int height = fastBitmap.getHeight() - 1;
        
        int sum;
        int[] g = new int[256];
        int gray;
        for (int x = 1; x < height; x++) {
            for (int y = 1; y < width; y++) {
                gray = fastBitmap.getGray(x, y);
                sum = 0;
                if (gray - fastBitmap.getGray(x - 1, y - 1) >= 0)    sum += 128;
                if (gray - fastBitmap.getGray(x - 1, y) >= 0)        sum += 64;
                if (gray - fastBitmap.getGray(x - 1, y + 1) >= 0)    sum += 32;
                if (gray - fastBitmap.getGray(x, y + 1) >= 0)        sum += 16;
                if (gray - fastBitmap.getGray(x + 1, y + 1) >= 0)    sum += 8;
                if (gray - fastBitmap.getGray(x + 1, y) >= 0)        sum += 4;
                if (gray - fastBitmap.getGray(x + 1, y - 1) >= 0)    sum += 2;
                if (gray - fastBitmap.getGray(x, y - 1) >= 0)        sum += 1;
                g[sum]++;
            }
        }
        return new ImageHistogram(g);
    }
}