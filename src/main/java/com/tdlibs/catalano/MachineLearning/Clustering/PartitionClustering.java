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

package com.tdlibs.catalano.MachineLearning.Clustering;

import com.tdlibs.catalano.Math.Distances.Distance;
import com.tdlibs.catalano.Math.Distances.IDivergence;
import com.tdlibs.catalano.Math.Tools;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Abstract class of partition clustering. Partition methods break
 * the observation into distinct non-overlapping groups.
 * 
 * @param <T> the type of input object.
 * 
 * @author Haifeng Li
 */
public abstract class PartitionClustering <T> implements Serializable {
    /**
     * The number of clusters.
     */
    int k;
    /**
     * The cluster labels of data.
     */
    int[] y;
    /**
     * The number of samples in each cluster.
     */
    int[] size;

    /**
     * Constructor.
     */
    PartitionClustering() {}

    /**
     * Returns the number of clusters.
     */
    public int getNumClusters() {
        return k;
    }

    /**
     * Returns the cluster labels of data.
     */
    public int[] getClusterLabel() {
        return y;
    }

    /**
     * Returns the size of clusters.
     */
    public int[] getClusterSize() {
        return size;
    }
    
    /**
     * Squared Euclidean distance with handling missing values (represented as NaN).
     */
    static double squaredDistance(double[] x, double[] y) {
        int n = x.length;
        int m = 0;
        double dist = 0.0;

        for (int i = 0; i < n; i++) {
            if (!Double.isNaN(x[i]) && !Double.isNaN(y[i])) {
                m++;
                double d = x[i] - y[i];
                dist += d * d;
            }
        }

        if (m == 0) {
            dist = Double.MAX_VALUE;
        } else {
            dist = n * dist / m;
        }

        return dist;
    }
    
    static enum DistanceMethod {
        /**
         * Squared Euclidean distance for K-Means.
         */
        EUCLIDEAN,
        /**
         * Squared Euclidean distance with missing value handling for K-Means.
         */
        EUCLIDEAN_MISSING_VALUES,
        /**
         * Jensen-Shannon divergence for SIB.
         */
        JENSEN_SHANNON_DIVERGENCE
    }
    
    /**
     * Initialize cluster membership of input objects with KMeans++ algorithm.
     * Many clustering methods, e.g. k-means, need a initial clustering
     * configuration as a seed.
     * <p>
     * K-Means++ is based on the intuition of spreading the k initial cluster
     * centers away from each other. The first cluster center is chosen uniformly
     * at random from the data points that are being clustered, after which each
     * subsequent cluster center is chosen from the remaining data points with
     * probability proportional to its distance squared to the point's closest
     * cluster center.
     * <p>
     * The exact algorithm is as follows:
     * <ol>
     * <li> Choose one center uniformly at random from among the data points. </li>
     * <li> For each data point x, compute D(x), the distance between x and the nearest center that has already been chosen. </li>
     * <li> Choose one new data point at random as a new center, using a weighted probability distribution where a point x is chosen with probability proportional to D<sup>2</sup>(x). </li>
     * <li> Repeat Steps 2 and 3 until k centers have been chosen. </li>
     * <li> Now that the initial centers have been chosen, proceed using standard k-means clustering. </li>
     * </ol>
     * This seeding method gives out considerable improvements in the final error
     * of k-means. Although the initial selection in the algorithm takes extra time,
     * the k-means part itself converges very fast after this seeding and thus
     * the algorithm actually lowers the computation time too.
     * 
     * <h2>References</h2>
     * <ol>
     * <li> D. Arthur and S. Vassilvitskii. "K-means++: the advantages of careful seeding". ACM-SIAM symposium on Discrete algorithms, 1027-1035, 2007.</li>
     * <li> Anna D. Peterson, Arka P. Ghosh and Ranjan Maitra. A systematic evaluation of different methods for initializing the K-means clustering algorithm. 2010.</li>
     * </ol>
     * 
     * @param data data objects to be clustered.
     * @param k the number of cluster.
     * @return the cluster labels.
     */
    static int[] seed(double[][] data, int k, DistanceMethod method) {
        int n = data.length;
        int[] y = new int[n];
        //Random r = new Random(0);
        //double[] centroid = data[r.nextInt(n)];
        double[] centroid = data[Tools.Random().nextInt(n)];

        double[] d = new double[n];
        for (int i = 0; i < n; i++) {
            d[i] = Double.MAX_VALUE;
        }

        // pick the next center
        for (int j = 1; j < k; j++) {
            // Loop over the samples and compare them to the most recent center.  Store
            // the distance from each sample to its closest center in scores.
            for (int i = 0; i < n; i++) {
                // compute the distance between this sample and the current center
                double dist = 0.0;
                switch (method) {
                    case EUCLIDEAN:
                        dist = Distance.SquaredEuclidean(data[i], centroid);
                        break;
                    case EUCLIDEAN_MISSING_VALUES:
                        dist = squaredDistance(data[i], centroid);
                        break;
                    case JENSEN_SHANNON_DIVERGENCE:
                        dist = Distance.JensenShannonDivergence(data[i], centroid);
                        break;
                }
                
                if (dist < d[i]) {
                    d[i] = dist;
                    y[i] = j - 1;
                }
            }

            double cutoff = Math.random() * Tools.Sum(d);//Math.random() * Tools.Sum(d);
            double cost = 0.0;
            int index = 0;
            for (; index < n; index++) {
                cost += d[index];
                if (cost >= cutoff) {
                    break;
                }
            }

            centroid = data[index];
        }

        for (int i = 0; i < n; i++) {
            // compute the distance between this sample and the current center
            double dist = 0.0;
            switch (method) {
                case EUCLIDEAN:
                    dist = Distance.SquaredEuclidean(data[i], centroid);
                    break;
                case EUCLIDEAN_MISSING_VALUES:
                    dist = squaredDistance(data[i], centroid);
                    break;
                case JENSEN_SHANNON_DIVERGENCE:
                    dist = Distance.JensenShannonDivergence(data[i], centroid);
                    break;
            }
            
            if (dist < d[i]) {
                d[i] = dist;
                y[i] = k - 1;
            }
        }

        return y;
    }

