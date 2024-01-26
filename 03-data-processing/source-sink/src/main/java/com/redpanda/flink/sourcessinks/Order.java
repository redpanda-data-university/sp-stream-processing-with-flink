package com.redpanda.flink.sourcessinks;

public class Order {
    public int id;
    public String ts;
    public float amount;
    public String region;

    public Order() {
    }

    public Order(int orderId, String ts, float amount, String region) {
        this.id = orderId;
        this.ts = ts;
        this.amount = amount;
        this.region = region;
    }
}
