package org.dotdi.DLSparkUtils.examples.classification

import org.datavec.api.records.reader.impl.csv.CSVRecordReader
import org.datavec.api.split.FileSplit
import java.io.File
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.Updater
import org.deeplearning4j.nn.conf.layers.DenseLayer
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.nn.conf.layers.OutputLayer
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.deeplearning4j.eval.Evaluation
import scala.collection.mutable.ArrayBuffer
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction
import org.nd4j.linalg.factory.Nd4j

object MLPClassifierLinear {

  def main(args: Array[String]): Unit = {
    val seed = 123;
    val learningRate = 0.01;
    val batchSize = 50;
    val nEpochs = 30;

    val numInputs = 2;
    val numOutputs = 2;
    val numHiddenNodes = 20;

    //Load the training data:
    val rr = new CSVRecordReader();
    rr.initialize(new FileSplit(new File("src/main/resources/classification/linear_data_train.csv")));
    var trainIter = new RecordReaderDataSetIterator(rr, batchSize, 0, 2);

    //Load the test/evaluation data:
    val rrTest = new CSVRecordReader();
    rrTest.initialize(new FileSplit(new File("src/main/resources/classification/linear_data_eval.csv")));
    var testIter = new RecordReaderDataSetIterator(rrTest, batchSize, 0, 2);

    val conf = new NeuralNetConfiguration.Builder()
      .seed(seed)
      .iterations(1)
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .learningRate(learningRate)
      .updater(Updater.NESTEROVS).momentum(0.9)
      .list()
      .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
        .weightInit(WeightInit.XAVIER)
        .activation("relu")
        .build())
      .layer(1, new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD)
        .weightInit(WeightInit.XAVIER)
        .activation("softmax").weightInit(WeightInit.XAVIER)
        .nIn(numHiddenNodes).nOut(numOutputs).build())
      .pretrain(false).backprop(true).build();

    val model = new MultiLayerNetwork(conf);
    model.init();
    model.setListeners(new ScoreIterationListener(10)); //Print score every 10 parameter updates

    for (n <- 0 until nEpochs) {
      model.fit(trainIter)
    }

    System.out.println("Evaluate model....");
    val eval = new Evaluation(numOutputs);
    while (testIter.hasNext()) {
      val t = testIter.next();
      val features = t.getFeatureMatrix();
      val lables = t.getLabels();
      val predicted = model.output(features, false);

      eval.eval(lables, predicted);

    }

    //Print the evaluation statistics
    System.out.println(eval.stats());

    //------------------------------------------------------------------------------------
    //Training is complete. Code that follows is for plotting the data & predictions only

    //Plot the data:
    val xMin: Double = 0;
    val xMax: Double = 1.0;
    val yMin: Double = -0.2;
    val yMax: Double = 0.8;

    //Let's evaluate the predictions at every point in the x/y input space
    val nPointsPerAxis = 100;
    val evalPoints = ArrayBuffer.fill[Double](nPointsPerAxis * nPointsPerAxis, 2)(0.0);
    var count = 0;
    for (i <- 0 until nPointsPerAxis) {
      for (j <- 0 until nPointsPerAxis) {
        val x: Double = i * (xMax - xMin) / (nPointsPerAxis - 1) + xMin;
        val y: Double = j * (yMax - yMin) / (nPointsPerAxis - 1) + yMin;

        evalPoints(count)(0) = x;
        evalPoints(count)(1) = y;

        count += 1;
      }
    }

    val allXYPoints = Nd4j.create(evalPoints.map(s => s.toArray).toArray);
    val predictionsAtXYPoints = model.output(allXYPoints);

    //Get all of the training data in a single array, and plot it:
    rr.initialize(new FileSplit(new File("src/main/resources/classification/linear_data_train.csv")));
    rr.reset();
    val nTrainPoints = 1000;
    trainIter = new RecordReaderDataSetIterator(rr, nTrainPoints, 0, 2);
    var ds = trainIter.next();
    PlotUtil.plotTrainingData(ds.getFeatures(), ds.getLabels(), allXYPoints, predictionsAtXYPoints, nPointsPerAxis);

    //Get test data, run the test data through the network to generate predictions, and plot those predictions:
    rrTest.initialize(new FileSplit(new File("src/main/resources/classification/linear_data_eval.csv")));
    rrTest.reset();
    val nTestPoints = 500;
    testIter = new RecordReaderDataSetIterator(rrTest, nTestPoints, 0, 2);
    ds = testIter.next();
    val testPredicted = model.output(ds.getFeatures());
    PlotUtil.plotTestData(ds.getFeatures(), ds.getLabels(), testPredicted, allXYPoints, predictionsAtXYPoints, nPointsPerAxis);

    System.out.println("****************Example finished********************");

  }

}