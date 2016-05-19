#Visualizing Cassandra-Solr Data with Banana

Lucidworks Banana is a quick and easy way to visualize your Solr time series data.

This guide will walk you through how to get Banana running with DataStax Enterprise. 

##Download Banana
cd  /usr/share/dse/solr/web
git clone https://github.com/lucidworks/banana
ls /usr/share/dse/solr/web

##Configure Banana

###config.js

cd  /usr/share/dse/solr/web/banana/src

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
