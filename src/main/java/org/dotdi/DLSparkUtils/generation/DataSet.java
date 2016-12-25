package org.dotdi.DLSparkUtils.generation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DataSet {

    // hold the centers
    private List<double[]> centers;

    // hold the samples
    private List<double[]> samples;

    // hold the labels
    private List<Double> labels;

    public DataSet(List<double[]> centers, List<double[]> samples, List<Double> labels) {
        this.centers = centers;
        this.samples = samples;
        this.labels = labels;
    }

    /**
     * Get the dimension of the data. Based on centers no. 0, length
     */
    public int getDim() {
        return centers.get(0).length;
    }

    /**
     * Get no. samples
     */
    public int getNumSamples() {
        return samples.size();
    }

    /**
     * Get the samples
     */
    public List<double[]> getSamples() {
        return samples;
    }

    /**
     * Get the centers
     */
    public List<double[]> getCenters() {
        return centers;
    }

    /**
     * get the labels as list
     */
    public List<Double> getLabels() {
        return labels;
    }

    public static int LIMIT_NUMBER_SAMPLES = 200;
    public static int LIMIT_NUMBER_COORDS = 200;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        int limit = getNumSamples() <= LIMIT_NUMBER_SAMPLES ? getNumSamples() : LIMIT_NUMBER_SAMPLES;
        for (int i = 0; i < limit; i++) {
            sb.append(showSample(labels.get(i), samples.get(i), LIMIT_NUMBER_COORDS));
            if (i < limit - 1)
                sb.append("\n");
        }
        return sb.toString();
    }

    private static String showSample(double label, double[] vec, int limitSamples) {
        StringBuilder sb = new StringBuilder();
        int limit = vec.length < limitSamples ? vec.length : limitSamples;
        sb.append(label).append(",");
        for (int i = 0; i < limit; i++) {
            sb.append(String.format("%.4f", vec[i]));
            if (i < limit - 1)
                sb.append(",");
        }
        return sb.toString();
    }

    public void shuffle(Random random) {
        List<Integer> shuffleIndices = getShuffleIndices(getNumSamples(), random);
        samples = shuffleByIndices(samples, shuffleIndices);
        labels = shuffleByIndices(labels, shuffleIndices);
    }

    /**
     * Doing a copy of a slice of data
     */
    public DataSet sliceAndCopy(int start, int end) {
        List<double[]> slicedSamples = slicePrimitiveAndCopy(samples, start, end);
        List<Double> slicedLabels = sliceAndCopy(labels, start, end);
        return new DataSet(centers, slicedSamples, slicedLabels);
    }

    public int[] getIndices(double[] weights) {
        // Summing the weights
        double sumWeights = 0;
        for (int i = 0; i < weights.length; i++)
            sumWeights += weights[i];
        
        // Normalizing the weights
        double[] weightsNormal = new double[weights.length];
        for (int i = 0; i < weights.length; i++)
            weightsNormal[i] = weights[i] / sumWeights;
        
        // Compute accumulated weights
        double[] weightsAccumulated = new double[weights.length + 1];
        weightsAccumulated[0] = 0;
        weightsAccumulated[weightsAccumulated.length - 1] = 1;
        for (int i = 1; i < weightsAccumulated.length - 1; i++)
            weightsAccumulated[i] = weightsAccumulated[i - 1] + weightsNormal[i - 1];
        
        // Compute the indices
        int[] indices = new int[weightsAccumulated.length];
        indices[0] = 0;
        indices[weightsAccumulated.length - 1] = getNumSamples();
        for (int i = 1; i < weightsAccumulated.length - 1; i++)
            indices[i] = (int) (weightsAccumulated[i] * ((double) getNumSamples()));
        return indices;
    }
    
    public List<DataSet> splitPortionsAndCopy(double[] weights) {
        int[] indices = getIndices(weights);
        // Split the dataset according to the indices.
        List<DataSet> dataSets = new ArrayList<DataSet>(weights.length);
        for (int i = 0; i < weights.length; i++) {
            dataSets.add(sliceAndCopy(indices[i], indices[i + 1]));
        }
        
        // Return
        return dataSets;
    }
    
    public void colorByLabel() {
        for (int i=0; i<labels.size(); i++)
            labels.set(i, (double)i);
    }
    
    public String getSampleAndLabelAsString(int idx) {
        StringBuilder sb = new StringBuilder();
        sb.append(labels.get(idx).intValue()).append(",");
        for (int i=0; i<samples.get(idx).length; i++) {
            sb.append(samples.get(idx)[i]);
            if (i<samples.get(idx).length-1)
                sb.append(",");
        }
        return sb.toString();
    }
    

    /****************************************************
     * Static generation of many data-sets
     ****************************************************/

    /**
     * Generate dataset
     */
    public static DataSet generateData(int noSamplesPerClass, int noClasses, double samplesVariance, int dim,
                    Random random) {
        List<double[]> centers;
        List<double[]> samples;
        List<Double> labels;

        // Generate the centers
        centers = generateRandomGaussianVectors(dim, noClasses, 1.0, random);
        samples = new ArrayList<double[]>(noSamplesPerClass * noClasses);
        labels = new ArrayList<Double>(noSamplesPerClass * noClasses);
        for (int c = 0; c < noClasses; c++) {
            List<double[]> classSamples =
                            generateRandomGaussianVectors(centers.get(c), noSamplesPerClass, samplesVariance, random);
            samples.addAll(classSamples);
            List<Double> tmpLabels = generateConstantVector(c, noSamplesPerClass);
            labels.addAll(tmpLabels);
        }
        return new DataSet(centers, samples, labels);
    }

    /**
     * Create random Gaussian vector
     * 
     * @return
     */
    public static double[] generateRandomGaussianVector(int dim, double std, Random random) {
        double[] vec = new double[dim];
        for (int i = 0; i < dim; i++) {
            vec[i] = random.nextGaussian() * std;
        }
        return vec;
    }

    public static double[] generateRandomGaussianVector(double[] center, double std, Random random) {
        double[] vec = new double[center.length];
        for (int i = 0; i < center.length; i++) {
            vec[i] = center[i] + random.nextGaussian() * std;
        }
        return vec;
    }

    public static List<double[]> generateRandomGaussianVectors(int dim, int num, double std, Random random) {
        List<double[]> vecs = new ArrayList<double[]>(num);
        for (int i = 0; i < num; i++) {
            vecs.add(generateRandomGaussianVector(dim, std, random));
        }
        return vecs;
    }

    public static List<double[]> generateRandomGaussianVectors(double[] center, int numOfSamples, double std,
                    Random random) {
        List<double[]> vecs = new ArrayList<double[]>(numOfSamples);
        for (int i = 0; i < numOfSamples; i++) {
            vecs.add(generateRandomGaussianVector(center, std, random));
        }
        return vecs;
    }

    public static List<Double> generateConstantVector(double value, int dim) {
        List<Double> vec = new ArrayList<Double>(dim);
        for (int i = 0; i < dim; i++)
            vec.add(value);
        return vec;
    }

    public static List<Integer> getShuffleIndices(int limit, Random random) {
        List<Integer> indices = new ArrayList<Integer>();
        for (int i = 0; i < limit; i++)
            indices.add(i);
        Collections.shuffle(indices, random);
        return indices;
    }

    public static <T> List<T> shuffleByIndices(List<T> vec, List<Integer> indices) {
        List<T> newArray = new ArrayList<T>(vec.size());
        for (int i = 0; i < vec.size(); i++) {
            newArray.add(vec.get(indices.get(i)));
        }
        return newArray;
    }

    public static double[] makeCopy(double[] vec) {
        double[] copy = new double[vec.length];
        for (int i = 0; i < vec.length; i++)
            copy[i] = vec[i];
        return copy;
    }

    public static List<double[]> slicePrimitiveAndCopy(List<double[]> original, int start, int end) {
        List<double[]> copy = new ArrayList<double[]>(end - start);
        for (int i = start; i < end; i++) {
            double[] makeCopy = makeCopy(original.get(i));
            copy.add(makeCopy);
        }
        return copy;
    }

    public static <T> List<T> sliceAndCopy(List<T> original, int start, int end) {
        List<T> copy = new ArrayList<T>(end - start);
        for (int i = start; i < end; i++)
            copy.add(original.get(i));
        return copy;
    }
    

}
