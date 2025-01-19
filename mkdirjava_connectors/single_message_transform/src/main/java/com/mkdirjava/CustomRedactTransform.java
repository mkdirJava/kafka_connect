package com.mkdirjava;

import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.transforms.Transformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomRedactTransform<R extends ConnectRecord<R>> implements Transformation<R> {
    private static final Logger log = LoggerFactory.getLogger(CustomRedactTransform.class);

    private String fieldName;
    private Integer shouldRedactLength;

    @Override
    public void configure(Map<String, ?> configs) {
        fieldName = (String) configs.get("field.name");
        shouldRedactLength = Integer.parseInt((String) configs.get("should.redact.length"));

        if (fieldName == null) {
            throw new ConfigException("CustomTransform requires should.redact.length configuration");
        }
    }

    @Override
    public R apply(R record) {
        var value = record.value();
        log.debug("Custom Redact Transform, shouldRedactLength: {}", value);
        if (value instanceof Struct structValue) {
            var fieldValue = structValue.get(fieldName);
            if (fieldValue instanceof String strValue) {
                if (strValue.length() >= shouldRedactLength) {
                    structValue.put(fieldName, String.format("REDACTED, origional %s", fieldValue));
                    return record.newRecord(record.topic(), record.kafkaPartition(), record.keySchema(), record.key(),
                            record.valueSchema(), value, record.timestamp());
                }
            }
        }
        return record;
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef()
                .define("field.name", ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "Field name to modify")
                .define("should.redact.length", ConfigDef.Type.INT, ConfigDef.Importance.HIGH,
                        "Convert odd length strings to redacted");
    }

    @Override
    public void close() {
        // Clean up resources
    }

}
