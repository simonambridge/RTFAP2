# Datastax Card Fraud Prevention Demo - Create the RTFAP Node.js/D3 ReST Server

## Install Node.js
On my Mac Node I used brew to install node:
```
$ brew install node
```
Installs node to ```/usr/local/Cellar/node/8.9.1/``` and sets up link to ```/usr/local/bin/node```

Testing your node and npm installation:

```
$ node -v
> v8.9.1

$ npm -v
> 5.5.1
```
## Install Express
We can now npm to install all the Node.js tools needed, including Express.

NPM 1.0 installs modules at the project-level. Command-line tools can be installed globally.

Because Express is a project module and a command line toolwe can install it in both scopes it can be installed globally:

$ npm install -g express
$ npm install -g express-generator
$ express --version
4.15.5


## Install Node Cassandra Driver

You will also need to install the Node Cassandra driver to allow the Node web application to communicate with the Cassandra database.

```
$ npm install cassandra-driver
+ cassandra-driver@3.3.0
updated 2 packages in 12.379s
```

## Test Access and data retrieval

Navigate to the restRTFAP directory in the repo:
```
$ cd restRTFAP
```

> Note: the Node.js application directory structure in the restRTFAP directory was originally created with Express, using the command ```$ express restRTFAP```.

Start the Node http server using the command ```DEBUG=restrtfap:* npm start``` 
Alternatively use the simple shell script provided ```./run.sh```

Output is logged to the screen. 

> Don't exit the terminal session - keep it open for diagnostic output and to preserve the web service.

You should see the console display the following:

```
> restrtfap@0.0.0 start /Users/johndoe/Documents/My Projects/GitHub/RTFAP2/restRTFAP
> node ./bin/www

  restrtfap:server Listening on port 3000 +0ms
```

Now go to the service URL: http://localhost:3000/

At this point you will be able to run some (but not all) of the Solr queries shown below.

The queries demonstrate the use of both straightforward CQL, and CQL-Solr, but the roll-up tables have not been populated yet so these will return no data.

You can find more detailed instructions for installing Node, D3, and jquery using my example at https://github.com/simonambridge/chartDSE

