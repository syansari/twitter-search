package io.ansari.consumer.config;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.core.internal.http.loader.DefaultSdkHttpClientBuilder;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.ansari.consumer.config.Constants.*;

@Configuration
public class TwitterConfiguration {

  @Bean
  HttpClient ESServiceHttpEndPoint(@Value(ES_SERVICE_CONNECT_TIMEOUT_SEC) int connectTimeout) {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(connectTimeout))
                .build();
    }

    @Bean
    KinesisAsyncClient kinesisAsyncClient(@Value(MAX_CONCURRENCY) int maxConcurrency, @Value(MAX_PENDING_CONNECTIONS_ACQUIRE) int maxConnections) {

        return KinesisAsyncClient.builder()
                .httpClientBuilder(NettyNioAsyncHttpClient.builder()
                        .maxConcurrency(maxConcurrency)
                        .maxPendingConnectionAcquires(maxConnections))
                .build();
    }

    @Bean
    KinesisClient kinesisClient() {

        return KinesisClient.builder()
                .httpClientBuilder(new DefaultSdkHttpClientBuilder())
                .build();
    }

    @Bean
    ExecutorService executors(@Value(WORKER_THREAD_POOL_COUNT) int count)
    {
        return Executors.newFixedThreadPool(count);
    }

    @Bean
    AtomicBoolean stopConsumer() {
        return new AtomicBoolean(false);
    }


    @Bean
    DynamoDB dynamoDBClient(@Value(DYNAMODB_ENDPOINT) String endpoint) {
        AmazonDynamoDB dynamoDBClient =  AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder
                                .EndpointConfiguration(endpoint,
                                Regions.US_EAST_1.getName()))
                .build();
        return new DynamoDB(dynamoDBClient);
    }

    @Bean
    Table dynamoTable(DynamoDB dynamoDB, @Value(DYNAMODB_TABLE_NAME) String tableName) {
        return dynamoDB.getTable(tableName);
    }
}