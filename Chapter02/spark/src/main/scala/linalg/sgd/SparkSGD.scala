package linalg.sgd
import scala.util.Random
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.optimization.GradientDescent
import org.apache.spark.mllib.optimization.SquaredL2Updater
import org.apache.spark.mllib.optimization.LogisticGradient
import org.apache.spark.SparkContext


//随机梯度下降
object SparkSGD {
  def main(args: Array[String]): Unit = {
    val m = 4
    val n = 200000
    val sc = new SparkContext("local[2]", "")
    //mapPartitionsWithIndex与mapPartition基本相同,只是在处理函数的参数是一个二元元组,
    //元组的第一个元素是当前处理的分区的index,元组的第二个元素是当前处理的分区元素组成的Iterator
    val points = sc.parallelize(0 until m, 2).mapPartitionsWithIndex { (idx, iter) =>
      val random = new Random(idx)
      iter.map(i => (1.0, Vectors.dense(Array.fill(n)(random.nextDouble()))))
    }.cache()
    val (weights, loss) = GradientDescent.runMiniBatchSGD(
      points,
      new LogisticGradient,
      new SquaredL2Updater,
      0.1,
      2,
      1.0,
      1.0,
      Vectors.dense(new Array[Double](n)))
    println("w:"  + weights(0))
    println("loss:" + loss(0))
    sc.stop()

  }
}