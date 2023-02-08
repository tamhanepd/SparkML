package com.sundogsoftware.spark

import com.sundogsoftware.spark.MinTemperatures.parseLine
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkContext

object TotalMoneySpentPDT {

  /** Find money spent by each customer */
  def parseLine(line:String): (Int, Float) = {
    val fields = line.split(",")
    val customerID = fields(0).toInt
    val moneySpent = fields(2).toFloat
    (customerID, moneySpent)
  }

  /** Our main function where the action happens */
  def main(args: Array[String]) {

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // Create a SparkContext using the local machine
    val sc = new SparkContext("local", "TotalMoneySpentPDT")

    // Load each line of my book into an RDD
    val input = sc.textFile("data/customer-orders.csv")

    // Convert to (customerID, moneySpent) tuples
    val parsedLines = input.map(parseLine)

    // Sum money spent by each customer
    val rdd = parsedLines.mapValues(x => (x, 1)).reduceByKey( (x,y) => (x._1 + y._1, x._2 + y._2))

    // Map customer id to total money spent
    val moneySpent = rdd.mapValues(x => x._1)

    // Flip (customer, moneySpent) tuples to (moneySpent, customer) and then sort by key (moneySpent)
    val moneySpentSorted = moneySpent.map(x => (x._2, x._1)).sortByKey()

    // Collect the result
    val results = moneySpentSorted.collect()

    // Print the results, flipping the (count, word) results to word: count as we go.
    for (result <- results) {
      val customer = result._2
      val expense = result._1
      println(s"$customer: $expense")
    }

  }

}
