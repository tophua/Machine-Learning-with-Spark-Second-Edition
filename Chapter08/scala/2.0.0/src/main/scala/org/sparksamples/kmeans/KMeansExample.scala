/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sparksamples.kmeans

// scalastyle:off println

// $example on$
import org.apache.spark.SparkConf
import org.apache.spark.ml.clustering.KMeans
// $example off$
import org.apache.spark.sql.SparkSession

/**
 * An example demonstrating k-means clustering.
  * 演示k均值聚类的一个例子
 * Run with
 * {{{
 * bin/run-example ml.KMeansExample
 * }}}
 */
object KMeansExample {
  val PATH = "/home/ubuntu/work/spark-2.0.0-bin-hadoop2.7/";

  def main(args: Array[String]): Unit = {
    // Creates a SparkSession.
    //val spark = SparkSession
    //  .builder
    //  .appName(s"${this.getClass.getSimpleName}")
    //  .getOrCreate()

    val spConfig = (new SparkConf).setMaster("local[1]").setAppName("SparkApp").
      set("spark.driver.allowMultipleContexts", "true")

    val spark = SparkSession
      .builder()
      .appName("Spark SQL Example")
      .config(spConfig)
      .getOrCreate()

    // $example on$
    // Loads data.
    val dataset = spark.read.format("libsvm").load(PATH + "data/mllib/sample_kmeans_data.txt")
    val cols = dataset.columns
    dataset.show()

    // Trains a k-means model.
    //训练一个k-means模型
    val kmeans = new KMeans().setK(2).setSeed(1L)
    val model = kmeans.fit(dataset)

    // Evaluate clustering by computing Within Set Sum of Squared Errors.
    //通过在平方误差平方和中计算评估聚类
    val WSSSE = model.computeCost(dataset)
    println(s"Within Set Sum of Squared Errors = $WSSSE")

    // Shows the result.
    println("Cluster Centers: ")
    model.clusterCenters.foreach(println)
    // $example off$

    spark.stop()
  }
}
// scalastyle:on println
