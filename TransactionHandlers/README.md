#Datastax Fraud Prevention Demo - Streaming Analytics

##Creating and Consuming Transactions

Based on an original creation by Cary Bourgeois. I'll use Cary's original description here as the overall functionality is unchanged.

This project consists of two elements:
   
* Transaction Producer
* Transaction Consumer

The transaction producer is a Scala application that leverages the Akka framework (lightly) to generate pseudo-random credit card transactions, and then place those transactions on a Kafka queue. There is some fairly trivial yet fun logic for spreading the transactions proportionally across the top 100 retailers in the world based on total sales. It does a similar thing for the countries of the world based on population. This is here strictly to make pretty graphs.

The Transaction consumer, also written in Scala, is a Spark streaming job. This job performs two main tasks. First, it consumes the messages put on the Kafka queue. It then parses those messages, evalutes the data and flags each transaction as "APPROVED" or "REJECTED". This is the place in the job where more application specific (or complex) logic should be placed. In a real world application I could see a scoring model used to decide if a transaction should be accepted or rejected. You would also want to implement things like black-list lookups and that sort of thing. Finally, once evaluated, the records are then written to the Datastax/Cassandra table.

The second part of the Spark consumer job counts the number of records processed each minute, and stores that data to an aggregates table. The only unique aspect of this flow is that the job also reads back from from this table and builds a rolling count of the data. The results can be displayed using the Node.js web service provided, for example:

<p align="left">
  <img src="txnchart.png"/>
</p>

##Demo tech set up

###Pre-requisites
The following components must be installed and available on your machine.

Please note, this demo is built using the 5.0.3 branch of Datastax Enterprise - Spark Direct Streams (Kafka in this demo) support is much improved in DSE 4.8+

  1. Datastax Enterprise 5.0.3
  2. Apache Kafka 0.10.1.0, I used the Scala 2.10 build
  3. git
  4. sbt
  5. An internet connection

##Getting Started with Kafka

###1. Download Apache Kafka

