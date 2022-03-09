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

    /* DO SOME STUFF */
    return null;

    //return new ServiceBean(name,code,protocol,alias,comment.trim());
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
