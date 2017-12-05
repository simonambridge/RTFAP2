##################
# Zoopkeeper and Kafka demo start script
# V0.1 1/12/17
#################
echo "\$KAFKA_HOME=/usr/local/kafka/kafka_2.11-1.0.0"
cd $KAFKA_HOME
echo "Start Zookeeper"
sudo -b nohup $KAFKA_HOME/bin/zookeeper-server-start.sh $KAFKA_HOME/config/zookeeper.properties > $KAFKA_HOME/logs/nohupz.out 2>&1
echo ""
echo "Start Kafka"
sudo -b nohup $KAFKA_HOME/bin/kafka-server-start.sh $KAFKA_HOME/config/server.properties > $KAFKA_HOME/logs/nohupk.out 2>&1 
echo ""
echo "Create New Topic:"
$KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --create --replication-factor 1 --partitions 1 --topic NewTransactions
echo ""
echo "Confirm that we created it correctly:"
$KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --list
echo ""
echo "Set msg. retention period to 1 hour (360000 milliseconds)"
$KAFKA_HOME/bin/kafka-configs.sh --zookeeper localhost:2181 --entity-type topics --alter --add-config retention.ms=3600000 --entity-name NewTransactions
echo ""
echo "Display topic configuration details:"
$KAFKA_HOME/bin/kafka-configs.sh --zookeeper localhost:2181 --describe --entity-name NewTransactions --entity-type topics
echo ""
echo "Describe the 'NewTransactions' Topic:"
$KAFKA_HOME/bin/kafka-topics.sh --describe --zookeeper localhost:2181 --topic NewTransactions
echo ""
echo "List messages from beginning:"
$KAFKA_HOME/bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic NewTransactions --from-beginning
