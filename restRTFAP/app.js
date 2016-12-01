var express = require('express');
var path = require('path');
var favicon = require('serve-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');
var index = require('./routes/index');
var users = require('./routes/users');
var cassandra = require('cassandra-driver');
var app = express();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

// uncomment after placing your favicon in /public
//app.use(favicon(path.join(__dirname, 'public', 'favicon.ico')));
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', index);
app.use('/users', users);

// ReST queries

app.get('/transactions', function(req, res) {
// 1. List all the card transactions across all cards and vendors
// e.g. http://[server_IP:Express_port]/transactions
// SELECT * FROM rtfap.transactions;

  var client = new cassandra.Client({ contactPoints: ['localhost'] , keyspace: 'rtfap'});
  const queryString = 'SELECT * FROM rtfap.transactions';
  console.log("Query = " + queryString);

  client.execute(queryString, function(err, result)
  {
    if (err) throw err;

//    Display rows returned
//    for (var item in result.rows) {
//      console.log(result.rows[item]);
//    }

    res.setHeader('Content-Type', 'application/json');
    jsonString=JSON.stringify(result.rows);
    console.log('JSON = ',jsonString);
    res.send(JSON.stringify(result.rows));
  });
});

app.get('/transactionsover', function(req, res) {
// 2. List all transactions over a specified amount
// e.g. http://[server_IP:Express_port]/transactionsover?amount=2500
// SELECT * FROM rtfap.transactions where solr_query = '{"q":"*:*",  "fq":"amount:[1000 TO *]"}}'

  var client = new cassandra.Client({ contactPoints: ['localhost'] , keyspace: 'rtfap'});

  var amount = 0;
  if (req.query.amount !== undefined) amount = [req.query.amount];

  console.log("Amount = " + amount);

  const queryString = 'SELECT * FROM rtfap.transactions where solr_query = \'\{"q":"*:*",  "fq":"amount:[' + amount + ' TO *]"\}\'';
  console.log("Query = " + queryString);

  client.execute(queryString, { prepare: true }, function(err, result)
  {
    if (err) throw err;

//    Display rows returned
//    for (var item in result.rows) {
//      console.log(result.rows[item]);
//    }

    res.setHeader('Content-Type', 'application/json');
    jsonString=JSON.stringify(result.rows);
    console.log('JSON = ',jsonString);
    res.send(JSON.stringify(result.rows));
  });
});

app.get('/rejectedtransactions', function(req, res) {
// 3. Retrieve all transactions in the TRANSACTIONS table where status="Rejected"
// e.g. http://[server_IP:Express_port]/rejectedtransactions
// SELECT * FROM rtfap.transactions WHERE solr_query='{"q":"status: Rejected"}';

  var client = new cassandra.Client({ contactPoints: ['localhost'] , keyspace: 'rtfap'});
  const queryString = 'SELECT * FROM rtfap.transactions WHERE solr_query=\'\{"q":"status: Rejected"\}\';';
  console.log("Query = " + queryString);

  client.execute(queryString, function(err, result)
  {
    if (err) throw err;

//    Display rows returned
//    for (var item in result.rows) {
//      console.log(result.rows[item]);
//    }

    res.setHeader('Content-Type', 'application/json');
    jsonString=JSON.stringify(result.rows);
    console.log('JSON = ',jsonString);
    res.send(JSON.stringify(result.rows));
  });
});


app.get('/transactionsfacetedbymerchant', function(req, res) {
// 4. Retrieve all transactions in the TRANSACTIONS table, faceted by merchant
// e.g. http://[server_IP:Express_port]/transactionsfacetedbymerchant
// SELECT * FROM rtfap.transactions WHERE solr_query='{"q":"*:*", "facet":{"field":"merchant"}}';

  var client = new cassandra.Client({ contactPoints: ['localhost'] , keyspace: 'rtfap'});
  const queryString = 'SELECT * FROM rtfap.transactions WHERE solr_query=\'\{"q":"*:*", "facet":{"field":"merchant"}\}\';';
  console.log("Query = " + queryString);

  client.execute(queryString, function(err, result)
  {
    if (err) throw err;

//    Display rows returned
//    for (var item in result.rows) {
//      console.log(result.rows[item]);
//    }

    res.setHeader('Content-Type', 'application/json');
    jsonString=JSON.stringify(result.rows);
    console.log('JSON = ',jsonString);
    res.send(JSON.stringify(result.rows));
  });
});

