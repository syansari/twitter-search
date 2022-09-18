package io.ansari.producer.model;

import java.time.LocalDate;

/** User tweet */
public final class Tweet {

  private String payload;
  private String tweetId;
  private String hasTag;
  private LocalDate creationDate;

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getTweetId() {
        return tweetId;
    }

    public void setTweetId(String tweetId) {
        this.tweetId = tweetId;
    }

    public String getHasTag() {
        return hasTag;
    }

    public void setHasTag(String hasTag) {
        this.hasTag = hasTag;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }
}