    /**
     * Initialize cluster membership of input objects with KMeans++ algorithm.
     * Many clustering methods, e.g. k-means, need a initial clustering
     * configuration as a seed.
     * <p>
     * K-Means++ is based on the intuition of spreading the k initial cluster
     * centers away from each other. The first cluster center is chosen uniformly
     * at random from the data points that are being clustered, after which each
     * subsequent cluster center is chosen from the remaining data points with
     * probability proportional to its distance squared to the point's closest
     * cluster center.
     * <p>
     * The exact algorithm is as follows:
     * <ol>
     * <li> Choose one center uniformly at random from among the data points. </li>
     * <li> For each data point x, compute D(x), the distance between x and the nearest center that has already been chosen. </li>
     * <li> Choose one new data point at random as a new center, using a weighted probability distribution where a point x is chosen with probability proportional to D<sup>2</sup>(x). </li>
     * <li> Repeat Steps 2 and 3 until k centers have been chosen. </li>
     * <li> Now that the initial centers have been chosen, proceed using standard k-means clustering. </li>
     * </ol>
     * This seeding method gives out considerable improvements in the final error
     * of k-means. Although the initial selection in the algorithm takes extra time,
     * the k-means part itself converges very fast after this seeding and thus
     * the algorithm actually lowers the computation time too.
     * 
     * <h2>References</h2>
     * <ol>
     * <li> D. Arthur and S. Vassilvitskii. "K-means++: the advantages of careful seeding". ACM-SIAM symposium on Discrete algorithms, 1027-1035, 2007.</li>
     * <li> Anna D. Peterson, Arka P. Ghosh and Ranjan Maitra. A systematic evaluation of different methods for initializing the K-means clustering algorithm. 2010.</li>
     * </ol>
     * 
     * @param <T> the type of input object.
     * @param data data objects array of size n.
     * @param medoids an array of size k to store cluster medoids on output.
     * @param y an array of size n to store cluster labels on output.
     * @param d an array of size n to store the distance of each sample to nearest medoid.
     * @return the initial cluster distortion.
     */
    static <T> double seed(IDivergence<T> distance, T[] data, T[] medoids, int[] y, double[] d) {
        int n = data.length;
        int k = medoids.length;
        //Random r = new Random(0);
        //T medoid = data[r.nextInt(n)];
        T medoid = data[Tools.Random().nextInt(n)];
        medoids[0] = medoid;

        Arrays.fill(d, Double.MAX_VALUE);

        // pick the next center
        for (int j = 1; j < k; j++) {
            // Loop over the samples and compare them to the most recent center.  Store
            // the distance from each sample to its closest center in scores.
            for (int i = 0; i < n; i++) {
                // compute the distance between this sample and the current center
                double dist = distance.Compute(data[i], medoid);
                if (dist < d[i]) {
                    d[i] = dist;
                    y[i] = j - 1;
                }
            }

            double cutoff = Math.random() * Tools.Sum(d);//Math.random() * Tools.Sum(d);
            double cost = 0.0;
            int index = 0;
            for (; index < n; index++) {
                cost += d[index];
                if (cost >= cutoff) {
                    break;
                }
            }

            medoid = data[index];
            medoids[j] = medoid;
        }

        for (int i = 0; i < n; i++) {
            // compute the distance between this sample and the current center
            double dist = distance.Compute(data[i], medoid);
            if (dist < d[i]) {
                d[i] = dist;
                y[i] = k - 1;
            }
        }

        double distortion = 0.0;
        for (int i = 0; i < n; ++i) {
            distortion += d[i];
        }

        return distortion;
    }
}