# RTFAP - Real-time Fraud Analysis Platform

A large bank wants to monitor its customer creditcard transactions to detect and deter fraud attempts. They want the ability to search and group transactions by merchant, credit card provider, amounts values. This is subject to change.
They also need reports generated for all merchants every morning encompassing all transaction data over the last day/week for each merchant. 

The client wants a REST API to return:  
- the ratio of transaction success based on the first 6 digits of their credit card no. (Blacklisting of CC Nos.)     
- the ratio of confirmed transactions against fraudulent transactions in the last minute. (Solr query to scan all in last 10 minutes filtered/faceted by status)
- the moving average of the transaction amount over the last hour compared with the transaction amount per minute. (60 min moving average, Streaming query)
- Daily Roll-Up Report of last-Week and last-Day transactions for each merchant.
- Search capability to search the entire transaction database by merchant, cc_no, amounts.

Performance SLAs:
- The client wants assurance that his data model can handle 1,000 transactions a sec with stable latencies. The client currently handles accounts for over 15000 merchants and hoping to grow to 50,000 in a year.

![alt text] (https://raw.githubusercontent.com/kunalak/RTFAP/master/img.png)

##Setup
DataStax Enterprise supplies built-in enterprise search functionality on Cassandra data that scales and performs in a way that meets the search requirements of modern Internet Enterprise applications. Using this search functionality will allow the volume of transactions to grow without a loss in performance. DSE Search also allows for live indexing for improved index throughput and reduced reader latency. More details about live indexing can be found here -  http://docs.datastax.com/en/datastax_enterprise/4.8/datastax_enterprise/srch/srchConfIncrIndexThruPut.html

We will need to start DSE in Search mode to allow us to use the search functionalities that we need on top on Cassandra. To do this see the following 
https://docs.datastax.com/en/datastax_enterprise/4.8/datastax_enterprise/srch/srchInstall.html
and the Spark (Analytics) functionality using:
http://docs.datastax.com/en/datastax_enterprise/4.8/datastax_enterprise/spark/sparkTOC.html

##DataModel 

We will need to multiple tables for fulfill the above query patterns and workloads. (De-normalization is a good thing with NoSQL databases!)

We will use single DC for testing purposes. For production deployment, we recommend an Active-Active HA setup across geographical regions with RF=2 or 3.
```
create keyspace if not exists rtfap WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1' };
```

Table for: Transactions by txn_time buckets; We will create a Solr index on this tables to fulfill the above search and grouping/faceting needs.

```
create table if not exists rtfap.transactions(
	cc_no text,
	cc_provider text,
	year int,
	month int,
	day int,
	hour int,
	min int,
	txn_time timestamp,
 	txn_id text,
 	user_id text,
	location text,
	items map<text, double>,
	merchant text,
	amount double,
	status text,
	notes text,
	tags <text>,
	PRIMARY KEY ((year, month, day), hour, min, txn_time, cc_no)
) WITH CLUSTERING ORDER BY (day desc, hour desc, min desc, txn_time desc);
```

##Sample inserts

```
insert into rtfap.transactions (year, month, day, hour, min, txn_time, cc_no, amount, cc_provider, items, location, merchant, notes, status, txn_id, user_id) VALUES ( 2016, 03, 09, 11, 04, '2016-03-09 11:04:19', '1234123412341234', 200.0, 'VISA', {'tshirt':25, 'dressshirt':50, 'trousers':125}, 'San Francisco', 'Nordstrom', 'pretty good clothing', 'Approved', '098765', 'kunalak') ; 
```

##Sample queries
still all patrick below this -
For straight-forward queries which only use the credit card no and transaction times, we will use cql to access the data. 

For the latest transaction table we can run the following types of queries
```
use datastax_banking_iot;

select * from latest_transactions where cc_no = '1234123412341234';

select * from latest_transactions where cc_no = '1234123412341234' and transaction_time > '2015-12-31';

select * from latest_transactions where cc_no = '1234123412341234' and transaction_time > '2015-12-31' and transaction_time < '2016-01-27';
```
For the (historic) transaction table we need to add the year into our queries.

```
select * from transactions where cc_no = '1234123412341234' and year = 2016;

select * from transactions where cc_no = '1234123412341234' and year = 2016 and transaction_time > '2015-12-31';

select * from transactions where cc_no = '1234123412341234' and year = 2016 and transaction_time > '2015-12-31' and transaction_time < '2016-01-27';
```

##Searching Data in DSE

The above queries allow us to query on the partition key and some or all of the clustering columns in the table definition. To query more generically on the other columns we will use DSE Search to index and search our data. To do this we use the dsetool to create a solr core. We will also use the dsetool to create the core based on our table for testing purposes. In a production environment we would only index the columns that we would want to query on. 

```
dsetool create_core datastax_banking_iot.transactions generateResources=true reindex=true

dsetool create_core datastax_banking_iot.latest_transactions generateResources=true reindex=true
```

To check that DSE Search is up and running sucessfully go to http://{servername}:8983/solr/

Now we can query our data in a number of ways. One is through cql using the solr_query column. The other is through a third party library like SolrJ which will interact with the search tool through rest.

An example of cql queries would be

Get all the latest transactions from PC World in Glasgow (This is accross all credit cards and users)
```
select * from latest_transactions where solr_query = 'merchant:PC+World location:London' limit  100;
```
Get all the latest transactions for credit card '1' that have a tag of Work. 
```
select * from latest_transactions where solr_query = '{"q":"cc_no:1234123412341234", "fq":"tags:Work"}' limit  1000;
```
Gell all the transaction for credit card '1' that have a tag of Work and are within the last month
```
select * from latest_transactions where solr_query = '{"q":"cc_no:1234123412341234", "fq":"tags:Work AND transaction_time:[NOW-30DAY TO *]"}' limit  1000;
```

##Stress yaml

To help show how DSE will perform in terms of latency and throughput we can use the Cassandra-stress tool to write and read from the system.

You will find the stress.yaml file here - 
https://gist.github.com/PatrickCallaghan/1e16c3eb38fada08a2c0

You can read more about stress testing a data model here 
http://www.datastax.com/dev/blog/improved-cassandra-2-1-stress-tool-benchmark-any-schema 
http://docs.datastax.com/en/cassandra/2.1/cassandra/tools/toolsCStress_t.html

Examples of running the stress tool are (please change node0 to whatever your contact point may be)

For inserts
```
cassandra-stress user profile=Bank-IoT-Stress.yaml  ops\(insert=1\) cl=LOCAL_ONE n=100000 -rate threads=4 -node node0 
```
For reads
```
cassandra-stress user profile=Bank-IoT-Stress.yaml  ops\(getall=1\) cl=LOCAL_ONE n=100000 -rate threads=4 -node node0 
```

##Code Sample

A full code example with inserts and queries can be found here - https://github.com/PatrickCallaghan/datastax-banking-iot

Please follow the instructions to download and populate your cluster with example data. This example also shows how to provide access to the data through a JSON rest web service. 



