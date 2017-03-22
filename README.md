# ToyJMS
A toy project, using SpringBoot, ExtJS, JMS(ActiveMQ)

## Start Database locally through Docker (to be run from /storage folder)

`docker run --name toyjms-postgres -e POSTGRES_USER=user -e POSTGRES_PASSWORD=password -e POSTGRES_DB=toyjms -d -p 5432:5432 postgres`

you should be able to connect to the database on the  image via
`psql -h localhost -p 5432 -U user -d toyjms`

`docker ps -a` to see the generated container id

`docker exec -it [container_id] bash` in case you want to check the database directly in the docker image

`docker stop [container_id]` `docker rm [container_id]` if needed

## Launch ActiveMQ:

`mvn org.apache.activemq.tooling:maven-activemq-plugin:5.2.0:run`

## Launch Producer:

`mvn clean compile exec:java -pl producer -Dexec.mainClass=com.amtware.toyjms.producer.App`

## Launch Consumer:

`mvn clean compile exec:java -pl consumer -Dexec.mainClass=com.amtware.toyjms.consumer.App`

