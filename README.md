# Kafka Connect local

This project runs a single node kafka connect cluster and demos kafka connects:

* Simple Message transformer
* Custom Kafka Connector Sink
* Custom Kafka Connector Source

This is runnable locally through the use of docker compose

You will need JAVA 21 to run this, out the box

The config for the connectors can be found in 

    kafka-connect-config

This is mounted in the kafka cluster 

## Getting Started

Steps:

1. Build the binaries

        cd mkdirjava_connectors && gradle build

    This will create jars for 
        
        * source connector
        * sink connector
        * Simple Message Transformer 

2. Run this command to test

        From project root
        
        docker compose up --exit-code-from test

    Take a look at docker-compose. This runs up the dependant services and then runs the "test" container. This test container has the source code mounted to it and then runs the e2e tests against the local docker compose instance