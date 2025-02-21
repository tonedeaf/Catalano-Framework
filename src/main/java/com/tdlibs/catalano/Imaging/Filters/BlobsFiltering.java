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

package com.tdlibs.catalano.Imaging.Filters;

import com.tdlibs.catalano.Core.IntPoint;
import com.tdlibs.catalano.Imaging.FastBitmap;
import com.tdlibs.catalano.Imaging.IApplyInPlace;
import com.tdlibs.catalano.Imaging.Tools.Blob;
import com.tdlibs.catalano.Imaging.Tools.BlobDetection;
import java.util.List;

/**
 * Eliminates blobs with certain area or size.
 * 
 * <p><li>Supported types: Grayscale.
 * <br><li>Coordinate System: Matrix.
 * 
 * @author Diego catalano
 */
public class BlobsFiltering implements IApplyInPlace{
    
    public static enum Filter {Area, Size};
    public static enum Logic {Or, And};
    private Filter filter = Filter.Area;
    private Logic logic = Logic.Or;
    private boolean remove;
    
    private int minArea = 0;

    private int minHeight = 0;
    private int minWidth = 0;

    /**
     * Get filtering type.
     * @return Filter.
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * Set filtering type.
     * @param filter Filter.
     */
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    /**
     * Get Minimum width allowed.
     * @return Minimum width.
     */
    public int getMinWidth() {
        return minWidth;
    }

    /**
     * Set Minimum width allowed.
     * @param minWidth Minimum width.
     */
    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
    }

    /**
     * Get Minimum height allowed.
     * @return Minimum height.
     */
    public int getMinHeight() {
        return minHeight;
    }

    /**
     * Set Minimum height allowed.
     * @param minHeight Minimum height.
     */
    public void setMinHeight(int minHeight) {
        this.minHeight = minHeight;
    }

    /**
     * Minimum area allowed for eliminate.
     * @return Minimum area.
     */
    public int getMinArea() {
        return minArea;
    }

    /**
     * Minimum area allowed for eliminate.
     * @param minArea Minimum area.
     */
    public void setMinArea(int minArea) {
        this.minArea = Math.max(0, minArea);
    }

    /**
     * Check if the filter gonna remove the blobs.
     * @return True if the blobs will be removed, otherwise false.
     */
    public boolean isRemove() {
        return remove;
    }

    /**
     * Remove or keep the blobs.
     * @param remove True if you want to remove, otherwise if you want to keep.
     */
    public void setRemove(boolean remove) {
        this.remove = remove;
    }
    
    /**
     * Initialize a new instance of the BlobsFiltering class.
     */
    public BlobsFiltering() {}
    
    /**
     * Initialize a new instance of the BlobsFiltering class.
     * @param minArea Minimum area to remove.
     */
    public BlobsFiltering(int minArea) {
        this(minArea, true);
    }
    
    /**
     * Initialize a new instance of the BlobsFiltering class.
     * @param minArea Minimum area to remove or keep.
     * @param remove True if you want to remove, otherwise if you want to keep.
     */
    public BlobsFiltering(int minArea, boolean remove){
        this.minArea = Math.max(0, minArea);
        this.remove = remove;
        this.filter = Filter.Area;
    }
    
    /**
     * Initialize a new instance of the BlobsFiltering class.
     * @param minWidth Minimum width to remove.
     * @param minHeight Minimum height to remove.
     */
    public BlobsFiltering(int minWidth, int minHeight){
        this(minWidth, minHeight, Logic.Or);
    }
    
    /**
     * Initialize a new instance of the BlobsFiltering class.
     * @param minWidth Minimum width to remove.
     * @param minHeight Minimum height to remove.
     * @param logic Logic.
     */
    public BlobsFiltering(int minWidth, int minHeight, Logic logic){
        this(minWidth, minHeight, logic, true);
    }
    
    /**
     * Initialize a new instance of the BlobsFiltering class.
     * @param minWidth Minimum width to remove or keep.
     * @param minHeight Minimum height to remove or keep.
     * @param logic Logic.
     * @param remove True if you want to remove, otherwise if you want to keep.
     */
    public BlobsFiltering(int minWidth, int minHeight, Logic logic, boolean remove){
        this.minWidth = minWidth;
        this.minHeight = minHeight;
        this.filter = Filter.Size;
        this.logic = Logic.Or;
        this.remove = remove;
    }
    
    @Override
    public void applyInPlace(FastBitmap fastBitmap){
        
        List<Blob> blobs = new BlobDetection().ProcessImage(fastBitmap);
        
        switch(filter){
            case Area:
                int area;
                for (int i = 0; i < blobs.size(); i++) {
                    area = blobs.get(i).getArea();
                    if(remove){
                        if (area < minArea) {
                            for (IntPoint p : blobs.get(i).getPoints()) {
                                fastBitmap.setGray(p.x, p.y, 0);
                            }
                        }
                    }
                    else{
                        if (area > minArea) {
                            for (IntPoint p : blobs.get(i).getPoints()) {
                                fastBitmap.setGray(p.x, p.y, 0);
                            }
                        }
                    }
                }
            break;
            case Size:
                int blobWidth;
                int blobHeight;
                for (int i = 0; i < blobs.size(); i++) {
                    blobWidth = blobs.get(i).getWidth();
                    blobHeight = blobs.get(i).getHeight();
                    if(logic == Logic.Or){
                        if(remove){
                            if (blobWidth < minWidth || blobHeight < minHeight) {
                                for (IntPoint p : blobs.get(i).getPoints()) {
                                    fastBitmap.setGray(p.x, p.y, 0);
                                }
                            }
                        }
                        else{
                            if (blobWidth > minWidth || blobHeight > minHeight) {
                                for (IntPoint p : blobs.get(i).getPoints()) {
                                    fastBitmap.setGray(p.x, p.y, 0);
                                }
                            }
                        }
                    }
                    else{
                        if(remove){
                            if (blobWidth < minWidth && blobHeight < minHeight) {
                                for (IntPoint p : blobs.get(i).getPoints()) {
                                    fastBitmap.setGray(p.x, p.y, 0);
                                }
                            }
                        }
                        else{
                            if (blobWidth > minWidth && blobHeight > minHeight) {
                                for (IntPoint p : blobs.get(i).getPoints()) {
                                    fastBitmap.setGray(p.x, p.y, 0);
                                }
                            }
                        }
                    }
                }
            break;
        }
    }
}