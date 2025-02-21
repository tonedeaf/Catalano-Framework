// catalano Machine Learning Library
// The catalano Framework
//
// Copyright 2015 Haifeng Li
// haifeng.hli at gmail.com
//
// Based on Smile (Statistical Machine Intelligence & Learning Engine)
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.tdlibs.catalano.MachineLearning.Classification;

import com.tdlibs.catalano.Core.Concurrent.MulticoreExecutor;
import com.tdlibs.catalano.Core.Structs.DoubleArrayList;
import com.tdlibs.catalano.Math.Matrix;
import com.tdlibs.catalano.Math.SparseArray;
import com.tdlibs.catalano.Math.Tools;
import com.tdlibs.catalano.Statistics.Kernels.IMercerKernel;
import com.tdlibs.catalano.Statistics.Kernels.Linear;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Support vector machines for classification. The basic support vector machine
 * is a binary linear classifier which chooses the hyperplane that represents
 * the largest separation, or margin, between the two classes. If such a
 * hyperplane exists, it is known as the maximum-margin hyperplane and the
 * linear classifier it defines is known as a maximum margin classifier.
 * <p>
 * If there exists no hyperplane that can perfectly split the positive and
 * negative instances, the soft margin method will choose a hyperplane
 * that splits the instances as cleanly as possible, while still maximizing
 * the distance to the nearest cleanly split instances.
 * <p>
 * The nonlinear SVMs are created by applying the kernel trick to
 * maximum-margin hyperplanes. The resulting algorithm is formally similar,
 * except that every dot product is replaced by a nonlinear kernel function.
 * This allows the algorithm to fit the maximum-margin hyperplane in a
 * transformed feature space. The transformation may be nonlinear and
 * the transformed space be high dimensional. For example, the feature space
 * corresponding Gaussian kernel is a Hilbert space of infinite dimension.
 * Thus though the classifier is a hyperplane in the high-dimensional feature
 * space, it may be nonlinear in the original input space. Maximum margin
 * classifiers are well regularized, so the infinite dimension does not spoil
 * the results.
 * <p>
 * The effectiveness of SVM depends on the selection of kernel, the kernel's
 * parameters, and soft margin parameter C. Given a kernel, best combination
 * of C and kernel's parameters is often selected by a grid-search with
 * cross validation.
 * <p>
 * The dominant approach for creating multi-class SVMs so is to reduce the
 * single multi-class problem into multiple binary classification problems.
 * Common methods for such reduction is to build binary classifiers which
 * distinguish between (i) one of the labels to the rest (one-versus-all)
 * or (ii) between every pair of classes (one-versus-one). Classification
 * of new instances for one-versus-all case is done by a winner-takes-all
 * strategy, in which the classifier with the highest output function assigns
 * the class. For the one-versus-one approach, classification
 * is done by a max-wins voting strategy, in which every classifier assigns
 * the instance to one of the two classes, then the vote for the assigned
 * class is increased by one vote, and finally the class with most votes
 * determines the instance classification.
 * 
 * <h2>References</h2>
 * <ol>
 * <li> Christopher J. C. Burges. A Tutorial on Support Vector Machines for Pattern Recognition. Data Mining and Knowledge Discovery 2:121-167, 1998.</li>
 * <li> John Platt. Sequential Minimal Optimization: A Fast Algorithm for Training Support Vector Machines.</li>
 * <li> Rong-En Fan, Pai-Hsuen, and Chih-Jen Lin. Working Set Selection Using Second Order Information for Training Support Vector Machines. JMLR, 6:1889-1918, 2005.</li>
 * <li> Antoine Bordes, Seyda Ertekin, Jason Weston and Leon Bottou. Fast Kernel Classifiers with Online and Active Learning, Journal of Machine Learning Research, 6:1579-1619, 2005.</li>
 * <li> Tobias Glasmachers and Christian Igel. Second Order SMO Improves SVM Online and Active Learning.</li>
 * <li> Chih-Chung Chang and Chih-Jen Lin. LIBSVM: a Library for Support Vector Machines.</li>
 * </ol>
 * 
 * @param <T> the type of input object.
 * 
 * @author Haifeng Li
 */
