// catalano Machine Learning Library
// The catalano Framework
//
// Copyright 2015 Diego catalano
// diego.catalano at live.com
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

package com.tdlibs.catalano.MachineLearning.Regression.RegressionTrees;

import com.tdlibs.catalano.Core.ArraysUtil;
import com.tdlibs.catalano.Core.Concurrent.MulticoreExecutor;
import com.tdlibs.catalano.MachineLearning.Dataset.DecisionVariable;
import com.tdlibs.catalano.MachineLearning.Classification.DecisionTrees.Learning.RandomForest;
import com.tdlibs.catalano.MachineLearning.Dataset.DatasetRegression;
import com.tdlibs.catalano.MachineLearning.Regression.IRegression;
import com.tdlibs.catalano.Math.Tools;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;

/**
 * Decision tree for regression. A decision tree can be learned by
 * splitting the training set into subsets based on an attribute value
 * test. This process is repeated on each derived subset in a recursive
 * manner called recursive partitioning.
 * <p>
 * Classification and Regression Tree techniques have a number of advantages
 * over many of those alternative techniques.
 * <dl>
 * <dt>Simple to understand and interpret.</dt>
 * <dd>In most cases, the interpretation of results summarized in a tree is
 * very simple. This simplicity is useful not only for purposes of rapid
 * classification of new observations, but can also often yield a much simpler
 * "model" for explaining why observations are classified or predicted in a
 * particular manner.</dd>
 * <dt>Able to handle both numerical and categorical data.</dt>
 * <dd>Other techniques are usually specialized in analyzing datasets that
 * have only one type of variable. </dd>
 * <dt>Tree methods are nonparametric and nonlinear.</dt>
 * <dd>The final results of using tree methods for classification or regression
 * can be summarized in a series of (usually few) logical if-then conditions
 * (tree nodes). Therefore, there is no implicit assumption that the underlying
 * relationships between the predictor variables and the dependent variable
 * are linear, follow some specific non-linear link function, or that they
 * are even monotonic in nature. Thus, tree methods are particularly well
 * suited for data mining tasks, where there is often little a priori
 * knowledge nor any coherent set of theories or predictions regarding which
 * variables are related and how. In those types of data analytics, tree
 * methods can often reveal simple relationships between just a few variables
 * that could have easily gone unnoticed using other analytic techniques.</dd>
 * </dl>
 * One major problem with classification and regression trees is their high
 * variance. Often a small change in the data can result in a very different
 * series of splits, making interpretation somewhat precarious. Besides,
 * decision-tree learners can create over-complex trees that cause over-fitting.
 * Mechanisms such as pruning are necessary to avoid this problem.
 * Another limitation of trees is the lack of smoothness of the prediction
 * surface.
 * <p>
 * Some techniques such as bagging, boosting, and random forest use more than
 * one decision tree for their analysis.
 * 
 * @see GradientTreeBoost
 * @see RandomForest
 *  
 * @author Haifeng LI
 */
public class RegressionTree implements IRegression, Serializable{
    
    private int[] samples;
    private NodeOutput nodeOutput;
    
    /**
     * The attributes of independent variable.
     */
    private DecisionVariable[] attributes;
    /**
     * Variable importance. Every time a split of a node is made on variable
     * the impurity criterion for the two descendent nodes is less than the
     * parent node. Adding up the decreases for each individual variable
     * over the tree gives a simple measure of variable importance.
     */
    private double[] importance;
    /**
     * The root of the regression tree
     */
    private Node root;
    /**
     * The number of instances in a node below which the tree will
     * not split, setting S = 5 generally gives good results.
     */
    private int S = 5;
    /**
     * The maximum number of leaf nodes in the tree.
     */
    private int J = 6;
    /**
     * The number of input variables to be used to determine the decision
     * at a node of the tree.
     */
    private int M;
    /**
     * The number of binary features.
     */
    private int numFeatures;
    /**
     * The index of training values in ascending order. Note that only numeric
     * attributes will be sorted.
     */
    private transient int[][] order;

