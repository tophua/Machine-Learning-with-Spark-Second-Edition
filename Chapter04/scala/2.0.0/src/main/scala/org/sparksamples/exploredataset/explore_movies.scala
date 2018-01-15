package org.sparksamples.exploredataset

import breeze.linalg.CSCMatrix
import org.apache.spark.SparkContext
import org.sparksamples.Util
import org.apache.spark.mllib.feature.Word2Vec
import scala.collection.mutable.ListBuffer

/**
  * Created by manpreet.singh on 27/02/16.
  * Modified by Rajdeep on 01/1016
  */
object explore_movies {
  //处理正则表达式
  def processRegex(input:String):String= {
    //^ 行的开头 ^(匹配未包含的任意字符
    val pattern = "^[^(]*".r

    val output = pattern.findFirstIn(input)
    //=befer==Toy Story (1995)==after==Toy Story
    //println("=befer=="+input+"==after=="+output.get)
    return output.get

  }

  def main(args: Array[String]) {
    val sc = new SparkContext("local[2]", "Explore Users in Movie Dataset")
    sc.setLogLevel("ERROR")
    val raw_title = org.sparksamples.Util.getMovieDataDF().select("name")
    /**
      * +---+--------------------+-----------+---+
        | id|                name|       date|url|
        +---+--------------------+-----------+---+
        |  1|    Toy Story (1995)|01-Jan-1995|   |
        |  2|    GoldenEye (1995)|01-Jan-1995|   |
        |  3|   Four Rooms (1995)|01-Jan-1995|   |
        |  4|   Get Shorty (1995)|01-Jan-1995|   |
        |  5|      Copycat (1995)|01-Jan-1995|   |
        |  6|Shanghai Triad (Y...|01-Jan-1995|   |
        |  7|Twelve Monkeys (1...|01-Jan-1995|   |
        |  8|         Babe (1995)|01-Jan-1995|   |
        |  9|Dead Man Walking ...|01-Jan-1995|   |
        +---+--------------------+-----------+---+
      */
    //raw_title.show()

    raw_title.createOrReplaceTempView("titles")
    //自定义函数,注意参数
    Util.spark.udf.register("processRegex", processRegex _)
    val processed_titles = Util.spark.sql("select processRegex(name) from titles")
    /**
      * +--------------------+
        |           UDF(name)|
        +--------------------+
        |          Toy Story |
        |          GoldenEye |
        |         Four Rooms |
        |         Get Shorty |
        |            Copycat |
        |     Shanghai Triad |
        |     Twelve Monkeys |
        | White Balloon, The |
        |     Antonia's Line |
        | Angels and Insects |
        +--------------------+
      */
    processed_titles.show()
    val titles_rdd = processed_titles.rdd.map(r => r(0).toString)
    //取出第一列的前5个
    val y = titles_rdd.take(5)
    titles_rdd.take(5).foreach(println)
    println(titles_rdd.first())

    //val title_terms = null
    //以空格分割,取出前五个
    val title_terms = titles_rdd.map(x => x.split(" "))
    title_terms.take(5).foreach(_.foreach(println))
    //1682
    println(title_terms.count())

    val all_terms_dic = new ListBuffer[String]()
    val all_terms = title_terms.flatMap(title_terms => title_terms).distinct().collect()
    for (term <- all_terms){

      all_terms_dic += term
    }
    // ListBuffer:2453
    println(" ListBuffer:"+all_terms_dic.length)
    //ListBuffer indexOf 543
    println(" ListBuffer indexOf"+all_terms_dic.indexOf("Dead"))
    //ListBuffer indexOf 2199
    println(" ListBuffer indexOf"+all_terms_dic.indexOf("Rooms"))

    val all_terms_withZip = title_terms.flatMap(title_terms => title_terms).distinct().zipWithIndex().collectAsMap()
    //Some(543)
    println(all_terms_withZip.get("Dead"))
    //Some(2199)
    println(all_terms_withZip.get("Rooms"))

    val word2vec = new Word2Vec()
    val rdd_terms = titles_rdd.map(title => title.split(" ").toSeq)
    val model = word2vec.fit(rdd_terms)
    println(model.findSynonyms("Dead", 40))

    val term_vectors = title_terms.map(title_terms => create_vector(title_terms, all_terms_dic))
    /**
      1 x 2453 CSCMatrix
      (0,84) 1
      (0,618) 1
      1 x 2453 CSCMatrix
      (0,410) 1
      1 x 2453 CSCMatrix
      (0,734) 1
      (0,2199) 1
      1 x 2453 CSCMatrix
      (0,166) 1
      (0,419) 1
      1 x 2453 CSCMatrix
      (0,1536) 1
      */
    term_vectors.take(5).foreach(println)

    sc.stop()
  }

  def create_vector(title_terms:Array[String], all_terms_dic:ListBuffer[String]): CSCMatrix[Int] = {
    var idx = 0
    val x = CSCMatrix.zeros[Int](1, all_terms_dic.length)
    title_terms.foreach(i => {
      if (all_terms_dic.contains(i)) {
        idx = all_terms_dic.indexOf(i)
        x.update(0, idx, 1)
      }
    })
    return x
  }

  def convert(year:String): String = {
    try{
      val mod_year = year.substring(year.length - 4,year.length)
      return mod_year
    }catch {
      case e : Exception => return "1900"
    }
  }

}
