package io.github.hindmasj.redis.client;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisClient{

  public static final String CFG_AUTH_PASSWORD="auth.password";
  public static final String CFG_SERVER_HOST="server.host";
  public static final String CFG_SERVER_PORT="server.port";

  private static final Logger logger = LogManager.getLogger();

  private static final Config config = ConfigFactory.load();

  private static final JedisPoolConfig poolConfig=new JedisPoolConfig();
  private static JedisPool jedisPool;

  public static final void main(String[] argv){
    logger.info("Redis Client Application");
    logger.info(String.format("Config says url=%s:%d",getRedisHost(),getRedisPort()));
    jedisPool=createJedisPool();

    RedisClient client=new RedisClient();
    client.runDemo(argv);
  }

  public static JedisPool createJedisPool(){
    return new JedisPool(poolConfig,getRedisHost(),getRedisPort());
  }

  private void runDemo(String[] argv){
    logger.info("Now running the demo");
    try(Jedis connection=jedisPool.getResource()){
      connection.auth(getRedisPassword());
      connection.flushDB();
      connection.set("bill","hello world");
      connection.set("fred","how are you");
      logger.info(String.format("Bill is: %s",connection.get("bill")));
      logger.info(String.format("Fred is: %s",connection.get("fred")));
      connection.flushDB();
      logger.info(String.format("Bill is now: %s",connection.get("bill")));
    }
    logger.info("Run complete");
  }

  static String getRedisPassword(){
    String cipherpass=config.getString(CFG_AUTH_PASSWORD);
    Cryptography decryptor=new Cryptography(config);
    return decryptor.decrypt(cipherpass);
  }

  static String getRedisHost(){
    return config.getString(CFG_SERVER_HOST);
  }

  static int getRedisPort(){
    return config.getInt(CFG_SERVER_PORT);
  }

  static Config getConfig(){
    return config;
  }

}
