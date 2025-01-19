package com.mkdirjava;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;

public class CustomBigQuerySinkConnector extends SinkConnector {

  private Map<String, String> configProperties;

  @Override
  public String version() {
    return "1.0";
  }

  @Override
  public void start(Map<String, String> props) {
    configProperties = props;
  }

  @Override
  public Class<? extends Task> taskClass() {
    return CustomBigQuerySinkTask.class;
  }

  @Override
  public List<Map<String, String>> taskConfigs(int maxTasks) {
    List<Map<String, String>> taskConfigs = new ArrayList<>();
    for (int i = 0; i < maxTasks; i++) {
      taskConfigs.add(configProperties);
    }
    return taskConfigs;
  }

  @Override
  public void stop() {
    // Do things that are necessary to stop your connector.
  }

  @Override
  public ConfigDef config() {
    return new ConfigDef()
                .define("projectId", ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "project id that has the big query dataset")
                .define("endpoint", ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "the big query endpoint to use")
                .define("dataset", ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "dataset that has the big query dataset")
                .define("table", ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "table that has the big query dataset");
  
  }
  
}
