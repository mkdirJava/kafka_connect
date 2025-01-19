package com.mkdirjava;

import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.connect.data.Schema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mkdirjava.schema.ValueSchema;
import com.mkdirjava.bigquery.BigQueryDAO;
public class CustomBigQuerySourceTask extends SourceTask {

    private String topicName;
    private String dataset;
    private String tableName;
    private BigQueryDAO bigQueryDAO;
    private static final Logger log = LoggerFactory.getLogger(CustomBigQuerySourceTask.class);
    private final Schema valueSchema = ValueSchema.getSchema();
    @Override
    public String version() {
        return "1.0";
    }

    @Override
    public void start(Map<String, String> props) {
        log.info("starting source task with detail");
        props.forEach((key, value) -> {
            log.info(String.format("key: %s, value: %s", key, value));
        });
        topicName = props.get("topic");
        bigQueryDAO = new BigQueryDAO(props.get("projectId"), props.get("endpoint"));
        dataset = props.get("bigquery.dataset");
        tableName = props.get("bigquery.table.name");
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException  {
        var sourcePartition = new HashMap<String,Object>();
        var sourceOffset = new HashMap<String,Object>();
        var data = bigQueryDAO.getNewData(dataset, tableName);
        var processedIds = data.streamAll().map(row -> row.get("id").getStringValue()).collect(Collectors.toList());
        bigQueryDAO.setConsumed(dataset, tableName, processedIds);
        return data.streamAll().map(row -> {
            var entry = new Struct(ValueSchema.getSchema());
            var sourceName = row.get("name").getStringValue();
            entry.put("name",sourceName);
            entry.put("id",row.get("id").getLongValue());
            return new SourceRecord(sourcePartition, sourceOffset, topicName, null, valueSchema, entry);
        }).collect(Collectors.toList());
    }


    @Override
    public void stop() {
        log.info("stopping source task");
    }
}