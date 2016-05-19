#Sample ReST Queries

The supplied Java-based Solr ReST interface provides a number of representative queries against the transaction table and roll-up/aggregate data.

Use the following URL's (substituting the external address of your server and the port number you used to start jetty) to run the queries shown. The results will be returned in JSON format suitable for consumption by an external system.

### - List all the card transactions across all cards and vendors
List all the card transactions across all cards and vendors in the TRANSACTIONS table:

http://[server_IP:jetty_port]/datastax-banking-iot/rest/getalltransactions 
```
SELECT * FROM transactions;
```
### - List all transactions for a merchant on a specified day
Retrieve data for all transactions for the specified day in the DAILYTXNS_BYMERCHANT rollup table where the merchant = "GAP" 

http://[server_IP:jetty_port]/datastax-banking-iot/rest/getdailytransactionsbymerchant/GAP/20160309 
```
SELECT * FROM dailytxns_bymerchant where merchant='GAP' and day= 20160309;
```
### - Aggregated purchase history for a specific card and year
Retrieve data for all transactions for the specified year in the YEARLYAGGREGATES_BYCC rollup table where the card number = "1234123412341235"

http://[server_IP:jetty_port]/datastax-banking-iot/rest/getyearlytransactionsbyccno/1234123412341235/2016
```
SELECT * FROM yearlyaggregates_bycc where cc_no='1234123412341235' and year=2016;
```
### - Rolling transaction success ratio and count, by minute and hour
Retrieve a rolling ratio of successful transactions and transaction count over the previous minute and hour

http://[server_IP:port]/datastax-banking-iot/rest/getTransactionsApprovalByDate/201603270521
```
select approved_rate_hr, approved_txn_hr, approved_rate_min, approved_txn_min from txn_count_min where year=2016 and month=3 and day=27 and hour=5 and minute=22;
```

### - List all transactions over a specified amount
Retrieve data for all transactions in the TRANSACTIONS table where the amount is greater than a specified value

http://[server_IP:jetty_port]/datastax-banking-iot/rest/getalltransactionsbyamount/1000
```
SELECT * FROM rtfap.transactions where solr_query = '{"q":"*:*",  "fq":"amount:[1000 TO *]"}}'
```
### - List all rejected transactions
Retrieve all transactions in the TRANSACTIONS table where status="Rejected"

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
### - List all transaction success ratio (faceted by status) in the last period e.g. MINUTE
Retrieve all transactions in the TRANSACTIONS table, faceted by status, over the last year/month/minute

http://[server_IP:jetty_port]/datastax-banking-iot/rest/getfacetedtransactionsbystatusinlastperiod/MINUTE
```
SELECT * FROM rtfap.transactions where solr_query = '{"q":"*:*",  "fq":"txn_time:[NOW-1" + lastPeriod + " TO *]","facet":{"field":"status"}}';
```
### - List all transaction success ratio (faceted by status) for a specified card in the last period e.g. MINUTE
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
