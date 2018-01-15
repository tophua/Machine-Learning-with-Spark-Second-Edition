package org.sparksamples.featureext

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.feature.Word2Vec

object ConvertWordsToVectors{
  def main(args: Array[String]) {
    val file = "/Users/apple/Idea/workspace/Machine-Learning-with-Spark-Second-Edition/Chapter04/data/text8_10000"
    val conf = new SparkConf().setMaster("local").setAppName("Word2Vector")
    val sc = new SparkContext(conf)
    val input = sc.textFile(file).map(line => line.split(" ").toSeq)
    val word2vec = new Word2Vec()
    val model = word2vec.fit(input)
    val vectors = model.getVectors
    vectors foreach ( (t2) => println (t2._1 + "-->" + t2._2.mkString(" ")))
  }
}