    /**
     * Get number of maximum leafs.
     * @return Number of maximum leafs.
     */
    public int getNumberOfLeafs() {
        return J;
    }

    /**
     * Set number of maximum leafs.
     * @param J Number of maximum leafs.
     */
    public void setNumberOfLeafs(int J) {
        this.J = J;
    }
    
    /**
     * Returns the variable importance. Every time a split of a node is made
     * on variable the impurity criterion for the two descendent nodes is less
     * than the parent node. Adding up the decreases for each individual
     * variable over the tree gives a simple measure of variable importance.
     *
     * @return the variable importance
     */
    public double[] getImportance() {
        return importance;
    }
    
    /**
     * An interface to calculate node output. Note that samples[i] is the
     * number of sampling of dataset[i]. 0 means that the datum is not
     * included and values of greater than 1 are possible because of
     * sampling with replacement.
     */
    public static interface NodeOutput {
        /**
         * Calculate the node output.
         * @param samples the samples in the node.
         * @return the node output
         */
        public double calculate(int[] samples);
    }
    
    /**
     * Regression tree node.
     */
    class Node implements Serializable{

        /**
         * Predicted real value for this node.
         */
        double output = 0.0;
        /**
         * The split feature for this node.
         */
        int splitFeature = -1;
        /**
         * The split value.
         */
        double splitValue = Double.NaN;
        /**
         * Reduction in squared error compared to parent.
         */
        double splitScore = 0.0;
        /**
         * Children node.
         */
        Node trueChild;
        /**
         * Children node.
         */
        Node falseChild;
        /**
         * Predicted output for children node.
         */
        double trueChildOutput = 0.0;
        /**
         * Predicted output for children node.
         */
        double falseChildOutput = 0.0;

        /**
         * Constructor.
         */
        public Node(double output) {
            this.output = output;
        }

        /**
         * Evaluate the regression tree over an instance.
         */
        public double predict(double[] x) {
            if (trueChild == null && falseChild == null) {
                return output;
            } else {
                if (attributes[splitFeature].type == DecisionVariable.Type.Discrete) {
                    if (x[splitFeature] == splitValue) {
                        return trueChild.predict(x);
                    } else {
                        return falseChild.predict(x);
                    }
                } else if (attributes[splitFeature].type == DecisionVariable.Type.Continuous) {
                    if (x[splitFeature] <= splitValue) {
                        return trueChild.predict(x);
                    } else {
                        return falseChild.predict(x);
                    }
                } else {
                    throw new IllegalStateException("Unsupported attribute type: " + attributes[splitFeature].type);
                }
            }
        }

        /**
         * Evaluate the regression tree over an instance.
         */
        public double predict(int[] x) {
            if (trueChild == null && falseChild == null) {
                return output;
            } else if (x[splitFeature] == (int) splitValue) {
                return trueChild.predict(x);
            } else {
                return falseChild.predict(x);
            }
        }
    }

    /**
     * Regression tree node for training purpose.
     */
    class TrainNode implements Comparable<TrainNode> {
        /**
         * The associated regression tree node.
         */
        Node node;
        /**
         * Child node that passes the test.
         */
        TrainNode trueChild;
        /**
         * Child node that fails the test.
         */
        TrainNode falseChild;
        /**
         * Training dataset.
         */
        double[][] x;
        /**
         * Training data response value.
         */
        double[] y;
        /**
         * The samples for training this node. Note that samples[i] is the
         * number of sampling of dataset[i]. 0 means that the datum is not
         * included and values of greater than 1 are possible because of
         * sampling with replacement.
         */
        int[] samples;

        /**
         * Constructor.
         */
        public TrainNode(Node node, double[][] x, double[] y, int[] samples) {
            this.node = node;
            this.x = x;
            this.y = y;
            this.samples = samples;
        }

