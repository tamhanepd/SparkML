package com.sundogsoftware.spark

import org.apache.log4j._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{IntegerType, StringType, StructType}

/** Find the superhero with the most co-appearances. */
object LeastPopularSuperheroesDataset {

  case class SuperHeroNames(id: Int, name: String)
  case class SuperHero(value: String)
 
  /** Our main function where the action happens */
  def main(args: Array[String]) {
   
    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // Create a SparkSession using every core of the local machine
    val spark = SparkSession
      .builder
      .appName("MostPopularSuperhero")
      .master("local[*]")
      .getOrCreate()

    // Create schema when reading Marvel-names.txt
    val superHeroNamesSchema = new StructType()
      .add("id", IntegerType, nullable = true)
      .add("name", StringType, nullable = true)

    // Build up a hero ID -> name Dataset
    import spark.implicits._
    val names = spark.read
      .schema(superHeroNamesSchema)
      .option("sep", " ")
      .csv("data/Marvel-names.txt")
      .as[SuperHeroNames]

    val lines = spark.read
      .text("data/Marvel-graph.txt")
      .as[SuperHero]

    val connections = lines
      .withColumn("id", split(col("value"), " ")(0))
      .withColumn("connections", size(split(col("value"), " ")) - 1)
      .groupBy("id").agg(sum("connections").alias("connections"))

    val leastPopular = connections
        .sort($"connections".asc)

    val leastPopularIds = leastPopular
      .filter($"connections" === leastPopular.first()(1))

    val minConnections = leastPopular.first()(1)

    val leastPopularSuperheroNames = names.join(leastPopularIds, names("id") === leastPopularIds("id"), "inner")

    println(s"The following superheroes have only $minConnections connection:")
    leastPopularSuperheroNames.select("name").show()

    // Instructor solution:
    // val minConnectionCount = connections.agg(min("connections)).first().getlong(0)
    // val minConnections = connections.filter($"connections" === minConnectionCount)
    // val minConnectionsWithNames = minConnections.join(names, usingColumn = "id")

  }
}
