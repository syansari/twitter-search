package io.ansari.producer.es;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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


// for local testing only, to write tweets directly to ES index
@Component
public final class WriteToElasticSearch {

  private static final Logger LOGGER = LogManager.getLogger(WriteToElasticSearch.class);
  private final HttpClient httpClient;

  public WriteToElasticSearch(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  public void writeToES(JsonObject tweet) {

      Configuration conf = Configuration.defaultConfiguration()
              .setOptions(Option.SUPPRESS_EXCEPTIONS);
      DocumentContext documentContext = JsonPath.using(conf).parse(tweet.toString());

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
      LOGGER.info(str);

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
            .uri(URI.create("http://localhost:8092/es/add"))
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