        @Override
        public int compareTo(TrainNode a) {
            return (int) Math.signum(a.node.splitScore - node.splitScore);
        }

        /**
         * Calculate the node output for leaves.
         * @param output the output calculate functor.
         */
        public void calculateOutput(NodeOutput output) {
            if (node.trueChild == null && node.falseChild == null) {
                node.output = output.calculate(samples);
            } else {
                if (trueChild != null) {
                    trueChild.calculateOutput(output);
                }
                if (falseChild != null) {
                    falseChild.calculateOutput(output);
                }
            }
        }
        
        /**
         * Finds the best attribute to split on at the current node. Returns
         * true if a split exists to reduce squared error, false otherwise.
         */
        public boolean findBestSplit() {
            int n = 0;
            for (int s : samples) {
                n += s;
            }

            if (n <= S) {
                return false;
            }
            
            double sum = node.output * n;
            int p = attributes.length;
            int[] variables = new int[p];
            for (int i = 0; i < p; i++) {
                variables[i] = i;
            }
            
            // Loop through features and compute the reduction of squared error,
            // which is trueCount * trueMean^2 + falseCount * falseMean^2 - count * parentMean^2                    
            if (M < p) {
                // Training of Random Forest will get into this race condition.
                // smile.math.Math uses a static object of random number generator.
                synchronized (RegressionTree.class) {
                    Tools.Permutate(variables);
                }
                
                // Random forest already runs on parallel.
                for (int j = 0; j < M; j++) {
                    Node split = findBestSplit(n, sum, variables[j]);
                    if (split.splitScore > node.splitScore) {
                        node.splitFeature = split.splitFeature;
                        node.splitValue = split.splitValue;
                        node.splitScore = split.splitScore;
                        node.trueChildOutput = split.trueChildOutput;
                        node.falseChildOutput = split.falseChildOutput;
                    }
                }
            } else {

                List<SplitTask> tasks = new ArrayList<SplitTask>(M);
                for (int j = 0; j < M; j++) {
                    tasks.add(new SplitTask(n, sum, variables[j]));
                }

                try {
                    for (Node split : MulticoreExecutor.run(tasks)) {
                        if (split.splitScore > node.splitScore) {
                            node.splitFeature = split.splitFeature;
                            node.splitValue = split.splitValue;
                            node.splitScore = split.splitScore;
                            node.trueChildOutput = split.trueChildOutput;
                            node.falseChildOutput = split.falseChildOutput;
                        }
                    }
                } catch (Exception ex) {
                    for (int j = 0; j < M; j++) {
                        Node split = findBestSplit(n, sum, variables[j]);
                        if (split.splitScore > node.splitScore) {
                            node.splitFeature = split.splitFeature;
                            node.splitValue = split.splitValue;
                            node.splitScore = split.splitScore;
                            node.trueChildOutput = split.trueChildOutput;
                            node.falseChildOutput = split.falseChildOutput;
                        }
                    }
                }
            }
            
            return (node.splitFeature != -1);
        }
        
        /**
         * Task to find the best split cutoff for attribute j at the current node.
         */
        class SplitTask implements Callable<Node> {

            /**
             * The number instances in this node.
             */
            int n;
            /**
             * The sum of responses of this node.
             */
            double sum;
            /**
             * The index of variables for this task.
             */
            int j;

            SplitTask(int n, double sum, int j) {
                this.n = n;
                this.sum = sum;                
                this.j = j;
            }

            @Override
            public Node call() {
                return findBestSplit(n, sum, j);
            }
        }
        
