package io.github.hindmasj.redis.client;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServicesFileParserTest{

  private ServicesFileParser testSubject;

  @Before
  public void init(){
    testSubject=new ServicesFileParser();
  }

  @Test
  public void testEmptyLine(){
    assertNull(testSubject.parseFileLine(""));
  }

  @Test
  public void testPureComments(){
    assertNull(testSubject.parseFileLine("#"));
    assertNull(testSubject.parseFileLine("# some text"));
    assertNull(testSubject.parseFileLine("#\tsome text"));
  }

  @Test
  public void testWithComment(){
    ServiceBean bean=testSubject.parseFileLine("tcpmux          1/tcp                           # TCP port service multiplexer");
    assertEquals("tcpmux",bean.getName());
    assertEquals(1,bean.getCode());
    assertEquals("tcp",bean.getProtocol());
    assertNull(bean.getAlias());
    assertEquals("TCP port service multiplexer",bean.getComment());
  }

  @Test
  public void testWithAlias(){
    ServiceBean bean=testSubject.parseFileLine("systat          11/tcp          users");
    assertEquals("systat",bean.getName());
    assertEquals(11,bean.getCode());
    assertEquals("tcp",bean.getProtocol());
    assertEquals("users",bean.getAlias());
    assertNull(bean.getComment());
  }

  @Test
  public void testWithTwoAliases(){
    ServiceBean bean=testSubject.parseFileLine("discard         9/udp           sink null");
    assertEquals("discard",bean.getName());
    assertEquals(9,bean.getCode());
    assertEquals("udp",bean.getProtocol());
    assertEquals("sink, null",bean.getAlias());
    assertNull(bean.getComment());
  }

  @Test
  public void testWithEverything(){
    ServiceBean bean=testSubject.parseFileLine("discard         9/tcp           sink null # this is some text");
    assertEquals("discard",bean.getName());
    assertEquals(9,bean.getCode());
    assertEquals("tcp",bean.getProtocol());
    assertEquals("sink, null",bean.getAlias());
    assertEquals("this is some text",bean.getComment());
  }

}
