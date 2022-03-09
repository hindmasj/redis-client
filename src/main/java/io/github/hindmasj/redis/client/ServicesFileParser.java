package io.github.hindmasj.redis.client;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/** Processes the file /etc/services ready for loading as a Redis source */
public class ServicesFileParser{

  public static final String INPUT_FILENAME="/etc/services";
  public static final String OUTPUT_FILENAME="services.output";
  public static final String INDEX_PREFIX="service.";

  private static final Logger logger = LogManager.getLogger();

  public static final void main(String[] args) throws Exception{
    /*FileParserHelper parser=new FileParserHelper();

    if(!parser.parseFile(INPUT_FILENAME)){
      logger.error("Could not parse input file.");
      System.exit(1);
    }

    if(!parser.writeFile(OUTPUT_FILENAME,INDEX_PREFIX)){
      logger.error("Could not save to output file.");
      System.exit(2);
    }*/
  }

}
