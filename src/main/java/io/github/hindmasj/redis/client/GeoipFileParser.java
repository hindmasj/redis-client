package io.github.hindmasj.redis.client;

import java.io.*;
import java.nio.file.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.*;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.typesafe.config.Config;

public class GeoipFileParser{

  public static final String CFG_INPUT_FILE="files.geoipv4.source";
  public static final String KEY_PREFIX="geoipv4.";
  public static final String KEY_FIELD="ipv4";
  public static final String LINK_PREFIX="#";
  public static final String LINK_SUFFIX="/32";
  public static final String LINK_FILE_PREFIX="links-";

  private static final Logger logger = LogManager.getLogger();

  private final Config config;

  private boolean fileLoaded=false;
  private boolean fileParsed=false;

  private String fileName=null;
  private Path filePath=null;
  private JsonStructure data;
  private Map<String,String> parsedData=new Hashtable<String,String>();

  public static final void main(String[] args) throws Exception{
    if(args.length==0){
      logger.error("You need to specify an output file.");
      System.exit(1);
    }
    String outputFileName=args[0];

    GeoipFileParser parser=new GeoipFileParser(RedisClient.getConfig());
    if(!parser.loadFile()){
      logger.error("Could not load input file.");
      System.exit(2);
    }
    logger.info(String.format("Loaded file %s",parser.getFileName()));
    parser.parseFile();
    logger.info(String.format(
      "File parsed into memory with %d records.",parser.getRecordCount()));
    parser.writeFile(Paths.get(outputFileName));
    logger.info(String.format(
      "Records written to output file %s.",outputFileName));

    String linksFileName=LINK_FILE_PREFIX+outputFileName;
    parser.writeLinksFile(Paths.get(linksFileName));
    logger.info(String.format(
      "Link records written to output file %s.",linksFileName));

  }

  public GeoipFileParser(Config config){
    this.config=config;
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

  public boolean loadFile(){
    return loadFile(config.getString(CFG_INPUT_FILE));
  }

  public boolean loadFile(String fileName){
    fileLoaded=false;
    filePath=Paths.get(fileName);
    try{
      JsonReader reader=getJsonReader(filePath);
      data=reader.read();
      fileLoaded=true;
      logger.info(String.format("Data file %s loaded",fileName));
      this.fileName=fileName;
      reader.close();
      return true;
    }catch(FileNotFoundException e){
      logger.error(String.format("Failed to load data file :%s",fileName),e);
      return false;
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

  public void writeLinksFile(Path outputFile) throws IOException{
    Writer writer=new FileWriter(outputFile.toFile());
    for(Map.Entry<String,String> entry : parsedData.entrySet()){
      writeLinkEntry(writer,entry);
    }
    writer.close();
  }

  private JsonReader getJsonReader(Path filePath) throws FileNotFoundException{
    File dataFile=filePath.toFile();
    FileReader fileReader=new FileReader(dataFile);
    return Json.createReader(fileReader);
  }

  private void writeLinkEntry(Writer writer,Map.Entry<String,String> entry)
  throws IOException{
    String net=entry.getKey().substring(KEY_PREFIX.length());
    SubnetUtils.SubnetInfo info=new SubnetUtils(net).getInfo();
    String link=LINK_PREFIX+net;

    //No need for links for /32 subnets
    if(info.getAddressCountLong()!=0){
      String nwAddress=info.getNetworkAddress();
      writeEntry(writer,KEY_PREFIX+nwAddress+LINK_SUFFIX,link);
      String[] addresses=info.getAllAddresses();
      for(String address : addresses){
        String key=KEY_PREFIX+address+LINK_SUFFIX;
        writeEntry(writer,key,link);
      }
      String bcastAddress=info.getBroadcastAddress();
      writeEntry(writer,KEY_PREFIX+bcastAddress+LINK_SUFFIX,link);
    }
  }

  private void writeEntry(Writer writer,Map.Entry<String,String> entry)
    throws IOException{
      writeEntry(writer,entry.getKey(),entry.getValue());
  }

  private void writeEntry(Writer writer,String key, String value)
  throws IOException{
    writer.write("*3\r\n");
    writer.write("$3\r\n");
    writer.write("SET\r\n");
    writer.write(String.format("$%d\r\n",key.length()));
    writer.write(String.format("%s\r\n",key));
    writer.write(String.format("$%d\r\n",value.getBytes().length));
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

}
