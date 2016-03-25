/**
 * Created by Kkusoorkar-MBP15 on 3/18/16.
 */

import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SQLContext, SaveMode}


object RollUpReportsByMerchant {

    def main (args: Array[String]){
      println("Beginning RollUp Reporting By Merchant...")

      val conf = new SparkConf()
        .setAppName("RollUpReportsByMerchant")

      val sc = SparkContext.getOrCreate(conf)
      val sqlContext = new HiveContext(sc)

      //1st , RollUp transactions by merchant by day and save to dailytxns_bymerchant table
      sqlContext.sql("""CREATE TEMPORARY TABLE temp_transactions
      USING org.apache.spark.sql.cassandra
      OPTIONS (
       table "transactions",
       keyspace "rtfap",
       cluster "Test Cluster",
       pushdown "true"
      )""")

      val rollup1= sqlContext.sql("select txn_time, cc_no, amount, cc_provider, items, location, merchant, notes, status, txn_id, user_id, tags, int(translate(string(date(txn_time)),'-','')) as day from temp_transactions")

      rollup1.write.format("org.apache.spark.sql.cassandra")
        .mode(SaveMode.Overwrite)
        .options(Map("keyspace" -> "rtfap", "table" -> "dailytxns_bymerchant"))
        .save()

      sqlContext.sql("""CREATE TEMPORARY TABLE temp_dailytxns_bymerchant
      USING org.apache.spark.sql.cassandra
      OPTIONS (
       table "dailytxns_bymerchant",
       keyspace "rtfap",
       cluster "Test Cluster",
       pushdown "true"
      )""")

      //2nd, Do the aggregations for daily totals etc. , Save to Static columns in the dailytxns_bymerchant table.
      sqlContext.udf.register("now", () => System.currentTimeMillis)
      val rollup2= sqlContext.sql("select date(timestamp(now())) as txn_time,  string(now()) as txn_id, merchant, day, count(*) as total_count, sum(amount) as total_amount, min(amount) as min_amount, max(amount) as max_amount from temp_dailytxns_bymerchant group by merchant, day")

      rollup2.write.format("org.apache.spark.sql.cassandra")
        .mode(SaveMode.Append)
        .options(Map("keyspace" -> "rtfap", "table" -> "dailytxns_bymerchant"))
        .save()

      println("Completed RollUps By Merchant...")
    }
}
