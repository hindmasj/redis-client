package io.github.hindmasj.redis.client;

import java.io.*;
import java.nio.file.*;

import java.util.Hashtable;
import java.util.Map;
import java.util.stream.Stream;

import com.google.gson.Gson;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class FileParserHelper{

  private static final Logger logger = LogManager.getLogger();

  private final Map<Integer,FileRecordBean> recordMap=
    new Hashtable<Integer,FileRecordBean>();

  public boolean parseFile(String inputFileName){
    logger.info(String.format("Loading from %s",inputFileName));
    recordMap.clear();
    Path filePath=Paths.get(inputFileName);
    try{
      try(Stream<String> lines = Files.lines(filePath)) {
        lines.forEach(line -> {
          FileRecordBean bean=parseLine(line);
          if(bean!=null){
            recordMap.put(bean.getCode(),bean);
          }
        });
      }
    }catch(FileNotFoundException e){
      logger.error(String.format("Failed to find file: %s",inputFileName),e);
      return false;
    }catch(IOException e){
      logger.error(String.format("Failed to load file: %s",inputFileName),e);
      return false;
    }

    logger.info(String.format("Loaded file %s",inputFileName));
    logger.info(
      String.format("File parsed into memory with %d records.",getRecordCount()));
    return true;
  }

  public boolean writeFile(String outputFileName, String indexPrefix){
    Gson gson=new Gson();
    try{
      RedisFileWriter writer=new RedisFileWriter(outputFileName);
      for(Integer key : recordMap.keySet()){
        FileRecordBean bean=recordMap.get(key);
        writer.addEntry(indexPrefix,key.toString(),gson.toJson(bean));
      }
      writer.close();
    }catch(IOException e){
      logger.error(String.format("Unable to write to file %s",outputFileName),e);
      return false;
    }
    logger.info(String.format("Records written to output file %s.",outputFileName));
    return true;
  }

  public int getRecordCount(){
    return recordMap.size();
  }

  public FileRecordBean parseLine(String line){
    logger.debug("Parsing line: "+line);
    String[] parsedArray=line.split("\t");
    if(parsedArray.length < 2){
      return null;
    }
    logger.debug("Line parses to "+parsedArray.length+" tokens");

    String name=parsedArray[0];
    int code=0;
    try{
      code=Integer.parseInt(parsedArray[1]);
    }catch(NumberFormatException e){
      return null;
    }
    String alias=parsedArray[2];

    String comment="";
    if(parsedArray.length>3){
      for(int i=3;i<parsedArray.length;i++){
        String token=parsedArray[i];
        if(token.startsWith("#")){
          comment=token.substring(1).trim();
        }else{
          if(!token.isEmpty()){
            alias=alias+", "+token;
          }
        }
      }
    }

    return new FileRecordBean(name,code,alias,comment.trim());
  }

}
