package io.github.hindmasj.redis.client;

import java.util.Hashtable;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/** Processes the file /etc/protocols ready for loading as a Redis source */
public class ProtocolsFileParser implements FileLinesParser<ProtocolBean>{

  public static final String INPUT_FILENAME="/etc/protocols";
  public static final String OUTPUT_FILENAME="protocols.output";
  public static final String INDEX_PREFIX="protocol.";

  private static final Logger logger = LogManager.getLogger();

  private final Map<Integer,ProtocolBean> protocolMap=
    new Hashtable<Integer,ProtocolBean>();

  public static final void main(String[] args) throws Exception{
    ProtocolsFileParser parser=new ProtocolsFileParser();
    FileParserHelper<ProtocolBean> parserHelper=new FileParserHelper<ProtocolBean>(parser);

    if(!parserHelper.parseFile(INPUT_FILENAME)){
      logger.error("Could not parse input file.");
      System.exit(1);
    }

    if(!parserHelper.writeFile(OUTPUT_FILENAME,INDEX_PREFIX,parser.protocolMap)){
      logger.error("Could not save to output file.");
      System.exit(2);
    }
  }

  public ProtocolBean parseFileLine(String line){
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
    return new ProtocolBean(name,code,alias,comment.trim());
  }

  public void storeParsedItem(ProtocolBean bean){
    if(bean != null){
      protocolMap.put(bean.getCode(),bean);
    }
  }

  public int getRecordCount(){
    return protocolMap.size();
  }

}
