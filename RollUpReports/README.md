#Datastax Fraud Prevention Demo - Batch Analytics

##Running Batch Roll-up Reports

Based on an original creation by Kuna Kusoorkar.

This project consists of two elements:
   
* Transactions by merchant roll-up
* Aggregates by credit card roll-up

The roll-up jobs will use the data in the Transactions table to generate aggregates by merchant and by credit card. These jobs can be run as frequently as necessary depending on the currency of the information required e.g. every 10 minutes or every hour etc.

###Pre-requisites
The following components must be installed and available on your machine.

  1. Datastax Enterprise 5.0.3 installed and working in Search Analytics mode
  2. sbt
  3. An internet connection is required to download sbt dependencies

  * If you havent already installed sbt (as root or use sudo) do this now:

```
echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 642AC823
apt-get update
apt-get install sbt
```

## Build the demo

###In order to run this demo navigate to the project RollUpReports directory

  * You should have already created the Cassandra keyspaces and tables using the creates_and_inserts.cql script
  * Ensure that you've run the streaming transaction producer/consumer jobs before you run the roll-ups so that there is data in the Transaction table to be rolled up

* Build the rollup jobs with this command:

    ```sbt package```
    
    Make sure the build is successful:
    ```
   [warn] Multiple main classes detected.  Run 'show discoveredMainClasses' to see the list
   [success] Total time: 1 s, completed Dec 4, 2016 9:20:03 PM
    ```
You can ignore the "Multiple main classes detected" warning - there are two roll-up jobs contained in the code.

##Run the demo

This assumes you already have DSE up and running and configured correctly.

###Grant required access to the dse user
```
sudo dse hadoop fs -chmod 777 /tmp/hive
```

###Run the transactions by merchant roll up: 

```
./run_rollupbymerchant.sh
```

This will run the command:

```
dse spark-submit --class RollUpReportsByMerchant ./target/scala-2.10/rollupreports_2.10-1.0.jar
Beginning RollUp Reporting By Merchant...
Completed RollUps By Merchant...
```

###Run the aggregates by credit card rollup: 

```
./run_rollupbycc.sh
```

This will run the command:
```
dse spark-submit --class RollUpReportsByCC ./target/scala-2.10/rollupreports_2.10-1.0.jar
Beginning RollUp Reporting By CC...
Completed RollUps By CC...  
```

When these jobs have completed you will be able to run the ReST queries that interrogate the rollup and aggregate tables.
