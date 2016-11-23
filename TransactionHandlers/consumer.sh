dse spark-submit --master spark://127.0.0.1:7077 --packages org.apache.spark:spark-streaming-kafka_2.10:1.6.2 --class TransactionConsumer consumer/target/scala-2.10/consumer_2.10-0.1.jar
