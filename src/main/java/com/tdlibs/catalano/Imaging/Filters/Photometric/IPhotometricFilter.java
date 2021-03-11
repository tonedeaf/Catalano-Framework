/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tdlibs.catalano.Imaging.Filters.Photometric;

import com.tdlibs.catalano.Imaging.FastBitmap;
import com.tdlibs.catalano.Imaging.IApplyInPlace;

/**
 *
 * @author Diego catalano
 */
public interface IPhotometricFilter extends IApplyInPlace{
    @Override
    public void applyInPlace(FastBitmap fastBitmap);
}
