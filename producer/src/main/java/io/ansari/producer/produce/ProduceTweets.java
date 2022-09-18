package io.ansari.producer.produce;

import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.KinesisException;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordResponse;

import java.nio.charset.Charset;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Component
public final class ProduceTweets {

    private static final Logger LOGGER = LogManager.getLogger(ProduceTweets.class);
    private static final String KINESIS_STREAM_NAME = "${kinesis.streamName}";

    private final ExecutorService executorService;
    private final KinesisAsyncClient kinesisAsyncClient;
    private final String streamName;

    public ProduceTweets( @Value(KINESIS_STREAM_NAME) String streamName,
            ExecutorService executorService,
                         KinesisAsyncClient kinesisAsyncClient) {

        this.executorService = executorService;
        this.kinesisAsyncClient = kinesisAsyncClient;
        this.streamName = streamName;
    }

    public void tweetProducer(JsonObject tweet) {
        Objects.requireNonNull(tweet, "tweet must not be null");

        JsonObject data = tweet.getAsJsonObject("data");
        PutRecordRequest recordRequest = PutRecordRequest.builder()
                .partitionKey(data.get("id").getAsString())
                .streamName(streamName)
                .data(SdkBytes.fromString(tweet.toString(), Charset.defaultCharset()))
                .build();

        try {
            CompletableFuture<PutRecordResponse> responseCompletableFuture =
                    kinesisAsyncClient.putRecord(recordRequest);

            responseCompletableFuture.whenCompleteAsync((r, t) -> {

                if (t != null) {
                    LOGGER.error("error writing the tweet to kinesis -----> ", t);
                    return;
                }
                LOGGER.info("message sent {}", r.toString());
            }, executorService);
        } catch (KinesisException e) {
            throw new RuntimeException("Unable to write to kinesis stream", e);
        }
    }
}
