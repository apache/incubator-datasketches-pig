/*
 * Copyright 2017, Yahoo! Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.sketches.pig.tuple;

import org.testng.annotations.Test;
import org.testng.Assert;
import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataByteArray;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;

import com.yahoo.sketches.tuple.ArrayOfDoublesUpdatableSketch;
import com.yahoo.sketches.tuple.ArrayOfDoublesUpdatableSketchBuilder;

import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.stat.StatUtils;

import java.util.Map;
import java.util.Random;

/**
 * Test p-value estimation of two ArrayOfDoublesSketch.
 */
public class ArrayOfDoublesSketchesToPValueEstimatesTest {
    /**
     * Check null input to UDF.
     */
    @Test
    public void nullInput() throws Exception {
        EvalFunc<Tuple> func = new ArrayOfDoublesSketchesToPValueEstimates();

        Tuple resultTuple = func.exec(null);

        Assert.assertNull(resultTuple);
    }

    /**
     * Check input of empty tuple.
     */
    @Test
    public void emptyInput() throws Exception {
        EvalFunc<Tuple> func = new ArrayOfDoublesSketchesToPValueEstimates();

        Tuple resultTuple = func.exec(TupleFactory.getInstance().newTuple());

        Assert.assertNull(resultTuple);
    }

    /**
     * Check input of single empty sketch.
     */
    @Test
    public void oneEmptySketch() throws Exception {
        EvalFunc<Tuple> func = new ArrayOfDoublesSketchesToPValueEstimates();

        ArrayOfDoublesUpdatableSketch sketch = new ArrayOfDoublesUpdatableSketchBuilder().build();

        Tuple inputTuple = PigUtil.objectsToTuple(new DataByteArray(sketch.compact().toByteArray()));

        Tuple resultTuple = func.exec(inputTuple);

        Assert.assertNull(resultTuple);
    }

    /**
     * Check input of two empty sketches.
     */
    @Test
    public void twoEmptySketches() throws Exception {
        EvalFunc<Tuple> func = new ArrayOfDoublesSketchesToPValueEstimates();

        ArrayOfDoublesUpdatableSketch sketchA = new ArrayOfDoublesUpdatableSketchBuilder().build();
        ArrayOfDoublesUpdatableSketch sketchB = new ArrayOfDoublesUpdatableSketchBuilder().build();

        Tuple inputTuple = PigUtil.objectsToTuple(new DataByteArray(sketchA.compact().toByteArray()),
                                                  new DataByteArray(sketchB.compact().toByteArray()));

        Tuple resultTuple = func.exec(inputTuple);

        Assert.assertNull(resultTuple);
    }

    /**
     * Check p-value for the smoker data set. Single metric.
     */
    @Test
    public void smokerDatasetSingleMetric() throws Exception {
        EvalFunc<Tuple> func = new ArrayOfDoublesSketchesToPValueEstimates();

        // Create the two sketches
        ArrayOfDoublesUpdatableSketch sketchA = new ArrayOfDoublesUpdatableSketchBuilder()
                                                    .setNumberOfValues(1)
                                                    .setNominalEntries(1)
                                                    .build();
        ArrayOfDoublesUpdatableSketch sketchB = new ArrayOfDoublesUpdatableSketchBuilder()
                                                    .setNumberOfValues(1)
                                                    .setNominalEntries(1)
                                                    .build();

        // Sample dataset (smoker/non-smoker brain size)
        double[] groupA = {7.3, 6.5, 5.2, 6.3, 7.0, 5.9, 5.2, 5.0, 4.7, 5.7, 5.7, 3.3, 5.0, 4.6, 4.8, 3.8, 4.6};
        double[] groupB = {4.2, 4.0, 2.6, 4.9, 4.4, 4.4, 5.5, 5.1, 5.1, 3.2, 3.9, 3.2, 4.9, 4.3, 4.8, 2.4, 5.5, 5.5, 3.7};

        // Add values to A sketch
        for (int i = 0; i < groupA.length; i++) {
            sketchA.update(i, new double[] {groupA[i]});
        }

        // Add values to B sketch
        for (int i = 0; i < groupB.length; i++) {
            sketchB.update(i, new double[] {groupB[i]});
        }

        // Convert to a tuple and execute the UDF
        Tuple inputTuple = PigUtil.objectsToTuple(new DataByteArray(sketchA.compact().toByteArray()),
                                                  new DataByteArray(sketchB.compact().toByteArray()));
        Tuple resultTuple = func.exec(inputTuple);

        // Should get 1 p-value back
        Assert.assertNotNull(resultTuple);
        Assert.assertEquals(resultTuple.size(), 1);

        // Check p-value values, with a delta
        Assert.assertEquals((double) ((Map) resultTuple.get(0)).get(ArrayOfDoublesSketchesToPValueEstimates.P_VALUE_KEY), 0.0043, 0.0001);
        // Check mean delta
        Assert.assertEquals((double) ((Map) resultTuple.get(0)).get(ArrayOfDoublesSketchesToPValueEstimates.DELTA_KEY), -0.19, 0.05);
    }

