package io.ansari.consumer.consume;

import io.ansari.consumer.repository.dynamodb.DynamoDBProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public final class ConsumeTweets {

    private static final Logger LOGGER = LogManager.getLogger(ConsumeTweets.class);
    private static final String KINESIS_BATCH_COUNT = "${kinesis.batchCount}";
    private static final String KINESIS_STREAM_NAME = "${kinesis.streamName}";

    private final AtomicBoolean stopConsumer;
    private final DynamoDBProcessor crud;
    private final ESProcessor esProcessor;
    private final ExecutorService executorService;
    private final KinesisClient kinesisClient;
    private final String streamName;
    private final int batchCount;

    public ConsumeTweets(@Value(KINESIS_STREAM_NAME) String streamName,
            @Value(KINESIS_BATCH_COUNT) int batchCount,
            AtomicBoolean stopConsumer,
                         DynamoDBProcessor crud,
                         ESProcessor esProcessor,
                         ExecutorService executorService,
                         KinesisClient kinesisClient) {

        this.batchCount = batchCount;
        this.crud = crud;
        this.esProcessor = esProcessor;
        this.executorService = executorService;
        this.kinesisClient = kinesisClient;
        this.stopConsumer = stopConsumer;
        this.streamName = streamName;
    }

    public void consumeTweets() {

        // Retrieve the Shards from a Stream
        DescribeStreamRequest describeStreamRequest = DescribeStreamRequest.builder()
                .streamName(streamName)
                .build();

        List<Shard> shards = new ArrayList<>();

        DescribeStreamResponse describeStreamResponse;
        do {

            describeStreamResponse = kinesisClient.describeStream(describeStreamRequest);
            shards.addAll(describeStreamResponse.streamDescription().shards());

        } while (describeStreamResponse.streamDescription().hasMoreShards());

        shards.forEach(shard -> executorService.submit(() -> readTweetsFromIndividualShards(shard)));
    }

    private void readTweetsFromIndividualShards(Shard shard) {

        GetShardIteratorRequest itReq = GetShardIteratorRequest.builder()
                .streamName(streamName)
                .shardIteratorType("TRIM_HORIZON")
                .shardId(shard.shardId())
                .build();

        GetShardIteratorResponse shardIteratorResult = null;

        try {
            shardIteratorResult = kinesisClient.getShardIterator(itReq);
        }
        catch (SdkException e) {
            LOGGER.error("Exception while getting iterator", e);
            return;
        }
        String shardIterator = shardIteratorResult.shardIterator();

        // Continuously read data records from shard.
        while (!stopConsumer.get()) {

            List<Record> records;

            // Create new GetRecordsRequest with existing shardIterator.
            GetRecordsRequest recordsRequest = GetRecordsRequest.builder()
                    .shardIterator(shardIterator)
                    .limit(batchCount)
                    .build();

            GetRecordsResponse result = kinesisClient.getRecords(recordsRequest);

            // Put result into record list. Result may be empty.
            records = result.records();

            // write to dynamodb and elasticsearch
            for (Record record : records) {
                SdkBytes byteBuffer = record.data();
                crud.writeTweetToDB(new String(byteBuffer.asByteArray()));
                esProcessor.writeToElasticSearch(new String(byteBuffer.asByteArray()));

            }
            shardIterator = result.nextShardIterator();
        }
    }
}