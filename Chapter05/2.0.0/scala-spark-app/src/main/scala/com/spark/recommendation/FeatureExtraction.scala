package com.spark.recommendation

import org.apache.spark.{sql, SparkConf}
import org.apache.spark.ml.recommendation.ALS
import org.apache.spark.sql.{Dataset, SparkSession}

/**
  * Created by manpreet.singh on 04/09/16.
  */
object FeatureExtraction {

  val spark = SparkSession.builder.master("local[2]").appName("FeatureExtraction").getOrCreate()

  case class Rating(userId: Int, movieId: Int, rating: Float, timestamp: Long)
  def parseRating(str: String): Rating = {
    //val fields = str.split("\t")
    val fields = str.split("::")
    Rating(fields(0).toInt, fields(1).toInt, fields(2).toFloat, fields(3).toLong)
  }

  /**
    * In earlier versions of spark, spark context was entry point for Spark. As RDD was main API, it was created and manipulated using context API’s.
    * 在Spark的早期版本中,Spark上下文是Spark的入口点,由于RDD是主要的API,它是使用上下文API创建和操作的。
    * For every other API,we needed to use different contexts.For streaming, we needed StreamingContext, for SQL sqlContext and for hive HiveContext.
    * 对于其他所有API，我们需要使用不同的上下文,对于流式处理,我们需要StreamingContext，SQL sqlContext和Hive HiveContext。
    * But as DataSet and Dataframe API’s are becoming new standard API’s we need an entry point build for them.
    * 但是随着DataSet和Dataframe API正在成为新的标准API,我们需要为它们建立一个入口点
    * So in Spark 2.0, we have a new entry point for DataSet and Dataframe API’s called as Spark Session.
    * 以在Spark 2.0中,我们有一个新的DataSet和Dataframe API的入口,称为Spark Session。
    * SparkSession is essentially combination of SQLContext, HiveContext and future StreamingContext.
    * SparkSession基本上是SQLContext,HiveContext和将来StreamingContext的组合。
    * All the API’s available on those contexts are available on spark session also. Spark session internally has a spark context for actual computation.
    * 所有在这些上下文中可用的API都可以在spark会话中使用,Spark会话内部有一个实际计算的Spark上下文
    */
  def getFeatures(): sql.DataFrame = {
    import spark.implicits._
    //val ratings = spark.read.textFile("/Users/manpreet.singh/Sandbox/codehub/github/machinelearning/spark-ml/Chapter_05/data/ml-100k 2/u.data").map(parseRating).toDF()
    val ratings = spark.read.textFile("/Users/apple/Idea/workspace/Machine-Learning-with-Spark-Second-Edition/Chapter05/2.0.0/scala-spark-app/src/main/scala/com/spark/recommendation/sample_movielens_ratings.txt").map(parseRating).toDF()
    //[0,2,3.0,1424380312]
    println(ratings.first())

//    val Array(training, test) = ratings.randomSplit(Array(0.8, 0.2))
//    println(training.first())

    return ratings
  }

  def getSpark(): SparkSession = {
    return spark
  }

  def main(args: Array[String]) {
    getFeatures()
  }

}