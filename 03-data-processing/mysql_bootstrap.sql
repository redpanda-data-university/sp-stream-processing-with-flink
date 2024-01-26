CREATE DATABASE IF NOT EXISTS sales_stats;
USE sales_stats;

GRANT ALL PRIVILEGES ON sales_stats.* TO 'mysqluser';
GRANT FILE on *.* to 'mysqluser';