public class SVM <T> implements Serializable {
    /**
     * The type of multi-class SVMs.
     */
    public enum Strategy {

        /**
         * One vs one classification.
         */
        ONE_VS_ONE,
        /**
         * One vs all classification.
         */
        ONE_VS_ALL,
    };

    /**
     * The default value for K_tt + K_ss - 2 * K_ts if kernel is not positive.
     */
    private static final double TAU = 1E-12;
    /**
     * Learned two-class support vector machine.
     */
    private LASVM svm;
    /**
     * Learned multi-class support vector machines.
     */
    private List<LASVM> svms;
    /**
     * The kernel function.
     */
    private IMercerKernel kernel;
    /**
     * The dimensionality of instances. Useful for sparse arrays.
     */
    private int p;
    /**
     * The number of classes;
     */
    private int k;
    /**
     * The strategy for multi-class classification.
     */
    private Strategy strategy = Strategy.ONE_VS_ONE;
    /**
     * The class weight.
     */
    private double[] wi;
    /**
     * The tolerance of convergence test.
     */
    private double tol = 1E-3;

    /**
     * Online Two-class SVM.
     */
    final class LASVM implements Serializable{

        /**
         * Support vector.
         */
        class SupportVector implements Serializable{
            /**
             * Support vector.
             */
            T x;
            /**
             * Support vector label.
             */
            int y;
            /**
             * Lagrangian multiplier of support vector.
             */
            double alpha;
            /**
             * Gradient y - K&alpha;.
             */
            double g;
            /**
             * Lower bound of alpha.
             */
            double cmin;
            /**
             * Upper bound of alpha.
             */
            double cmax;
            /**
             * Kernel value k(x, x)
             */
            double k;
            /**
             * Kernel value cache.
             */
            DoubleArrayList kcache;
        }
        /**
         * The soft margin penalty parameter for positive samples.
         */
        private double Cp = 1.0;
        /**
         * The soft margin penalty parameter for negative samples.
         */
        private double Cn = 1.0;
        /**
         * Support vectors.
         */
        List<SupportVector> sv = new ArrayList<SupportVector>();
        /**
         * Weight vector for linear SVM.
         */
        double[] w;
        /**
         * Threshold of decision function.
         */
        double b = 0.0;
        /**
         * The number of support vectors.
         */
        int nsv = 0;
        /**
         * The number of bounded support vectors.
         */
        int nbsv = 0;
        /**
         * If minimax is called after update.
         */
        transient boolean minmaxflag = false;
        /**
         * Most violating pair.
         * argmin gi of m_i < alpha_i
         * argmax gi of alpha_i < M_i
         * where m_i = min{0, y_i * C}
         * and   M_i = max{0, y_i * C}
         */
        transient SupportVector svmin = null;
        transient SupportVector svmax = null;
        transient double gmin = Double.MAX_VALUE;
        transient double gmax = -Double.MAX_VALUE;

        /**
         * Constructor.
         * @param Cp the soft margin penalty parameter for positive instances.
         * @param Cn the soft margin penalty parameter for negative instances.
         */
        LASVM(double Cp, double Cn) {
            this.Cp = Cp;
            this.Cn = Cn;
        }
        
        /**
         * Trains the SVM with the given dataset for one epoch. The caller may
         * call this method multiple times to obtain better accuracy although
         * one epoch is usually sufficient. After calling this method sufficient
         * times (usually 1 or 2), the users should call {@link #finalize()}
         * to further process support vectors.
         */
        void learn(T[] x, int[] y) {
            learn(x, y, null);
        }
        
