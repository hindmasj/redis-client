package io.github.hindmasj.redis.client;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileParserHelperTest{

  private FileParserHelper testSubject;

  @Before
  public void init(){
    testSubject=new FileParserHelper();
  }

  @Test
  public void testEmptyLine(){
    assertNull(testSubject.parseLine(""));
  }

  @Test
  public void testPureComments(){
    assertNull(testSubject.parseLine("#"));
    assertNull(testSubject.parseLine("# some text"));
    assertNull(testSubject.parseLine("#\tsome text"));
  }

  @Test
  public void testNoComments(){
    FileRecordBean bean=testSubject.parseLine("fred\t99\tFRED");
    assertEquals("fred",bean.getName());
    assertEquals(99,bean.getCode());
    assertEquals("FRED",bean.getAlias());
    assertEquals("",bean.getComment());
  }

  @Test
  public void testWithComment(){
    FileRecordBean bean=testSubject.parseLine("fred\t99\tFRED\t\t# some text");
    assertEquals("fred",bean.getName());
    assertEquals(99,bean.getCode());
    assertEquals("FRED",bean.getAlias());
    assertEquals("some text",bean.getComment());
  }

  @Test
  public void testWithMultiAlias(){
    FileRecordBean bean=testSubject.parseLine("fred\t99\tFRED\tFRED2\t# some text");
    assertEquals("fred",bean.getName());
    assertEquals(99,bean.getCode());
    assertEquals("FRED, FRED2",bean.getAlias());
    assertEquals("some text",bean.getComment());
  }

  @Test
  public void testNoAlias(){
    FileRecordBean bean=testSubject.parseLine("fred\t99\t\t\t# some text");
    assertEquals("fred",bean.getName());
    assertEquals(99,bean.getCode());
    assertEquals("",bean.getAlias());
    assertEquals("some text",bean.getComment());
  }

  @Test
  public void testHashName(){
    FileRecordBean bean=testSubject.parseLine("#\t99\t\t\t# some text");
    assertEquals("#",bean.getName());
    assertEquals(99,bean.getCode());
    assertEquals("",bean.getAlias());
    assertEquals("some text",bean.getComment());
  }
}
