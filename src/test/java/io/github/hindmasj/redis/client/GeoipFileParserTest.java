package io.github.hindmasj.redis.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.Set;

import com.typesafe.config.Config;

import org.apache.commons.io.FileUtils;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GeoipFileParserTest{

  private static final String REF_FILE_OUTPUT="data/geoipv4-reference.output";

  private Config config;
  private Path outputFile;
  private Path expectedFile;

  private GeoipFileParser testSubject;

  @Before
  public void init() throws IOException{
    config=RedisClient.getConfig();
    FileAttribute<Set<PosixFilePermission>> writable=PosixFilePermissions.asFileAttribute​(
      PosixFilePermissions.fromString("rw-rw-r--")
    );
    outputFile=Files.createTempFile("output-",".output",writable);
    testSubject=new GeoipFileParser(config);
  }

  @Test
  public void testLoadFile(){
     testSubject.loadFile();
     assertEquals(config.getString(GeoipFileParser.CFG_INPUT_FILE),testSubject.getFileName());
     assertTrue(testSubject.isFileLoaded());
  }

  @Test
  public void testParseFile(){
    testSubject.loadFile();
    testSubject.parseFile();
    assertTrue(testSubject.isFileParsed());
    assertEquals(6,testSubject.getRecordCount());
  }

  @Test
  public void testWriteFile(){
    testSubject.loadFile();
    testSubject.parseFile();
    try{
      testSubject.writeFile(outputFile);
      assertTrue("Does output file exist",Files.exists(outputFile));
      assertTrue("Does output file match reference",FileUtils.contentEquals(
      new File(REF_FILE_OUTPUT),outputFile.toFile()
      ));
    }catch(FileNotFoundException e){
      fail(e.getMessage());
    }catch(IOException e){
      fail(e.getMessage());
    }
  }

  @After
  public void tearDown() throws IOException{
    Files.deleteIfExists(outputFile);
  }

}