        /**
         * Finds the best split cutoff for attribute j at the current node.
         * @param n the number instances in this node.
         * @param count the sample count in each class.
         * @param impurity the impurity of this node.
         * @param j the attribute to split on.
         */
        public Node findBestSplit(int n, double sum, int j) {
            int N = x.length;
            Node split = new Node(0.0);
            if (attributes[j].type == DecisionVariable.Type.Discrete) {
                int m = x.length;
                double[] trueSum = new double[m];
                int[] trueCount = new int[m];

                for (int i = 0; i < N; i++) {
                    if (samples[i] > 0) {
                        double target = samples[i] * y[i];

                        // For each true feature of this datum increment the
                        // sufficient statistics for the "true" branch to evaluate
                        // splitting on this feature.
                        int index = (int) x[i][j];
                        trueSum[index] += target;
                        trueCount[index] += samples[i];
                    }
                }

                for (int k = 0; k < m; k++) {
                    double tc = (double) trueCount[k];
                    double fc = n - tc;

                    // If either side is empty, skip this feature.
                    if (tc == 0 || fc == 0) {
                        continue;
                    }

                    // compute penalized means
                    double trueMean = trueSum[k] / tc;
                    double falseMean = (sum - trueSum[k]) / fc;

                    double gain = (tc * trueMean * trueMean + fc * falseMean * falseMean) - n * split.output * split.output;
                    if (gain > split.splitScore) {
                        // new best split
                        split.splitFeature = j;
                        split.splitValue = k;
                        split.splitScore = gain;
                        split.trueChildOutput = trueMean;
                        split.falseChildOutput = falseMean;
                    }
                }
            } else if (attributes[j].type == DecisionVariable.Type.Continuous) {
                double trueSum = 0.0;
                int trueCount = 0;
                double prevx = Double.NaN;

                for (int i : order[j]) {
                    if (samples[i] > 0) {
                        if (Double.isNaN(prevx) || x[i][j] == prevx) {
                            prevx = x[i][j];
                            trueSum += samples[i] * y[i];
                            trueCount += samples[i];
                            continue;
                        }

                        double falseCount = n - trueCount;

                        // If either side is empty, skip this feature.
                        if (trueCount == 0 || falseCount == 0) {
                            prevx = x[i][j];
                            trueSum += samples[i] * y[i];
                            trueCount += samples[i];
                            continue;
                        }

                        // compute penalized means
                        double trueMean = trueSum / trueCount;
                        double falseMean = (sum - trueSum) / falseCount;

                        // The gain is actually -(reduction in squared error) for
                        // sorting in priority queue, which treats smaller number with
                        // higher priority.
                        double gain = (trueCount * trueMean * trueMean + falseCount * falseMean * falseMean) - n * split.output * split.output;
                        if (gain > split.splitScore) {
                            // new best split
                            split.splitFeature = j;
                            split.splitValue = (x[i][j] + prevx) / 2;
                            split.splitScore = gain;
                            split.trueChildOutput = trueMean;
                            split.falseChildOutput = falseMean;
                        }

                        prevx = x[i][j];
                        trueSum += samples[i] * y[i];
                        trueCount += samples[i];
                    }
                }
            } else {
                throw new IllegalStateException("Unsupported attribute type: " + attributes[j].type);
            }

            return split;
        }
    
