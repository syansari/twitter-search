package io.ansari.search.config;

import io.github.acm19.aws.interceptor.http.AwsRequestSigningApacheInterceptor;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams;
import software.amazon.awssdk.regions.Region;

import static io.ansari.search.config.Constants.*;

@Configuration
public class ApplicationConfiguration {

  @Bean
  RestHighLevelClient restHighLevelClient(@Value(ES_PORT) int esPort,
                                          @Value(ES_SCHEME) String esScheme,
                                          @Value(ES_HOSTNAME) String esHostname) {

    var credentials = DefaultCredentialsProvider.create();
    var params =
        Aws4SignerParams.builder()
            .awsCredentials(credentials.resolveCredentials())
            .signingName("es")
            .signingRegion(Region.US_EAST_1)
            .build();

    AwsCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();
    Aws4Signer signer = Aws4Signer.create();

    HttpRequestInterceptor interceptor =
        new AwsRequestSigningApacheInterceptor("es", signer, credentialsProvider, Region.US_EAST_1);

    return new RestHighLevelClient(RestClient.builder(
                    new HttpHost(
                            esHostname,
                            esPort,
                            esScheme))
            .setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)));
  }
}