        /**
         * Trains the SVM with the given dataset for one epoch. The caller may
         * call this method multiple times to obtain better accuracy although
         * one epoch is usually sufficient. After calling this method sufficient
         * times (usually 1 or 2), the users should call {@link #finalize()}
         * to further process support vectors.
         */
        void learn(T[] x, int[] y, double[] weight) {
            if (p == 0 && kernel instanceof Linear) {
                if (x instanceof double[][]) {
                    double[] x0 = (double[]) x[0];
                    p = x0.length;
                } else if (x instanceof float[][]) {
                    float[] x0 = (float[]) x[0];
                    p = x0.length;
                } else {
                    throw new UnsupportedOperationException("Unsupported data type for linear kernel.");
                }
            }

            int c1 = 0, c2 = 0;
            for (SupportVector v : sv) {
                if (v != null) {
                    if (v.y > 0) c1++;
                    else if (v.y < 0) c2++;
                }
            }
            
            // If the SVM is empty or has very few support vectors, use some
            // instances as initial support vectors.
            final int n = x.length;
            if (c1 < 5 || c2 < 5) {
                for (int i = 0; i < n; i++) {
                    if (y[i] == 1 && c1 < 5) {
                        if (weight == null) {
                            process(x[i], y[i]);
                        } else {
                            process(x[i], y[i], weight[i]);
                        }
                        c1++;
                    }
                    if (y[i] == -1 && c2 < 5) {
                        if (weight == null) {
                            process(x[i], y[i]);
                        } else {
                            process(x[i], y[i], weight[i]);
                        }
                        c2++;
                    }
                    if (c1 >= 5 && c2 >= 5) {
                        break;
                    }
                }
            }

            // train SVM in a stochastic order.
            int[] index = Tools.Random().permutate(n);
            for (int i = 0; i < n; i++) {
                if (weight == null) {
                    process(x[index[i]], y[index[i]]);
                } else {
                    process(x[index[i]], y[index[i]], weight[index[i]]);                    
                }

                do {
                    reprocess(tol); // at least one call to reprocess
                    minmax();
                } while (gmax - gmin > 1000);
            }
        }

        /**
         * Returns the function value after training.
         */
        double predict(T x) {
            double f = b;

            if (kernel instanceof Linear && w != null) {
                if (x instanceof double[]) {
                    f += Matrix.InnerProduct(w, (double[]) x);
                } else if (x instanceof SparseArray) {
                    for (SparseArray.Entry e : (SparseArray) x) {
                        f += w[e.i] * e.x;
                    }
                } else {
                    throw new UnsupportedOperationException("Unsupported data type for linear kernel");
                }
            } else {

                for (SupportVector v : sv) {
                    if (v != null) {
                        f += v.alpha * kernel.Function(v.x, x);
                    }
                }
            }

            return f;
        }

        /**
         * Find support vectors with smallest (of I_up) and largest (of I_down) gradients.
         */
        void minmax() {
            if (!minmaxflag) {
                gmin = Double.MAX_VALUE;
                gmax = -Double.MAX_VALUE;

                for (SupportVector v : sv) {
                    if (v != null) {
                        double gi = v.g;
                        double ai = v.alpha;
                        if (gi < gmin && ai > v.cmin) {
                            svmin = v;
                            gmin = gi;
                        }
                        if (gi > gmax && ai < v.cmax) {
                            svmax = v;
                            gmax = gi;
                        }
                    }
                }
                
                minmaxflag = true;
            }
        }

