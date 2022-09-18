package io.ansari.consumer.client;

import io.ansari.consumer.consume.ConsumeTweets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

@Component
public final class ApplicationStartup {

    private static final Logger LOGGER = LogManager.getLogger(ApplicationStartup.class);

    private final ConsumeTweets consumeTweets;
    private final ExecutorService executorService;

    public ApplicationStartup(ConsumeTweets consumeTweets, ExecutorService executorService) {

        this.consumeTweets = consumeTweets;
        this.executorService = executorService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() {

        LOGGER.info("Tweet consumer started...");
        executorService.submit(consumeTweets::consumeTweets);
    }
}
