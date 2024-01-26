package com.redpanda.flink.stateful;

import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.StatementSet;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;

import static org.apache.flink.table.api.Expressions.$;

public class StatefulCompleted {

    public static void main(String[] args) throws Exception {
        EnvironmentSettings settings = EnvironmentSettings
                .newInstance()
                .inStreamingMode()
                .build();

        TableEnvironment tableEnv = TableEnvironment.create(settings);

        //Create the source table from a Redpanda topic
        String sourceTableSql = "CREATE TABLE OrdersTable (\n" +
                "  `id` INT,\n" +
                "  `amount` FLOAT,\n" +
                "  `region` STRING,\n" +
                "  `ts` TIMESTAMP(3)\n" +
                ") WITH (\n" +
                "  'connector' = 'kafka',\n" +
                "  'topic' = 'orders',\n" +
                "  'properties.bootstrap.servers' = 'redpanda:9092',\n" +
                "  'properties.group.id' = 'testGroup',\n" +
                "  'properties.auto.offset.reset' = 'earliest',\n" +
                "  'format' = 'json'\n" +
                ")";

        // Register the source table
        tableEnv.executeSql(sourceTableSql);

        // Create the sink table aggregating sales by the region
        String saleByRegionSql = "CREATE TABLE SalesByRegion(\n" +
                "  region STRING NOT NULL,\n" +
                "  total_sales FLOAT,\n" +
                "  PRIMARY KEY (region) NOT ENFORCED\n" +
                ") WITH (\n" +
                "   'connector' = 'jdbc',\n" +
                "   'url' = 'jdbc:mysql://mysql:3306/sales_stats',\n" +
                "   'table-name' = 'sales_by_region',\n" +
                "   'username' = 'mysqluser',\n" +
                "   'password' = 'mysqlpw'\n" +
                ")";

        // Register the sink table for sales
        tableEnv.executeSql(saleByRegionSql);

        // Create a StatementSet as we have more than one INSERT statement to execute
        StatementSet statementSet = tableEnv.createStatementSet();

        // Aggregate orders by region to calculate total sales by region
        String aggregationQuery = "SELECT region, SUM(amount) as total_sales FROM OrdersTable GROUP BY region";
        // Sink the aggregated query results into SalesByRegion
        statementSet.add(tableEnv.sqlQuery(aggregationQuery).insertInto("SalesByRegion"));


        String orderByRegionSql = "CREATE TABLE OrdersByRegion(\n" +
                "  region STRING NOT NULL,\n" +
                "  total_orders BIGINT,\n" +
                "  PRIMARY KEY (region) NOT ENFORCED\n" +
                ") WITH (\n" +
                "   'connector' = 'jdbc',\n" +
                "   'url' = 'jdbc:mysql://mysql:3306/sales_stats',\n" +
                "   'table-name' = 'orders_by_region',\n" +
                "   'username' = 'mysqluser',\n" +
                "   'password' = 'mysqlpw'\n" +
                ")";

        // Register the sink table for order summary
        tableEnv.executeSql(orderByRegionSql);

        Table ordersTbl = tableEnv.from("OrdersTable");
        Table results = ordersTbl
                .groupBy($("region"))
                .select(
                        $("region"),
                        $("amount").count().as("total_orders")
                );

        // Sink the aggregated query results into OrdersByRegion
        statementSet.add(results.insertInto("OrdersByRegion"));

        // Finally, execute the StatementSet
        statementSet.execute();
    }

}
