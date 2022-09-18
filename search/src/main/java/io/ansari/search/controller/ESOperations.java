package io.ansari.search.controller;

import io.ansari.search.db.DBProcessor;
import io.ansari.search.model.Tweet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/es")
public class ESOperations {

  private static final Logger LOGGER = LogManager.getLogger(ESOperations.class);
  private static final String QUERY_PARAM = "phrase";

  private final DBProcessor dbProcessor;

  public ESOperations(DBProcessor dbProcessor) {

    this.dbProcessor = dbProcessor;
  }

  /** Add document to Elasticsearch index */
  @PostMapping(path = "/add")
  public ResponseEntity<String> addDocument(@RequestBody Tweet tweet) {

      LOGGER.debug("message received: {}", tweet.getPayload());

    try {
      dbProcessor.addDocument(tweet);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
    return ResponseEntity.ok("Document added successfully");
  }

  /** Search document in Elastic search Index */
  @GetMapping(path = "/search")
  public ResponseEntity<List<Tweet>> searchPhrase(@RequestParam(QUERY_PARAM) String phrase) {

      try {

          List<Tweet> tweets = dbProcessor.searchPhrase(phrase);
          return ResponseEntity.ok(tweets);

      } catch (Exception e) {
          return ResponseEntity.internalServerError().build();
      }
  }
}
