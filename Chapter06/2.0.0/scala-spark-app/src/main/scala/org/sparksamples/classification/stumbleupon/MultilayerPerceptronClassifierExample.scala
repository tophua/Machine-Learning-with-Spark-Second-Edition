package org.sparksamples.classification.stumbleupon

import org.apache.spark.ml.classification.MultilayerPerceptronClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.sql.SparkSession

// set VM Option as -Dspark.master=local[1]
object MultilayerPerceptronClassifierExample {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      .appName("MultilayerPerceptronClassifierExample")
      .getOrCreate()

    // Load the data stored in LIBSVM format as a DataFrame.
    //加载LIBSVM格式的数据作为DataFrame
    val data = spark.read.format("libsvm")
      .load("/Users/manpreet.singh/Sandbox/codehub/github/machinelearning/spark-ml/Chapter_06/2.0.0/scala-spark-app/src/main/scala/org/sparksamples/classification/dataset/spark-data/sample_multiclass_classification_data.txt")

    // Split the data into train and test
    val splits = data.randomSplit(Array(0.6, 0.4), seed = 1234L)
    val train = splits(0)
    val test = splits(1)

    // specify layers for the neural network:
    //为神经网络指定图层：
    // input layer of size 4 (features), two intermediate of size 5 and 4
    // and output of size 3 (classes)
    //大小为4（特征）的输入层，大小为5和4的两个中间和大小为3（类）的输出，
    val layers = Array[Int](4, 5, 4, 3)

    // create the trainer and set its parameters
    //创建教练并设置其参数
    val trainer = new MultilayerPerceptronClassifier()
      .setLayers(layers)
      .setBlockSize(128)
      .setSeed(1234L)
      .setMaxIter(100)

    // train the model
    val model = trainer.fit(train)

    // compute accuracy on the test set
    //计算测试集的精度
    val result = model.transform(test)
    val predictionAndLabels = result.select("prediction", "label")
    val evaluator = new MulticlassClassificationEvaluator()
      .setMetricName("accuracy")

    println("Test set accuracy = " + evaluator.evaluate(predictionAndLabels))

    spark.stop()
  }
}
