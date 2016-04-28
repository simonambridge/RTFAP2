//
// In order to run these demo's it is assumed that the Keyspace and tables below exist
//
// PLEASE NOTE: The Keyspace creation command will need to reflect the Topology of your
//              environment. As presented it will only work in a single node test deployment.

CREATE KEYSPACE rtfap
WITH durable_writes = true
AND replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 1
};

CREATE TABLE rtfap.transactions (
	cc_no text,
	year int,
	month int,
	day int,
	txn_time timestamp,
	amount double,
	cc_provider text,
	country text,
	date_text text,
	hour int,
	items MAP<text, double>,
	location text,
	merchant text,
	min int,
	notes text,
	solr_query text,
	status text,
	tags SET<text>,
	txn_id text,
	user_id text,
	PRIMARY KEY (( cc_no, year, month, day ), txn_time)
) WITH CLUSTERING ORDER BY ( txn_time DESC )
AND default_time_to_live = 86400;

CREATE TABLE rtfap.txn_count_min (
	year int,
	month int,
	day int,
	hour int,
	minute int,
	approved_rate_hr double,
	approved_rate_min double,
	approved_txn_hr int,
	approved_txn_min int,
	ttl_txn_hr int,
	ttl_txn_min int,
	PRIMARY KEY (( year, month, day, hour, minute ))
);
