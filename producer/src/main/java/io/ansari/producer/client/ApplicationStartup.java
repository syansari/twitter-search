package io.ansari.producer.client;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

@Component
public class ApplicationStartup {

    private final ExecutorService executorService;
    private final TweetReader tweetReader;

    public ApplicationStartup(ExecutorService executorService, TweetReader readTweets) {

        this.executorService = executorService;
        this.tweetReader = readTweets;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup()  {

        executorService.submit(tweetReader::readTweets);
    }
}
