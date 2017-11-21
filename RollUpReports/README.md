# Datastax Fraud Prevention Demo - Batch Analytics

## Running Batch Roll-up Reports

Based on an original creation by Kunal Kusoorkar.

This project consists of two elements:
   
* Transactions by merchant roll-up
* Aggregates by credit card roll-up

The roll-up jobs will use the data in the Transactions table to generate aggregates by merchant and by credit card. 
These jobs can be run as frequently as necessary depending on the currency of the information required e.g. every 10 minutes or every hour etc.

The dailytxns_bymerchant table is the data in the transactions table but keyed on merchant and day with a clustering column ascending on credit card number.
There are four static columns for example total_amount, min_amount, max_amount - so these are static for each merchant and day. This allows us to track daily transactions for each merchant at both a transaction level and at a summary level by merchant by day.

### Pre-requisites
The following components must be installed and available on your machine.

  1. Datastax Enterprise 5.1.4 installed and working in Search Analytics mode
  2. sbt
  3. An internet connection is required to download sbt dependencies

  * If you havent already installed sbt (as root or use sudo) do this now:

> If you want to play with the sample Scala code interactively, you should install the DSE version of Zeppelin from the inestimabe Duy Hai Doan here: https://drive.google.com/open?id=0B6wR2aj4Cb6wcm10ZmJYV1dzUzQ 
Once you have installed zeppelin, start it with the ```zeppelin-daemon.sh start``` command. Then load the notebook entitled "RTFAP2 RUP BY Merchant". It'n not a requirement for these excercises though, so you can skip and come bak later.

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