        /**
         * Sequential minimal optimization.
         * @param v1 the first vector of working set.
         * @param v2 the second vector of working set.
         * @param epsgr the tolerance of convergence test.
         */
        boolean smo(SupportVector v1, SupportVector v2, double epsgr) {
            // SO working set selection
            // Determine coordinate to process
            if (v1 == null || v2 == null) {
                if (v1 == null && v2 == null) {
                    minmax();

                    if (gmax > -gmin) {
                        v2 = svmax;
                    } else {
                        v1 = svmin;
                    }
                }

                if (v2 == null) {
                    if (v1.kcache == null) {
                        v1.kcache = new DoubleArrayList(sv.size());
                        for (SupportVector v : sv) {
                            if (v != null) {
                                v1.kcache.add(kernel.Function(v1.x, v.x));
                            } else {
                                v1.kcache.add(0.0);
                            }
                        }
                    }

                    // determine imax
                    double km = v1.k;
                    double gm = v1.g;
                    double best = 0.0;
                    for (int i = 0; i < sv.size(); i++) {
                        SupportVector v = sv.get(i);
                        if (v == null) {
                            continue;
                        }

                        double Z = v.g - gm;
                        double k = v1.kcache.get(i);
                        double curv = km + v.k - 2.0 * k;
                        // double curv = 2.0 - 2.0 * k;   // for Gaussian kernel only
                        if (curv <= 0.0) curv = TAU;
                        double mu = Z / curv;
                        if ((mu > 0.0 && v.alpha < v.cmax) || (mu < 0.0 && v.alpha > v.cmin)) {
                            double gain = Z * mu;
                            if (gain > best) {
                                best = gain;
                                v2 = v;
                            }
                        }
                    }
                } else {
                    if (v2.kcache == null) {
                        v2.kcache = new DoubleArrayList(sv.size());
                        for (SupportVector v : sv) {
                            if (v != null) {
                                v2.kcache.add(kernel.Function(v2.x, v.x));
                            } else {
                                v2.kcache.add(0.0);
                            }
                        }
                    }

                    // determine imin
                    double km = v2.k;
                    double gm = v2.g;
                    double best = 0.0;
                    for (int i = 0; i < sv.size(); i++) {
                        SupportVector v = sv.get(i);
                        if (v == null) {
                            continue;
                        }

                        double Z = gm - v.g;
                        double k = v2.kcache.get(i);
                        double curv = km + v.k - 2.0 * k;
                        // double curv = 2.0 - 2.0 * k;   // for Gaussian kernel only
                        if (curv <= 0.0) curv = TAU;

                        double mu = Z / curv;
                        if ((mu > 0.0 && v.alpha > v.cmin) || (mu < 0.0 && v.alpha < v.cmax)) {
                            double gain = Z * mu;
                            if (gain > best) {
                                best = gain;
                                v1 = v;
                            }
                        }
                    }
                }
            }

            if (v1 == null || v2 == null) {
                return false;
            }

            if (v1.kcache == null) {
                v1.kcache = new DoubleArrayList(sv.size());
                for (SupportVector v : sv) {
                    if (v != null) {
                        v1.kcache.add(kernel.Function(v1.x, v.x));
                    } else {
                        v1.kcache.add(0.0);
                    }
                }
            }

            if (v2.kcache == null) {
                v2.kcache = new DoubleArrayList(sv.size());
                for (SupportVector v : sv) {
                    if (v != null) {
                        v2.kcache.add(kernel.Function(v2.x, v.x));
                    } else {
                        v2.kcache.add(0.0);
                    }
                }
            }

            // Determine curvature
            double curv = v1.k + v2.k - 2 * kernel.Function(v1.x, v2.x);		
            if (curv <= 0.0) curv = TAU;
            
            double step = (v2.g - v1.g) / curv;

            // Determine maximal step
            if (step >= 0.0) {
                double ostep = v1.alpha - v1.cmin;
                if (ostep < step) {
                    step = ostep;
                }
                ostep = v2.cmax - v2.alpha;
                if (ostep < step) {
                    step = ostep;
                }
            } else {
                double ostep = v2.cmin - v2.alpha;
                if (ostep > step) {
                    step = ostep;
                }
                ostep = v1.alpha - v1.cmax;
                if (ostep > step) {
                    step = ostep;
                }
            }

            // Perform update
            v1.alpha -= step;
            v2.alpha += step;
            for (int i = 0; i < sv.size(); i++) {
                SupportVector v = sv.get(i);
                if (v != null) {
                    v.g -= step * (v2.kcache.get(i) - v1.kcache.get(i));
                }
            }

            minmaxflag = false;

            // optimality test
            minmax();
            b = (gmax + gmin) / 2;            
            if (gmax - gmin < epsgr) {
                return false;
            }

            return true;
        }

        /**
         * Process a new sample.
         */
        boolean process(T x, int y) {
            return process(x, y, 1.0);
        }
        
