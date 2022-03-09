package io.github.hindmasj.redis.client;

public class ProtocolBean{

  private String name;
  private int code;
  private String alias;
  private String comment;

  public ProtocolBean(String name, int code, String alias, String comment){
    this.name=name;
    this.code=code;
    this.alias=alias;
    this.comment=comment;
  }

  public String toString(){
    return String.format("%d=>%s(%s)[%s]",code,name,alias,comment);
  }

  public String getName(){
    return name;
  }

  public int getCode(){
    return code;
  }

  public String getAlias(){
    return alias;
  }

  public String getComment(){
    return comment;
  }

}