        /**
         * Split the node into two children nodes. Returns true if split success.
         */
        public boolean split(PriorityQueue<TrainNode> nextSplits) {
            if (node.splitFeature < 0) {
                throw new IllegalStateException("Split a node with invalid feature.");
            }

            int n = x.length;
            int tc = 0;
            int fc = 0;
            int[] trueSamples = new int[n];
            int[] falseSamples = new int[n];

            if (attributes[node.splitFeature].type == DecisionVariable.Type.Discrete) {
                for (int i = 0; i < n; i++) {
                    if (samples[i] > 0) {
                        if (x[i][node.splitFeature] == node.splitValue) {
                            trueSamples[i] = samples[i];
                            tc += samples[i];
                        } else {
                            falseSamples[i] = samples[i];                            
                            fc += samples[i];
                        }
                    }
                }
            } else if (attributes[node.splitFeature].type == DecisionVariable.Type.Continuous) {
                for (int i = 0; i < n; i++) {
                    if (samples[i] > 0) {
                        if (x[i][node.splitFeature] <= node.splitValue) {
                            trueSamples[i] = samples[i];
                            tc += samples[i];
                        } else {
                            falseSamples[i] = samples[i];                            
                            fc += samples[i];
                        }
                    }
                }
            } else {
                throw new IllegalStateException("Unsupported attribute type: " + attributes[node.splitFeature].type);
            }
            
            if (tc == 0 || fc == 0) {
                node.splitFeature = -1;
                node.splitValue = Double.NaN;
                node.splitScore = 0.0;
                return false;
            }
            
            node.trueChild = new Node(node.trueChildOutput);
            node.falseChild = new Node(node.falseChildOutput);
            
            trueChild = new TrainNode(node.trueChild, x, y, trueSamples);
            if (tc > S && trueChild.findBestSplit()) {
                if (nextSplits != null) {
                    nextSplits.add(trueChild);
                } else {
                    trueChild.split(null);
                }
            }

            falseChild = new TrainNode(node.falseChild, x, y, falseSamples);
            if (fc > S && falseChild.findBestSplit()) {
                if (nextSplits != null) {
                    nextSplits.add(falseChild);
                } else {
                    falseChild.split(null);
                }
            }
            
            importance[node.splitFeature] += node.splitScore;
            
            return true;
        }
    }
    
    /**
     * Regression tree training node for sparse binary features.
     */
    class SparseBinaryTrainNode implements Comparable<SparseBinaryTrainNode> {

        /**
         * The associated regression tree node.
         */
        Node node;
        /**
         * Child node that passes the test.
         */
        SparseBinaryTrainNode trueChild;
        /**
         * Child node that fails the test.
         */
        SparseBinaryTrainNode falseChild;
        /**
         * Training dataset.
         */
        int[][] x;
        /**
         * Training data response value.
         */
        double[] y;
        /**
         * The samples for training this node. Note that samples[i] is the
         * number of sampling of dataset[i]. 0 means that the datum is not
         * included and values of greater than 1 are possible because of
         * sampling with replacement.
         */
        int[] samples;

        /**
         * Constructor.
         */
        public SparseBinaryTrainNode(Node node, int[][] x, double[] y, int[] samples) {
            this.node = node;
            this.x = x;
            this.y = y;
            this.samples = samples;
        }

        @Override
        public int compareTo(SparseBinaryTrainNode a) {
            return (int) Math.signum(a.node.splitScore - node.splitScore);
        }

        /**
         * Finds the best attribute to split on at the current node. Returns
         * true if a split exists to reduce squared error, false otherwise.
         */
        public boolean findBestSplit() {
            if (node.trueChild != null || node.falseChild != null) {
                throw new IllegalStateException("Split non-leaf node.");
            }

            int p = numFeatures;
            double[] trueSum = new double[p];
            int[] trueCount = new int[p];
            int[] featureIndex = new int[p];

            int n = com.tdlibs.catalano.Math.Tools.Sum(samples);
            double sumX = 0.0;
            for (int i = 0; i < x.length; i++) {
                if (samples[i] == 0) {
                    continue;
                }

                double target = samples[i] * y[i];
                sumX += y[i];

                // For each true feature of this datum increment the
                // sufficient statistics for the "true" branch to evaluate
                // splitting on this feature.
                for (int j = 0; j < x[i].length; ++j) {
                    int index = x[i][j];
                    trueSum[index] += target;
                    trueCount[index] += samples[i];
                    featureIndex[index] = j;
                }
            }

            // Loop through features and compute the reduction
            // of squared error, which is trueCount * trueMean^2 + falseCount * falseMean^2 - count * parentMean^2

            // Initialize the information in the leaf
            node.splitScore = 0.0;
            node.splitFeature = -1;
            node.splitValue = -1;

            for (int i = 0; i < p; ++i) {
                double tc = (double) trueCount[i];
                double fc = n - tc;

                // If either side would have fewer than 2 data, skip this feature.
                if ((tc < 2) || (fc < 2)) {
                    continue;
                }

                // compute penalized means
                double trueMean = trueSum[i] / tc;
                double falseMean = (sumX - trueSum[i]) / fc;

                double gain = (tc * trueMean * trueMean + fc * falseMean * falseMean) - n * node.output * node.output;
                if (gain > node.splitScore) {
                    // new best split
                    node.splitFeature = featureIndex[i];
                    node.splitValue = i;
                    node.splitScore = gain;
                    node.trueChildOutput = trueMean;
                    node.falseChildOutput = falseMean;
                }
            }

            return (node.splitFeature != -1);
        }

