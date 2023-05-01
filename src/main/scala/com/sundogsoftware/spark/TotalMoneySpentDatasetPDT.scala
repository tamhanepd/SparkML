package com.sundogsoftware.spark

import org.apache.spark.sql.types.{DoubleType, FloatType, IntegerType, StringType, StructType}
import org.apache.log4j._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

/** Find the minimum temperature by weather station */
object TotalMoneySpentDataset {

  case class Money(customerID: Int, itemID: Int, moneySpent: Double)

  /** Our main function where the action happens */
  def main(args: Array[String]) {

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // Create a SparkSession using every core of the local machine
    val spark = SparkSession
      .builder
      .appName("TotalMoneySpent")
      .master("local[*]")
      .getOrCreate()

    val moneySchema = new StructType()
      .add("customerID", IntegerType, nullable = true)
      .add("itemID", IntegerType, nullable = true)
      .add("moneySpent", DoubleType, nullable = true)

    // Read the file as dataset
    import spark.implicits._
    val ds = spark.read
      .schema(moneySchema)
      .csv("data/customer-orders.csv")
      .as[Money]

    // Aggregate by customerID
    val moneySpentbyCustomerID = ds
      .groupBy("customerID")
      .agg(round(sum("moneySpent"), 2)
        .alias("totalSpent"))

    // Collect, format, and print the results
    val results = moneySpentbyCustomerID.collect()

    moneySpentbyCustomerID.show(moneySpentbyCustomerID.count.toInt)

    for (result <- results) {
      val customerID = result(0)
      val moneySpent = result(1).asInstanceOf[Float]
      val formattedTemp = f"$moneySpent%.2f"
      println(s"$customerID spent: $formattedTemp")
    }
  }
}