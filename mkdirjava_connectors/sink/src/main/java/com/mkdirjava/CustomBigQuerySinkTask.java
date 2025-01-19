package com.mkdirjava;

import com.google.cloud.bigquery.JobException;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import com.mkdirjava.bigquery.BigQueryDAO;

public class CustomBigQuerySinkTask extends SinkTask {

  private static final Logger log = LoggerFactory.getLogger(CustomBigQuerySinkTask.class);
  private String dataset;
  private BigQueryDAO bigQueryDAO;
  private String table;

  @Override
  public String version() {
    return "1.0";
  }

  @Override
  public void start(Map<String, String> props) {
    log.info("starting custom sink task with props: {}", props);
    dataset = props.get("dataset");
    table = props.get("table");
    bigQueryDAO = new BigQueryDAO(props.get("projectId"), props.get("endpoint"));
  }

  @Override
  public void put(Collection<SinkRecord> records) {
    // Process the records
    for (SinkRecord record : records) {
      log.debug("Processing record: {}", record);
      Struct value = (Struct) record.value();
      try {
        bigQueryDAO.insertData(dataset, table, String.valueOf(record.kafkaOffset()), Map.of(
          "id",value.get("id"),
          "name",value.get("name"),
          "createdTimeStamp",ZonedDateTime.now(ZoneOffset.UTC).toString()
        ));
      } catch (JobException | InterruptedException e) {
        e.printStackTrace();
        log.info("could not process record: {}", record);
      }

    }

  }

  @Override
  public void stop() {
    log.info("stopping custom sink");
  }

}