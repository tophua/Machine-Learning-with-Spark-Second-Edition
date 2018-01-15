import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._

/**
 * A simple Spark app in Scala
 */
object ScalaApp {

  def main(args: Array[String]) {
    val sc = new SparkContext("local[2]", "First Spark App")
    //sc.setLogLevel("ERROR")
    // we take the raw data in CSV format and convert it into a set of records of the form (user, product, price)
    //我们以CSV格式获取原始数据,并将其转换为表单的一组记录（用户、产品、价格）
    val data = sc.textFile("/Users/apple/Idea/workspace/Machine-Learning-with-Spark-Second-Edition/Chapter01/scala-spark-app/data/UserPurchaseHistory.csv")
      .map(line => line.split(","))
      .map(purchaseRecord => (purchaseRecord(0), purchaseRecord(1), purchaseRecord(2)))

    // let's count the number of purchases
    //让我们数一数购买的数量
    val numPurchases = data.count()

    // let's count how many unique users made purchases
    //让我们数一数有多少独特的用户购买
    val uniqueUsers = data.map { case (user, product, price) => user }.distinct().count()

    // let's sum up our total revenue
    //让我们总结一下我们的总收入
    val totalRevenue = data.map { case (user, product, price) => price.toDouble }.sum()

    // let's find our most popular product
    //让我们找到最受欢迎的产品
    val productsByPopularity = data
      .map { case (user, product, price) => (product, 1) }
      .reduceByKey(_ + _)
      .collect()
      .sortBy(-_._2)
    val mostPopular = productsByPopularity(0)



    // finally, print everything out
    //最后,把所有的东西打印出来
    println("Total purchases: " + numPurchases)
    println("Unique users: " + uniqueUsers)
    println("Total revenue: " + totalRevenue)
    println("Most popular product: %s with %d purchases".format(mostPopular._1, mostPopular._2))

    sc.stop()
  }

}
