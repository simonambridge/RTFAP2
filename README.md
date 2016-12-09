# RTFAP2 - Real-time Fraud Analysis Platform

Based on the original RTFAP at https://github.com/simonambridge/RTFAP


##Use Case
A large bank wants to monitor its customer creditcard transactions to detect and deter fraud attempts. They want the ability to search and group transactions by credit card, period, merchant, credit card provider, amounts, status etc.

The client wants a REST API to return:  

- Identify all transactions tagged as fraudulent in the last minute/day/month/year.
- Identify all transactions tagged as fraudulent for a specific card.
- Report of transactions for a merchant on a specific day.
- Roll-up report of transactions by card and year.
- Search capability to search the entire transaction database by merchant, cc_no, amounts.
- The ratio of transaction success based on the first 6 digits of their credit card no.     
- The ratio of confirmed transactions against fraudulent transactions in the last minute.
- A moving ratio of approved transactions per minute, per hour.
- A count of approved transactions per minute, per hour.

They also want a graphic visualisation - a dashboard - of the data.

##Performance SLAs:
- The client wants an assurance that the data model can handle 1,000 transactions a second with stable latencies. The client currently handles accounts for over 15000 merchants and hopes to grow to 50,000 in a year.

![alt text] (architecture-1.png)

##Setup
DataStax Enterprise supplies built-in enterprise search functionality on Cassandra data that scales and performs in a way that meets the search requirements of modern Internet Enterprise applications. 
Using this search functionality allows the volume of transactions to grow without a loss in performance. DSE Search also allows for live indexing for improved index throughput, and reduced reader latency. 

We will need to start DSE in Analytics and Search mode
- Analytics to allow us to use the integrated Spark feature, and 
- Search mode to allow us to use the search functionalities that we need on top of Cassandra. 

We want to use Seaprch (Solr) and Analytics (Spark) so we need to delete the default datacentre and restart the cluster in SearchAnalytics mode.

1. Stop the service.
<pre>
$ sudo service dse stop
Stopping DSE daemon : dse                                  [  OK  ]
</pre>

2. Enable Solr and Spark by changing the flag from "0" to "1" in:
<pre>
$ sudo vi /etc/default/dse
</pre>
e.g.:
<pre>
# Start the node in DSE Search mode
SOLR_ENABLED=1
# Start the node in Spark mode
SPARK_ENABLED=1
</pre>

