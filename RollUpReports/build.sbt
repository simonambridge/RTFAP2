name := "RollUpReports"

version := "1.0"

scalaVersion := "2.10.6"

val Spark = "1.6.2"
val SparkCassandra = "1.5.0"


libraryDependencies ++= Seq(
"org.apache.spark" % "spark-core_2.10" % Spark % "provided" withSources() withJavadoc(),
"org.apache.spark" %% "spark-sql" % Spark % "provided" withSources() withJavadoc(),
"org.apache.spark" %% "spark-hive" % Spark % "provided" withSources() withJavadoc(),
"com.datastax.spark" %% "spark-cassandra-connector" % SparkCassandra % "provided" withSources() withJavadoc()
)

    
