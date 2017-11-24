# Datastax Fraud Prevention Demo - Batch Analytics

## Running Batch Roll-up Reports

Redesigned and rewritten from an original creation by Kunal Kusoorkar.

This project consists of two elements:
   
* Transactions by merchant roll-up
* Aggregates by credit card roll-up

The roll-up jobs will use the data in the Transactions table to generate aggregates by merchant and by credit card. 
These jobs can be run as frequently as necessary depending on the currency of the information required e.g. every 10 minutes or every hour etc.

### dailytxns_bymerchant
The dailytxns_bymerchant table is the data in the transactions table, but keyed on merchant and day with a clustering column ascending on credit card number.
There are four static columns - total_amount, min_amount, max_amount and total_count. Static columns are a powerful feature of Cassandra - the static values are static for each partition key (merchant and day). This allows us to track daily transactions for each merchant at both a transaction level and (using the static columns) at a summary level by merchant by day.

### aggregate tables 
The aggregate tables are rolled up by credit card e.g. hourlyaggregates_bycc:
```
+----------------+----------+------------+----------+----------+-----------+
|           cc_no|      hour|total_amount|min_amount|max_amount|total_count|
+----------------+----------+------------+----------+----------+-----------+
|1234123412341234|2016030911|       200.0|     200.0|     200.0|          1|
|1234123412341234|2016031721|       200.0|     200.0|     200.0|          1|
|1234123412341235|2016030911|      1200.0|     400.0|     800.0|          2|
|1234123412341236|2016030911|       750.0|     750.0|     750.0|          1|
|1234123412341237|2016030912|      1500.0|    1500.0|    1500.0|          1|
+----------------+----------+------------+----------+----------+-----------+
```
versus e.g. yearlyaggregates_bycc:
```
+----------------+----+------------+----------+----------+-----------+
|           cc_no|year|total_amount|min_amount|max_amount|total_count|
+----------------+----+------------+----------+----------+-----------+
|1234123412341234|2016|       400.0|     200.0|     200.0|          2|
|1234123412341235|2016|      1200.0|     400.0|     800.0|          2|
|1234123412341236|2016|       750.0|     750.0|     750.0|          1|
|1234123412341237|2016|      1500.0|    1500.0|    1500.0|          1|
+----------------+----+------------+----------+----------+-----------+
```


### Pre-requisites
The following components must be installed and available on your machine.

  1. Datastax Enterprise 5.1.4 installed and working in Search Analytics mode
  2. sbt
  3. An internet connection is required to download sbt dependencies

  * If you havent already installed sbt (as root or use sudo) do this now:

> If you want to play with the sample Scala code interactively, you should install the DSE version of Zeppelin from the inestimable Duy Hai Doan here: https://drive.google.com/open?id=0B6wR2aj4Cb6wcm10ZmJYV1dzUzQ 
Once you have installed Zeppelin, start it with the ```zeppelin-daemon.sh start``` command. Then load the notebook entitled "RTFAP2 RUP BY Merchant". Setting up Zeppelin isn't a requirement for these excercises though, so you can skip and come back later if you want to try it out.

On MacOS:
```
$ brew install sbt
```
On Debian Linux:
```
echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 642AC823
apt-get update
apt-get install sbt
```

## Build the demo

  * You should have already created the Cassandra keyspaces and tables using the creates_and_inserts.cql script
  * Ensure that you've run the streaming transaction producer/consumer jobs before you run the roll-ups so that there is data in the Transaction table to be rolled up

> !! Remember - there is a TTL on the transactions table, so the data will gradually age out after 24 hours!! :)


OK, so go to the Rollup Reports directory from the main project directory RTFAP2:

    ```$ cd RollUpReports```
    
  * Update sbt:

    ```$ sbt update```
    
 * Clean up the build environment:

    ```$ sbt clean```
    
 * Build the rollup jobs with this command:

    ```$ sbt package```
    
    Make sure the build is successful:
    ```
    ...
   [warn] Multiple main classes detected.  Run 'show discoveredMainClasses' to see the list
   [success] Total time: 1 s, completed Dec 4, 2016 9:20:03 PM
    ```
You can ignore the ```Multiple main classes detected``` warning - there are two roll-up jobs contained in the code.

## Run the demo

This assumes you already have DSE up and running and configured correctly.

### Run the transactions by merchant roll up: 

```
./run_rollupbymerchant.sh
```

This will run the command:

```
dse spark-submit --class RollUpReportsByMerchant ./target/scala-2.11/rollupreports_2.11-1.0.jar
```
Output will be 
```
- Populating dailytxns_bymerchant
- Aggregating in dailytxns_bymerchant
Completed RollUps By Merchant
Shutting down...
```

> If you encounter a permissions problem with /tmp/hive run this command to grant the required access to the dse user: ```
$ sudo dse hadoop fs -chmod 777 /tmp/hive```

### Run the aggregates by credit card rollup: 

```
./run_rollupbycc.sh
```

This will run the command:
```
dse spark-submit --class RollUpReportsByCC ./target/scala-2.11/rollupreports_2.11-1.0.jar
```
Output will be 
```
Beginning RollUp Reporting By Credit Card number...
WARN  2017-11-21 13:37:35,329 org.apache.spark.SparkContext: Use an existing SparkContext, some configuration may not take effect.
 - 1. Populating hourlyaggregates_bycc
 - 2. Populating dailyaggregates_bycc
 - 3. Populating monthlyaggregates_bycc
 - 4. Populating yearlyaggregates_bycc
Completed RollUps By CC
Shutting down... 
```

When these jobs have completed you will be able to run the ReST queries that interrogate the rollup and aggregate tables.
