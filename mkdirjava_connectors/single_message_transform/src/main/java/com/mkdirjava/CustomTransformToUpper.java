package com.mkdirjava;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.transforms.Transformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class CustomTransformToUpper<R extends ConnectRecord<R>> implements Transformation<R> {
    private static final Logger log = LoggerFactory.getLogger(CustomTransformToUpper.class);

    private String fieldName;

    @Override
    public void configure(Map<String, ?> configs) {
        fieldName = (String) configs.get("field.name");
        if (fieldName == null) {
            throw new ConfigException("CustomTransform requires field.name configuration");
        }
    }

    @Override
    public R apply(R record) {
        var value = record.value();
        log.debug("Custom Transform To Upper with value: {}", value);
        if (value instanceof Struct structValue) {
            var fieldValue = structValue.get(fieldName);
            if (fieldValue instanceof String strValue) {
                structValue.put(fieldName, strValue.toUpperCase());
                // return record.newRecord(record.topic(), record.kafkaPartition(),
                // record.keySchema(), record.key(), record.valueSchema(), value,
                // record.timestamp());
            }
        }
        return record;
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef()
                .define("field.name", ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "Field name to modify");
    }

    @Override
    public void close() {
        // Clean up resources
    }

}