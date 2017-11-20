name := "RollUpReports"

version := "1.0"

scalaVersion := "2.11.8"

val Spark = "2.0.2"
val SparkCassandra = "2.0.5"


libraryDependencies ++= Seq(
"org.apache.spark" %% "spark-core" % Spark % "provided" withSources() withJavadoc(),
"org.apache.spark" %% "spark-sql" % Spark % "provided" withSources() withJavadoc(),
"org.apache.spark" %% "spark-hive" % Spark % "provided" withSources() withJavadoc(),
"com.datastax.spark" %% "spark-cassandra-connector" % SparkCassandra % "provided" withSources() withJavadoc()
)

    
