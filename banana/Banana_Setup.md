#Visualizing Cassandra-Solr Data with Banana

Lucidworks Banana is a quick and easy way to visualize your Solr time series data.

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
curl --data-binary @solrconfig.xml -H 'Content-type:text/xml; charset=utf-8' "http://172.31.36.22:8983/solr/resource/banana.dashboards/solrconfig.xml"
SUCCESS

curl --data-binary @schema.xml -H 'Content-type:text/xml; charset=utf-8' "http://172.31.36.22:8983/solr/resource/banana.dashboards/schema.xml"
SUCCESS
```

(if you change schema.xml, you will need to reload it: 
e.g. 'curl -X POST -H ‘Content-type:text/xml; charset=utf-8’ “http://localhost:8983/solr/admin/cores?action=RELOAD&name=banana.dashboards"')

Core banana.dashboards should now appear in SolrAdmin UI
