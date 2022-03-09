package io.github.hindmasj.redis.client;

import java.io.*;
import java.nio.file.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.stream.Stream;

import com.google.gson.Gson;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/** Processes the file /etc/protocols ready for loading as a Redis source */
public class ProtocolsFileParser{

  public static final String INPUT_FILENAME="/etc/protocols";
  public static final String OUTPUT_FILENAME="protocols.output";
  public static final String INDEX_PREFIX="protocol.";

  private static final Logger logger = LogManager.getLogger();

  private boolean fileLoaded=false;
  private final Map<Integer,ProtocolBean> protocolMap=
    new Hashtable<Integer,ProtocolBean>();

  public static final void main(String[] args) throws Exception{
    ProtocolsFileParser parser=new ProtocolsFileParser();

    if(!parser.loadFile(INPUT_FILENAME)){
      logger.error("Could not load input file.");
      System.exit(2);
    }
    logger.info(String.format("Loaded file %s",INPUT_FILENAME));
    logger.info(String.format(
      "File parsed into memory with %d records.",parser.getRecordCount()));

    Gson gson=new Gson();
    try{
      RedisFileWriter writer=new RedisFileWriter(OUTPUT_FILENAME);
      for(Integer key : parser.protocolMap.keySet()){
        ProtocolBean bean=parser.protocolMap.get(key);
        writer.addEntry(INDEX_PREFIX,key.toString(),gson.toJson(bean));
      }
      writer.close();
    }catch(IOException e){
      logger.error("Could not save to otput file.",e);
      System.exit(3);
    }
    logger.info(String.format(
      "Records written to output file %s.",OUTPUT_FILENAME));

  }

  public int getRecordCount(){
    return protocolMap.size();
  }

  public boolean isFileLoaded(){
    return fileLoaded;
  }

  public boolean loadFile(String inputFilename){
    fileLoaded=false;
    protocolMap.clear();
    Path filePath=Paths.get(inputFilename);
    try{
      try(Stream<String> lines = Files.lines(filePath)) {
        lines.forEach(line -> {
          ProtocolBean bean=parseLine(line);
          if(bean!=null){
            protocolMap.put(bean.getCode(),bean);
          }
        });
      }
    }catch(FileNotFoundException e){
      logger.error(String.format("Failed to find protocols file: %s",inputFilename),e);
      return false;
    }catch(IOException e){
      logger.error(String.format("Failed to load protocols file: %s",inputFilename),e);
      return false;
    }
    fileLoaded=true;
    return true;
  }

  public ProtocolBean parseLine(String line){
    //System.out.println(line);
    String[] parsedArray=line.split("\t");
    if(parsedArray.length < 2){
      return null;
    }
    //System.out.println(parsedArray.length+" tokens");

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

    return new ProtocolBean(name,code,alias,comment.trim());
  }

}