app.get('/transactionsbystatusinlast', function(req, res) {
// 5. Retrieve all transactions in the TRANSACTIONS table, faceted by status, over the last year/month/minute
// e.g. http://[server_IP:Express_port]/transactionsbystatusinlast?period=MINUTE
// SELECT * FROM rtfap.transactions WHERE solr_query = '{"q":"*:*",  "fq":"txn_time:[NOW-1DAY TO *]","facet":{"field":"status"}}';

  var client = new cassandra.Client({ contactPoints: ['localhost'] , keyspace: 'rtfap'});

  var period = "Undefined"

  console.log("period = " + req.query.period);

  if (req.query.period !== undefined) {

    const queryString = 'SELECT * FROM rtfap.transactions WHERE solr_query = \'\{"q":"*:*",  "fq":"txn_time:[NOW-1' + req.query.period + ' TO *]","facet":{"field":"status"}}\';';
    console.log("Query = " + queryString);

    client.execute(queryString, { prepare: true }, function(err, result)
    {
//    if (err) throw err;
      if (err) console.log(err);

//    Display rows returned
//    for (var item in result.rows) {
//      console.log(result.rows[item]);
//   }

      res.setHeader('Content-Type', 'application/json');
      jsonString=JSON.stringify(result.rows);
      console.log('JSON = ',jsonString);
      res.send(JSON.stringify(result.rows));
    });
  } else {
      res.setHeader('Content-Type', 'application/json');
      jsonString="Missing Parameters";
      console.log('JSON = ',jsonString);
      res.send(jsonString);
  }
});

app.get('/transactionsbycardandstatusinlast', function(req, res) {
// 6. Retrieve all transactions in the TRANSACTIONS table, faceted by status, for the specified card number and period
// e.g. http://[server_IP:jetty_port]/transactionsbycardandstatusinlast?card=123*&period=YEAR
// SELECT * FROM rtfap.transactions WHERE solr_query = '{"q":"cc_no:123*",  "fq":"txn_time:[NOW-1YEAR TO *]","facet":{"field":"status"}}';

  var client = new cassandra.Client({ contactPoints: ['localhost'] , keyspace: 'rtfap'});

  console.log("card   = " + req.query.card);
  console.log("period = " + req.query.period);

  if (req.query.card !== undefined && req.query.period !== undefined) {

    const queryString = 'SELECT * FROM rtfap.transactions where solr_query = \'{"q":"cc_no:' + req.query.card + '*",  "fq":"txn_time:[NOW-1' + req.query.period + ' TO *]","facet":{"field":"status"}}\';';
    console.log("Query = " + queryString);

    client.execute(queryString, { prepare: true }, function(err, result)
    {
//    if (err) throw err;
      if (err) console.log(err);

//    Display rows returned
//    for (var item in result.rows) {
//      console.log(result.rows[item]);
//   }

      res.setHeader('Content-Type', 'application/json');
      jsonString=JSON.stringify(result.rows);
      console.log('JSON = ',jsonString);
      res.send(JSON.stringify(result.rows));
    });
  } else {
      res.setHeader('Content-Type', 'application/json');
      jsonString="Missing Parameters";
      console.log('JSON = ',jsonString);
      res.send(jsonString);
  }
});

app.get('/transactionsbycard', function(req, res) {
// 7. Retrieve all transactions in the TRANSACTIONS table for a specified card number (optional wild card)
// e.g. http://[server_IP:Express_port]/transactionsbycard?card=123*
// SELECT * FROM rtfap.transactions where solr_query='{"q":"cc_no:123*"}';

  var client = new cassandra.Client({ contactPoints: ['localhost'] , keyspace: 'rtfap'});

  console.log("card   = " + req.query.card);

  if (req.query.card !== undefined) {

    const queryString = 'SELECT * FROM rtfap.transactions where solr_query=\'{"q":"cc_no:' + req.query.card + '"}\';';

    console.log("Query = " + queryString);

    client.execute(queryString, { prepare: true }, function(err, result)
    {
//    if (err) throw err;
      if (err) console.log(err);

//    Display rows returned
//    for (var item in result.rows) {
//      console.log(result.rows[item]);
//   }

      res.setHeader('Content-Type', 'application/json');
      jsonString=JSON.stringify(result.rows);
      console.log('JSON = ',jsonString);
      res.send(JSON.stringify(result.rows));
    });
  } else {
      res.setHeader('Content-Type', 'application/json');
      jsonString="Missing Parameters";
      console.log('JSON = ',jsonString);
      res.send(jsonString);
  }
});

