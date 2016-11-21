#Sample Solr ReST Queries

The supplied Express-based Solr ReST interface provides a number of representative queries against the transaction table and roll-up/aggregate data tables.

Use the following URL's (substituting the external address of your server and the port number you used when you started the Express Server) to run the queries shown. 

The results will be returned in JSON format suitable for consumption by an external system.

##CQL Queries On The Transactions Table

###1. List all the card transactions across all cards and vendors
List all the card transactions across all cards and vendors in the TRANSACTIONS table:
http://[server_IP:Express_port]/getalltransactions
```
SELECT * FROM rtfap.transactions;
```

##Solr Queries On The Transactions Table

###2. List all transactions over a specified amount
Retrieve data for all transactions in the TRANSACTIONS table where the amount is greater than a specified value
http://[server_IP:Express_port]/getalltransactionsbyamount/1000
```
SELECT * FROM rtfap.transactions WHERE solr_query = '{"q":"*:*",  "fq":"amount:[1000 TO *]"}';
```

###3. List all rejected transactions
Retrieve all transactions in the TRANSACTIONS table where status="Rejected"
http://[server_IP:Express_port]/getallrejectedtransactions
```
SELECT * FROM rtfap.transactions WHERE solr_query='{"q":"status: Rejected"}';
```

###4. List all transactions faceted by merchant
Retrieve all transactions in the TRANSACTIONS table, faceted by merchant
http://[server_IP:Express_port]/getfacetedtransactionsbymerchant
```
SELECT * FROM rtfap.transactions WHERE solr_query='{"q":"*:*", "facet":{"field":"merchant"}}';
```

###5. List all transaction success ratio (faceted by status) in the last period e.g. minute
Retrieve all transactions in the TRANSACTIONS table, faceted by status, over the last year/month/minute
http://[server_IP:Express_port]/getfacetedtransactionsbystatusinlastperiod/MINUTE
```
SELECT * FROM rtfap.transactions WHERE solr_query = '{"q":"*:*",  "fq":"txn_time:[NOW-1" + lastPeriod + " TO *]","facet":{"field":"status"}}';
```

http://[server_IP:jetty_port]/datastax-banking-iot/rest/getallrejectedtransactions 
```
SELECT * FROM transactions where solr_query='{"q":"status: Rejected"}';
```
### - List all transactions faceted by merchant
Retrieve all transactions in the TRANSACTIONS table, faceted by merchant

http://[server_IP:jetty_port]/datastax-banking-iot/rest/getfacetedtransactionsbymerchant 
```
SELECT * FROM transactions where solr_query='{"q":"*:*", "facet":{"field":"merchant"}}';
```
### - List all transaction success ratio (faceted by status) in the last period e.g. minute
Retrieve all transactions in the TRANSACTIONS table, faceted by status, over the last year/month/minute

http://[server_IP:jetty_port]/datastax-banking-iot/rest/getfacetedtransactionsbystatusinlastperiod/MINUTE
```
SELECT * FROM rtfap.transactions where solr_query = '{"q":"*:*",  "fq":"txn_time:[NOW-1" + lastPeriod + " TO *]","facet":{"field":"status"}}';
```
### - List all transaction success ratio (faceted by status) for a specified card in the last period e.g. year
Retrieve all transactions in the TRANSACTIONS table, faceted by status, for the specified card number and period

http://[server_IP:jetty_port]/datastax-banking-iot/rest/getfacetedtransactionsbyccnoandstatusinlastperiod/123412*/YEAR
```
SELECT * FROM rtfap.transactions where solr_query = '{"q":"cc_no:123412*",  "fq":"txn_time:[NOW-1MINUTE TO *]","facet":{"field":"status"}}';
```
### - List all transactions for a specific card
Retrieve all transactions in the TRANSACTIONS table for a specified card number (optional wild card)

http://[server_IP:jetty_port]/datastax-banking-iot/rest/getalltransactionsbyccno/123412*
```
SELECT * FROM transactions where solr_query='{"q":"cc_no:123412*"}';
```
### - List all fraudulent transactions for a specific card
Retrieve all transactions in the TRANSACTIONS table tagged as "Fraudulent" for a specified card number

http://[server_IP:jetty_port]/datastax-banking-iot/rest/getallfraudulenttransactionsbyccno/123412*
```
SELECT * FROM transactions where solr_query='{"q":"cc_no:123412*", "fq":["tags:Fraudulent"]}';
```
### - List all fraudulent transactions for a specified period-to-date
Retrieve data for all transactions in the TRANSACTIONS table tagged as "Fraudulent" over the last year

http://[server_IP:jetty_port]/datastax-banking-iot/rest/getallfraudulenttransactionsinlastperiod/YEAR
```
SELECT * FROM transactions where solr_query = '{"q":"*:*", "fq":["txn_time:[NOW-1YEAR TO *]", "tags:Fraudulent"]}';
```
Retrieve data for all transactions in the TRANSACTIONS table tagged as "Fraudulent" over the last month

http://[server_IP:jetty_port]/datastax-banking-iot/rest/getallfraudulenttransactionsinlastperiod/MONTH
```
SELECT * FROM transactions where solr_query = '{"q":"*:*", "fq":["txn_time:[NOW-1MONTH TO *]", "tags:Fraudulent"]}';
```
Retrieve data for all transactions in the TRANSACTIONS table tagged as "Fraudulent" over the last day

http://[server_IP:jetty_port]/datastax-banking-iot/rest/getallfraudulenttransactionsinlastperiod/DAY
```
SELECT * FROM transactions where solr_query = '{"q":"*:*", "fq":["txn_time:[NOW-1DAY TO *]", "tags:Fraudulent"]}';
```
