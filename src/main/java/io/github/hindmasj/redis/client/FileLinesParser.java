package io.github.hindmasj.redis.client;

public interface FileLinesParser<T>{

  public T parseFileLine(String line);

  public void storeParsedItem(T item);

  public int getRecordCount();

}
