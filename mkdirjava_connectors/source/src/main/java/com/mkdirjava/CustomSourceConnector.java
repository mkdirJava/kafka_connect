package com.mkdirjava;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomSourceConnector extends SourceConnector {

    private Map<String, String> connectorProperty;
    private static final Logger log = LoggerFactory.getLogger(CustomSourceConnector.class);
    
    @Override
    public String version() {
        log.info("source connector task version");
        return "1.0";
    }

    @Override
    public void start(Map<String, String> props) {
        log.info("source connector start");
        connectorProperty = props;
    }

    @Override
    public Class<? extends Task> taskClass() {
        log.info("source connector task class");
        return CustomBigQuerySourceTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        log.info("source connector task configs");
        var config =  new ArrayList<Map<String,String>>();
        for (int i = 0; i < maxTasks; i++) {
            config.add(connectorProperty);
        }
        return config;
    }

    @Override
    public void stop() {
        log.info("source connector stop");
        // Clean up resources
    }

    @Override
    public ConfigDef config() {
        log.debug("source connector config");
        // Define the configuration options
        return new ConfigDef()
                .define("bigquery.dataset", ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "BigQuery dataset name to pull from")
                .define("bigquery.table.name", ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "BigQuery table name to pull from")
                .define("topic", ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "topic name to push to");
    }
}