        /**
         * Split the node into two children nodes.
         */
        public void split(PriorityQueue<SparseBinaryTrainNode> nextSplits) {
            if (node.splitFeature < 0) {
                throw new IllegalStateException("Split a node with invalid feature.");
            }

            if (node.trueChild != null || node.falseChild != null) {
                throw new IllegalStateException("Split non-leaf node.");
            }

            int n = x.length;
            int tc = 0;
            int fc = 0;
            int[] trueSamples = new int[n];
            int[] falseSamples = new int[n];

            for (int i = 0; i < n; i++) {
                if (samples[i] > 0) {
                    if (x[i][node.splitFeature] == (int) node.splitValue) {
                        trueSamples[i] = samples[i];
                        tc += samples[i];
                    } else {
                        falseSamples[i] = samples[i];
                        fc += samples[i];
                    }
                }
            }

            node.trueChild = new Node(node.trueChildOutput);
            node.falseChild = new Node(node.falseChildOutput);

            trueChild = new SparseBinaryTrainNode(node.trueChild, x, y, trueSamples);
            if (tc > S && trueChild.findBestSplit()) {
                if (nextSplits != null) {
                    nextSplits.add(trueChild);
                } else {
                    trueChild.split(null);
                }
            }

            falseChild = new SparseBinaryTrainNode(node.falseChild, x, y, falseSamples);
            if (fc > S && falseChild.findBestSplit()) {
                if (nextSplits != null) {
                    nextSplits.add(falseChild);
                } else {
                    falseChild.split(null);
                }
            }
            
            importance[node.splitFeature] += node.splitScore;
            
        }
        
        /**
         * Calculate the node output for leaves.
         * @param output the output calculate functor.
         */
        public void calculateOutput(NodeOutput output) {
            if (node.trueChild == null && node.falseChild == null) {
                node.output = output.calculate(samples);
            } else {
                if (trueChild != null) {
                    trueChild.calculateOutput(output);
                }
                if (falseChild != null) {
                    falseChild.calculateOutput(output);
                }
            }
        }
    }
    
    /**
     * Initialize a new instance of the RegressionTree class.
     */
    public RegressionTree(){
        this(6);
    }
    
    /**
     * Initialize a new instance of the RegressionTree class.
     * @param attributes Attributes.
     */
    public RegressionTree(DecisionVariable[] attributes){
        this(attributes, 6);
    }
    
    /**
     * Initialize a new instance of the RegressionTree class.
     * @param J the maximum number of leaf nodes in the tree.
     */
    public RegressionTree(int J) {
        this(null, J);
    }
    
    /**
     * Initialize a new instance of the RegressionTree class.
     * @param attributes the attribute properties.
     * @param J the maximum number of leaf nodes in the tree.
     */
    public RegressionTree(DecisionVariable[] attributes, int J) {
        this(attributes, J, null, null, null);
    }
    
