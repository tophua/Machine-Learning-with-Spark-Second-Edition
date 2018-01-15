import org.apache.spark.mllib.feature.{StandardScaler, StandardScalerModel}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.{SparkConf, SparkContext}

object StandardScalarSample {
  def main(args: Array[String]) {
    val conf = new SparkConf().setMaster("local").setAppName("Word2Vector")
    val sc = new SparkContext(conf)
    val data = MLUtils.loadLibSVMFile(sc,
      org.sparksamples.Util.SPARK_HOME +  "/data/mllib/sample_libsvm_data.txt")
    //StandardScaler 零均值规范化
    //使数据均值为0,方差为1,Spark中可以选择是带或者不带均值和方差。
    val scaler1 = new StandardScaler().fit(data.map(x => x.features))
    val scaler2 = new StandardScaler(withMean = true, withStd = true).fit(data.map(x => x.features))
    // scaler3 is an identical model to scaler2, and will produce identical transformations
    //scaler3是与scaler2相同的模型，并且会产生相同的转换
    val scaler3 = new StandardScalerModel(scaler2.std, scaler2.mean)

    // data1 will be unit variance.
    //data1将是单位差异
    val data1 = data.map(x => (x.label, scaler1.transform(x.features)))
    println(data1.first())

    // Without converting the features into dense vectors, transformation with zero mean will raise
    // exception on sparse vector.
    // data2 will be unit variance and zero mean.
    //如果不将特征转化为密集向量,零均值转换会引起稀疏向量的异常
    val data2 = data.map(x => (x.label, scaler2.transform(Vectors.dense(x.features.toArray))))
    println(data2.first())
  }
}