<h2>Run DSE in Search Analytics mode</h2>

To setup your environment, you'll also need the following resources:
- Python 2.7
- Java 8
- For Red Hat, CentOS and Fedora, install EPEL (Extra Packages for Enterprise Linux).

Set up and install DataStax Enterprise with Spark and Solr enabled
- This demo is based upon DSE 5.4.1.x with Spark 2.0.2, Scala 2.11, Kafka 1.0.0 and Akka 2.3.15
- This demo uses an installation running on MacOS Sierra 10.12, using the packaged install method from http://academy.datastax.com/downloads
- You can find installation guides for other platforms here:
  - Ubuntu/Debian - http://docs.datastax.com/en/dse/5.1/dse-admin/datastax_enterprise/install/installDEBdse.html
  - Red Hat/Fedora/CentOS/Oracle Linux - http://docs.datastax.com/en/dse/5.1/dse-admin/datastax_enterprise/install/installRHELdse.html
  - Tarball install - http://docs.datastax.com/en/dse/5.1/dse-admin/datastax_enterprise/install/installTARdse.html

When DSE has been installed, set some environment variables to make life easier (add these to your shell init script e.g. ```$HOME/.bash profile``` on MacOS:

```
DSE_HOME=/Users/simonambridge/dse export DSE_HOME
PATH=$PATH:$DSE_HOME/bin:$DSE_HOME/Cassandra/bin export PATH
```
<br>
Now start the DSE services:
```
$DSE_HOME/bin/start_server
```
<br>
Check the system log for successful startup:

```
tail -f $DSE_HOME/logs/cassandra/output.log
```
 - you should see ```state jump to normal``` to indicate successful startup:
 
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
When the services are running you can check the cluster status with the ```nodetool``` command. 
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
Check what has been configured:
```
cat $DSE_HOME/service/etc/default/dse - all set

$DSE_HOME/datastax-studio/bin/start_studio_server 

$DSE_HOME/logs/cassandra/output.log

$DSE_HOME/resources/cassandra/bin/cqlsh

```

If your datacenter is not running in SearchAnalytics mode on this node you will need to follow the instructions below to remove the existing configuration and recreate the database:
 
 1. Stop the service.
 <pre>
 $ sudo service dse stop
 Stopping DSE daemon : dse                                  [  OK  ]
 </pre>
 
 2. Enable Solr and Spark
 Change the flag from "0" to "1" for Solr and Spark in /etc/default/dse:
 <pre>
 $ sudo vi /etc/default/dse
 </pre>
 e.g.:
 <pre>
 # Start the node in DSE Search mode
 SOLR_ENABLED=1
 # Start the node in Spark mode
 SPARK_ENABLED=1
 </pre>
 
 3. Delete the default (Cassandra-only) datacentre databases:
 <pre>
 $ sudo rm -rf /var/lib/cassandra/data/*
 $ sudo rm -rf /var/lib/cassandra/saved_caches/*
 $ sudo rm -rf /var/lib/cassandra/commitlog/*
 $ sudo rm -rf /var/lib/cassandra/hints/*
 </pre>
 
 4. Remove the old system.log:
<pre>
$ sudo rm /var/log/cassandra/system.log 
 rm: remove regular file `/var/log/cassandra/system.log'? y
</pre>
 
 5. Restart DSE
 <pre>
 $ sudo service dse start
 </pre>
 <br>
 
<h3>Solr Documentation (Search):</h3>
https://docs.datastax.com/en/datastax_enterprise/5.0/datastax_enterprise/srch/searchOverview.html

<h3>Spark Documentation (Analytics):</h3>
https://docs.datastax.com/en/datastax_enterprise/5.0/datastax_enterprise/ana/analyticsTOC.html 
