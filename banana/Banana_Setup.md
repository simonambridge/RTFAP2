#Visualizing Cassandra-Solr Data with Banana

[Lucidworks Banana](https://github.com/lucidworks/banana) is a quick and easy way to visualize your Solr time series data.

This guide will walk you through how to get Banana running with DataStax Enterprise. 

##Download Banana
Clone the Banana repo to `/usr/share/dse/solr/web`
```
cd  /usr/share/dse/solr/web

git clone https://github.com/lucidworks/banana

ls /usr/share/dse/solr/web
banana  demos  solr
```

##Configure Banana

###config.js

`cd  /usr/share/dse/solr/web/banana/src`

Edit config.js
* set `solr_core` to `solr_core: "rtfap.transactions"`
* set `banana_index` to `banana_index: "banana.dashboards"`

Your `config.js` will look like this:
```
    solr: "/solr/",
    # solr_core: "logstash_logs",
    solr_core: "rtfap.transactions",
    timefield: 'event_timestamp',

    /**
     * The default Solr index to use for storing objects internal to Banana, such as 
     * stored dashboards. If you have been using a collection named kibana-int 
     * to save your dashboards (the default provided in Banana 1.2 and earlier), then you
     * simply need to replace the string "banana-int" with "kibana-int" and your old 
     * dashboards will be accessible. 
     *
     * This banana-int (or equivalent) collection must be created and available in the 
     * default solr server specified above, which serves as the persistence store for data 
     * internal to banana.
     * @type {String}
     */
    # banana_index: "banana-int",
    banana_index: "banana.dashboards",
```

###Generate Banana Solr Index

Replace the default banana solrconfig..xml with the one generated when the RTFAP solr core was created.
```
cd /usr/share/dse/solr/web/banana/resources/banana-int-solr-4.5/banana-int/conf
cp solrconfig.xml solrconfig.xml.original
```

Edit `solrconfig.xml` and replace the contents with the `solrconfig.xml` from the web page at: 

`http://[DSE_Host_IP]:8983/solr/#/rtfap.transactions/files?file=solrconfig.xml`

###Create banana.dashboards Core

```
curl --data-binary @solrconfig.xml -H 'Content-type:text/xml; charset=utf-8' "http://[DSE_Host_IP]:8983/solr/resource/banana.dashboards/solrconfig.xml"

SUCCESS

curl --data-binary @schema.xml -H 'Content-type:text/xml; charset=utf-8' "http://[DSE_Host_IP]:8983/solr/resource/banana.dashboards/schema.xml"

SUCCESS
```

```
curl -X POST -H 'Content-type:text/xml; charset=utf-8' "http://localhost:8983/solr/admin/cores?action=CREATE&name=banana.dashboards"

<?xml version="1.0" encoding="UTF-8"?>
<response>
<lst name="responseHeader"><int name="status">0</int><int name="QTime">25462</int></lst>
</response>
```

(if you change schema.xml, you will need to reload it: 
e.g. 

```
curl -X POST -H 'Content-type:text/xml; charset=utf-8' "http://[DSE_Host_IP]:8983/solr/admin/cores?action=RELOAD&name=banana.dashboards"

<?xml version="1.0" encoding="UTF-8"?>
<response>
<lst name="responseHeader"><int name="status">400</int><int name="QTime">9</int></lst><lst name="error"><str name="msg">Core banana.dashboards does not exist, please use the CREATE action instead if you meant to create it.</str><int name="code">400</int></lst><str name="params">name=banana.dashboards&amp;action=RELOAD</str>
</response>
```


Core banana.dashboards should now appear in SolrAdmin UI in the drop-down list of available cores.

###Update Tomcat conf

`cd /usr/share/dse/tomcat/conf`

Edit server.xml.
Add the following inside the `<Host>` tag:

`<Context docBase="/usr/share/dse/solr/web/banana/src" path="/banana" />`

Delete the Tomcat work directory:

```
rm /usr/share/dse/tomcat/work
```

Restart DSE:
```
service dse restart
```

###Customise Dashboard

In the browser go to `http://[DSE_Host_IP]:8983/banana`

In menu in the top right of the browser page select New -> Time-series Dashboard

Enter/check the data and press create:
* Solr Server => `/solr/`
* Collection => `rtfap.transactions`
* Time Field => `txn_time` (this is the only one you should need to change)

See Section 10 on Caroline's page [here](https://medium.com/@carolinerg/visualizing-cassandra-solr-data-with-banana-b54bf9dd24c#.jgeib56h5) for some hints on adding fields to a dashboard.

However - you can use the default supplied dashboard!!!
```
cd /usr/share/dse/solr/web/banana/src/app/dashboards
cp default.json default.json.original
cp /<RTFAP repo location>/banana/default.json .
```

Back in the browser hit refresh (or navigate to `http://[DSE_Node_IP]:8983/banana).

You will see the nice RTFAP dashboard shown on the main repo:

![alt dashboard](https://github.com/simonambridge/RTFAP/blob/master/banana/TransactionDashboard.png)

If you have already generated data you will be able to select a data range to view.

Remember - there is a TTL on the transactions table, so the data will gradually age out after 24 hours :)