    private void BuildModel(DecisionVariable[] attributes, double[][] x, double[] y, int J, int[][] order, int[] samples, NodeOutput output){
        
        if (x.length != y.length) {
            throw new IllegalArgumentException(String.format("The sizes of X and Y don't match: %d != %d", x.length, y.length));
        }

        if (J < 2) {
            throw new IllegalArgumentException("Invalid maximum leaves: " + J);
        }

        if (attributes == null) {
            int p = x[0].length;
            attributes = new DecisionVariable[p];
            for (int i = 0; i < p; i++) {
                attributes[i] = new DecisionVariable("F" + i);
            }
        }
                
        this.attributes = attributes;
        this.J = J;
        this.M = attributes.length;
        importance = new double[attributes.length];
        
        if (order != null) {
            this.order = order;
        } else {
            int n = x.length;
            int p = x[0].length;

            double[] a = new double[n];
            this.order = new int[p][];

            for (int j = 0; j < p; j++) {
                for (int i = 0; i < n; i++) {
                    a[i] = x[i][j];
                }
                this.order[j] = ArraysUtil.Argsort(a, true);
            }
        }

        // Priority queue for best-first tree growing.
        PriorityQueue<TrainNode> nextSplits = new PriorityQueue<TrainNode>();

        int n = 0;
        double sum = 0.0;
        if (samples == null) {
            n = y.length;
            samples = new int[n];
            for (int i = 0; i < n; i++) {
                samples[i] = 1;
                sum += y[i];
            }
        } else {
            for (int i = 0; i < y.length; i++) {
                n += samples[i];
                sum += samples[i] * y[i];
            }
        }
        
        root = new Node(sum / n);
        
        TrainNode trainRoot = new TrainNode(root, x, y, samples);
        // Now add splits to the tree until max tree size is reached
        if (trainRoot.findBestSplit()) {
            nextSplits.add(trainRoot);
        }

        // Pop best leaf from priority queue, split it, and push
        // children nodes into the queue if possible.
        for (int leaves = 1; leaves < this.J; leaves++) {
            // parent is the leaf to split
            TrainNode node = nextSplits.poll();
            if (node == null) {
                break;
            }

            node.split(nextSplits); // Split the parent node into two children nodes
        }
        
        if (output != null) {
            trainRoot.calculateOutput(output);
        }
    }
    
    /**
     * Constructor. Learns a regression tree for gradient tree boosting.
     * @param attributes the attribute properties.
     * @param x the training instances. 
     * @param y the response variable.
     * @param order  the index of training values in ascending order. Note
     * that only numeric attributes need be sorted.
     * @param J the maximum number of leaf nodes in the tree.
     * @param samples the sample set of instances for stochastic learning.
     * samples[i] should be 0 or 1 to indicate if the instance is used for training.
     */
    public RegressionTree(DecisionVariable[] attributes, int J, int[][] order, int[] samples, NodeOutput output) {
        this.attributes = attributes;
        this.J = J;
        this.order = order;
        this.samples = samples;
        this.nodeOutput = output;
    }
    
    /**
     * Constructor. Learns a regression tree for random forest.
     *
     * @param attributes the attribute properties.
     * @param x the training instances. 
     * @param y the response variable.
     * @param order the index of training values in ascending order. Note
     * that only numeric attributes need be sorted.
     * @param M the number of input variables to pick to split on at each
     * node. It seems that dim/3 give generally good performance, where dim
     * is the number of variables.
     * @param S number of instances in a node below which the tree will
     * not split, setting S = 5 generally gives good results.
     * @param samples the sample set of instances for stochastic learning.
     * samples[i] is the number of sampling for instance i.
     */
    public RegressionTree(DecisionVariable[] attributes, double[][] x, double[] y, int M, int S, int[][] order, int[] samples) {
        if (x.length != y.length) {
            throw new IllegalArgumentException(String.format("The sizes of X and Y don't match: %d != %d", x.length, y.length));
        }

        if (M <= 0 || M > x[0].length) {
            throw new IllegalArgumentException("Invalid number of variables to split on at a node of the tree: " + M);
        }
        
        if (S <= 0) {
            throw new IllegalArgumentException("Invalid mimum number of instances in leaf nodes: " + S);
        }
        
        if (samples == null) {
            throw new IllegalArgumentException("Sampling array is null.");
        }
        
        if (attributes == null) {
            int p = x[0].length;
            attributes = new DecisionVariable[p];
            for (int i = 0; i < p; i++) {
                attributes[i] = new DecisionVariable("F" + i);
            }
        }
                
        this.attributes = attributes;
        this.J = Integer.MAX_VALUE;
        this.M = M;
        this.S = S;
        this.order = order;
        importance = new double[attributes.length];
        
        int n = 0;
        double sum = 0.0;
        for (int i = 0; i < y.length; i++) {
            n += samples[i];
            sum += samples[i] * y[i];
        }
        
        root = new Node(sum / n);
        
        TrainNode trainRoot = new TrainNode(root, x, y, samples);
        if (trainRoot.findBestSplit()) {
            trainRoot.split(null);
        }
    }
    
