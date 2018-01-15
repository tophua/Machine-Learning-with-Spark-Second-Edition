package org.sparksamples

import java.awt.Font

import org.jfree.chart.axis.CategoryLabelPositions

import scalax.chart.module.ChartFactories

/**
  * Created by @rajdeepdua on 2/22/2016.
  * Modified for DataFrame on 9/4/2016
  * 职业分布情况
  */
object UserOccupationChart {

  def main(args: Array[String]) {
    val userDataFrame = Util.getUserFieldDataFrame()
    val occupation = userDataFrame.select("occupation")
    val occupation_groups = userDataFrame.groupBy("occupation").count()

    //occupation_groups.show()
    val occupation_groups_sorted = occupation_groups.sort("count")
    /**
      * +-------------+-----+
        |   occupation|count|
        +-------------+-----+
        |    homemaker|    7|
        |       doctor|    7|
        |         none|    9|
        |     salesman|   12|
        |       lawyer|   12|
        |      retired|   14|
        |   healthcare|   16|
        |entertainment|   18|
        |    marketing|   26|
        |   technician|   27|
        |       artist|   28|
        |    scientist|   31|
        |    executive|   32|
        |       writer|   45|
        |    librarian|   51|
        |   programmer|   66|
        |     engineer|   67|
        |administrator|   79|
        |     educator|   95|
        |        other|  105|
        +-------------+-----+
      */
    occupation_groups_sorted.show()
    val occupation_groups_collection = occupation_groups_sorted.collect()

    val ds = new org.jfree.data.category.DefaultCategoryDataset
    val mx = scala.collection.immutable.ListMap()

    for( x <- 0 until occupation_groups_collection.length) {
      val occ = occupation_groups_collection(x)(0)
      val count = Integer.parseInt(occupation_groups_collection(x)(1).toString)
      ds.addValue(count,"UserAges", occ.toString)
    }

    val chart = ChartFactories.BarChart(ds)
    val font = new Font("Dialog", Font.PLAIN,5);

    chart.peer.getCategoryPlot.getDomainAxis().
      setCategoryLabelPositions(CategoryLabelPositions.UP_90);
    chart.peer.getCategoryPlot.getDomainAxis.setLabelFont(font)
    chart.show()
    Util.sc.stop()
  }
}
