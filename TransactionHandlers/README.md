#Datastax Frad Prevention Demo
##Creating and Consuming Transactions

In this project (Idea IntelliJ) there are two different pieces
   
* Transaction Producer
* Transaction Consumer

The transaction producer Is a Scala app that leverages the Akka framework (Lightly) to generate random transactions and then place those transactions on a Kafka queue. There is some fairly trival yet fun logic for spreading the transactions proportionally across the top 100 retailers in the world based on total sales. It does a similar thing for the countries of the world based on population. This is here strickly to make pretty graphs.

The Transaction consumer, also written in Scala, is a Spark streaming job. This job performs two main tasks. First, it consumers the the messages put on the Kafka queue. It then parses those messages, evalutes the data and flags each transaction as "APPROVED" or "REJECTED". This is the place in the job where more application specific or complex logic should be placed. In a real world application I could see a scoring model used for deciding if a transaction was accepted or rejected. You would also want to implement things like balck-list lookupsa and that sort of thing. Finally, once evaluated, the records are then written to the Datastax/Cassandra table.

The second part of the Spark job counts the number of records processed each minute and stores that data to an aggregates table. The only unique aspect of this flow is that the job also reads back from from this table and builds a rolling count of the data. THe results should look something like this.

<p align="left">
  <img src="TransCount.png" width="714" height="155" />
</p>

The details of the how to deploy this sample are located here: [DemoHowTo.md](DemoHowTo.md)
    