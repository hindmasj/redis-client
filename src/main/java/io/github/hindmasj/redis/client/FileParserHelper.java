package io.github.hindmasj.redis.client;

import java.io.*;
import java.nio.file.*;
import java.util.Map;
import java.util.stream.Stream;

import com.google.gson.Gson;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class FileParserHelper<T>{

  private static final Logger logger = LogManager.getLogger();
  private final FileLinesParser<T> parser;

  public FileParserHelper(FileLinesParser<T> parser){
    this.parser=parser;
  }

  /** Parse a file line by line, using the supplied parser to parse the line and then
    * ask the parser to store the result as it sees fit. */
  public boolean parseFile(String inputFileName){
    logger.info(String.format("Loading from %s",inputFileName));
    Path filePath=Paths.get(inputFileName);
    try{
      try(Stream<String> lines = Files.lines(filePath)) {
        lines.forEach(line -> {
          T parsedItem=parser.parseFileLine(line);
          parser.storeParsedItem(parsedItem);
        });
      }
    }catch(FileNotFoundException e){
      logger.error(String.format("Failed to find file: %s",inputFileName),e);
      return false;
    }catch(IOException e){
      logger.error(String.format("Failed to load file: %s",inputFileName),e);
      return false;
    }

    logger.info(String.format("Parsed file %s",inputFileName));
    logger.info(
      String.format("File parsed into memory with %d records.",parser.getRecordCount()));
    return true;
  }

  /** Write the parsed objects out from a map of stored objects */
  public boolean writeFile(String outputFileName, String indexPrefix, Map store){
    Gson gson=new Gson();
    try{
      RedisFileWriter writer=new RedisFileWriter(outputFileName);
      for(Object key : store.keySet()){
        Object value=store.get(key);
        writer.addEntry(indexPrefix,key.toString(),gson.toJson(value));
      }
      writer.close();
    }catch(IOException e){
      logger.error(String.format("Unable to write to file %s",outputFileName),e);
      return false;
    }
    logger.info(String.format("Records written to output file %s.",outputFileName));
    return true;
  }

}
