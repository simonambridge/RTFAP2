val globalSettings = Seq(
  version := "0.1",
  scalaVersion := "2.10.6"
)

val akkaVersion = "2.3.12"
val sparkVersion = "1.4.1"
val sparkCassandraConnectorVersion = "1.4.0"
val cassandraDriverVersion = "2.1.7.1"
val kafkaVersion = "0.9.0.1"
val scalaTestVersion = "2.2.4"


lazy val producer = (project in file("producer"))
  .settings(name := "producer")
  .settings(globalSettings:_*)
  .settings(libraryDependencies ++= producerDeps)

lazy val consumer = (project in file("consumer"))
  .settings(name := "consumer")
  .settings(globalSettings:_*)
  .settings(libraryDependencies ++= consumerDeps)

lazy val producerDeps = Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "org.apache.kafka" % "kafka_2.10" % kafkaVersion
    exclude("javax.jms", "jms")
    exclude("com.sun.jdmk", "jmxtools")
    exclude("com.sun.jmx", "jmxri"),
  "com.datastax.cassandra" % "cassandra-driver-core" % cassandraDriverVersion
)

lazy val consumerDeps = Seq(
  "com.datastax.spark" % "spark-cassandra-connector_2.10" % sparkCassandraConnectorVersion,
  "org.apache.spark"  %% "spark-mllib"           % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-graphx"          % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-sql"             % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-streaming"       % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-streaming-kafka" % sparkVersion % "provided",
  "com.databricks"    %% "spark-csv"             % "1.2.0"
)
    