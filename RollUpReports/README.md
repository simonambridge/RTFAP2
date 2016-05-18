#Datastax Fraud Prevention Demo - Batch Analytics


If you havent yet installed sbt (as root or use sudo):

```
echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 642AC823
apt-get update
apt-get install sbt
```

In order to run this demo navigate to the RollUpReports directory

NB ensure that you've run the streaming transaction producer/consumer before you run the roll ups so that there is data in the Transaction table to be rolled up).

* Build the rollup jobs with this command:

`sbt package`

* Submit the transactions by merchant roll up: 

`./run_rollupbymerchant.sh`

* Submit the aggregates by credit card rollup: 

`./run_rollupbycc.sh`
