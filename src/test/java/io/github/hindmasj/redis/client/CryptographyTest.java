package io.github.hindmasj.redis.client;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class CryptographyTest{

  private Cryptography testSubject;

  private String plaintext="hellobob";
  private String cyphertext="302E9E67F184AFC69D1267FC0D7BEF720D818E1F909F5CFF";
  private String cypherkey="mynameiskey";

  @Before
  public void init(){
    Config config=ConfigFactory.empty().withValue(
      Cryptography.CFG_CKEY,
      ConfigValueFactory.fromAnyRef(cypherkey));
    testSubject=new Cryptography(config);
  }

  @Test
  /* Can only test that ciphertext is not the plaintext, as salting and IV creation
   * mean that each enryption is unique.
   */
  public void testEncrypt(){
    String actual=testSubject.encrypt(plaintext);
    assertNotEquals(plaintext,actual);
  }

  @Test
  /* Decrypt works each time as the salt and IV are built into the ciphertext. */
  public void testDecrypt(){
    String actual=testSubject.decrypt(cyphertext);
    assertEquals(plaintext,actual);
  }

  @Test
  public void testBothWay(){
    String cypher=testSubject.encrypt(plaintext);
    String actual=testSubject.decrypt(cypher);
    assertEquals(plaintext,actual);
  }

}
