package org.sparksamples.regression.bikesharing

import org.apache.log4j.Logger
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.{VectorAssembler, VectorIndexer}
import org.apache.spark.ml.regression.{DecisionTreeRegressionModel, DecisionTreeRegressor}
import org.apache.spark.sql.{SparkSession, _}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.VectorIndexer
import org.apache.spark.ml.regression.{RandomForestRegressionModel, RandomForestRegressor}

/**
  * Created by manpreet.singh on 22/11/16.
  */
object RandomForestRegressionPipeline {

  @transient lazy val logger = Logger.getLogger(getClass.getName)

  def randForestRegressionWithVectorFormat(vectorAssembler: VectorAssembler, vectorIndexer: VectorIndexer, dataFrame: DataFrame) = {
    val lr = new RandomForestRegressor()
      .setFeaturesCol("features")
      .setLabelCol("label")

    val pipeline = new Pipeline().setStages(Array(vectorAssembler, vectorIndexer, lr))

    val Array(training, test) = dataFrame.randomSplit(Array(0.8, 0.2), seed = 12345)

    val model = pipeline.fit(training)

    // Make predictions.
    val predictions = model.transform(test)

    // Select example rows to display.
    predictions.select("prediction", "label", "features").show(5)

    // Select (prediction, true label) and compute test error.
    val evaluator = new RegressionEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")
      .setMetricName("rmse")
    val rmse = evaluator.evaluate(predictions)
    println("Root Mean Squared Error (RMSE) on test data = " + rmse)

    val treeModel = model.stages(1).asInstanceOf[RandomForestRegressionModel]
    println("Learned regression tree model:\n" + treeModel.toDebugString)  }

  def randForestRegressionWithSVMFormat(spark: SparkSession) = {
    // Load training data
    val training = spark.read.format("libsvm")
      .load("/Users/apple/Idea/workspace/Machine-Learning-with-Spark-Second-Edition/spark-ml/Chapter_07/scala/2.0.0/scala-spark-app/src/main/scala/org/sparksamples/regression/dataset/BikeSharing/lsvmHours.txt")

    // Automatically identify categorical features, and index them.
    // Set maxCategories so features with > 4 distinct values are treated as continuous.
    val featureIndexer = new VectorIndexer()
      .setInputCol("features")
      .setOutputCol("indexedFeatures")
      .setMaxCategories(4)
      .fit(training)

    // Split the data into training and test sets (30% held out for testing).
    val Array(trainingData, testData) = training.randomSplit(Array(0.7, 0.3))

    // Train a RandomForest model.
    val rf = new RandomForestRegressor()
      .setLabelCol("label")
      .setFeaturesCol("indexedFeatures")

    // Chain indexer and forest in a Pipeline.
    val pipeline = new Pipeline()
      .setStages(Array(featureIndexer, rf))

    // Train model. This also runs the indexer.
    val model = pipeline.fit(trainingData)

    // Make predictions.
    val predictions = model.transform(testData)

    // Select example rows to display.
    predictions.select("prediction", "label", "features").show(5)

    // Select (prediction, true label) and compute test error.
    val evaluator = new RegressionEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")
      .setMetricName("rmse")
    val rmse = evaluator.evaluate(predictions)
    println("Root Mean Squared Error (RMSE) on test data = " + rmse)

    val rfModel = model.stages(1).asInstanceOf[RandomForestRegressionModel]
    println("Learned regression forest model:\n" + rfModel.toDebugString)
  }


}