app.get('/fraudulenttransactionsbycard', function(req, res) {
// 8. Retrieve all transactions in the TRANSACTIONS table tagged as "Fraudulent" for a specified card number
// e.g. http://[server_IP:Express_port]/fraudulenttransactionsbycard?card=123*
// SELECT * FROM rtfap.transactions where solr_query='{"q":"cc_no:123412*", "fq":["tags:Fraudulent"]}';

  var client = new cassandra.Client({ contactPoints: ['localhost'] , keyspace: 'rtfap'});

  console.log("card   = " + req.query.card);

  if (req.query.card !== undefined) {

    const queryString = 'SELECT * FROM rtfap.transactions where solr_query=\'{"q":"cc_no:' + req.query.card + '", "fq":["tags:Fraudulent"]}\';';

    console.log("Query = " + queryString);

    client.execute(queryString, { prepare: true }, function(err, result)
    {
//    if (err) throw err;
      if (err) console.log(err);

//    Display rows returned
//    for (var item in result.rows) {
//      console.log(result.rows[item]);
//   }

      res.setHeader('Content-Type', 'application/json');
      jsonString=JSON.stringify(result.rows);
      console.log('JSON = ',jsonString);
      res.send(JSON.stringify(result.rows));
    });
  } else {
      res.setHeader('Content-Type', 'application/json');
      jsonString="Missing Parameters";
      console.log('JSON = ',jsonString);
      res.send(jsonString);
  }
});

app.get('/fraudulenttransactionsinlast', function(req, res) {
// 9. Retrieve data for all transactions in the TRANSACTIONS table tagged as "Fraudulent" over the last year
// e.g.http://[server_IP:Express_port]/fraudulenttransactionsinlast?period=YEAR
// SELECT * FROM transactions where solr_query = '{"q":"*:*", "fq":["txn_time:[NOW-1YEAR TO *]", "tags:Fraudulent"]}';


  var client = new cassandra.Client({ contactPoints: ['localhost'] , keyspace: 'rtfap'});

  console.log("period   = " + req.query.period);

  if (req.query.period !== undefined) {

    const queryString = 'SELECT * FROM transactions where solr_query = \'{"q":"*:*", "fq":["txn_time:[NOW-1' + req.query.period + ' TO *]", "tags:Fraudulent"]}\';';

    console.log("Query = " + queryString);

    client.execute(queryString, { prepare: true }, function(err, result)
    {
//    if (err) throw err;
      if (err) console.log(err);

//    Display rows returned
//    for (var item in result.rows) {
//      console.log(result.rows[item]);
//   }

      res.setHeader('Content-Type', 'application/json');
      jsonString=JSON.stringify(result.rows);
      console.log('JSON = ',jsonString);
      res.send(JSON.stringify(result.rows));
    });
  } else {
      res.setHeader('Content-Type', 'application/json');
      jsonString="Missing Parameters";
      console.log('JSON = ',jsonString);
      res.send(jsonString);
  }
});



app.get('/dailytransactionsbymerchant', function(req, res) {
// 10. List all transactions for a merchant on a specified day
// e.g. http://localhost:3000/dailytransactionsbymerchant?merchant=GAP&day=20161123
// SELECT * FROM rtfap.dailytxns_bymerchant WHERE merchant='Safeway' AND day=20161123;

  var client = new cassandra.Client({ contactPoints: ['localhost'] , keyspace: 'rtfap'});

  var merchant = "Undefined"
  var day = "Undefined"

  console.log("Merchant = " + req.query.merchant);
  console.log("Day = " + req.query.day);

  if (req.query.merchant !== undefined && req.query.day !== undefined) {

    const params = [req.query.merchant, req.query.date];
    const queryString = 'SELECT * FROM dailytxns_bymerchant where merchant=\'' + req.query.merchant + '\' and day=' + req.query.day;
    console.log("Query = " + queryString);

    client.execute(queryString, { prepare: true }, function(err, result)
    {
//    if (err) throw err;
      if (err) console.log(err);

//    Display rows returned
//    for (var item in result.rows) {
//      console.log(result.rows[item]);
//   }

      res.setHeader('Content-Type', 'application/json');
      jsonString=JSON.stringify(result.rows);
      console.log('JSON = ',jsonString);
      res.send(JSON.stringify(result.rows));
    });
  } else {
      res.setHeader('Content-Type', 'application/json');
      jsonString="Missing Parameters";
      console.log('JSON = ',jsonString);
      res.send(jsonString);
  }
});

