package linalg.matrix

import org.apache.spark.ml.linalg.Matrix
import org.apache.spark.ml.linalg.Matrices
import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.mllib.linalg.distributed.IndexedRowMatrix
import org.apache.spark.mllib.linalg.distributed.CoordinateMatrix
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.linalg.distributed.IndexedRow
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.linalg.distributed.MatrixEntry
import org.apache.spark.sql.SparkSession

object SparkMatrix {

  def main(args: Array[String]) {

    val dMatrix: Matrix = Matrices.dense(2, 2, Array(1.0, 2.0, 3.0, 4.0))
    println("dMatrix: \n" + dMatrix)

    val sMatrixOne: Matrix = Matrices.sparse(3, 2, Array(0, 1, 3), Array(0, 2, 1), Array(5, 6, 7))
    println("sMatrixOne: \n" + sMatrixOne)

    val sMatrixTwo: Matrix = Matrices.sparse(3, 2, Array(0, 1, 3), Array(0, 1, 2), Array(5, 6, 7))
    println("sMatrixTwo: \n" + sMatrixTwo)

    val spark = SparkSession
      .builder
      .appName("BikeSharing")
      .master("local[1]")
      .getOrCreate()

    val sc = spark.sparkContext
    //密集：[1.0,0.0,3.0] 其和一般的数组无异
    val denseData = Seq(
      Vectors.dense(0.0, 1.0, 2.1),
      Vectors.dense(3.0, 2.0, 4.0),
      Vectors.dense(5.0, 7.0, 8.0),
      Vectors.dense(9.0, 0.0, 1.1)
    )
    //稀疏：(3,[0,2],[1.0,3.0]) 其表示的含义(向量大小，序号，值) 序号从0开始
    val sparseData = Seq(
      Vectors.sparse(3, Seq((1, 1.0), (2, 2.1))),
      Vectors.sparse(3, Seq((0, 3.0), (1, 2.0), (2, 4.0))),
      Vectors.sparse(3, Seq((0, 5.0), (1, 7.0), (2, 8.0))),
      Vectors.sparse(3, Seq((0, 9.0), (2, 1.0)))
    )

    val denseMat = new RowMatrix(sc.parallelize(denseData, 2))
    val sparseMat = new RowMatrix(sc.parallelize(sparseData, 2))

    println("Dense Matrix - Num of Rows :" + denseMat.numRows())
    println("Dense Matrix - Num of Cols:" + denseMat.numCols())
    println("Sparse Matrix - Num of Rows :" + sparseMat.numRows())
    println("Sparse Matrix - Num of Cols:" + sparseMat.numCols())

    val data = Seq(
      (0L, Vectors.dense(0.0, 1.0, 2.0)),
      (1L, Vectors.dense(3.0, 4.0, 5.0)),
      (3L, Vectors.dense(9.0, 0.0, 1.0))
    ).map(x => IndexedRow(x._1, x._2))
    //增加索引index标识列
    val indexedRows: RDD[IndexedRow] = sc.parallelize(data, 2)
   val d= spark.createDataFrame(indexedRows)
    /**
      * +-----+-------------+
        |index|       vector|
        +-----+-------------+
        |    0|[0.0,1.0,2.0]|
        |    1|[3.0,4.0,5.0]|
        |    3|[9.0,0.0,1.0]|
        +-----+-------------+
      */
    d.show()
    val indexedRowsMat = new IndexedRowMatrix(indexedRows)
    //Indexed Row Matrix - No of Rows: 4
    println("Indexed Row Matrix - No of Rows: " + indexedRowsMat.numRows())
    //Indexed Row Matrix - No of Cols: 3
    println("Indexed Row Matrix - No of Cols: " + indexedRowsMat.numCols())

    val entries = sc.parallelize(Seq(
      (0, 0, 1.0),
      (0, 1, 2.0),
      (1, 1, 3.0),
      (1, 2, 4.0),
      (2, 2, 5.0),
      (2, 3, 6.0),
      (3, 0, 7.0),
      (3, 3, 8.0),
      (4, 1, 9.0)), 3).map { case (i, j, value) =>
      MatrixEntry(i, j, value)
    }

    val coordinateMat = new CoordinateMatrix(entries)

    println("Coordinate Matrix - No of Rows: " + coordinateMat.numRows())
    println("Coordinate Matrix - No of Cols: " + coordinateMat.numCols())

    sc.stop()

  }

}