3. Delete the old (Cassandra-only) datacentre databases:
<pre>
$ sudo rm -rf /var/lib/cassandra/data/*
$ sudo rm -rf /var/lib/cassandra/saved_caches/*
$ sudo rm -rf /var/lib/cassandra/commitlog/*
$ sudo rm -rf /var/lib/cassandra/hints/*
</pre>

4. Remove the old system.log:
<pre>
$ sudo rm /var/log/cassandra/system.log 
rm: remove regular file `/var/log/cassandra/system.log'? y
</pre>

5. Restart DSE
<pre>
$ sudo service dse start
</pre>
<br>

###Solr (Search):
https://docs.datastax.com/en/datastax_enterprise/5.0/datastax_enterprise/srch/searchOverview.html

###Spark (Analytics):
https://docs.datastax.com/en/datastax_enterprise/5.0/datastax_enterprise/ana/analyticsTOC.html

##Install information

- Set up and install DataStax Enterprise with Spark and Solr enabled - this demo is based upon DSE 5.0.3.x with Spark 1.6.1 and Scala 2.10, using the packaged install method:
 - Ubuntu/Debian - https://docs.datastax.com/en/datastax_enterprise/5.0/datastax_enterprise/install/installDEBdse.html
 - Red Hat/Fedora/CentOS/Oracle Linux - https://docs.datastax.com/en/datastax_enterprise/5.0/datastax_enterprise/install/installRHELdse.html
- Note down the IP's of the node(s)

To setup your environment, you'll also need the following resources:
- Python 2.7
- Java 8
- For Red Hat, CentOS and Fedora, install EPEL (Extra Packages for Enterprise Linux).

Your URL's will be: 
- Opscenter => http://[DSE_NODE_IP]:8888/opscenter/index.html
- Spark Master => http://[DSE_NODE_IP]:7080/
- Solr admin page => http://[DSE_NODE_IP]:8983/solr/
- Node.js ReST interface => e.g. http://[DSE_NODE_IP]:3000
- Visual Dashboard => http://[DSE_NODE_IP]:8983/banana/#/dashboard

(where [DSE_NODE_IP] is the public IP address of your single node DSE installation)

Finally, clone this repo to a directory on the machine where you installed DSE.

##DataModel

We will need multiple tables to fulfill the above query patterns and workloads. De-normalization is a good thing with NoSQL databases - it allows you to optimise your Cassandra schema specifically to enable extremely fast queries.

For testing purposes we will use a single DC with one node and RF=1. 
For a production deployment we recommend a multi-datacenter Active-Active HA setup across geographical regions with RF=3.
```
create keyspace if not exists rtfap WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1' };
```

To create this keyspace and the tables described below, run the create schema script:
```
cqlsh <node name or IP>
cqlsh> source 'creates_and_inserts.cql'
```
This creates the following tables:

- Table Transactions - main transactions table
We will create a Solr index on transactions to support a variety of ReST API queries.

- Table hourlyaggregates_bycc - hourly roll-up of transactions by credit card

- Table dailyaggregates_bycc - daily roll-up of transactions by credit card

- Table monthlyaggregates_bycc - monthly roll-up of transactions by credit card

- Table yearlyaggregates_bycc - yearly roll-up of transactions by credit card

- Table dailytxns_bymerchant - daily roll-up of transactions by merchant

- Table txn_count_min - track transactions in a rolling window for analytics

We will create a Solr index on txn_count_min to enable flexible reporting and charting.

The create script also creates some sample data for example:

```
insert into rtfap.transactions (year, month, day, hour, min, txn_time, cc_no, amount, cc_provider, items, location, merchant, notes, status, txn_id, user_id, tags) VALUES ( 2016, 03, 09, 12, 30, '2016-03-09 12:30:00', '1234123412341237', 1500.0, 'AMEX', {'clothes':1500}, 'New York', 'Ann Taylor', 'frequent customer', 'Approved', '876302', 'caroline', {'HighValue'});
```

##Sample queries

We can now run CQL queries to look up all transactions for a given credit card (`cc_no`). 
The Transactions table is primarily write-oriented - it's the destination table for the streamed transactions and used for searches and we don't update the transactions once they have been written.
The table has a primary key and clustering columns so a typical query would look like this:
```
SELECT * FROM rtfap.transactions WHERE cc_no='1234123412341234' and year=2016 and month=3 and day=9;
```
The roll-up tables can also be queried - for example transactions for each merchant by day use the dailytxns_bymerchant table.
>The roll-up tables will be empty at this point - they get populated using the Spark batch and streaming analytics jobs that we run later.
```
SELECT * FROM rtfap.dailytxns_bymerchant where merchant='Nordstrom' and day=20160317;
```

##Searching Data in DSE

The above queries allow us to query on the partition key and some or all of the clustering columns in the table definition. To query more generically on the other columns we will use DSE Search to index and search our data.

###Create SOlr Cores
To do this we use the dsetool to create a Solr core based on the Transactions table. In a production environment we would only index the columns that we would want to query on (pre-requisite: run the CQL schema create script as described above to create the necessary tables).

To check that DSE Search is up and running sucessfully go to http://[DSE node]:8983/solr/

By default, when you automatically generate resources, existing data is not re-indexed so that you can check and customize the resources before indexing. To override the default and reindex existing data, use the reindex=true option, for example:

```
dsetool create_core rtfap.transactions generateResources=true reindex=true
```
Navigate to the Solr directory to create the Solr cores.
Run the script ```build_solr_indexes.sh``` to create the indexes. The script will run the following commands:
```
dsetool create_core rtfap.transactions generateResources=true reindex=true
dsetool create_core rtfap.txn_count_min generateResources=true reindex=true
dsetool reload_core rtfap.txn_count_min schema=./txn_count_min.xml reindex=true
```
Note that we're using a custom schema definition for the core that we're cresating on the txn_count_min table. The schema definition file txn_count_min.xml file contains the line:
```
<field indexed="true" multiValued="false" name="time" stored="true" type="TrieDateField" docValues="true" />
```
We're using the docValues option on the time column to allow us to sort on the time field.

###Using Solr with CQL
Now that we have created the Solr cores (lucene indexes) we can query our data in a number of ways. One is through cql using the solr_query column. The other is through a third party library like SolrJ which will interact with the search tool through ReST.

Below are the CQL Solr queries addressing some of the client requirements (&more) for searching the data in DSE:

Get counts (&records) of transactions faceted by merchant or cc_provider.
```
SELECT * FROM rtfap.transactions where solr_query='{"q":"*:*", "facet":{"field":"merchant"}}';
SELECT * FROM rtfap.transactions where solr_query='{"q":"*:*", "facet":{"field":"cc_provider"}}';
```

Get transactions by first 6 digits of cc_no (and perhaps filter query it further by the status!).
```
SELECT * FROM rtfap.transactions where solr_query='{"q":"cc_no: 123412*",  "fq":"status: Rejected"}';
```

Get all the transactions tagged as Fraudulent in the last day and last minute.
```
SELECT * FROM rtfap.transactions where solr_query = '{"q":"*:*", "fq":["txn_time:[NOW-1YEAR TO *]", "tags:Fraudulent"]}';
```
When we start generating some live data we'll be able to analyse up-to-date information e.g.
```
SELECT * FROM rtfap.transactions where solr_query = '{"q":"*:*", "fq":["txn_time:[NOW-1MINUTE TO *]", "tags:Fraudulent"]}';
```
These samples demonstrate that full, ad-hoc search on any of the transaction fields is possible including amounts, merchants etc.
We will use queries like this to build the ReST interface.

##Querying Data Using A ReST Web Interface

A ReSTful web interface provides an API for calling programs to query the data in Cassandra.
To use the web service, use the example url’s supplied - these will return a json representation of the data using the ReST service.

The sample queries are served by a web service written in Node.js. The code for this web service is provided in the repo.

Navigate to the restDSE directory:

> This Node.js application directory structure was created with express using the command ```$ express restDSE```

You can start the Node http server using the command ```DEBUG=restrtfap:* npm start``` alternatively use the simple shell script ```./run.sh```

At this point you will be able to run some of the solr queries shown below.

The queries demonstrate the use of both straightforward CQL and CQL-Solr but the roll-up tables have not been populated yet so these will return no data.

You can explore the list of provided ReST queries [here](http://github.com/simonambridge/RTFAP2/tree/master/Solr_Queries.md).

## Analyzing data using DSE Spark Analytics

DSE provides integration with Spark out of the box. This allows for analysis of data in-place on the same cluster where the data is ingested with workload isolation and without the need to ETL the data. The data ingested in a Cassandra only (OLTP) data center is automatically replicated to a logical data center of Cassandra nodes also hosting Spark Workers.

This tight integration between Cassandra and Spark offers huge value in terms of significantly reduced ETL complexity (no data movement to different clusters) and thus reducing time to insight from your data through a much less complex "cohesive lambda architecture" .

###Streaming Analytics

The streaming analytics element of this application is made up of two parts:

* The transaction producer is a Scala/Akka app that generates random transactions and then places those transactions on a Kafka queue. 
* The transaction consumer, also written in Scala, is a Spark streaming job that (a) consumes the messages put on the Kafka queue and then (b) parses those messages, evalutes the transaction status and then writes them to the Datastax/Cassandra table. It also generates rolling summary lines into the txn_count_min table every minute.

Streaming analytics code can be found under the directory 'TransactionHandlers' (pre-requisite: run the CQL schema create script as described above to create the necessary tables).

Follow the installation and set up instructions [here:](https://github.com/simonambridge/RTFAP2/tree/master/TransactionHandlers)

###Batch Analytics

Two Spark batch jobs have been included. 
* `run_rollupbymerchant.sh` provides a daily roll-up of all the transactions in the last day, by merchant. 
* `run_rollupbycc.sh` populates the hourly/daily/monthly/yearly aggregate tables by credit card, calculating the total_amount, avg_amount and total_count.

The roll up batch analytics code and submit scripts can be found under the directory 'RollUpReports' (pre-requisite: run the streaming analytics to populate the Transaction table with transaction data).

Follow the installation and set up instructions [here:](https://github.com/simonambridge/RTFAP2/tree/master/RollUpReports)


##Stress yaml

Running a cassandra-stress test with the appropriate YAML profile for the table helps show how DSE will perform in terms of latency and throughput for writes and reads to/from the system.

You can read more about using stress yamls to stress test a data model  [here](http://www.datastax.com/dev/blog/improved-cassandra-2-1-stress-tool-benchmark-any-schema) and [here](http://docs.datastax.com/en/cassandra/2.1/cassandra/tools/toolsCStress_t.html).

The stress YAML files are in the [stress_yamls directory](https://github.com/simonambridge/RTFAP2/tree/master/stress_yamls).

The stress tool will inject synthetic data so we will use a different table specifically for the stress testing.

To create the dummy table `txn_by_cc` navigate to the `stress_yamls` directory and run the create table script:

```
cqlsh <node name or IP>
cqlsh> source 'create_txn_by_cc_stress.cql'
```

The YAML tries to mirror real data, for example: month is a value between 1 and 12, year is between 2010 and 2016, credit card number is 16 characters in length, etc

An example of running the stress tool is shown below using [txn_by_cc_stress.yaml](https://github.com/simonambridge/RTFAP2/blob/master/stress_yamls/txn_by_cc_stress.yaml):

For inserts
```
cassandra-stress user profile=./txn_by_cc_stress.yaml ops\(insert=1\) cl=LOCAL_ONE n=100000 -rate auto -node 10.0.0.5

Results:
op rate                   : 1846 [insert:1846]
partition rate            : 1846 [insert:1846]
row rate                  : 1846 [insert:1846]
latency mean              : 2.1 [insert:2.1]
latency median            : 1.4 [insert:1.4]
latency 95th percentile   : 3.8 [insert:3.8]
latency 99th percentile   : 11.0 [insert:11.0]
latency 99.9th percentile : 28.0 [insert:28.0]
latency max               : 1753.2 [insert:1753.2]
Total partitions          : 100000 [insert:100000]
Total errors              : 0 [insert:0]
total gc count            : 0
total gc mb               : 0
total gc time (s)         : 0
avg gc time(ms)           : NaN
stdev gc time(ms)         : 0
Total operation time      : 00:00:54
```
    
Increasing the number of threads increases the op rate as expected:

```
cassandra-stress user profile=./txn_by_cc_stress.yaml ops\(insert=1\) cl=LOCAL_ONE n=100000 -rate threads=8 -node 10.0.0.4,10.0.0.5,10.0.0.7

Results:
op rate                   : 2639 [insert:2639]
partition rate            : 2639 [insert:2639]
row rate                  : 2639 [insert:2639]
latency mean              : 3.0 [insert:3.0]
latency median            : 1.7 [insert:1.7]
latency 95th percentile   : 7.0 [insert:7.0]
latency 99th percentile   : 11.3 [insert:11.3]
latency 99.9th percentile : 26.6 [insert:26.6]
latency max               : 722.7 [insert:722.7]
Total partitions          : 100000 [insert:100000]
Total errors              : 0 [insert:0]
total gc count            : 0
total gc mb               : 0
total gc time (s)         : 0
avg gc time(ms)           : NaN
stdev gc time(ms)         : 0
Total operation time      : 00:00:37
```


For reads
```
cassandra-stress user profile=./txn_by_cc_stress.yaml ops\(singletrans=1\) -node 10.0.0.4

cassandra-stress user profile=./txn_by_cc_stress.yaml ops\(dailytrans=1\) -node 10.0.0.4

```

##Visual Dashboard - Lucidworks Banana

The Banana project was forked from Kibana, and works with all kinds of time series (and non-time series) data stored in Apache Solr. It uses Kibana's powerful dashboard configuration capabilities, ports key panels to work with Solr, and provides significant additional capabilities, including new panels that leverage D3.js.

Banana allows you to create rich and flexible UIs, enabling users to rapidly develop end-to-end applications that leverage the power of Apache Solr.

The dashboard below was created using Banana.

![alt dashboard](https://github.com/simonambridge/RTFAP2/blob/master/banana/TransactionDashboard.png)

 
Follow this [guide](https://github.com/simonambridge/RTFAP2/tree/master/banana/Banana_Setup.md) to set it up.