        /**
         * Process a new sample.
         */
        boolean process(T x, int y, double weight) {
            if (y != +1 && y != -1) {
                throw new IllegalArgumentException("Invalid label: " + y);
            }

            if (weight <= 0.0) {
                throw new IllegalArgumentException("Invalid instance weight: " + weight);                
            }
            
            // Compute gradient
            double g = y;
            DoubleArrayList kcache = new DoubleArrayList(sv.size() + 1);
            if (sv.size() > 0) {
                for (SupportVector v : sv) {
                    if (v != null) {
                        // Bail out if already in expansion?
                        if (v.x == x) {
                            return true;
                        }

                        double k = kernel.Function(v.x, x);
                        g -= v.alpha * k;
                        kcache.add(k);
                    } else {
                        kcache.add(0.0);
                    }
                }

                // Decide insertion
                minmax();
                if (gmin < gmax) {
                    if ((y > 0 && g < gmin) || (y < 0 && g > gmax)) {
                        return false;
                    }
                }
            }

            // Insert
            SupportVector v = new SupportVector();
            v.x = x;
            v.y = y;
            v.alpha = 0.0;
            v.g = g;
            v.k = kernel.Function(x, x);
            v.kcache = kcache;
            if (y > 0) {
                v.cmin = 0;
                v.cmax = weight * Cp;
            } else {
                v.cmin = -weight * Cn;
                v.cmax = 0;
            }

            int i = sv.size();
            for (; i < sv.size(); i++) {
                if (sv.get(i) == null) {
                    sv.set(i, v);
                    kcache.set(i, v.k);

                    for (int j = 0; j < sv.size(); j++) {
                        SupportVector v1 = sv.get(j);
                        if (v1 != null && v1.kcache != null) {
                            v1.kcache.set(i, kcache.get(j));
                        }
                    }

                    break;
                }
            }

            if (i >= sv.size()) {
                for (int j = 0; j < sv.size(); j++) {
                    SupportVector v1 = sv.get(j);
                    if (v1 != null && v1.kcache != null) {
                        v1.kcache.add(kcache.get(j));
                    }
                }

                v.kcache.add(v.k);
                sv.add(v);
            }

            // Process
            if (y > 0) {
                smo(null, v, 0.0);
            } else {
                smo(v, null, 0.0);
            }
            
            minmaxflag = false;
            return true;
        }

        /**
         * Reprocess support vectors.
         * @param epsgr the tolerance of convergence test.
         */
        boolean reprocess(double epsgr) {
            boolean status = smo(null, null, epsgr);
            evict();
            return status;
        }

        /**
         * Call reprocess until converge.
         */
        void finish() {
            finish(tol);
        }

        /**
         * Call reprocess until converge.
         * @param epsgr the tolerance of convergence test.
         */
        void finish(double epsgr) {
            //System.out.print("SVM finializes the training.");
            for (int count = 1; smo(null, null, epsgr); count++) {
                if (count % 1000 == 0) {
                    //System.out.print('.');
                }
            }
            //System.out.println();

            Iterator<SupportVector> iter = sv.iterator();
            while (iter.hasNext()) {
                SupportVector v = iter.next();
                if (v == null) {
                    iter.remove();
                } else if (v.alpha == 0) {
                    if ((v.g >= gmax && 0 >= v.cmax) || (v.g <= gmin && 0 <= v.cmin)) {
                        iter.remove();
                    }
                }
            }
            cleanup();

            if (kernel instanceof Linear) {                
                w = new double[p];

                for (SupportVector v : sv) {
                    if (v.x instanceof double[]) {
                        double[] x = (double[]) v.x;

                        for (int i = 0; i < w.length; i++) {
                            w[i] += v.alpha * x[i];
                        }

                    } else if (v.x instanceof int[]) {
                        int[] x = (int[]) v.x;

                        for (int i = 0; i < x.length; i++) {
                            w[x[i]] += v.alpha;
                        }

                    } else if (v.x instanceof SparseArray) {
                        for (SparseArray.Entry e : (SparseArray) v.x) {
                            w[e.i] += v.alpha * e.x;
                        }
                    }
                }
            }
        }

