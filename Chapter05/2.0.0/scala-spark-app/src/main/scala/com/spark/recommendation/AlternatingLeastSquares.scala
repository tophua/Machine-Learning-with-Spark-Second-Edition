package com.spark.recommendation

import org.apache.commons.math3.linear._
import org.apache.spark.sql.SparkSession

/**
  * Created by manpreet.singh on 04/09/16.
  *
  * Matrix factorization assumes that:
  * 矩阵分解假定：
  * 1. Each user can be described by k attributes or features.
  *    每个用户可以用k个属性或特征来描述
  *    For example, feature 1 might be a number that says how much each user likes sci-fi movies.
  *    例如:功能1可能是一个数字,表示每个用户喜欢多少科幻电影
  * 2. Each item (movie) can be described by an analagous set of k attributes or features.
  *    每个项目(电影)都可以用一组一致的属性或特征来描述
  *    To correspond to the above example, feature 1 for the movie might be a number that says how close the movie is to pure sci-fi.
  *    为了与上面的例子相对应,电影的特征1可能是一个数字,表示电影与纯科幻有多接近
  * 3. If we multiply each feature of the user by the corresponding feature of the movie and add everything together,
  *    如果我们将用户的每个特征乘以电影的相应特征并将所有内容加在一起，这对于用户给予该电影的评价将是一个很好的近似值
  *    this will be a good approximation for the rating the user would give that movie.
  */

object AlternatingLeastSquares {

  var movies = 0
  var users = 0
  var features = 0
  var ITERATIONS = 0
  val LAMBDA = 0.01 // Regularization coefficient 正则化系数

  private def vector(n: Int): RealVector =
    new ArrayRealVector(Array.fill(n)(math.random))

  private def matrix(rows: Int, cols: Int): RealMatrix =
    new Array2DRowRealMatrix(Array.fill(rows, cols)(math.random))

  def rSpace(): RealMatrix = {
    val mh = matrix(movies, features)
    val uh = matrix(users, features)
    mh.multiply(uh.transpose())
  }

  def rmse(targetR: RealMatrix, ms: Array[RealVector], us: Array[RealVector]): Double = {
    val r = new Array2DRowRealMatrix(movies, users)
    for (i <- 0 until movies; j <- 0 until users) {
      r.setEntry(i, j, ms(i).dotProduct(us(j)))
    }
    val diffs = r.subtract(targetR)
    var sumSqs = 0.0
    for (i <- 0 until movies; j <- 0 until users) {
      val diff = diffs.getEntry(i, j)
      sumSqs += diff * diff
    }
    math.sqrt(sumSqs / (movies.toDouble * users.toDouble))
  }

  def update(i: Int, m: RealVector, us: Array[RealVector], R: RealMatrix) : RealVector = {
    val U = us.length
    val F = us(0).getDimension
    var XtX: RealMatrix = new Array2DRowRealMatrix(F, F)
    var Xty: RealVector = new ArrayRealVector(F)
    // For each user that rated the movie
    //针对每位评价该电影的用户
    for (j <- 0 until U) {
      val u = us(j)
      // Add u * u^t to XtX
      XtX = XtX.add(u.outerProduct(u))
      // Add u * rating to Xty
      Xty = Xty.add(u.mapMultiply(R.getEntry(i, j)))
    }
    // Add regularization coefs to diagonal terms
    // 将正则化系数添加到对角线项
    for (d <- 0 until F) {
      XtX.addToEntry(d, d, LAMBDA * U)
    }
    // Solve it with Cholesky
      new CholeskyDecomposition(XtX).getSolver.solve(Xty)
  }

  def main(args: Array[String]) {

    //initialize variables for rand data generation
    //为rand数据生成初始化变量
    movies = 100
    users = 500
    features = 10
    ITERATIONS = 5
    var slices = 2

    //initiate Spack Context
    //启动Spack上下文
    val spark = SparkSession.builder.master("local[2]").appName("AlternatingLeastSquares").getOrCreate()
    val sc = spark.sparkContext

    // create a Realmatrix with the follow
    //创建一个Realmatrix跟随
    // movies matrix : 100 x 10
    // feature matrix : 500 x 10
    // populate with random numbers
    //填充随机数字
    // multiple movie matrix with transpose of user matric
    //具有用户矩阵转置的多个电影矩阵
    // (100 x 10 ) x ( 10 x 500) = 100 x 500 matrix

    val r_space = rSpace()
    println("No of rows:" + r_space.getRowDimension)
    println("No of cols:" + r_space.getColumnDimension)

    // Initialize m and u randomly
    //初始化m和u随机
    var ms = Array.fill(movies)(vector(features))
    var us = Array.fill(users)(vector(features))

    // Iteratively update movies then users
    //迭代更新电影,然后用户
    val Rc = sc.broadcast(r_space)
    var msb = sc.broadcast(ms)
    var usb = sc.broadcast(us)
    //Objective is to find ms and us matrices by iterating over existing values and comparing with the real value matrix
    //目标是通过对现有值进行迭代并与实值矩阵进行比较来查找ms和us矩阵
    //Choksey decomposition is being used to solve this
    //Choksey分解正被用来解决这个问题
    for (iter <- 1 to ITERATIONS) {
      println(s"Iteration $iter:")
      ms = sc.parallelize(0 until movies, slices)
        .map(i => update(i, msb.value(i), usb.value, Rc.value))
        .collect()
      msb = sc.broadcast(ms) // Re-broadcast ms because it was updated
      us = sc.parallelize(0 until users, slices)
        .map(i => update(i, usb.value(i), msb.value, Rc.value.transpose()))
        .collect()
      usb = sc.broadcast(us) // Re-broadcast us because it was updated
      println("RMSE = " + rmse(r_space, ms, us))
      println()
    }
    spark.stop()
  }
}
