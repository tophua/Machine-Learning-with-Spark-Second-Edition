package org.sparksamples.exploredataset

import java.io.File

import org.apache.spark.SparkContext
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.{ChartFactory, ChartUtilities}
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.statistics.{HistogramDataset, HistogramType}

/**
  * Created by manpreet.singh on 27/02/16.
  */
object explore_users {

  def main(args: Array[String]) {
    val sc = new SparkContext("local[2]", "Explore Users in Movie Dataset")

    val user_fields = sc.textFile("data/ml-100k/u.user").map(line => line.split("\\|"))
      .map(records => (records(0), records(1), records(2), records(3), records(4)))

    val num_users = user_fields.count()
    //943
    println(num_users)

    // print what rdd has ?
    /**
      (1,24,M,technician,85711)
      (2,53,F,other,94043)
      (3,23,M,writer,32067)
      (4,24,M,technician,43537)
      (5,33,F,other,15213)
      (6,42,M,executive,98101)
      (7,57,M,administrator,91344)
      (8,36,M,administrator,05201)
      (9,29,M,student,01002)
      (10,53,M,lawyer,90703)
      */
    user_fields.take(10).foreach(println)
    // use map to get new rdd
    //使用Map来获得新的rdd
    println(user_fields.map(user_fields => user_fields._3).distinct().count())

    val num_genders = user_fields.map{case(id, age, gender, occupations, zip) => gender}.distinct().count()
    //打印性别数:2
    println(num_genders)
    //职业数
    val num_occupations = user_fields.map{case(id, age, gender, occupations, zip) => occupations}.distinct().count()
    //职业21
    println(num_occupations)
    //邮政编码795
    val num_zipcodes = user_fields.map{case(id, age, gender, occupations, zip) => zip}.distinct().count()
    println(num_zipcodes)
    //jfreechar 直方图数据集
    val dataset1 = new HistogramDataset()
    //相对频率
    dataset1.setType(HistogramType.RELATIVE_FREQUENCY)
    //取出年龄
    val ages = user_fields.map(user_fields => user_fields._2.toDouble).collect()
    //ages.take(5).foreach(println)
    dataset1.addSeries("Histogram", ages, 20)
    val plotTitle1 = "Age Histogram";
    val xaxis1 = "age";
    val yaxis1 = "scale";
    val orientation1 = PlotOrientation.VERTICAL;
    val show1 = false;
    val toolTips1 = false;
    val urls1 = false;
    val chart1 = ChartFactory.createHistogram( plotTitle1, xaxis1, yaxis1, dataset1, orientation1, show1, toolTips1, urls1);
    val width1 = 600;
    val height1 = 400;
    //保存图片
    ChartUtilities.saveChartAsPNG(new File("/Users/apple/Idea/workspace/Machine-Learning-with-Spark-Second-Edition/data/age_histogram.png"), chart1, width1, height1);
    //职业
    val occs = user_fields.map(user_fields => (user_fields._4,1)).reduceByKey(_+_).collect()
    val dataset2 = new DefaultCategoryDataset()
    /**
      (marketing,26)
      (librarian,51)
      (technician,27)
      (scientist,31)
      (none,9)
      (executive,32)
      (other,105)
      (programmer,66)
      (lawyer,12)
      (entertainment,18)
      (salesman,12)
      (retired,14)
      (healthcare,16)
      (administrator,79)
      (student,196)
      (doctor,7)
      (writer,45)
      (engineer,67)
      (homemaker,7)
      (educator,95)
      (artist,28)
      **/
    occs.foreach(println)
    for (occ <- occs) {
      dataset2.setValue(occ._2, "count", occ._1);
    }

    val plotTitle2 = "Occ Histogram";
    val xaxis2 = "occ";
    val yaxis2 = "count";
    val orientation2 = PlotOrientation.VERTICAL;
    val show2 = false;
    val toolTips2 = false;
    val urls2 = false;
    val chart2 = ChartFactory.createBarChart( plotTitle2, xaxis2, yaxis2, dataset2, orientation2, show2, toolTips2, urls2);
    val width2 = 2000;
    val height2 = 500;
    //保存图片
    ChartUtilities.saveChartAsPNG(new File("/Users/apple/Idea/workspace/Machine-Learning-with-Spark-Second-Edition/data/occ_histogram.png"), chart2, width2, height2);

    sc.stop()
  }

}
