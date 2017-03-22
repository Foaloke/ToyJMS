# ToyJMS
A toy project, using SpringBoot, ExtJS, JMS(ActiveMQ)

## Launch ActiveMQ:

mvn org.apache.activemq.tooling:maven-activemq-plugin:5.2.0:run

## Launch Producer:

mvn clean compile exec:java -pl producer -Dexec.mainClass=com.amtware.toyjms.producer.App

## Launch Consumer:

mvn clean compile exec:java -pl consumer -Dexec.mainClass=com.amtware.toyjms.consumer.App

