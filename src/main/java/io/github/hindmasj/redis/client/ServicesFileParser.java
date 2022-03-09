package io.github.hindmasj.redis.client;

import java.util.Hashtable;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/** Processes the file /etc/services ready for loading as a Redis source */
public class ServicesFileParser implements FileLinesParser<ServiceBean>{

  public static final String INPUT_FILENAME="/etc/services";
  public static final String OUTPUT_FILENAME="services.output";
  public static final String INDEX_PREFIX="service.";

  private static final Logger logger = LogManager.getLogger();

  private final Map<String,ServiceBean> serviceMap=
    new Hashtable<String,ServiceBean>();

  public static final void main(String[] args) throws Exception{
    ServicesFileParser parser=new ServicesFileParser();
    FileParserHelper<ServiceBean> parserHelper=new FileParserHelper<ServiceBean>(parser);

    if(!parserHelper.parseFile(INPUT_FILENAME)){
      logger.error("Could not parse input file.");
      System.exit(1);
    }

    if(!parserHelper.writeFile(OUTPUT_FILENAME,INDEX_PREFIX,parser.serviceMap)){
      logger.error("Could not save to output file.");
      System.exit(2);
    }

  }

  public ServiceBean parseFileLine(String line){
    logger.debug("Parsing line: "+line);
    if(line.startsWith("#")){
      return null;
    }

    String[] tokens=line.split(" ");
    if(tokens.length<2){
      return null;
    }

    String name=null;
    String protocol=null;
    int code=0;
    String alias=null;
    String comment=null;
    boolean isComment=false;

    for(String token:tokens){
      if(token.isEmpty()){
        continue;
      }

      if(name==null){
        name=token;
        continue;
      }

      if(protocol==null){
        String[] id=token.split("/");
        try{
          code=Integer.parseInt(id[0]);
          protocol=id[1];
        }catch(NumberFormatException e){
          logger.error(String.format("Unable to parse integer value from %s in %s",token,line));
          return null;
        }catch(ArrayIndexOutOfBoundsException e){
          logger.error(String.format("Unable to extract protocol from token %s in %s",token,line));
          return null;
        }
        continue;
      }

      if(token.equals("#")){
        isComment=true;
        continue;
      }

      if(isComment){
        if(comment==null){
          comment=token;
        }else{
          comment=String.format("%s %s",comment,token);
        }
      }else{
        if(alias==null){
          alias=token;
        }else{
          alias=String.format("%s, %s",alias,token);
        }
      }
    }

    return new ServiceBean(name,code,protocol,alias,comment);
  }

  public void storeParsedItem(ServiceBean bean){
    if(bean != null){
      String key=String.format("%d/%s",bean.getCode(),bean.getProtocol());
      serviceMap.put(key,bean);
    }
  }

  public int getRecordCount(){
    return serviceMap.size();
  }

}
