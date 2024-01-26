
Let's create a Java project first.

> This project works with Flink 1.16.0. First, it didn't work with the latest version, 1.18.0

https://redpanda.com/blog/apache-flink-redpanda-real-time-word-count-application

Make sure you have Maven installed and `mvn` command available in the path.

## Generate a project

```bash
mvn archetype:generate                \
  -DarchetypeGroupId=org.apache.flink   \
  -DarchetypeArtifactId=flink-quickstart-java \
  -DarchetypeVersion=1.16.0
```
Provide values as follows:
- **groupId** - redpanda.masterclass.flink
- **artifactId** - flink-quickstart
- **packaging** - jar

The generated project already included the Maven dependency for the DataStreaming API for Java.

```xml
<dependency>
			<groupId>org.apache.flink</groupId>
			<artifactId>flink-streaming-java</artifactId>
			<version>${flink.version}</version>
			<scope>provided</scope>
		</dependency>
```
## Import the project into IDE
Once the project folder and files have been created, we recommend that you import this project into your IDE for developing and testing.

IntelliJ IDEA supports Maven projects out-of-the-box. Eclipse offers the m2e plugin to import Maven projects.

Note: The default JVM heap size for Java may be too small for Flink and you have to manually increase it. In Eclipse, choose Run Configurations -> Arguments and write into the VM Arguments box: -Xmx800m. In IntelliJ IDEA recommended way to change JVM options is from the Help | Edit Custom VM Options menu. See this article for details.

Note on IntelliJ: To make the applications run within IntelliJ IDEA, it is necessary to tick the Include dependencies with "Provided" scope box in the run configuration. If this option is not available (possibly due to using an older IntelliJ IDEA version), then a workaround is to create a test that calls the application’s main() method.

## Write some code

```java
public class DataStreamJob {

	public static void main(String[] args) throws Exception {
		// Sets up the execution environment, which is the main entry point
		// to building Flink applications.
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		DataStream<Long> numbers = env.fromSequence(1,100);

		numbers.map(new MapFunction<Long,Long>() {
			@Override
			public Long map(Long value) throws Exception {
				return value * 2;
			}
		}).print();

		// Execute program, beginning computation.
		env.execute("Flink Java API Skeleton");
	}
}
```

## Building the project

Build the project by running:

```
mvn clean package
```

## Adding Kafka and FileSystem connectors

Connector

```xml
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-connector-kafka</artifactId>
        <version>1.16.0</version>
    </dependency>

    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-connector-files</artifactId>
        <version>1.16.0</version>
    </dependency>
```
For reference, read https://github.com/redpanda-data-blog/2022-apache-flink-wordcount/blob/main/pom.xml