        void evict() {
            minmax();
            
            for (int i = 0; i < sv.size(); i++) {
                SupportVector v = sv.get(i);
                if (v != null && v.alpha == 0) {
                    if ((v.g >= gmax && 0 >= v.cmax) || (v.g <= gmin && 0 <= v.cmin)) {
                        sv.set(i, null);
                    }
                }
            }
        }

        /**
         * Cleanup kernel cache to free memory.
         */
        void cleanup() {
            nsv = 0;
            nbsv = 0;
            for (SupportVector v : sv) {
                if (v != null) {
                    nsv++;

                    v.kcache = null;
                    if (v.alpha == v.cmin || v.alpha == v.cmax) {
                        nbsv++;
                    }
                }
            }

            //System.out.format("%d support vectors, %d bounded\n", nsv, nbsv);
        }
    }

    /**
     * Constructor of binary SVM.
     * @param kernel the kernel function.
     * @param C the soft margin penalty parameter.
     */
    public SVM(IMercerKernel kernel, double C) {
        this(kernel, C, C);
    }

    /**
     * Constructor of binary SVM.
     * @param kernel the kernel function.
     * @param Cp the soft margin penalty parameter for positive instances.
     * @param Cn the soft margin penalty parameter for negative instances.
     */
    public SVM(IMercerKernel kernel, double Cp, double Cn) {
        if (Cp < 0.0) {
            throw new IllegalArgumentException("Invalid postive instance soft margin penalty: " + Cp);
        }
        
        if (Cn < 0.0) {
            throw new IllegalArgumentException("Invalid negative instance soft margin penalty: " + Cn);
        }
        
        this.kernel = kernel;
        this.k = 2;
        svm = new LASVM(Cp, Cn);
    }

    /**
     * Constructor of multi-class SVM.
     * @param kernel the kernel function.
     * @param C the soft margin penalty parameter.
     * @param k the number of classes.
     */
    public SVM(IMercerKernel kernel, double C, int k, Strategy strategy) {
        if (C < 0.0) {
            throw new IllegalArgumentException("Invalid soft margin penalty: " + C);
        }
        
        if (k < 3) {
            throw new IllegalArgumentException("Invalid number of classes: " + k);
        }
        
        this.kernel = kernel;
        this.k = k;
        this.strategy = strategy;
        
        if (strategy == Strategy.ONE_VS_ALL) {
            svms = new ArrayList<LASVM>(k);
            for (int i = 0; i < k; i++) {
                svms.add(new LASVM(C, C));
            }
        } else {
            svms = new ArrayList<LASVM>(k * (k - 1) / 2);
            for (int i = 0; i < k; i++) {
                for (int j = i + 1; j < k; j++) {
                    svms.add(new LASVM(C, C));
                }
            }
        }
    }

    /**
     * Constructor of multi-class SVM.
     * @param kernel the kernel function.
     * @param C the soft margin penalty parameter
     * @param weight class weight. Must be positive. The soft margin penalty
     * of class i will be weight[i] * C.
     */
    public SVM(IMercerKernel kernel, double C, double[] weight, Strategy strategy) {
        if (C < 0.0) {
            throw new IllegalArgumentException("Invalid soft margin penalty: " + C);
        }
        
        if (weight.length < 3) {
            throw new IllegalArgumentException("Invalid number of classes: " + weight.length);
        }
        
        for (int i = 0; i < weight.length; i++) {
            if (weight[i] <= 0.0) {
                throw new IllegalArgumentException("Invalid class weight: " + weight[i]);            
            }
        }
        
        this.kernel = kernel;
        this.k = weight.length;
        this.strategy = strategy;
        this.wi = weight;
        
        if (strategy == Strategy.ONE_VS_ALL) {
            svms = new ArrayList<LASVM>(k);
            for (int i = 0; i < k; i++) {
                svms.add(new LASVM(C, C));
            }
        } else {
            svms = new ArrayList<LASVM>(k * (k - 1) / 2);
            for (int i = 0; i < k; i++) {
                for (int j = i + 1; j < k; j++) {
                    svms.add(new LASVM(weight[i]*C, weight[j]*C));
                }
            }
        }
    }
    
