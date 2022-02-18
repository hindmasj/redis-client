package io.github.hindmasj.redis.client;

import java.io.*;
import java.nio.file.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.typesafe.config.Config;

public class GeoipFileParser{

  public static final String CFG_INPUT_FILE="files.geoipv4.source";
  public static final String KEY_PREFIX="geoipv4.";
  public static final String KEY_FIELD="ipv4";

  private static final Logger logger = LogManager.getLogger();

  private final Config config;

  private boolean fileLoaded=false;
  private boolean fileParsed=false;

  private String fileName=null;
  private Path filePath=null;
  private JsonStructure data;
  private Map<String,String> parsedData=new Hashtable<String,String>();

  public GeoipFileParser(Config config){
    this.config=config;
  }

  private JsonReader getJsonReader(Path filePath) throws FileNotFoundException{
    File dataFile=filePath.toFile();
    FileReader fileReader=new FileReader(dataFile);
    return Json.createReader(fileReader);
  }

  public void loadFile(){
    fileName=config.getString(CFG_INPUT_FILE);
    filePath=Paths.get(fileName);
    try{
      JsonReader reader=getJsonReader(filePath);
      data=reader.read();
      fileLoaded=true;
      logger.info(String.format("Data file %s loaded",fileName));
      reader.close();
    }catch(FileNotFoundException e){
      logger.error(String.format("Failed to load data file :%s",fileName),e);
    }
  }

  public void parseFile(){
    if(!fileLoaded){
      throw new IllegalStateException("Cannot parse file, not loaded.");
    }
    if(data==null){
      throw new IllegalStateException("Cannot parse file, no data available.");
    }
    fileParsed=false;
    parsedData.clear();
    switch(data.getValueType()){
      case OBJECT:
        parseObject((JsonObject)data);
        break;
      case ARRAY:
        parseArray((JsonArray)data);
        break;
      default:
        throw new IllegalStateException("Cannot parse file, no valid data type.");
    }
    fileParsed=true;
  }

  public void writeFile(Path outputFile) throws IOException{
    Writer writer=new FileWriter(outputFile.toFile());
    for(Map.Entry<String,String> entry : parsedData.entrySet()){
      writeEntry(writer,entry);
    }
    writer.close();
  }

  private void writeEntry(Writer writer,Map.Entry<String,String> entry)
    throws IOException{
    writer.write("*3\r\n");
    writer.write("$3\r\n");
    writer.write("SET\r\n");
    String key=entry.getKey();
    writer.write(String.format("$%d\r\n",key.length()));
    writer.write(String.format("%s\r\n",key));
    String value=entry.getValue();
    writer.write(String.format("$%d\r\n",value.length()));
    writer.write(String.format("%s\r\n",value));

  }

  private void parseArray(JsonArray array){
    for(int i=0;i<array.size();i++){
      parseObject(array.getJsonObject(i));
    }
  }

  private void parseObject(JsonObject object){
    try{
      String keyString=KEY_PREFIX+object.getString(KEY_FIELD);
      parsedData.put(keyString,object.toString());
    }catch(NullPointerException e){
      logger.error(String.format("JSON record does not contain %s",KEY_FIELD));
      logger.info(String.format("Suspect record: %s",object));
      throw e;
    }catch(ClassCastException e){
      logger.error(String.format("Field %s does not contain a String",KEY_FIELD));
      logger.info(String.format("Suspect record: %s",object));
      throw e;
    }
  }

  public int getRecordCount(){
    return parsedData.size();
  }

  public String getFileName(){
    return fileName;
  }

  public boolean isFileLoaded(){
    return fileLoaded;
  }

  public boolean isFileParsed(){
    return fileParsed;
  }

}
