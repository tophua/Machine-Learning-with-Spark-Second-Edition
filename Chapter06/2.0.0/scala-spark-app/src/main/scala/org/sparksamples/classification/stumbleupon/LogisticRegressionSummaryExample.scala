package org.sparksamples.classification.stumbleupon

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.ml.classification.{BinaryLogisticRegressionSummary, LogisticRegression}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.max

// set VM Option as -Dspark.master=local[1]
object LogisticRegressionSummaryExample {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      .appName("LogisticRegressionSummaryExample")
      .getOrCreate()
    import spark.implicits._

    // Load training data 加载训练数据
    val training = spark.read.format("libsvm").load("/Users/manpreet.singh/Sandbox/codehub/github/machinelearning/spark-ml/Chapter_06/2.0.0/scala-spark-app/src/main/scala/org/sparksamples/classification/dataset/spark-data/sample_libsvm_data.txt")

    val lr = new LogisticRegression()
      .setMaxIter(10)
      .setRegParam(0.3)
      .setElasticNetParam(0.8)

    // Fit the model
    val lrModel = lr.fit(training)

    // Extract the summary from the returned LogisticRegressionModel instance trained in the earlier
    //从前面训练的返回的LogisticRegressionModel实例中提取摘要
    // example
    val trainingSummary = lrModel.summary

    // Obtain the objective per iteration. 每次迭代获得目标
    val objectiveHistory = trainingSummary.objectiveHistory
    println("objectiveHistory:")
    objectiveHistory.foreach(loss => println(loss))

    // Obtain the metrics useful to judge performance on test data.
    //获取用于判断测试数据性能的指标。
    // We cast the summary to a BinaryLogisticRegressionSummary since the problem is a
    // binary classification problem.
    //由于该问题是一个二元分类问题,因此我们将该总结转换为BinaryLogisticRegressionSummary
    val binarySummary = trainingSummary.asInstanceOf[BinaryLogisticRegressionSummary]

    // Obtain the receiver-operating characteristic as a dataframe and areaUnderROC.
    //获取接收器操作特性作为dataframe和areaUnderROC。
    val roc = binarySummary.roc
    roc.show()
    println(s"areaUnderROC: ${binarySummary.areaUnderROC}")

    // Set the model threshold to maximize F-Measure
    //设置模型阈值以最大化F-Measure
    val fMeasure = binarySummary.fMeasureByThreshold
    val maxFMeasure = fMeasure.select(max("F-Measure")).head().getDouble(0)
    val bestThreshold = fMeasure.where($"F-Measure" === maxFMeasure)
      .select("threshold").head().getDouble(0)
    lrModel.setThreshold(bestThreshold)

    spark.stop()
  }
}