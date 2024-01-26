package com.redpanda.flink.sourcessinks;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.formats.json.JsonDeserializationSchema;
import org.apache.flink.formats.json.JsonSerializationSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class KafkaSourceSinkCompleted {
    private final static String REDPANDA_URL = "redpanda:9092";

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaSource<Order> kafkaSource = KafkaSource.<Order>builder()
                .setBootstrapServers(REDPANDA_URL)
                .setTopics("orders-raw")
                .setGroupId("my-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new JsonDeserializationSchema<>(Order.class))
                .build();

        KafkaRecordSerializationSchema<Order> serializer = KafkaRecordSerializationSchema.builder()
                .setValueSerializationSchema(new JsonSerializationSchema<Order>())
                .setTopic("orders")
                .build();

        KafkaSink<Order> kafkaSink = KafkaSink.<Order>builder()
                .setBootstrapServers(REDPANDA_URL)
                .setRecordSerializer(serializer)
                .build();

        env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "redpanda-source")
                .sinkTo(kafkaSink);

        env.execute("kafka-source-sink");
    }
}