    /**
     * Sets the tolerance of convergence test.
     * 
     * @param tol the tolerance of convergence test.
     */
    public void setTolerance(double tol) {
        if (tol <= 0.0) {
            throw new IllegalArgumentException("Invlaid tolerance of convergence test:" + tol);
        }
        
        this.tol = tol;
    }

    
    public void Learn(T x, int y) {
        Learn(x, y, 1.0);
    }

    /**
     * Online update the classifier with a new training instance.
     * Note that this method is NOT multi-thread safe.
     * 
     * @param x training instance.
     * @param y training label.
     * @param weight instance weight. Must be positive. The soft margin penalty
     * parameter for instance will be weight * C.
     */
    public void Learn(T x, int y, double weight) {
        if (y < 0 || y >= k) {
            throw new IllegalArgumentException("Invalid label");
        }

        if (weight <= 0.0) {
            throw new IllegalArgumentException("Invalid instance weight: " + weight);
        }
            
        if (k == 2) {
            if (y == 1) {
                svm.process(x, +1, weight);
            } else {
                svm.process(x, -1, weight);
            }
        } else if (strategy == Strategy.ONE_VS_ALL) {
            if (wi != null) {
                weight *= wi[y];
            }
            
            for (int i = 0; i < k; i++) {
                if (y == i) {
                    svms.get(i).process(x, +1, weight);
                } else {
                    svms.get(i).process(x, -1, weight);
                }
            }
        } else {
            for (int i = 0, m = 0; i < k; i++) {
                for (int j = i + 1; j < k; j++, m++) {
                    if (y == i) {
                        svms.get(m).process(x, +1, weight);
                    } else if (y == j) {
                        svms.get(m).process(x, -1, weight);
                    }
                }
            }
        }
    }

    /**
     * Trains the SVM with the given dataset for one epoch. The caller may
     * call this method multiple times to obtain better accuracy although
     * one epoch is usually sufficient. After calling this method sufficient
     * times (usually 1 or 2), the users should call {@link #finalize()}
     * to further process support vectors.
     * 
     * @param x training instances.
     * @param y training labels in [0, k), where k is the number of classes.
     */
    public void Learn(T[] x, int[] y) {
        Learn(x, y, null);
    }
    
