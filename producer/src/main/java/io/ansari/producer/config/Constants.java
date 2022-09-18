package io.ansari.producer.config;

public final class Constants {

    public static final String HTTP_CONNECT_TIMEOUT_SEC = "${http.client.connectTimeoutSec}";
    public static final String MAX_CONCURRENCY = "${kinesis.maxConcurrency}";
    public static final String MAX_PENDING_CONNECTIONS_ACQUIRE = "${kinesis.maxPendingConnectionAcquires}";
    public static final String SECRET_NAME = "token";
    public static final String TWITTER_STREAM_BASE_URL = "https://api.twitter.com/2/tweets/search/stream";
    public static final String TWITTER_STREAM_QUERY_PARAM = "?expansions=author_id&user.fields=description";
    public static final String TWITTER_TOKEN = "TWITTER_TOKEN";
    public static final String WORKER_THREAD_POOL_COUNT = "${worker.threadPool.count}";

    private Constants() {}
}
