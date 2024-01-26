# Source and sink connectors of Flink

This lesson explains how to install, configure, and use source and sink connectors with Flink.

## Use case
We have `states.csv`, a CSV file containing information about regions in the USA. 

```csv
AL,Alabama
AK,Alaska
AZ,Arizona
AR,Arkansas
CA,California
```
This file can be used as a static data source that can be looked up by the state code. Perhaps, to enrich an order record with the state name.

We will read this file into a Flink table using the FileSystem source connector. And then join the table with a stream of orders.

## File system source 

```sql
CREATE TABLE us_states (
  state_code STRING,
  state_name  STRING
) WITH (
  'connector' = 'filesystem',
  'path' = 'file:///data/states.csv',
  'format' = 'csv'
);
```

```
SET 'sql-client.execution.result-mode' = 'tableau';
```

## Redpanda sink
Let's create a Redpanda sink using the Kafka sink connector.

```sql
CREATE TABLE states WITH (
    'connector' = 'kafka',
    'topic' = 'us_states',
    'properties.bootstrap.servers' = 'redpanda:9092',
    'format' = 'json'
) AS
SELECT
  state_code,
  state_name
FROM us_states;
```
