package io.ansari.consumer.repository.dynamodb;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;


@Component
public final class DynamoDBProcessor {

    private static final Logger LOGGER = LogManager.getLogger(DynamoDBProcessor.class);
    private final Table dynamoTable;

    public DynamoDBProcessor(Table dynamoDBTable) {
        this.dynamoTable = dynamoDBTable;
    }

    public void writeTweetToDB(String tweet) {

        Configuration conf = Configuration.defaultConfiguration()
                .setOptions(Option.SUPPRESS_EXCEPTIONS);
        DocumentContext documentContext = JsonPath.using(conf).parse(tweet);

        String text = null;

        try {
            text =  documentContext.read( "$.data.text");
        }
        catch (Exception e) {
            LOGGER.error("exception while reading text in tweet {}", tweet, e);
        }

        String userId = documentContext.read("$.includes.users[0].username");
        String hashtag =  documentContext.read("$.matching_rules[0].tag");
        String tweetId = documentContext.read("$.data.id");

        String str = String.format("tweetId = %s, userid = %s, hashtag = %s, text = %s",
                tweetId, userId, hashtag, text);
        LOGGER.debug(str);

        try {
            dynamoTable
                    .putItem(new Item()
                            .withPrimaryKey("tweetId", tweetId)
                            .withString("hashtag", hashtag)
                            .withNumber("creationDateInEpoch", LocalDate.now(Clock.systemDefaultZone()).toEpochDay())
                            .withJSON("payload", tweet));
        }
        catch (Exception e) {
            LOGGER.error("DB op error", e);
        }
    }
}
