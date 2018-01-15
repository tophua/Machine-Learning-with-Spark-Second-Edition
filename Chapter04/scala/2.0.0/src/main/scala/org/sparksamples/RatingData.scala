package org.sparksamples

/**
  * Created by Rajdeep on 12/22/15.
  * Modified by Rajdeep on 09/19/16
  */
import java.util.Date

import breeze.linalg.DenseVector
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{IntegerType, StructField, StructType}
//评级数据
object RatingData {
  def main(args: Array[String]) {

    val customSchema = StructType(Array(
      StructField("user_id", IntegerType, true),
      StructField("movie_id", IntegerType, true),
      StructField("rating", IntegerType, true),
      StructField("timestamp", IntegerType, true)))

    val spConfig = (new SparkConf).setMaster("local").setAppName("SparkApp")
    val spark = SparkSession
      .builder()
      .appName("SparkRatingData").config(spConfig)
      .getOrCreate()

    val rating_df = spark.read.format("com.databricks.spark.csv")
      .option("delimiter", "\t").schema(customSchema)
      .load("data/ml-100k/u.data")
    rating_df.createOrReplaceTempView("df")
    val num_ratings = rating_df.count()
    val num_movies = Util.getMovieDataDF().count()
    val first = rating_df.first()
    //first:[196,242,3,881250949]
    println("first:" + first)
    //num_ratings:100000
    println("num_ratings:" + num_ratings)

    val max = Util.spark.sql("select max(rating)  from df")
    /**
      +-----------+
      |max(rating)|
      +-----------+
      |          5|
      +-----------+
      */
    max.show()

    val min = Util.spark.sql("select min(rating)  from df")
    /**
      +-----------+
      |min(rating)|
      +-----------+
      |          1|
      +-----------+
      */
    min.show()

    val avg = Util.spark.sql("select avg(rating)  from df")
    /**
      +-----------+
      |avg(rating)|
      +-----------+
      |    3.52986|
      +-----------+
      */
    avg.show()

    val ratings_grouped = rating_df.groupBy("rating")
    /**
      +------+-----+
      |rating|count|
      +------+-----+
      |     1| 6110|
      |     3|27145|
      |     5|21201|
      |     4|34174|
      |     2|11370|
      +------+-----+
      */
    ratings_grouped.count().show()
    val ratings_byuser_local = rating_df.groupBy("user_id").count()
    val count_ratings_byuser_local = ratings_byuser_local.count()
    /**
      * +-------+-----+
        |user_id|count|
        +-------+-----+
        |    148|   65|
        |    463|  133|
      */
    ratings_byuser_local.show(ratings_byuser_local.collect().length)
    val movie_fields_df = Util.getMovieDataDF()
    val user_data_df = Util.getUserFieldDataFrame()
    val occupation_df = user_data_df.select("occupation").distinct()
    /**
      +-------------+
      |   occupation|
      +-------------+
      |administrator|
      |       artist|
      |       doctor|
      |     educator|
      |     engineer|
      |entertainment|
      |    executive|
      |   healthcare|
      |    homemaker|
      |       lawyer|
      |    librarian|
      |    marketing|
      |         none|
      |        other|
      |   programmer|
      |      retired|
      |     salesman|
      |    scientist|
      |      student|
      |   technician|
      +-------------+
      */
    occupation_df.sort("occupation").show()
    val occupation_df_collect = occupation_df.collect()

    var all_occupations_dict_1:Map[String, Int] = Map()
    var idx = 0;
    // for loop execution with a range
    //用范围循环执行
    for( idx <- 0 to (occupation_df_collect.length -1)){
      all_occupations_dict_1 += occupation_df_collect(idx)(0).toString() -> idx
    }
    //Encoding of 'doctor : 20
    println("Encoding of 'doctor : " + all_occupations_dict_1("doctor"))
    //Encoding of 'programmer' : 5
    println("Encoding of 'programmer' : " + all_occupations_dict_1("programmer"))

    var k = all_occupations_dict_1.size
    var binary_x = DenseVector.zeros[Double](k)
    var k_programmer = all_occupations_dict_1("programmer")
    binary_x(k_programmer) = 1
    //Binary feature vector: %sDenseVector(0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    //二进制特征向量
    println("Binary feature vector: %s" + binary_x)
    //Length of binary vector: 21
    println("Length of binary vector: " + k)

    Util.spark.udf.register("getCurrentHour", getCurrentHour _)
    /**
      * +----+
        |hour|
        +----+
        |  12|
        |  15|
        |  12|
        |  12|
        |  14|
        |  13|*/
    val timestamps_df =  Util.spark.sql("select getCurrentHour(timestamp) as hour from df")
    timestamps_df.show()

    Util.spark.udf.register("assignTod", assignTod _)
    timestamps_df.createOrReplaceTempView("timestamps")
    /**
      * +---------+
        |      tod|
        +---------+
        |    lunch|
        |afternoon|
        |    lunch|
        |    lunch|
        |afternoon|
        |    lunch|
        |    lunch|
        |afternoon|
        |afternoon|
      */
    val tod = Util.spark.sql("select assignTod(hour) as tod from timestamps")
    tod.show()

  }


  def getCurrentHour(dateStr: String) : Integer = {
    var currentHour = 0
    try {
      val date = new Date(dateStr.toLong)
      return int2Integer(date.getHours)
    } catch {
      case _ => return currentHour
    }
    return 1
  }

  def assignTod(hr : Integer) : String = {
    if(hr >= 7 && hr < 12){
      return "morning" //早上
    }else if ( hr >= 12 && hr < 14) {//中午
      return "lunch"
    } else if ( hr >= 14 && hr < 18) {
      return "afternoon" //下午
    } else if ( hr >= 18 && hr.<(23)) {
      return "evening" //晚上
    } else if ( hr >= 23 && hr <= 24) {
      return "night" // 夜里
    } else if (  hr < 7) {
      return "night" //夜里
    } else {
      return "error"
    }
  }

  def mean( x:Array[Int]) : Int = {
    return x.sum/x.length
  }

  def median( x:Array[Int]) : Int = {
    val middle = x.length/2
    if (x.length%2 == 1) {
      return x(middle)
    } else {
      return (x(middle-1) + x(middle)) / 2
    }
  }
}