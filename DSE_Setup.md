<h1>Install And Configure DSE 5.1.4</h1>

To setup your environment, you'll also need the following resources:
- Python 2.7
- Java 8
- For Red Hat, CentOS and Fedora, install EPEL (Extra Packages for Enterprise Linux).

Set up and install DataStax Enterprise with Spark and Solr enabled
- This demo is based upon DSE 5.1.4.x with Spark 2.0.2, Scala 2.11, Kafka 1.0.0 and Akka 2.3.15
- This demo uses an installation running on MacOS Sierra 10.12, using the packaged install method from http://academy.datastax.com/downloads
- You can find installation guides for other platforms here:
  - Ubuntu/Debian - http://docs.datastax.com/en/dse/5.1/dse-admin/datastax_enterprise/install/installDEBdse.html
  - Red Hat/Fedora/CentOS/Oracle Linux - http://docs.datastax.com/en/dse/5.1/dse-admin/datastax_enterprise/install/installRHELdse.html
  - Tarball install - http://docs.datastax.com/en/dse/5.1/dse-admin/datastax_enterprise/install/installTARdse.html

When DSE has been installed, set some environment variables to make life easier (add these to your shell init script e.g. ```$HOME/.bash profile``` on MacOS):

```
DSE_HOME=/Users/simonambridge/dse export DSE_HOME
PATH=$PATH:$DSE_HOME/bin:$DSE_HOME/Cassandra/bin export PATH
```
<br>

## Start DSE Services

Now start the DSE services:
- MacOS: ```$DSE_HOME/bin/start_server```
- Linux: ```sudo service dse start```

<br>
Check the system log for successful startup:

```
$ tail -f $DSE_HOME/logs/cassandra/output.log
```
> You should see ```state jump to normal``` to indicate successful startup:
 
```
INFO  [main] 2017-11-09 18:22:17,557  StorageService.java:2318 - Node /127.0.0.1 state jump to NORMAL
INFO  [main] 2017-11-09 18:22:17,565  VersionBarrier.java:65 - DseRoleManager unlocked after upgrade! you may now create/alter/delete roles
INFO  [main] 2017-11-09 18:22:17,573  AuthCache.java:172 - (Re)initializing CredentialsCache (validity period/update interval/max entries) (2000/2000/1000)
INFO  [main] 2017-11-09 18:22:17,606  DseDaemon.java:788 - Waiting for other nodes to become alive...
INFO  [main] 2017-11-09 18:22:17,607  DseDaemon.java:801 - Wait for nodes completed
INFO  [main] 2017-11-09 18:22:17,608  AuditLogger.java:51 - Audit logging is disabled
INFO  [main] 2017-11-09 18:22:17,610  TokenMetadata.java:479 - Updating topology for /127.0.0.1
INFO  [main] 2017-11-09 18:22:17,644  VersionBarrier.java:65 - DigestTokensManager unlocked after upgrade!
```
<br>
When the services are running you can check the cluster status with the ```nodetool status``` command. 
Here we can see that:
- the datacenter is running in SearchGraphAnalytics mode
- The IP for this node is 127.0.0.1
- "State=normal" (UN also signifies Up, Normal)

```
$ nodetool status

Datacenter: SearchGraphAnalytics
================================
Status=Up/Down
|/ State=Normal/Leaving/Joining/Moving
--  Address    Load       Owns    Host ID                               Token                                    Rack
UN  127.0.0.1  623.33 KiB  ?       580ed10f-47b4-40fb-9b15-e4fe7f15279d  -852629894434374786                      rack1

Note: Non-system keyspaces don't have the same replication settings, effective ownership information is meaningless
```
<br>
Now try logging in to cqlsh to ensure that Cassandra is available:

```
$ cqlsh
Connected to Test Cluster at 127.0.0.1:9042.
[cqlsh 5.0.1 | Cassandra 3.11.0.1900 | DSE 5.1.4 | CQL spec 3.4.4 | Native protocol v4]
Use HELP for help.
cqlsh>
```
<br>
Check That Spark is working and available:

```
$ $DSE_HOME/bin/dse spark

The log file is at /Users/simonambridge/.spark-shell.log
WARN  2017-11-07 13:44:27,161 com.datastax.driver.core.NettyUtil: Found Netty's native epoll transport, but not running on linux-based operating system. Using NIO instead.
Creating a new Spark Session
WARN  2017-11-07 13:44:34,552 org.apache.spark.SparkContext: Use an existing SparkContext, some configuration may not take effect.
Extracting Spark Context
Extracting SqlContext
Spark context Web UI available at http://10.155.201.63:4040
Spark Context available as 'sc' (master = dse://?, app id = app-20171107134434-0000).
Spark Session available as 'spark'.
Spark SqlContext (Deprecated use Spark Session instead) available as 'sqlContext'
Welcome to
      ____              __
     / __/__  ___ _____/ /__
    _\ \/ _ \/ _ `/ __/  '_/
   /___/ .__/\_,_/_/ /_/\_\   version 2.0.2.6-de611f9
      /_/

Using Scala version 2.11.11 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_121)
Type in expressions to have them evaluated.
Type :help for more information.

scala> :quit
```
<br>
Check That the Spark Master is available at: ```http://localhost:7080``` 
<br>
<br>
Check That Solr is available at: ```http://localhost:8983/solr/#/``` 
<br>
<br>

## Set Your Datacentre To SearchAnalytics Mode

If your datacenter is not running in SearchAnalytics mode on this node you will need to follow the instructions below to remove the existing configuration and recreate the database:
 
 1. Stop the DSE services
- MacOS: ```$DSE_HOME/bin/stop_server```
- Linux: ```sudo service dse stop```
 
 2. Locate the DSE defaults file
 
 To enable Solr and Spark, change the flag from "0" to "1" for Solr and Spark in the DSE config file
- MacOS: ```$DSE_HOME/service/etc/default/dse```
- Linux: ```/etc/default/dse```
 - Edit the dse file: 
 
 ```
 $ sudo vi /etc/default/dse
 ```
 Ensure Solr and Spark are set to "1" e.g. (you can enable/disable Graph too if you wish):
 ```
 # Start the node in DSE Search mode
 SOLR_ENABLED=1
 # Start the node in Spark mode
 SPARK_ENABLED=1
 ```
 
 
 3. Delete the default (Cassandra-only) datacentre databases:
 ```
 $ sudo rm -rf /var/lib/cassandra/data/*
 $ sudo rm -rf /var/lib/cassandra/saved_caches/*
 $ sudo rm -rf /var/lib/cassandra/commitlog/*
 $ sudo rm -rf /var/lib/cassandra/hints/*
 ```
 
 4. Remove the old system.log:
```
$ sudo rm /var/log/cassandra/system.log 
 rm: remove regular file `/var/log/cassandra/system.log'? y
```
 
 5. Restart DSE services
- MacOS: ```$DSE_HOME/bin/start_server```
- Linux: ```sudo service dse start```


<br>

## References 
<h3>Solr Documentation (Search):</h3>
https://docs.datastax.com/en/datastax_enterprise/5.0/datastax_enterprise/srch/searchOverview.html

<h3>Spark Documentation (Analytics):</h3>
https://docs.datastax.com/en/datastax_enterprise/5.0/datastax_enterprise/ana/analyticsTOC.html 
