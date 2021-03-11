// catalano Imaging Library
// The catalano Framework
//
// Copyright © Diego catalano, 2012-2018
// diego.catalano at live.com
//
// Copyright © Andrew Kirillov, 2005-2008
// andrew.kirillov@gmail.com
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

package com.tdlibs.catalano.Imaging;

/**
 * Common interface to apply filters.
 * @author Diego catalano
 */
public interface IApply {
    
    /**
     * Apply the filter
     * @param fastBitmap Image to be processed.
     * @return A processed image.
     */
    FastBitmap apply(FastBitmap fastBitmap);
}
