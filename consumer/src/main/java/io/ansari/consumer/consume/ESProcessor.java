package io.ansari.consumer.consume;

import com.google.gson.Gson;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.http.Header;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.utils.ImmutableMap;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Map;


@Component
public final class ESProcessor {

  private static final Logger LOGGER = LogManager.getLogger(ESProcessor.class);
  private static final String ES_SERVICE_ENDPOINT = "${esService.endpoint}";

  private final HttpClient httpClient;
  private final String endpoint;

  public ESProcessor(@Value(ES_SERVICE_ENDPOINT) String endpoint,
                     HttpClient httpClient) {

    this.endpoint = endpoint;
    this.httpClient = httpClient;
  }

  public void writeToElasticSearch(String tweet) {

      Configuration conf = Configuration.defaultConfiguration()
              .setOptions(Option.SUPPRESS_EXCEPTIONS);
      DocumentContext documentContext = JsonPath.using(conf).parse(tweet);

      String text = null;

      try {
          text =  documentContext.read( "$.data.text");
      }
      catch (Exception e) {
          LOGGER.error("exception while parsing tweet {}",tweet,e);
      }

      String userId = documentContext.read("$.includes.users[0].username");
      String hashtag =  documentContext.read("$.matching_rules[0].tag");
      String tweetId = documentContext.read("$.data.id");

      String str = String.format("tweetId = %s, userid = %s, hashtag = %s, text = %s", tweetId, userId, hashtag, text);
      LOGGER.debug(str);

      Map<String, Object> tweetMap =
        ImmutableMap.of(
            "userId",
            userId,
            "payload",
            text,
            "hashtag",
            hashtag,
            "creationDate",
                LocalDate.now().toString());

    HttpRequest request =
        HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(new Gson().toJson(tweetMap)))
                .uri(URI.create(endpoint))
            .header(Header.CONTENT_TYPE, "application/json")
            .build();

    try {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == HttpStatusCode.OK) {
          LOGGER.debug("Successfully sent the tweet to search service");
        }
    }
    catch (IOException | InterruptedException e) {
      LOGGER.error("exception while sending request to remote", e);
    }
  }
}
