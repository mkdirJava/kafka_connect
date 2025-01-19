package com.mkdirjava;

import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.transforms.predicates.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomLengthPredicate<R extends ConnectRecord<R>> implements Predicate<R> {

    private static final Logger log = LoggerFactory.getLogger(CustomLengthPredicate.class);

    private String fieldName;
    private Integer length;

    @Override
    public void configure(Map<String, ?> configs) {
        fieldName = (String) configs.get("field.name");
        length = Integer.valueOf( (String) configs.get("field.greater.or.equal.to.length"));
    }

    @Override
    public boolean test(R record) {
        log.debug("custom predicate with data: {}", record.value());
        var value = record.value();
        if (value instanceof Struct castedValue) {
            return ((String) castedValue.get(fieldName)).length() >= length;
        }
        return false;
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef()
                .define("field.name", ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "Field name to check")
                .define("field.greater.or.equal.to.length", ConfigDef.Type.INT, ConfigDef.Importance.HIGH,
                        "lenght of value greate or equal returns possitve");
    }

    @Override
    public void close() {
        // Clean up resources
    }
}
