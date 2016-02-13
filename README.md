# RTFAP - Real-time Fraud Analysis Platform
A bank wants to help locate and tag all their expenses/transactions in their bank account to help them categorise their spending. The users will be able to tag any expense/transaction to allow for efficient retrieval and reporting. There will be 10 millions customers with on average 500 transactions a year. Some business customers may have up to 10,000 transactions a year. The bank wants the user's tagged items to show up in searches in less than a second to give users a seamless experience between devices.

The bank would like

1. to understand how this can be done with DSE
2. some latency examples of how they can search all of their users data
3. the users need to be able to filter the queries using time as well as tags e.g. show me all shopping vs luxury in the last 3 months.
4. some assurances that peak traffic of over 10,000 writes, 4,000 reads and 1,000 searches per second can be accommodated by the solution using DSE.
5. a short example of how they can access the data through a JSON rest service.

##Setup
DataStax Enterprise supplies built-in enterprise search functionality on Cassandra data that scales and performs in a way that meets the search requirements of modern Internet Enterprise applications. Using this search functionality will allow the volume of transactions to grow without a loss in performance. DSE Search also allows for live indexing for improved index throughput and reduced reader latency. More details about live indexing can be found here -  http://docs.datastax.com/en/datastax_enterprise/4.8/datastax_enterprise/srch/srchConfIncrIndexThruPut.html

We will need to start DSE in Search mode to allow us to use the search functionalities that we need on top on Cassandra. To do this see the following 
https://docs.datastax.com/en/datastax_enterprise/4.8/datastax_enterprise/srch/srchInstall.html

##DataModel 

Depending on the lenght of time that the transactions are to be held for it may be worth breaking the transactions table into 2 tables, one for historic requests and one for the most recent transactions.

We can use a testing keyspace for the time being as their is no requirement for this to multi-data-center.
```
create keyspace if not exists datastax_banking_iot WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1' };
```
The general table for transactions would be 
```
create table if not exists datastax_banking_iot.transactions(
	cc_no text,	
	year int,
	transaction_time timestamp,
 	transaction_id text,
 	user_id text,
	location text,
	items map<text, double>,
	merchant text,
	amount double,
	status text,
	notes text,
	tags set<text>,
	PRIMARY KEY ((cc_no, year), transaction_time)
) WITH CLUSTERING ORDER BY (transaction_time desc);
```
Note because of the number  of transactions per year for some business customers it may be beneficial to separate the transaction into years for each card. This will only be needed for the historic transactions. 
If another 'latest transactions' table is required we could add another table with a specific default time to live. 
```
create table if not exists datastax_banking_iot.latest_transactions(
	cc_no text,
	transaction_time timestamp,
 	transaction_id text,
 	user_id text,
	location text,
	items map<text, double>,
	merchant text,
	amount double,
	status text,
	notes text,
	tags set<text>,
	PRIMARY KEY (cc_no, transaction_time)
) WITH CLUSTERING ORDER BY (transaction_time desc) 
and default_time_to_live = 2592000;
```
This default_time_to_live of 2592000 seconds ensures that data will be removed from the table 90 days after its been inserted.

##Sample inserts

```
use datastax_banking_iot;
Insert into latest_transactions (cc_no, transaction_time, transaction_id, location, merchant, amount, user_id, status, notes, tags) values ('1234123412341234', '2016-01-26 15:30:14', '1231514114', 'London, UK', 'PC World', 100.00, '141511111', '', '', {'Work', 'Tech'});
Insert into latest_transactions (cc_no, transaction_time, transaction_id, location, merchant, amount, user_id, status, notes, tags) values ('1234123412341234', '2016-01-26 15:33:41', '1231514111', 'Glasgow, UK', 'Amazon', 100.00, '141511111', '', '', {'Home', 'Tech'});

Insert into transactions (cc_no, year, transaction_time, transaction_id, location, merchant, amount, user_id, status, notes, tags) values ('1234123412341234', 2016, '2016-01-26 15:30:14', '1231514114', 'London, UK', 'PC World', 100.00, '141511111', '', '', {'Work', 'Tech'});
Insert into transactions (cc_no, year, transaction_time, transaction_id, location, merchant, amount, user_id, status, notes, tags) values ('1234123412341234', 2016, '2016-01-26 15:33:41', '1231514111', 'Glasgow, UK', 'Amazon', 100.00, '141511111', '', '', {'Home', 'Tech'});

```

##Sample queries

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



