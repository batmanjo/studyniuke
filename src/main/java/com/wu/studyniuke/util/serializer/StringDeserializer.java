package com.wu.studyniuke.util.serializer;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * @author me
 * @create 2021-07-20-11:08
 */
public class StringDeserializer implements Deserializer<String> {
    private String encoding = "UTF8";

    public StringDeserializer() {
    }

    public void configure(Map<String, ?> configs, boolean isKey) {
        String propertyName = isKey ? "key.deserializer.encoding" : "value.deserializer.encoding";
        Object encodingValue = configs.get(propertyName);
        if (encodingValue == null) {
            encodingValue = configs.get("deserializer.encoding");
        }

        if (encodingValue instanceof String) {
            this.encoding = (String)encodingValue;
        }

    }

    public String deserialize(String topic, byte[] data) {
        try {
            System.out.println();
            return data == null ? null : new String(data, this.encoding);
        } catch (UnsupportedEncodingException var4) {
            throw new SerializationException("Error when deserializing byte[] to string due to unsupported encoding " + this.encoding);
        }
    }
}