    /**
     * Check p-value for a large data set.
     */
    @Test
    public void largeDataSet() throws Exception {
        EvalFunc<Tuple> func = new ArrayOfDoublesSketchesToPValueEstimates();

        // Create the two sketches
        ArrayOfDoublesUpdatableSketch sketchA = new ArrayOfDoublesUpdatableSketchBuilder()
                                                    .setNumberOfValues(1)
                                                    .setNominalEntries(16000)
                                                    .build();
        ArrayOfDoublesUpdatableSketch sketchB = new ArrayOfDoublesUpdatableSketchBuilder()
                                                    .setNumberOfValues(1)
                                                    .setNominalEntries(16000)
                                                    .build();

        // Number of values to use.
        int n = 100000;
        int bShift = 1000;
        double[] a = new double[n];
        double[] b = new double[n];

        // Random number generator
        Random rand = new Random(41L);

        // Add values to A sketch
        for (int i = 0; i < n; i++) {
            double val = rand.nextGaussian();
            sketchA.update(i, new double[] {val});
            a[i] = val;
        }

        // Add values to B sketch
        for (int i = 0; i < n; i++) {
            double val = rand.nextGaussian() + bShift;
            sketchB.update(i, new double[] {val});
            b[i] = val;
        }

        TTest tTest = new TTest();
        double expectedPValue = tTest.tTest(a, b);

        // Convert to a tuple and execute the UDF
        Tuple inputTuple = PigUtil.objectsToTuple(new DataByteArray(sketchA.compact().toByteArray()),
                                                  new DataByteArray(sketchB.compact().toByteArray()));
        Tuple resultTuple = func.exec(inputTuple);

        // Should get 1 p-value back
        Assert.assertNotNull(resultTuple);
        Assert.assertEquals(resultTuple.size(), 1);

        // Check p-value values, with a delta
        Assert.assertEquals((double) ((Map) resultTuple.get(0)).get(ArrayOfDoublesSketchesToPValueEstimates.P_VALUE_KEY), expectedPValue, 0.01);
        // Check mean delta
        Assert.assertEquals((double) ((Map) resultTuple.get(0)).get(ArrayOfDoublesSketchesToPValueEstimates.DELTA_KEY), 76892, 5000);

    }

