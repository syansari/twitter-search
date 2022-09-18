package io.ansari.producer.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.ansari.producer.produce.ProduceTweets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public final class TweetReader {

    private static final Logger LOGGER = LogManager.getLogger(TweetReader.class);
    private static final int MIN_TWEET_LENGTH = 15;

    private final HttpClient httpClient;
    private final HttpRequest httpRequest;
    private final ProduceTweets produceTweets;

    public TweetReader(HttpClient httpClient,
                       HttpRequest httpRequest, ProduceTweets produceTweets) {

        this.httpClient = httpClient;
        this.httpRequest = httpRequest;
        this.produceTweets = produceTweets;
    }

    public void readTweets()  {

        try {
        BufferedReader br = getTweetReader();
            String jsonString;
                // keep reading until the HTTP socket connection is open
                // read is block when there is no data to read
                while ((jsonString = br.readLine()) != null) {

                    if (!jsonString.isBlank() || jsonString.length() > MIN_TWEET_LENGTH) {
                        JsonObject data = (JsonObject) JsonParser.parseString(jsonString);
                        produceTweets.tweetProducer(data);
                    }
                }
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException("Unable to read status event stream", e);
            }
    }

    private BufferedReader getTweetReader()
            throws IOException, InterruptedException {

        HttpResponse<InputStream> response =
                httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());

        LOGGER.info(response.statusCode());
        LOGGER.info(response.headers());

        InputStream eventStream = response.body();
        return new BufferedReader(new InputStreamReader(eventStream));
    }
}
