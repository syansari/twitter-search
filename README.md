# Twitter Search Application

  ### This application consumes tweets from Twitter API and write to DB to make it searchable for user.  
  #### Ready to run on AWS cloud with minimal setup. 

![tweet-app](https://user-images.githubusercontent.com/20521981/190920101-f7747a63-a366-4649-9f61-ab923b24b620.jpg)


 - Consume tweets directly from Twitter API via long-running HTTPS connection. 
 - Sends the tweets to ingestor service via Kinesis stream.
 - Ingestor service writes the user information to dynamoDB. Complete tweet including metadata is stored in an ElasticSearch index.
 - End user can request tweets or user information via REST API.