Kafka can be downloaded from this URL: [http://kafka.apache.org/downloads.html](http://kafka.apache.org/downloads.html)

Download and install the binary version for Scala 2.10 - you can use wget or curl to download to the server e.g:
```
curl --remote-name http://mirror.ox.ac.uk/sites/rsync.apache.org/kafka/0.10.1.0/kafka_2.10-0.10.1.0.tgz
```

###2. Install Apache Kafka

Once downloaded you will need to extract the file. It will create a folder/directory - you can then move this to a location of your choice.

```
$ gunzip kafka_2.10-0.10.1.0.tgz
$ tar xvf kafka_2.10-0.10.1.0.tar
$ rm kafka_2.10-0.10.1.0.tar
```

Move the kafka directory tree to your preferred location, e.g.:

```
$ mv kafka_2.10-0.10.1.0 /Sogtware/Kafka
$ KAFKA_HOME=/Software/Kafka/kafka_2.10-0.10.1.0 export KAFKA_HOME
```

For a more permanent installation you might want to move it to e.g. /usr/share:

```
$ sudo mv kafka_2.10-0.10.1.0 /usr/share
$ KAFKA_HOME=/usr/share/kafka_2.10-0.10.1.0 export KAFKA_HOME
```


###3. Start ZooKeeper and Kafka

3.a. Start local copy of zookeeper (in its own terminal or use nohup):

```
$ cd $KAFKA_HOME
$ ./bin/zookeeper-server-start.sh config/zookeeper.properties`
```
 
For example:
```
$ cd $KAFKA_HOME
$ nohup ./bin/zookeeper-server-start.sh config/zookeeper.properties &
```

3.b. Start local copy of Kafka (in its own terminal or use nohup):

```
$ cd $KAFKA_HOME
$ ./bin/kafka-server-start.sh config/server.properties`
```

For example (using a different output file to the one created by the Zookeeper process):
```
$ cd $KAFKA_HOME
$ nohup ./bin/kafka-server-start.sh config/server.properties > nohup2.out 2>&1 &
```

###4. Prepare a message topic for use.

4.a. Create the topic we will use for the demo

  * `$KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --create --replication-factor 1 --partitions 1 --topic NewTransactions`

For example:
```
$ cd $KAFKA_HOME
$ ./bin/kafka-topics.sh --zookeeper localhost:2181 --create --replication-factor 1 --partitions 1 --topic NewTransactions
Created topic "NewTransactions".
```

4.b. Validate the topic was created:

  * `$KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --list`

For example:
```
$ cd $KAFKA_HOME
$ ./bin/kafka-topics.sh --zookeeper localhost:2181 --list
NewTransactions
```

##Some more useful Kafka commands

Delete the topic. (Note: The server.properties file must contain `delete.topic.enable=true` for this to work):

  * `$KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --delete --topic NewTransactions`
  
Show all of the messages in a topic from the beginning:

  * `$KAFKA_HOME/bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic NewTransactions --from-beginning`
  
Describe the 'NewTransactions' Topic:

  * `$KAFKA_HOME/bin/kafka-topics.sh --describe --zookeeper localhost:2181 --topic NewTransactions
  Topic:NewTransactions	PartitionCount:1	ReplicationFactor:1	Configs:retention.ms=1680000
	Topic: NewTransactions	Partition: 0	Leader: 0	Replicas: 0	Isr: 0`
	
Set message retention for 1 hour:

By default Kafka will retain messages in the queue for 7 days - to change retention to e.g. 1 hour (360000 milliseconds) 

> Kafka does not automatically remove messages from the queue after they have been read. This allows for the possibility of recovery in the event that the consumer dies

  * `$KAFKA_HOME/bin/kafka-configs.sh --zookeeper localhost:2181 --entity-type topics --alter --add-config retention.ms=3600000 --entity-name NewTransactions
  Updated config for entity: topic 'NewTransactions'.`

Display topic configuration details:

  * `$KAFKA_HOME/bin/kafka-configs.sh --zookeeper localhost:2181 --describe --entity-name NewTransactions --entity-type topics
  Configs for topic 'NewTransactions' are retention.ms=3600000`


## Build the demo

###In order to run this demo navigate to the project TransactionHandlers directory

  * You should have already created the Cassandra keyspaces and tables using the creates_and_inserts.cql script
  * If you havent yet installed sbt (as root or use sudo) do this now:
  ```
echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 642AC823
apt-get update
apt-get install sbt
```

1. Build the Producer with this command:
  
    ```sbt producer/package```
    
    Make sure the build is successful:
    ```
    [info] Done packaging.
    [success] Total time: 44 s, completed Nov 21, 2016 10:09:12 PM
    ```
      
2. Build the Consumer with this command:
  
    ```sbt consumer/package```
    
    Make sure the build is successful:
    ```
    [info] Done packaging.
    [success] Total time: 32 s, completed Nov 21, 2016 10:10:32 PM
    ```
  
##Run the demo

This assumes you already have Kafka and DSE up and running and configured as in the steps above.

###Start the Transaction Producer

From the root directory of the project (`~/RTFAP2/TransactionHandlers`) start the producer app:
  
    `sbt producer/run`

After some initial output you will see card transactions being created and posted to Kafka:
```
[info] Set current project to transactionhandlers (in build file:/u02/dev/dse_dev/RTFAP/RTFAP2/TransactionHandlers/)
[info] Running TransactionProducer 
kafkaHost 127.0.0.1:9092
kafkaTopic NewTransactions
maxNumTransPerWait 5
waitMillis 500
runDurationSeconds -1
[DEBUG] [11/22/2016 13:39:37.061] [run-main-0] [EventStream(akka://TransactionProducer)] logger log1-Logging$DefaultLogger started
[DEBUG] [11/22/2016 13:39:37.063] [run-main-0] [EventStream(akka://TransactionProducer)] Default Loggers started
...
19845 Transactions created.
(cc_no=,6557000040986661, txn_time=,2016-12-03 00:37:57.407, items=,Item_3240->237.76, amount=,237.76)
(cc_no=,9963000035510891, txn_time=,2016-12-03 00:37:57.407, items=,Item_70347->463.65,Item_92354->797.65,Item_29626->76.39, amount=,1337.70)
(cc_no=,8744000094938745, txn_time=,2016-12-03 00:37:57.407, items=,Item_52058->804.00,Item_56871->536.83,Item_50450->543.21, amount=,1884.04)
(cc_no=,7554000058844783, txn_time=,2016-12-03 00:37:57.407, items=,Item_15281->824.13, amount=,824.13)
(cc_no=,6835000071495639, txn_time=,2016-12-03 00:37:57.407, items=,Item_596->172.98,Item_81946->517.99,Item_23021->739.64, amount=,1430.60)
19849 Transactions created.
(cc_no=,2001000046589052, txn_time=,2016-12-03 00:37:57.908, items=,Item_80751->379.57,Item_61627->614.89,Item_76470->577.95,Item_22016->390.50, amount=,1962.91)
(cc_no=,9048000013833608, txn_time=,2016-12-03 00:37:57.909, items=,Item_16779->438.04,Item_97042->172.26,Item_91668->523.98, amount=,1134.29)
(cc_no=,9589000089111077, txn_time=,2016-12-03 00:37:57.909, items=,Item_59355->84.39,Item_7516->912.12,Item_63996->525.70, amount=,1522.21)
(cc_no=,7653000040508112, txn_time=,2016-12-03 00:37:57.909, items=,Item_88972->597.19,Item_29442->508.24, amount=,1105.43)
19851 Transactions created.
(cc_no=,3472000068224395, txn_time=,2016-12-03 00:37:58.409, items=,Item_13700->54.50,Item_4441->177.90,Item_31018->607.78, amount=,840.18)
(cc_no=,9470000056653610, txn_time=,2016-12-03 00:37:58.409, items=,Item_106->402.68,Item_19047->500.58,Item_33249->759.90, amount=,1663.16)
```

You can leave this process running as you wish.

###Start the Transaction Consumer
 
  1. Identify the location of the SparkMaster node:
  
  For DSE versions < 4.x:
  ```
  $ dsetool sparkmaster
  spark://127.0.0.1:7077
  ```
  For DSE 5.0.0 and above:
  ```
  dse client-tool spark master-address
  spark://127.0.0.1:7077
  ```
  2. From the root directory of the project start the consumer app:
  
    ```dse spark-submit --master spark://[SparkMaster_IP]:7077 --packages org.apache.spark:spark-streaming-kafka_2.10:1.6.2 --class TransactionConsumer consumer/target/scala-2.10/consumer_2.10-0.1.jar```

  After some initial output you will see records being consumed from Kafka by Spark:
  
  ```
  Ivy Default Cache set to: /home/dse/.ivy2/cache
  The jars for the packages stored in: /home/dse/.ivy2/jars
  :: loading settings :: url = jar:file:/usr/share/dse/spark/lib/ivy-2.4.0.jar!/org/apache/ivy/core/settings/ivysettings.xml
  org.apache.spark#spark-streaming-kafka_2.10 added as a dependency
  :: resolving dependencies :: org.apache.spark#spark-submit-parent;1.0
  6 rows processed...
  +----------------+-----------+----+-----+---+----+---+--------------------+--------------------+----------------+--------+-------+-------+--------+---------+
  |           cc_no|cc_provider|year|month|day|hour|min|            txn_time|              txn_id|        merchant|location|country| amount|  status|date_text|
  +----------------+-----------+----+-----+---+----+---+--------------------+--------------------+----------------+--------+-------+-------+--------+---------+
  |1567000016674783|       1567|2016|   11|  3|   0| 37|2016-12-03 00:37:...|e4425655-348c-47d...|Lowe's Companies|        |     KH|1153.26|APPROVED| 20161103|
  |8797000077172306|       8797|2016|   11|  3|   0| 37|2016-12-03 00:37:...|abeae2c2-173c-429...|       SUPERVALU|        |     DE| 812.28|APPROVED| 20161103|
  |5034000081986740|       5034|2016|   11|  3|   0| 37|2016-12-03 00:37:...|afa129ee-6829-4b0...|  Dollar General|        |     IN|1324.22|REJECTED| 20161103|
  |5859000021039989|       5859|2016|   11|  3|   0| 37|2016-12-03 00:37:...|ef372bb1-dedf-421...|          Costco|        |     ID| 757.88|APPROVED| 20161103|
  +----------------+-----------+----+-----+---+----+---+--------------------+--------------------+----------------+--------+-------+-------+--------+---------+

  4 rows processed...
  +----------------+-----------+----+-----+---+----+---+--------------------+--------------------+---------------+--------+-------+-------+--------+---------+
  |           cc_no|cc_provider|year|month|day|hour|min|            txn_time|              txn_id|       merchant|location|country| amount|  status|date_text|
 +----------------+-----------+----+-----+---+----+---+--------------------+--------------------+---------------+--------+-------+-------+--------+---------+
  |5907000019173296|       5907|2016|   11|  3|   0| 37|2016-12-03 00:37:...|93c3741b-5132-49e...|     DineEquity|        |     EG| 896.46|APPROVED| 20161103|
  |7624000055927622|       7624|2016|   11|  3|   0| 37|2016-12-03 00:37:...|e7a09e38-543b-4bf...|     Albertsons|        |     FR|1944.73|APPROVED| 20161103|
  |5539000022858144|       5539|2016|   11|  3|   0| 37|2016-12-03 00:37:...|a2aefc67-c97c-48e...|Jack in the Box|        |     CN|2491.93|APPROVED| 20161103|
  +----------------+-----------+----+-----+---+----+---+--------------------+--------------------+---------------+--------+-------+-------+--------+---------+
  ```

  You can leave this running as you wish.

  3. At this point you can use cqlsh to check the number of rows in the Transactions table - you should see that there are records appearing as they are posted by the consumer process:

  ```
  cqlsh> select count(*) from rtfap.transactions;

   count
  -------
    13657
  ```

  4. Every 60 seconds you will also see the consumer process generate output similar to the following:
```
  Time=Sat Dec 03 00:37:44 GMT 2016
  +----+-----+---+----+------+-------------------+-----------------+-----------+----------------+-----------------+----------+---------------+
  |year|month|day|hour|minute|               time|approved_rate_min|ttl_txn_min|approved_txn_min| approved_rate_hr|ttl_txn_hr|approved_txn_hr|
  +----+-----+---+----+------+-------------------+-----------------+-----------+----------------+-----------------+----------+---------------+
  |2016|   12|  3|   0|    37|2016-12-03 00:37:44|94.55958549222798|        386|             365|95.27943966146213|     13706|          13059|
  +----+-----+---+----+------+-------------------+-----------------+-----------+----------------+-----------------+----------+---------------+
  ```

  This is real-time analysis of the approved vs. rejected transactions rate and percentage. These records are stored in the txn_count_min table, for example:
  
  ```
  cqlsh:rtfap> SELECT * FROM rtfap.txn_count_min WHERE solr_query = '{"q":"*:*",  "fq":"time:[NOW-1HOUR TO *]","sort":"time asc"}';

   year | month | day | hour | minute | approved_rate_hr | approved_rate_min | approved_txn_hr | approved_txn_min | solr_query | time                     | ttl_txn_hr | ttl_txn_min
  +-----+-------+-----+------+--------+------------------+-------------------+-----------------+------------------+------------+--------------------------+------------+-------------+
  2016 |    12 |   2 |   23 |     43 |          95.7958 |           95.7958 |             319 |              319 |       null | 2016-12-02 23:43:44+0000 |        333 |         333
  2016 |    12 |   2 |   23 |     44 |         94.86405 |          93.92097 |             628 |              309 |       null | 2016-12-02 23:44:44+0000 |        662 |         329
  2016 |    12 |   2 |   23 |     45 |         94.90695 |          94.98607 |             969 |              341 |       null | 2016-12-02 23:45:44+0000 |       1021 |         359
  2016 |    12 |   2 |   23 |     46 |         94.76015 |          94.31138 |            1284 |              315 |       null | 2016-12-02 23:46:44+0000 |       1355 |         334
  2016 |    12 |   2 |   23 |     47 |         94.72769 |          94.60916 |            1635 |              351 |       null | 2016-12-02 23:47:44+0000 |       1726 |         371
  2016 |    12 |   2 |   23 |     48 |         94.77218 |          94.98607 |            1976 |              341 |       null | 2016-12-02 23:48:44+0000 |       2085 |         359
  2016 |    12 |   2 |   23 |     49 |         94.65021 |          93.91304 |            2300 |              324 |       null | 2016-12-02 23:49:44+0000 |       2430 |         345
  2016 |    12 |   2 |   23 |     50 |         94.53041 |          93.69628 |            2627 |              327 |       null | 2016-12-02 23:50:44+0000 |       2779 |         349
  2016 |    12 |   2 |   23 |     51 |         94.56522 |          94.84241 |            2958 |              331 |       null | 2016-12-02 23:51:44+0000 |       3128 |         349
  2016 |    12 |   2 |   23 |     52 |         94.61231 |                95 |            3319 |              361 |       null | 2016-12-02 23:52:44+0000 |       3508 |         380
  2016 |    12 |   2 |   23 |     53 |          94.5497 |          93.91304 |            3643 |              324 |       null | 2016-12-02 23:53:44+0000 |       3853 |         345
  2016 |    12 |   2 |   23 |     54 |         94.64328 |          95.62842 |            3993 |              350 |       null | 2016-12-02 23:54:44+0000 |       4219 |         366
  2016 |    12 |   2 |   23 |     55 |         94.65381 |          94.78261 |            4320 |              327 |       null | 2016-12-02 23:55:44+0000 |       4564 |         345
  2016 |    12 |   2 |   23 |     56 |         94.63454 |              94.4 |            4674 |              354 |       null | 2016-12-02 23:56:44+0000 |       4939 |         375
  2016 |    12 |   2 |   23 |     57 |         94.67097 |          95.20958 |            4992 |              318 |       null | 2016-12-02 23:57:44+0000 |       5273 |         334
  2016 |    12 |   2 |   23 |     58 |         94.60566 |          93.60465 |            5314 |              322 |       null | 2016-12-02 23:58:44+0000 |       5617 |         344
  2016 |    12 |   2 |   23 |     59 |         94.63087 |          95.04373 |            5640 |              326 |       null | 2016-12-02 23:59:44+0000 |       5960 |         343
  2016 |    12 |   3 |    0 |      0 |         96.73913 |          96.73913 |             356 |              356 |       null | 2016-12-03 00:00:44+0000 |        368 |         368
  2016 |    12 |   3 |    0 |      1 |         96.32249 |          95.87021 |             681 |              325 |       null | 2016-12-03 00:01:44+0000 |        707 |         339
  2016 |    12 |   3 |    0 |      2 |         96.13936 |          95.77465 |            1021 |              340 |       null | 2016-12-03 00:02:44+0000 |       1062 |         355
  2016 |    12 |   3 |    0 |      3 |         96.11033 |          96.02273 |            1359 |              338 |       null | 2016-12-03 00:03:44+0000 |       1414 |         352
  ```

  The txn_count_min table will be used to service the D3 chart displayed at the top of this page.

