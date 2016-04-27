#Datastax Frad Prevention Demo
##How to Technically run the Demo

In order to run this demo, It is assumed that you have the following installed and available on your local system. (Please note, this demo is built using the 4.8.x branch of Datastax Enterprise as 5.0 was not GA. IN the future you should use Datastax Enterprise 5.x as Spark Direct Streams (Kafka in this demo) support is much improved in this version.)

  1. Datastax Enterprise 4.8.x
  2. Apache Kafka 0.9.0.1, I used the Scala 2.10 build
  3. git
  4. sbt

##Getting Started with Kafka
Use the steps below to setup up a local instance of Kafka for this example. THis is based off of apache-kafka_2.10-0.9.0.1.

###1. Locate and download Apache Kafka

Kafka can be located at this URL: [http://kafka.apache.org/downloads.html](http://kafka.apache.org/downloads.html)

You will want to download and install the binary version for Scala 2.10.

###2. Install Apache Kafka

Once downloaded you will need to extract the file. It will create a folder/directory. Move this to a location of your choice.

###3. Start ZooKeeper and Kafka

Start local copy of zookeeper

  * `<kafka home dir>bin/zookeeper-server-start.sh config/zookeeper.properties`

Start local copy of Kafka

  * `<kafka home dir>bin/kafka-server-start.sh config/server.properties`

###4. Prepare a message topic for use.

Create the topic we will use for the demo

  * `<kafka home dir>bin/kafka-topics.sh --zookeeper localhost:2181 --create --replication-factor 1 --partitions 1 --topic NewTransactions`

Validate the topics were created. 

  * `<kafka home dir>bin/kafka-topics.sh --zookeeper localhost:2181 --list`
  
##A Couple of other useful Kafka commands

Delete the topic. (Note: The server.properties file must contain `delete.topic.enable=true` for this to work)

  * `<kafka home dir>bin/kafka-topics.sh --zookeeper localhost:2181 --delete --topic NewTransactions`
  
Show all of the messages in a topic from the beginning

  * `<kafka home dir>bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic NewTransactions --from-beginning`
  
#Getting Started with Local DSE/Cassandra

If you intend to use a cassandra cluster for this demo you will need to have started that cluster with Spark enabled. Please see the documentation here: [https://docs.datastax.com/en/datastax_enterprise/4.6/datastax_enterprise/spark/sparkStart.html](https://docs.datastax.com/en/datastax_enterprise/4.6/datastax_enterprise/spark/sparkStart.html)

###1. Download and install Datastax Enterprise v4.8.x

  * `https://academy.datastax.com/downloads/welcome`

###2. Starting DSE tarball install on the local OSX or Linux machine

  * `dse cassandra -k -s -Dcassandra.logdir=~/Datastax/data/cassandra/log`
  
###3. (Optional) Starting the ODBC driver in DSE tarball install on Local OSX or Linux
 
If you would like to access Cassandra Table using SparkSQL you will need to start the SparkSQL Thirft Server. Since we are doing this on a laptop want to limit the resources the thrift server consumes.

  * `dse start-spark-sql-thriftserver --conf spark.cores.max=2`
  
##Getting and running the demo

###In order to run this demo you will need to download the source from GitHub.

  * Navigate to the directory where you would like to save the code.
  * Execute the following command:
  
  
       `git clone https://github.com/CaryBourgeois/DatastaxFraudPreventionDemo.git`
  
###To build the demo

  * Create the Cassandra Keyspaces and Tables using the `CreateTables.cql` script
  * Navigate to the root directory of the project where you downloaded
  * Build the Producer with this command:
  
    `sbt producer/package`
      
  * Build the Consumer with this command:
  
    `sbt consumer/package`
  
###To run the demo

This assumes you already have Kafka and DSE up and running and configured as in the steps above.

  * From the root directory of the project start the producer app 
  
    `sbt producer/run`
    
  
  * From the root directory of the project start the consumer app
  
    `dse spark-submit --master spark://127.0.0.1:7077 --packages org.apache.spark:spark-streaming-kafka_2.10:1.4.1 --class com.datastax.demo.fraudprevention.TransactionConsumer consumer/target/scala-2.10/consumer_2.10-0.1.jar`
    
  