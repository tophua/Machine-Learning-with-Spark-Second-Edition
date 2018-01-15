package org.sparksamples.df
//import org.apache.spark.sql.SQLContext
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
/**
  * Created by Rajdeep Dua on 8/22/16.
  */
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType};
object UserData {
  def main(args: Array[String]): Unit = {
    val customSchema = StructType(Array(
      StructField("no", IntegerType, true),
      StructField("age", StringType, true),
      StructField("gender", StringType, true),
      StructField("occupation", StringType, true),
      StructField("zipCode", StringType, true)));
    val spConfig = (new SparkConf).setMaster("local").setAppName("SparkApp")
    val spark = SparkSession
      .builder()
      .appName("SparkUserData").config(spConfig)
      .getOrCreate()

    val user_df = spark.read.format("com.databricks.spark.csv")
      .option("delimiter", "|").schema(customSchema)
      .load("data/ml-100k/u.user")
    val first = user_df.first()
    //First Record : [1,24,M,technician,85711]
    println("First Record : " + first)

    val num_genders = user_df.groupBy("gender").count().count()

    val num_occupations = user_df.groupBy("occupation").count().count()
    val num_zipcodes = user_df.groupBy("zipCode").count().count()
    //总数
    //num_users : 943
    println("num_users : " + user_df.count())
    //性别数
    //num_genders : 2
    println("num_genders : "+ num_genders)
    //职业数
    //num_occupations : 21
    println("num_occupations : "+ num_occupations)
    //邮编
    //num_zipcodes: 795
    println("num_zipcodes: " + num_zipcodes)
    println("Distribution by Occupation")
    //按职业分配
    /**
        +-------------+-----+
        |   occupation|count|
        +-------------+-----+
        |    librarian|   51|
        |      retired|   14|
        |       lawyer|   12|
        |         none|    9|
        |       writer|   45|
        |   programmer|   66|
        |    marketing|   26|
        |        other|  105|
        |    executive|   32|
        |    scientist|   31|
        |      student|  196|
        |     salesman|   12|
        |       artist|   28|
        |   technician|   27|
        |administrator|   79|
        |     engineer|   67|
        |   healthcare|   16|
        |     educator|   95|
        |entertainment|   18|
        |    homemaker|    7|
        +-------------+-----+
      */
    println(user_df.groupBy("occupation").count().show())
    val u=user_df.groupBy("occupation").count().show()
    //occupation:21
    println("occupation:"+user_df.groupBy("occupation").count().count())
  }
}
