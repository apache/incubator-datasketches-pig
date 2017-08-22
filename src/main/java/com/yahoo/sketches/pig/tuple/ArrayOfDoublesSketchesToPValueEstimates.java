/*
 * Copyright 2017, Yahoo! Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.sketches.pig.tuple;

import java.io.IOException;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.inference.TTest;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataByteArray;
import org.apache.pig.data.Tuple;

import com.yahoo.memory.Memory;
import com.yahoo.sketches.tuple.ArrayOfDoublesSketch;
import com.yahoo.sketches.tuple.ArrayOfDoublesSketchIterator;
import com.yahoo.sketches.tuple.ArrayOfDoublesSketches;

/**
 * Calculate p-values given two ArrayOfDoublesSketch. Each value in the sketch
 * is treated as a separate metric measurement, and a p-value will be generated
 * for each metric.
 */
public class ArrayOfDoublesSketchesToPValueEstimates extends EvalFunc<Tuple> {

  @Override
  public Tuple exec(final Tuple input) throws IOException {
    if ((input == null) || (input.size() != 2)) {
      return null;
    }

    // Get the two sketches
    final DataByteArray dbaA = (DataByteArray) input.get(0);
    final DataByteArray dbaB = (DataByteArray) input.get(1);
    final ArrayOfDoublesSketch sketchA = ArrayOfDoublesSketches.wrapSketch(Memory.wrap(dbaA.get()));
    final ArrayOfDoublesSketch sketchB = ArrayOfDoublesSketches.wrapSketch(Memory.wrap(dbaB.get()));

    // Check that the size of the arrays in the sketches are the same
    if (sketchA.getNumValues() != sketchB.getNumValues()) {
      throw new IllegalArgumentException("Both sketches must have the same number of values");
    }

    // Store the number of metrics
    final int numMetrics = sketchA.getNumValues();

    // If the sketches contain fewer than 2 values, the p-value can't be calculated
    if (sketchA.getRetainedEntries() < 2 || sketchB.getRetainedEntries() < 2) {
      return null;
    }

    // Get the statistical summary from each sketch
    final SummaryStatistics[] summaryA = sketchToSummaryStatistics(sketchA, numMetrics);
    final SummaryStatistics[] summaryB = sketchToSummaryStatistics(sketchB, numMetrics);

    // Calculate the p-values
    final double[] pValues = new double[numMetrics];
    final TTest tTest = new TTest();
    for (int i = 0; i < numMetrics; i++) {
      // Pass the sampled values for each metric
      pValues[i] = tTest.tTest(summaryA[i], summaryB[i]);
    }

    return Util.doubleArrayToTuple(pValues);
  }
  
  /**
   * Convert sketch to a summary statistic.
   *
   * @param sketch ArrayOfDoublesSketch to convert to a summary statistic.
   * @param numMetrics Number of metrics (values) in the ArrayOfDoublesSketch.
   * @return A summary statistic.
   */
  private static SummaryStatistics[] sketchToSummaryStatistics(final ArrayOfDoublesSketch sketch,
      final int numMetrics) {
    // Store a summary statistic object for each metric
    final SummaryStatistics[] summaryStatistics = new SummaryStatistics[numMetrics];

    // Init the array
    for (int i = 0; i < numMetrics; i++) {
      summaryStatistics[i] = new SummaryStatistics();
    }

    // Add sketch values to the summary statistic object
    final ArrayOfDoublesSketchIterator it = sketch.iterator();
    while (it.next()) {
      for (int i = 0; i < it.getValues().length; i++) {
        summaryStatistics[i].addValue(it.getValues()[i]);
      }
    }

    return summaryStatistics;
  }
  
}
