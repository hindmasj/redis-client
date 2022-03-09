package io.github.hindmasj.redis.client;

public class ServiceBean{

  private String name;
  private int code;
  private String protocol;
  private String alias;
  private String comment;

  public ServiceBean(String name, int code, String protocol, String alias, String comment){
    this.name=name;
    this.code=code;
    this.protocol=protocol;
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

  public String getProtocol(){
    return protocol;
  }

  public String getAlias(){
    return alias;
  }

  public String getComment(){
    return comment;
  }

}
