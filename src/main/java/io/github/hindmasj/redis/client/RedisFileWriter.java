package io.github.hindmasj.redis.client;

import java.io.*;

public class RedisFileWriter{

  private final Writer writer;

  public RedisFileWriter(String fileName) throws IOException{
    writer=new FileWriter(fileName);
  }

  public RedisFileWriter(File file) throws IOException{
    writer=new FileWriter(file);
  }

  public void close() throws IOException{
    writer.close();
  }

  public void addEntry(String prefix,String key,String value)
    throws IOException{
      String actualKey=prefix+key;
      writer.write("*3\r\n");
      writer.write("$3\r\n");
      writer.write("SET\r\n");
      writer.write(String.format("$%d\r\n",actualKey.length()));
      writer.write(String.format("%s\r\n",actualKey));
      writer.write(String.format("$%d\r\n",value.getBytes().length));
      writer.write(String.format("%s\r\n",value));
  }
}