    /**
     * Trains the SVM with the given dataset for one epoch. The caller may
     * call this method multiple times to obtain better accuracy although
     * one epoch is usually sufficient. After calling this method sufficient
     * times (usually 1 or 2), the users should call {@link #finalize()}
     * to further process support vectors.
     * 
     * @param x training instances.
     * @param y training labels in [0, k), where k is the number of classes.
     * @param weight instance weight. Must be positive. The soft margin penalty
     * parameter for instance i will be weight[i] * C.
     */
    @SuppressWarnings("unchecked")
    public void Learn(T[] x, int[] y, double[] weight) {
        if (x.length != y.length) {
            throw new IllegalArgumentException(String.format("The sizes of X and Y don't match: %d != %d", x.length, y.length));
        }

        if (weight != null && x.length != weight.length) {
            throw new IllegalArgumentException(String.format("The sizes of X and instance weight don't match: %d != %d", x.length, weight.length));
        }

        int miny = Matrix.Min(y);
        if (miny < 0) {
            throw new IllegalArgumentException("Negative class label:" + miny);
        }

        int maxy = Matrix.Max(y);
        if (maxy >= k) {
            throw new IllegalArgumentException("Invalid class label:" + maxy);
        }

        if (k == 2) {
            int[] yi = new int[y.length];
            for (int i = 0; i < y.length; i++) {
                if (y[i] == 1) {
                    yi[i] = +1;
                } else {
                    yi[i] = -1;
                }
            }
            
            if (weight == null) {
                svm.learn(x, yi);
            } else {
                svm.learn(x, yi, weight);
            }
        } else if (strategy == Strategy.ONE_VS_ALL) {
            List<TrainingTask> tasks = new ArrayList<TrainingTask>(k);
            for (int i = 0; i < k; i++) {
                int[] yi = new int[y.length];
                double[] w = wi == null ? weight : new double[y.length];
                for (int l = 0; l < y.length; l++) {
                    if (y[l] == i) {
                        yi[l] = +1;
                    } else {
                        yi[l] = -1;
                    }
                    
                    if (wi != null) {
                        w[l] = wi[y[l]];
                        if (weight != null) {
                            w[l] *= weight[l];
                        }
                    }
                }

                tasks.add(new TrainingTask(svms.get(i), x, yi, w));
            }

            try {
                MulticoreExecutor.run(tasks);
            } catch (Exception e) {
                //System.err.println(e.getMessage());
            }
        } else {
            List<TrainingTask> tasks = new ArrayList<TrainingTask>(k * (k - 1) / 2);
            for (int i = 0, m = 0; i < k; i++) {
                for (int j = i + 1; j < k; j++, m++) {
                    int n = 0;
                    for (int l = 0; l < y.length; l++) {
                        if (y[l] == i || y[l] == j) {
                            n++;
                        }
                    }

                    T[] xij = (T[]) java.lang.reflect.Array.newInstance(x.getClass().getComponentType(), n);
                    int[] yij = new int[n];
                    double[] wij = weight == null ? null : new double[n];

                    for (int l = 0, q = 0; l < y.length; l++) {
                        if (y[l] == i) {
                            xij[q] = x[l];
                            yij[q] = +1;
                            if (weight != null) {
                                wij[q] = weight[l];                                
                            }
                            q++;
                        } else if (y[l] == j) {
                            xij[q] = x[l];
                            yij[q] = -1;
                            if (weight != null) {
                                wij[q] = weight[l];                                
                            }
                            q++;
                        }
                    }

                    tasks.add(new TrainingTask(svms.get(m), xij, yij, wij));
                }
            }

            try {
                MulticoreExecutor.run(tasks);
            } catch (Exception e) {
                //System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Process support vectors until converge.
     */
    public void Finish() {
        if (k == 2) {
            svm.finish();
        } else {
            List<ProcessTask> tasks = new ArrayList<ProcessTask>(svms.size());
            for (LASVM s : svms) {
                tasks.add(new ProcessTask(s));
            }

            try {
                MulticoreExecutor.run(tasks);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Trains a LASVM.
     */
    class TrainingTask implements Callable<LASVM> {
        LASVM svm;
        T[] x;
        int[] y;
        double[] weight; // instance weight

        TrainingTask(LASVM svm, T[] x, int[] y, double[] weight) {
            this.svm = svm;
            this.x = x;
            this.y = y;
            this.weight = weight;
        }

        @Override
        public LASVM call() {
            svm.learn(x, y, weight);
            return svm;
        }
    }

    /**
     * Reprocess a LASVM.
     */
    class ProcessTask implements Callable<LASVM> {
        LASVM svm;
        ProcessTask(LASVM svm) {
            this.svm = svm;
        }

        @Override
        public LASVM call() {
            svm.finish();
            return svm;
        }
    }

    
    public int Predict(T x) {
        if (k == 2) {
            // two class
            if (svm.predict(x) > 0) {
                return 1;
            } else {
                return 0;
            }
        } else if (strategy == Strategy.ONE_VS_ALL) {
            // one-vs-all
            int label = 0;
            double maxf = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < svms.size(); i++) {
                double f = svms.get(i).predict(x);
                if (f > maxf) {
                    label = i;
                    maxf = f;
                }
            }
            
            return label;
        } else {
            // one-vs-one
            int[] count = new int[k];
            for (int i = 0, m = 0; i < k; i++) {
                for (int j = i + 1; j < k; j++, m++) {
                    double f = svms.get(m).predict(x);
                    if (f > 0) {
                        count[i]++;
                    } else {
                        count[j]++;
                    }
                }
            }

            int max = 0;
            int label = 0;
            for (int i = 0; i < k; i++) {
                if (count[i] > max) {
                    max = count[i];
                    label = i;
                }
            }
            
            return label;
        }
    }
}