#Datastax Frad Prevention Demo
##Creating and Consuming Transactions

In this project (Idea IntelliJ) there are two different pieces
   
* Transaction Producer
* Transaction Consumer

The transaction producer Is a scala app that leverages the Akka framework (Lightly) to generate random transactions and then place those transactions on a Kafka queue.

The Transaction consumer, also written in Scala, is a Spark streaming job. 
    