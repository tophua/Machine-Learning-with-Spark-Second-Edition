package linalg.vector
import org.apache.spark.ml.linalg.{Vector, Vectors}

object SparkVector {

  def main(args: Array[String]): Unit = {
    // Create a dense vector (1.0, 0.0, 3.0).
    //创建一个密集的向量
    val dVectorOne: Vector = Vectors.dense(1.0, 0.0, 2.0)
    println("dVectorOne:" + dVectorOne)

    //  Sparse vector (1.0, 0.0, 2.0, 3.0)
    // corresponding to nonzero entries.
    //稀疏的向量
    val sVectorOne: Vector = Vectors.sparse(4,  Array(0, 2,3),  Array(1.0, 2.0, 3.0))

    // Create a sparse vector (1.0, 0.0, 2.0, 2.0) by specifying its
    // nonzero entries.
    //非零的条目
    val sVectorTwo: Vector = Vectors.sparse(4,  Seq((0, 1.0), (2, 2.0), (3, 3.0)))

    println("sVectorOne:" + sVectorOne)
    println("sVectorTwo:" + sVectorTwo)

    val sVectorOneMax = sVectorOne.argmax
    val sVectorOneNumNonZeros = sVectorOne.numNonzeros
    val sVectorOneSize = sVectorOne.size
    val sVectorOneArray = sVectorOne.toArray

    println("sVectorOneMax:" + sVectorOneMax)
    println("sVectorOneNumNonZeros:" + sVectorOneNumNonZeros)
    println("sVectorOneSize:" + sVectorOneSize)
    println("sVectorOneArray:" + sVectorOneArray)
    val dVectorOneToSparse = dVectorOne.toSparse

    println("dVectorOneToSparse:" + dVectorOneToSparse)


  }
}