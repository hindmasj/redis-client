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
    jedisPool=new JedisPool(poolConfig,getRedisHost(),getRedisPort());

    RedisClient client=new RedisClient();
    client.run(argv);
  }

  private void run(String[] argv){
    logger.info("Now running");
    try(Jedis connection=jedisPool.getResource()){
      connection.auth(getRedisPassword());
      connection.set("clientName","jedis");
      logger.info(String.format("Bill is: %s",connection.get("bill")));
      logger.info(String.format("Fred is: %s",connection.get("fred")));
    }
    logger.info("Run complete");
  }

  private static String getRedisPassword(){
    String cipherpass=config.getString(CFG_AUTH_PASSWORD);
    Cryptography decryptor=new Cryptography(config);
    return decryptor.decrypt(cipherpass);
  }

  private static String getRedisHost(){
    return config.getString(CFG_SERVER_HOST);
  }

  private static int getRedisPort(){
    return config.getInt(CFG_SERVER_PORT);
  }

  static Config getConfig(){
    return config;
  }

}
