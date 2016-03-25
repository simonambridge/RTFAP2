// create keyspace
create keyspace if not exists rtfap WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1' };

// create tables
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
	tags set<text>,
	PRIMARY KEY ((cc_no, year, month, day),txn_time)
) WITH CLUSTERING ORDER BY (txn_time desc);

create table if not exists rtfap.dailytxns_bymerchant(
	cc_no text,
	cc_provider text,
	day int,
	txn_time timestamp,
 	txn_id text,
 	user_id text,
	location text,
	items map<text, double>,
	merchant text,
	amount double,
	status text,
	notes text,
	tags set<text>,
	total_amount double STATIC,
	max_amount double STATIC,
	min_amount double STATIC,
	total_count bigint STATIC,
	PRIMARY KEY ((merchant, day), txn_time, txn_id)
) WITH CLUSTERING ORDER BY (txn_time desc);

create table if not exists rtfap.hourlyaggregates_bycc(
    cc_no text,
		hour int,
		total_amount double,
    max_amount double,
		min_amount double,
		total_count bigint,
    PRIMARY KEY ((cc_no, hour))
);

create table if not exists rtfap.dailyaggregates_bycc(
    cc_no text,
		day int,
		total_amount double,
    max_amount double,
		min_amount double,
		total_count bigint,
    PRIMARY KEY ((cc_no, day))
);

create table if not exists rtfap.monthlyaggregates_bycc(
    cc_no text,
		month int,
		total_amount double,
    max_amount double,
		min_amount double,
		total_count bigint,
    PRIMARY KEY ((cc_no, month))
);

create table if not exists rtfap.yearlyaggregates_bycc(
    cc_no text,
		year int,
		total_amount double,
    max_amount double,
		min_amount double,
		total_count bigint,
    PRIMARY KEY ((cc_no, year))
);


//inserts
insert into rtfap.transactions (year, month, day, hour, min, txn_time, cc_no, amount, cc_provider, items, location, merchant, notes, status, txn_id, user_id, tags) VALUES ( 2016, 03, 09, 11, 04, '2016-03-09 11:04:19', '1234123412341234', 200.0, 'VISA', {'tshirt':25, 'dressshirt':50, 'trousers':125}, 'San Francisco', 'Nordstrom', 'pretty good clothing', 'Approved', '098765', 'kunalak', {'Suspicious'});
insert into rtfap.transactions (year, month, day, hour, min, txn_time, cc_no, amount, cc_provider, items, location, merchant, notes, status, txn_id, user_id, tags) VALUES ( 2016, 03, 09, 11, 04, '2016-03-09 11:04:24', '1234123412341235', 400.0, 'VISA', {'cap':25, 'lamps':275, 'trousers':100}, 'San Diego', 'Macy', 'cool stuff-good customer', 'Rejected', '876354', 'simonanbridge', {'Fraudulent'});
insert into rtfap.transactions (year, month, day, hour, min, txn_time, cc_no, amount, cc_provider, items, location, merchant, notes, status, txn_id, user_id, tags) VALUES ( 2016, 03, 09, 11, 04, '2016-03-09 11:04:53', '1234123412341235', 800.0, 'VISA', {'chocolates':300, 'electronics':500}, 'London', 'Harrods', 'customer likes electronics', 'Approved', '982538', 'simonanbridge', {'HighValue'});
insert into rtfap.transactions (year, month, day, hour, min, txn_time, cc_no, amount, cc_provider, items, location, merchant, notes, status, txn_id, user_id, tags) VALUES ( 2016, 03, 09, 11, 04, '2016-03-09 11:04:59', '1234123412341236', 750.0, 'MASTERCARD', {'shoes':300, 'belts':150, 'clothes':300}, 'San Jose', 'GAP', 'customer likes electronics', 'Approved', '092753', 'cary', {'HighValue'});
insert into rtfap.transactions (year, month, day, hour, min, txn_time, cc_no, amount, cc_provider, items, location, merchant, notes, status, txn_id, user_id, tags) VALUES ( 2016, 03, 09, 12, 30, '2016-03-09 12:30:00', '1234123412341237', 1500.0, 'AMEX', {'clothes':1500}, 'New York', 'Ann Taylor', 'frequent customer', 'Approved', '876302', 'caroline', {'HighValue'});
insert into rtfap.transactions (year, month, day, hour, min, txn_time, cc_no, amount, cc_provider, items, location, merchant, notes, status, txn_id, user_id, tags) VALUES ( 2016, 03, 17, 21, 04, '2016-03-17 21:04:19', '1234123412341234', 200.0, 'VISA', {'tshirt':25, 'dressshirt':50, 'trousers':125}, 'San Francisco', 'Nordstrom', 'asked for discounts', 'Approved', '763629', 'kunalak', {'Fraudulent'});
