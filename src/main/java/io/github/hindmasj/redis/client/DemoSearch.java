package io.github.hindmasj.redis.client;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class DemoSearch{
  public static final String[] sampleTerms=new String[]{
    "111.102.105.112","172.78.81.101","0.0.0.0"};

  private static final String LINK_MATCHER=GeoipFileParser.LINK_PREFIX+".+";
  private static final Logger logger = LogManager.getLogger();

  private final JedisPool jedisPool=RedisClient.createJedisPool();
  private final String password=RedisClient.getRedisPassword();

  public static final void main(String[] argv){
    logger.info("Redis Demo Search Application");
    DemoSearch client=new DemoSearch();
    for(String term : sampleTerms){
      client.doSearch(term);
    }
  }

  public void doSearch(String address){
    logger.info("Searching for "+address);
    try(Jedis connection=jedisPool.getResource()){
      connection.auth(password);
      String key=formKey(address);
      String result=searchOnKey(connection,key);
      if(isALink(result)){
        key=formKeyFromLink(result);
        result=searchOnKey(connection,key);
      }
      logger.info("Result is "+result);

    }
  }

  public String formKey(String term){
    return GeoipFileParser.KEY_PREFIX+term+GeoipFileParser.LINK_SUFFIX;
  }

  public String searchOnKey(Jedis connection, String key){
    return connection.get(key);
  }

  public boolean isALink(String value){
    if(value==null)return false;
    if(value.matches(LINK_MATCHER))return true;
    return false;
  }

  public String formKeyFromLink(String link){
    return GeoipFileParser.KEY_PREFIX+link.substring(1);
  }

}