    /**
     * Constructor. Learns a regression tree on sparse binary samples.
     * @param numFeatures the number of sparse binary features.
     * @param x the training instances of sparse binary features. 
     * @param y the response variable.
     * @param J the maximum number of leaf nodes in the tree.
     */
    public RegressionTree(int numFeatures, int[][] x, double[] y, int J) {
        this(numFeatures, x, y, J, null, null);        
    }
    
    /**
     * Constructor. Learns a regression tree on sparse binary samples.
     * @param numFeatures the number of sparse binary features.
     * @param x the training instances. 
     * @param y the response variable.
     * @param J the maximum number of leaf nodes in the tree.
     * @param samples the sample set of instances for stochastic learning.
     * samples[i] should be 0 or 1 to indicate if the instance is used for training.
     */
    public RegressionTree(int numFeatures, int[][] x, double[] y, int J, int[] samples, NodeOutput output) {
        if (x.length != y.length) {
            throw new IllegalArgumentException(String.format("The sizes of X and Y don't match: %d != %d", x.length, y.length));
        }

        if (J < 2) {
            throw new IllegalArgumentException("Invalid maximum leaves: " + J);
        }

        this.J = J;
        this.numFeatures = numFeatures;
        importance = new double[numFeatures];
        
        // Priority queue for best-first tree growing.
        PriorityQueue<SparseBinaryTrainNode> nextSplits = new PriorityQueue<SparseBinaryTrainNode>();

        int n = 0;
        double sum = 0.0;
        if (samples == null) {
            n = y.length;
            samples = new int[n];
            for (int i = 0; i < n; i++) {
                samples[i] = 1;
                sum += y[i];
            }
        } else {
            for (int i = 0; i < y.length; i++) {
                n += samples[i];
                sum += samples[i] * y[i];
            }
        }
        
        root = new Node(sum / n);
        
        SparseBinaryTrainNode trainRoot = new SparseBinaryTrainNode(root, x, y, samples);
        // Now add splits to the tree until max tree size is reached
        if (trainRoot.findBestSplit()) {
            nextSplits.add(trainRoot);
        }

        // Pop best leaf from priority queue, split it, and push
        // children nodes into the queue if possible.
        for (int leaves = 1; leaves < this.J; leaves++) {
            // parent is the leaf to split
            SparseBinaryTrainNode node = nextSplits.poll();
            if (node == null) {
                break;
            }

            node.split(nextSplits); // Split the parent node into two children nodes
        }
        
        if (output != null) {
            trainRoot.calculateOutput(output);
        }
    }
    
    @Override
    public void Learn(DatasetRegression dataset) {
        Learn(dataset.getInput(), dataset.getOutput());
    }
    
    @Override
    public void Learn(double[][] input, double[] output){
        BuildModel(attributes, input, output, J, order, samples, nodeOutput);
    }

    @Override
    public double Predict(double[] feature) {
        return root.predict(feature);
    }
    
    public double Predict(int[] feature){
        return root.predict(feature);
    }
    
    @Override
    public IRegression clone() {
        try {
            return (IRegression)super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new IllegalArgumentException("Clone not supported: " + ex.getMessage());
        }
    }
}