# Twitter Search Application

  ### This application consumes tweets from Twitter API and write to DB to make it searchable for user.  
  #### Ready to run on AWS cloud with minimal setup. 


  ![Application Flow](/Users/ansari/Documents/misc/tweet-app.jpg)


 - Consume tweets directly from Twitter API via long-running HTTPS connection. 
 - Sends the tweets to ingestor service via Kinesis stream.
 - Ingestor service writes the user information to dynamoDB. Complete tweet including metadata is stored in an ElasticSearch index.
 - End user can request tweets or user information via REST API.