    /**
     * Check p-value for two metrics at the same time.
     */
    @Test
    public void twoMetrics() throws Exception {
        EvalFunc<Tuple> func = new ArrayOfDoublesSketchesToPValueEstimates();

        // Create the two sketches
        ArrayOfDoublesUpdatableSketch sketchA = new ArrayOfDoublesUpdatableSketchBuilder()
                                                    .setNumberOfValues(2)
                                                    .setNominalEntries(128)
                                                    .build();
        ArrayOfDoublesUpdatableSketch sketchB = new ArrayOfDoublesUpdatableSketchBuilder()
                                                    .setNumberOfValues(2)
                                                    .setNominalEntries(128)
                                                    .build();

        // Sample dataset (smoker/non-smoker brain size)
        double[] groupA = {7.3, 6.5, 5.2, 6.3, 7.0, 5.9, 5.2, 5.0, 4.7, 5.7, 5.7, 3.3, 5.0, 4.6, 4.8, 3.8, 4.6};
        double[] groupB = {4.2, 4.0, 2.6, 4.9, 4.4, 4.4, 5.5, 5.1, 5.1, 3.2, 3.9, 3.2, 4.9, 4.3, 4.8, 2.4, 5.5, 5.5, 3.7};

        // Add values to A sketch
        for (int i = 0; i < groupA.length; i++) {
            sketchA.update(i, new double[] {groupA[i], i});
        }

        // Add values to B sketch
        for (int i = 0; i < groupB.length; i++) {
            sketchB.update(i, new double[] {groupB[i], i});
        }

        // Convert to a tuple and execute the UDF
        Tuple inputTuple = PigUtil.objectsToTuple(new DataByteArray(sketchA.compact().toByteArray()),
                                                  new DataByteArray(sketchB.compact().toByteArray()));
        Tuple resultTuple = func.exec(inputTuple);

        // Should get 2 p-values back
        Assert.assertNotNull(resultTuple);
        Assert.assertEquals(resultTuple.size(), 2);

        // Check expected p-value values, and mean deltas
        Assert.assertEquals((double) ((Map) resultTuple.get(0)).get(ArrayOfDoublesSketchesToPValueEstimates.P_VALUE_KEY), 0.0043, 0.0001);
        Assert.assertEquals((double) ((Map) resultTuple.get(0)).get(ArrayOfDoublesSketchesToPValueEstimates.DELTA_KEY), -0.19, 0.05);

        // Check expected p-value values, and mean deltas
        Assert.assertEquals((double) ((Map) resultTuple.get(1)).get(ArrayOfDoublesSketchesToPValueEstimates.P_VALUE_KEY), 0.58, 0.01);
        Assert.assertEquals((double) ((Map) resultTuple.get(1)).get(ArrayOfDoublesSketchesToPValueEstimates.DELTA_KEY), 0.125, 0.01);

    }

    /**
     * Check with sketch having only one input.
     */
    @Test
    public void sketchWithSingleValue() throws Exception {
        EvalFunc<Tuple> func = new ArrayOfDoublesSketchesToPValueEstimates();

        // Create the two sketches
        ArrayOfDoublesUpdatableSketch sketchA = new ArrayOfDoublesUpdatableSketchBuilder()
                                                    .setNumberOfValues(1)
                                                    .setNominalEntries(128)
                                                    .build();
        ArrayOfDoublesUpdatableSketch sketchB = new ArrayOfDoublesUpdatableSketchBuilder()
                                                    .setNumberOfValues(1)
                                                    .setNominalEntries(128)
                                                    .build();

        // Sample dataset
        double[] groupA = {7.3, 6.5, 5.2, 6.3, 7.0, 5.9, 5.2, 5.0, 4.7, 5.7, 5.7, 3.3, 5.0, 4.6, 4.8, 3.8, 4.6};
        double[] groupB = {5.0};

        // Add values to A sketch
        for (int i = 0; i < groupA.length; i++) {
            sketchA.update(i, new double[] {groupA[i]});
        }

        // Add values to B sketch
        for (int i = 0; i < groupB.length; i++) {
            sketchB.update(i, new double[] {groupB[i]});
        }

        // Convert to a tuple and execute the UDF
        Tuple inputTuple = PigUtil.objectsToTuple(new DataByteArray(sketchA.compact().toByteArray()),
                                                  new DataByteArray(sketchB.compact().toByteArray()));
        Tuple resultTuple = func.exec(inputTuple);

        // Should get null back, as one of the sketches had fewer than 2 items
        Assert.assertNull(resultTuple);
    }
}