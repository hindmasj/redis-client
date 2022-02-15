package io.github.hindmasj.redis.client;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class RedisClient{

  private static final Logger logger = LogManager.getLogger();

  private static final Config config = ConfigFactory.load();

  public static final void main(String[] argv){
    logger.info("Redis Client Application");
    logger.info(String.format("Config says url=%s:%d",config.getString("server.host"),config.getInt("server.port")));
  }

}
