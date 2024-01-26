package com.redpanda.flink.stateless;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.formats.json.JsonDeserializationSchema;
import org.apache.flink.formats.json.JsonSerializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class StatelessCompleted {
    private static final String REDPANDA_URL = "redpanda:9092";

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
                .flatMap(new RegionMapper())
                .sinkTo(kafkaSink);

        env.execute("stateless-job");
    }
    public static class LATAMFilter implements FilterFunction<Order> {
        @Override
        public boolean filter(Order order) {
            return !order.region.equalsIgnoreCase("LATAM");
        }
    }

    public static class RegionTransformer implements MapFunction<Order, Order> {
        @Override
        public Order map(Order o) throws Exception {
            return new Order(
                    o.id,
                    o.ts,
                    o.amount,
                    o.region.toUpperCase()
            );
        }
    }

    public static class RegionMapper implements FlatMapFunction<Order, Order> {
        @Override
        public void flatMap(Order o, Collector<Order> collector) throws Exception {
            if(!o.region.equalsIgnoreCase("LATAM")) {
                Order newOrder = new Order(
                        o.id,
                        o.ts,
                        o.amount,
                        o.region.toUpperCase()
                );
                collector.collect(newOrder);
            }
        }
    }
}
