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
// e.g. http://localhost:3000/transaction
// SELECT * FROM rtfap.transactions;

  var client = new cassandra.Client({ contactPoints: ['localhost'] , keyspace: 'rtfap'});
  const queryString = 'SELECT * FROM transactions';
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
// e.g. http://localhost:3000/transactionsover?amount=500
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


app.get('/dailytransactionsbymerchant', function(req, res) {
// 10. List all transactions for a merchant on a specified day
// e.g. http://localhost:3000/dailytransactionsbymerchant?merchant=GAP&day=20160309
// SELECT * FROM rtfap.dailytxns_bymerchant WHERE merchant='GAP' AND day=20160309;

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
  }
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
