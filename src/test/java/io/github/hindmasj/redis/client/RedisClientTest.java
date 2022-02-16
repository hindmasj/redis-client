package io.github.hindmasj.redis.client;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class RedisClientTest{

  private Config config;

  @Test
  public void testGetConfig(){
    config=RedisClient.getConfig();
    assertNotNull(config);
  }

  @Test
  public void testGetHost(){
    assertEquals("mary",RedisClient.getRedisHost());
  }

  @Test
  public void testGetPort(){
    assertEquals(1234,RedisClient.getRedisPort());
  }

  @Test
  public void testGetPassword(){
    assertEquals("flipflop",RedisClient.getRedisPassword());
  }

}
