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
// e.g. http://localhost:3000/transaction

  var client = new cassandra.Client({ contactPoints: ['localhost'] , keyspace: 'rtfap'});
  var queryString = 'SELECT * FROM transactions';
  client.execute(queryString, function(err, result) 
  {
    if (err) throw err;
    //for (var item in result.rows) {
    //  console.log(result.rows[item]);
    //}
    res.setHeader('Content-Type', 'application/json');
    jsonString=JSON.stringify(result.rows);
    console.log('JSON = ',jsonString);
    res.send(JSON.stringify(result.rows));
  });
});

app.get('/dailytransactionsbymerchant', function(req, res) {
// e.g. http://localhost:3000/dailytransactionsbymerchant?merchant=GAP&day=20160309

  var client = new cassandra.Client({ contactPoints: ['localhost'] , keyspace: 'rtfap'});

  console.log("Merchant = " + req.query.merchant);
  console.log("Day = " + req.query.day);

  const params = [req.query.merchant, req.query.date];
  const queryString = 'SELECT * FROM dailytxns_bymerchant where merchant=? and day= ?';

  client.execute(queryString, params, { prepare: true }, function(err, result) 
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

app.get('/transactionsover', function(req, res) {
// e.g. http://localhost:3000/transactionsover?amount=500
// SELECT * FROM rtfap.transactions where solr_query = '{"q":"*:*",  "fq":"amount:[1000 TO *]"}}'

  var client = new cassandra.Client({ contactPoints: ['localhost'] , keyspace: 'rtfap'});

  console.log("Amount = " + req.query.amount);

  const params = [req.query.amount];
  const queryString = 'SELECT * FROM rtfap.transactions where solr_query = \'\{"q":"*:*",  "fq":"amount:[' + req.query.amount + ' TO *]"\}\'';

  client.execute(queryString, params, { prepare: true }, function(err, result) 
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
