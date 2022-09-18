package io.ansari.producer.config;

import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.ansari.producer.config.Constants.*;

@Configuration
public class TwitterConfiguration {

    @Bean
    HttpClient httpClient(@Value(HTTP_CONNECT_TIMEOUT_SEC) int timeout) {

        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(timeout))
                .build();
    }

    @Bean
    HttpRequest httpRequest(@Qualifier(TWITTER_TOKEN)String twitterToken) {

        return HttpRequest.newBuilder()
                .GET()
                .header("Authorization", String.format("Bearer %s", twitterToken))
                .uri(URI.create(TWITTER_STREAM_BASE_URL.concat(TWITTER_STREAM_QUERY_PARAM)))
                .build();
    }

    @Bean
    KinesisAsyncClient kinesisClient(@Value(MAX_CONCURRENCY) int maxConcurrency,
                                     @Value(MAX_PENDING_CONNECTIONS_ACQUIRE) int maxConnections) {

        return KinesisAsyncClient.builder()
                .httpClientBuilder(NettyNioAsyncHttpClient.builder()
                        .maxConcurrency(maxConcurrency)
                        .maxPendingConnectionAcquires(maxConnections))
                .build();
    }

    @Bean
    ExecutorService executorService(@Value(WORKER_THREAD_POOL_COUNT) int count) {

        return Executors.newFixedThreadPool(count);
    }

    @Bean
    @Qualifier(TWITTER_TOKEN)
    String TwitterBearerToken() {

        Region region = Region.US_EAST_1;
        SecretsManagerClient secretsClient = SecretsManagerClient.builder()
                .region(region)
                .build();

        try {

            GetSecretValueRequest valueRequest =
                    GetSecretValueRequest.builder().secretId(SECRET_NAME).build();

            GetSecretValueResponse valueResponse = secretsClient.getSecretValue(valueRequest);
            return JsonParser.parseString(valueResponse.secretString()).getAsJsonObject().get(TWITTER_TOKEN).getAsString();

        } catch (SecretsManagerException e) {
            throw new RuntimeException("Error fetch secret from secret manager", e);
        }
    }
}
