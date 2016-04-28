/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.sql.Timestamp
import java.util.{Calendar, GregorianCalendar}

import com.typesafe.config.ConfigFactory
import kafka.serializer.StringDecoder
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{SQLContext, SaveMode}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Minutes, Seconds, StreamingContext, Time}
import org.apache.spark.{SparkConf, SparkContext}

// This implementation uses the Kafka Direct API supported in Spark 1.4+
object TransactionConsumer extends App {

  /*
   * Get runtime properties from application.conf
   */
  val systemConfig = ConfigFactory.load()

  val appName = systemConfig.getString("TransactionConsumer.sparkAppName")

  val kafkaHost = systemConfig.getString("TransactionConsumer.kafkaHost")
  val kafkaDataTopic = systemConfig.getString("TransactionConsumer.kafkaDataTopic")

  // this does not work in this example at this point as these variables appear to be out of scope for the
  // map portion of the forEachRDD
  //
  val pctTransactionToDecline = systemConfig.getString("TransactionConsumer.pctTransactionToDecline")

  val dseKeyspace = systemConfig.getString("TransactionConsumer.dseKeyspace")
  val dseDetailTable = systemConfig.getString("TransactionConsumer.dseDetailTable")
  val dseAggTable = systemConfig.getString("TransactionConsumer.dseAggTable")

  val conf = new SparkConf()
    .set("spark.cores.max", "2")
    .set("spark.executor.memory", "2048M")
    .setAppName(appName)
  val sc = SparkContext.getOrCreate(conf)

  val sqlContext = SQLContext.getOrCreate(sc)
  import sqlContext.implicits._

  val ssc = new StreamingContext(sc, Seconds(1))
  ssc.checkpoint(appName)

  val kafkaParams = Map[String, String]("metadata.broker.list" -> kafkaHost)
  val kafkaTopics = Set(kafkaDataTopic)

