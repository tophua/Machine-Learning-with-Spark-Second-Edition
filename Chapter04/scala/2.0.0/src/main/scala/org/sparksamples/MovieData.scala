package org.sparksamples
/**
  * Created by Rajdeep on 12/22/15.
  */

object MovieData {

    def getMovieYearsCountSorted(): scala.Array[(Int,String)] = {
    val movie_data_df = Util.getMovieDataDF()
    movie_data_df.createOrReplaceTempView("movie_data")
    movie_data_df.printSchema()

    Util.spark.udf.register("convertYear", Util.convertYear _)
      /**
        * +---+----------------------------------------------------+-----------+---+
          |id |name                                                |date       |url|
          +---+----------------------------------------------------+-----------+---+
          |1  |Toy Story (1995)                                    |01-Jan-1995|   |
          |2  |GoldenEye (1995)                                    |01-Jan-1995|   |
          |3  |Four Rooms (1995)                                   |01-Jan-1995|   |
          |4  |Get Shorty (1995)                                   |01-Jan-1995|   |
          |5  |Copycat (1995)                                      |01-Jan-1995|   |
          |6  |Shanghai Triad (Yao a yao yao dao waipo qiao) (1995)|01-Jan-1995|   |
          |7  |Twelve Monkeys (1995)                               |01-Jan-1995|   |
          +---+----------------------------------------------------+-----------+---+
        */
    movie_data_df.show(false)

    val movie_years = Util.spark.sql("select convertYear(date) as year from movie_data")
    val movie_years_count = movie_years.groupBy("year").count()
      /**
          +----+-----+
          |year|count|
          +----+-----+
          |1959|4    |
          |1990|24   |
          |1975|6    |
          |1977|4    |
          |1974|8    |
          |1955|5    |
          |1978|4    |
          |1961|3    |
          |1942|2    |
          |1939|7    |
          |1944|5    |
          |1922|1    |
          |1952|3    |
          |1956|4    |
          |1934|4    |
          |1997|286  |
          |1988|11   |
          |1994|214  |
          |1968|6    |
          |1951|5    |
          +----+-----+
        */
    movie_years_count.show(false)
    val movie_years_count_rdd = movie_years_count.rdd.map(row => (Integer.parseInt(row(0).toString), row(1).toString))
    val movie_years_count_collect = movie_years_count_rdd.collect()
    val movie_years_count_collect_sort = movie_years_count_collect.sortBy(_._1)
    return movie_years_count_collect_sort

  }
  def main(args: Array[String]) {

    val movie_years = MovieData.getMovieYearsCountSorted()
    for( a <- 0 to (movie_years.length -1)){
      /**
        (1900,1)
        (1922,1)
        (1926,1)
        (1930,1)
        (1931,1)
        (1932,1)
        (1933,2)
        (1934,4)
        (1935,4)
        (1936,2)
        (1937,4)
        **/
      println(movie_years(a))
    }

  }



}