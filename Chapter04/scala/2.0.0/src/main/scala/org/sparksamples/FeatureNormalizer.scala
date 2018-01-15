package org.sparksamples


import breeze.linalg.{DenseVector, norm}
import org.apache.spark.SparkContext
import org.apache.spark.mllib.feature.Normalizer
import org.apache.spark.mllib.linalg.Vectors
/**
  * Created by ubuntu on 10/2/16.
  */
object FeatureNormalizer {
  def main(args: Array[String]): Unit = {

    val v = Vectors.dense(0.49671415, -0.1382643, 0.64768854, 1.52302986, -0.23415337, -0.23413696, 1.57921282,
      0.76743473, -0.46947439, 0.54256004)
    //标准化
    //计算其p-范数，然后对该每个元素除以p-范数。将原始特征Normalizer以后可以使得机器学习算法有更好的表现。
    val normalizer = new Normalizer(2)
    val norm_op = normalizer.transform(v)
    println(norm_op)
  }

}
