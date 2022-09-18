package io.ansari.consumer.config;

public class Constants {

    public static final String DYNAMODB_ENDPOINT = "${dynamodb.endpoint}";
    public static final String DYNAMODB_TABLE_NAME = "${dynamodb.tableName}";
    public static final String ES_SERVICE_CONNECT_TIMEOUT_SEC = "${esService.connectTimeoutSec}";
    public static final String MAX_CONCURRENCY = "${kinesis.maxConcurrency}";
    public static final String MAX_PENDING_CONNECTIONS_ACQUIRE = "${kinesis.maxPendingConnectionAcquires}";
    public static final String WORKER_THREAD_POOL_COUNT = "${worker.threadPool.count}";
    private Constants() {}
}