//////////////////////////
// Chart URLs
/////////////////////////
app.get('/transactionsperhour', function(req, res) {
// 5. Retrieve count of transactions in the TRANSACTIONS table in the last hour
// e.g. http://[server_IP:Express_port]/transactionsper hour
// SELECT count(*) FROM rtfap.transactions WHERE solr_query = '{"q":"*:*",  "fq":"txn_time:[NOW-1HOUR TO *]"}';

  var client = new cassandra.Client({ contactPoints: ['localhost'] , keyspace: 'rtfap'});
//  const queryString = 'SELECT ttl_txn_hr, time FROM rtfap.txn_count_min WHERE solr_query = \'{"q":"*:*",  "fq":"time:[NOW-24HOUR TO *]","sort":"time asc"}\';';
  const queryString = 'SELECT ttl_txn_hr, time FROM rtfap.txn_count_min WHERE solr_query = \'{"q":"*:*",  "fq":"time:[NOW-1YEAR TO *]","sort":"time asc"}\';';
  console.log("Query = " + queryString);

  client.execute(queryString, { prepare: true }, function(err, result)
  {
//    if (err) throw err;
      if (err) console.log(err);

//    Display rows returned
//    for (var item in result.rows) {
//      console.log(result.rows[item]);
//   }

    res.setHeader('Content-Type', 'application/json');
    jsonString=JSON.stringify(result.rows);
    console.log('JSON = ',jsonString);
    res.send(JSON.stringify(result.rows));
  });
});

app.get('/approvedtransactionsperhour', function(req, res) {
// 5. Retrieve count of transactions in the TRANSACTIONS table in the last hour
// e.g. http://[server_IP:Express_port]/transactionsper hour
// SELECT count(*) FROM rtfap.transactions WHERE solr_query = '{"q":"*:*",  "fq":"txn_time:[NOW-1HOUR TO *]"}';

  var client = new cassandra.Client({ contactPoints: ['localhost'] , keyspace: 'rtfap'});
//  const queryString = 'SELECT ttl_txn_hr, time FROM rtfap.txn_count_min WHERE solr_query = \'{"q":"*:*",  "fq":"time:[NOW-24HOUR TO *]","sort":"time asc"}\';';
  const queryString = 'SELECT approved_txn_hr, time FROM rtfap.txn_count_min WHERE solr_query = \'{"q":"*:*",  "fq":"time:[NOW-1YEAR TO *]","sort":"time asc"}\';';
  console.log("Query = " + queryString);

  client.execute(queryString, { prepare: true }, function(err, result)
  {
//    if (err) throw err;
      if (err) console.log(err);

//    Display rows returned
//    for (var item in result.rows) {
//      console.log(result.rows[item]);
//   }

    res.setHeader('Content-Type', 'application/json');
    jsonString=JSON.stringify(result.rows);
    console.log('JSON = ',jsonString);
    res.send(JSON.stringify(result.rows));
  });
});


app.get('/sensordata', function(req, res) {
  var client = new cassandra.Client({ contactPoints: ['localhost'] , keyspace: 'sparksensordata'});
  var queryString = 'select time, value from sparksensordata.sensordata';
  client.execute(queryString, function(err, result)
  {
    if (err) throw err;
    for (var item in result.rows) {
      console.log(result.rows[item]);
    }
    res.setHeader('Content-Type', 'application/json');
    jsonString=JSON.stringify(result.rows);
    console.log('JSON = ',jsonString);
    res.send(JSON.stringify(result.rows));
  });
});

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  var err = new Error('Not Found');
  err.status = 404;
  next(err);
});

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

module.exports = app;
