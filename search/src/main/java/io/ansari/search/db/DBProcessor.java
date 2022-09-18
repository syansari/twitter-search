package io.ansari.search.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ansari.search.model.Tweet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public final class DBProcessor {

  private static final Logger LOGGER = LogManager.getLogger(DBProcessor.class);
  private static final String ES_INDEX = "tweets";
  private static final String SEARCH_FIELD = "payload";

  private final RestHighLevelClient client;

  public DBProcessor(RestHighLevelClient restHighLevelClient) {
    this.client = restHighLevelClient;
  }

  public void addDocument(Tweet tweet) {

    IndexRequest request = new IndexRequest(ES_INDEX);

    try {

      request.source(new ObjectMapper().writeValueAsString(tweet), XContentType.JSON);
      IndexResponse response = client.index(request, RequestOptions.DEFAULT);

      if (response.status().getStatus() == RestStatus.CREATED.getStatus()) {
        LOGGER.debug("successfully added the document");
        return;
      }
      LOGGER.error("error returned while updating the doc {}", response.status().getStatus());

    } catch (Exception e) {
      LOGGER.error("exception during document index", e);
      throw new RuntimeException(e);
    }
  }

  public List<Tweet> searchPhrase(String phrase) {

    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    sourceBuilder.query(QueryBuilders.matchQuery(SEARCH_FIELD, phrase));

    SearchRequest request = new SearchRequest();
    request.indices(ES_INDEX);
    request.source(sourceBuilder);

    try {
      SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);

      List<SearchHit> result =  Arrays.stream(searchResponse.getHits().getHits()).collect(Collectors.toList());
      return deserializeResponse(result);

    } catch (IOException e) {
      LOGGER.error("exception occurred during search", e);
      throw new RuntimeException(e);
    }
  }

  private List<Tweet> deserializeResponse(List<SearchHit> searchHits) {

    return searchHits.stream().map(s -> {
      try {
        return new ObjectMapper().readValue(s.getSourceAsString(), Tweet.class);
      } catch (JsonProcessingException e) {
        LOGGER.error("Unable to deserialize response, e");
        throw new RuntimeException(e);
      }
    }).collect(Collectors.toList());
  }
}