  val kafkaStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, kafkaTopics)

  case class Transaction(cc_no:String,
                         cc_provider: String,
                         year: Int,
                         month: Int,
                         day: Int,
                         hour: Int,
                         min: Int,
                         txn_time: Timestamp,
                         txn_id: String,
                         merchant: String,
                         location: String,
                         country: String,
//                         items: Map[String, Double],
                         amount: Double,
                         status: String,
                         date_test: String)

  case class TransCount(status: String)

  /*
   * This stream handles the immediate stream of data to the DB
   */
  kafkaStream.window(Seconds(1), Seconds(1))
    .foreachRDD {
      /*
       * This section down to the .toDF call is where the records from Kafka are consumed and parsed
       */
      (message: RDD[(String, String)], batchTime: Time) => {
        val df = message.map {
          case (k, v) => v.split(";")
        }.map(payload => {
          val cc_no = payload(0)
          val cc_provider = payload(1)

          val txn_time = Timestamp.valueOf(payload(2))
          val calendar = new GregorianCalendar()
          calendar.setTime(txn_time)

          val year = calendar.get(Calendar.YEAR)
          val month = calendar.get(Calendar.MONTH)
          val day = calendar.get(Calendar.DAY_OF_MONTH)
          val hour = calendar.get(Calendar.HOUR)
          val min = calendar.get(Calendar.MINUTE)

          val txn_id = payload(3)
          val merchant = payload(4)
          val location = payload(5)
          val country = payload(6)

          //
          // not including items as the map data type get resolved in the search engine as a dynamic field
          // which will eventually blow out the Solr index from a sizing perspective.
          //val items = payload(6).split(",").map(_.split("->")).map { case Array(k, v) => (k, v.toDouble) }.toMap
          //
          val amount = payload(8).toDouble

          //
          // In a real app this sould need to be updated to include more evaluation rules.
          //
          val initStatus = payload(9).toInt
          val status = if (initStatus < 5) s"REJECTED" else s"APPROVED"

          val date_text = f"$year%04d$month%02d$day%02d"

          Transaction(cc_no, cc_provider, year, month, day, hour, min, txn_time, txn_id, merchant, location, country, amount, status, date_text)
        }).toDF("cc_no", "cc_provider", "year", "month", "day", "hour", "min","txn_time", "txn_id", "merchant", "location", "country", "amount", "status", "date_text")

        /*
         * The coolness of the spark connector. Super simple to write out the records to DSE/Cassandra
         */
        df
          .write
          .format("org.apache.spark.sql.cassandra")
          .mode(SaveMode.Append)
          .options(Map("keyspace" -> dseKeyspace, "table" -> dseDetailTable))
          .save()

//        df.show(5)
        println(s"${df.count()} rows processed.")
      }
    }


  /*
   * This stream handles the one hour roll up every minute
   */
  kafkaStream.window(Minutes(1), Seconds(60))
    .foreachRDD {
      /*
       * Here we take the records and parse just the last value to be able to count them.
       * NOTE: we reapply the score here which is hugely inefficinet and need to be worked out in a btter way.
       */
      (message: RDD[(String, String)], batchTime: Time) => {
        val df = message.map {
          case (k, v) => v.split(";")
        }.map(payload => {
          val initStatus = payload(9).toInt
          val status = if (initStatus < 5) s"REJECTED" else s"APPROVED"

          TransCount(status)
        }).toDF("status")


        /*
         * The next several section set up all the data arithmetic so we can query correctly query the
         * aggregate table and write back to it.
         */
        val timeInMillis = System.currentTimeMillis()

        val currCal = new GregorianCalendar()
        currCal.setTime(new Timestamp(timeInMillis))

        val year = currCal.get(Calendar.YEAR)
        val month = currCal.get(Calendar.MONTH)
        val day = currCal.get(Calendar.DAY_OF_MONTH)
        val hour = currCal.get(Calendar.HOUR)
        val min = currCal.get(Calendar.MINUTE)

        val prevCal = new GregorianCalendar()
        prevCal.setTime(new Timestamp(timeInMillis))
        prevCal.add(Calendar.MINUTE, -1)

        val prevYear = prevCal.get(Calendar.YEAR)
        val prevMonth = prevCal.get(Calendar.MONTH)
        val prevDay = prevCal.get(Calendar.DAY_OF_MONTH)
        val prevHour = prevCal.get(Calendar.HOUR)
        val prevMin = prevCal.get(Calendar.MINUTE)

        /*
         * In this section we we count the records in the resulting Dataframe
         */
        val totalTxnMin = df.count()
        val approvedTxnMin = df.filter("status = 'APPROVED'").count()
        val pctApprovedMin = if (totalTxnMin > 0) ((approvedTxnMin/totalTxnMin.toDouble)*100.0) else 0.0

        /*
         * Read from the aggregate table to get the value from the previous minute.
         */
        val dfPrev = sqlContext
          .read
          .format("org.apache.spark.sql.cassandra")
          .options(Map("keyspace" -> dseKeyspace, "table" -> dseAggTable, "spark.cassandra.input.consistency.level" -> "LOCAL_QUORUM"))
          .load()

        val result = dfPrev
          .filter(s"year = ${prevYear} and month = ${prevMonth} and day = ${prevDay} and hour = ${prevHour} and minute = ${prevMin}")
          .select("ttl_txn_hr", "approved_txn_hr")


        val totalTxnHr = totalTxnMin + (if (result.count() > 0) result.first.getInt(0) else 0)
        val approvedTxnHr = approvedTxnMin + (if (result.count() > 0) result.first.getInt(1) else 0)
        val pctApprovedHr = if (totalTxnHr > 0) ((approvedTxnHr/totalTxnHr.toDouble)*100.0) else 0.0


        /*
         * Make a new DataFrame with tour results
         */
        val dfCount = sc.makeRDD(Seq((year, month, day, hour, min, pctApprovedMin, totalTxnMin, approvedTxnMin, pctApprovedHr, totalTxnHr, approvedTxnHr)))
          .toDF("year", "month", "day", "hour", "minute", "approved_rate_min", "ttl_txn_min", "approved_txn_min", "approved_rate_hr", "ttl_txn_hr", "approved_txn_hr")

        /*
         * show and write the results out to the aggregate table.
         */
        dfCount.show()
        dfCount
          .write
          .format("org.apache.spark.sql.cassandra")
          .mode(SaveMode.Append)
          .options(Map("keyspace" -> dseKeyspace, "table" -> dseAggTable, "spark.cassandra.output.consistency.level" -> "LOCAL_QUORUM"))
          .save()

      }
    }


  ssc.start()
  ssc.awaitTermination()
}