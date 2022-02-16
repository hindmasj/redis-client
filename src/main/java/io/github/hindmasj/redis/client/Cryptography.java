package io.github.hindmasj.redis.client;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import com.typesafe.config.Config;

import java.util.Scanner;

public class Cryptography{

  public static final String CFG_CKEY="auth.ckey";

  private final StringEncryptor encryptor;

  public static final void main(String[] argv){
    Config config=RedisClient.getConfig();
    Cryptography crypto=new Cryptography(config);
    String input=crypto.getInput();
    String output=crypto.encrypt(input);
    System.out.print(String.format("Encrypted Version:-\n%s\n-----\n",output));

    //crypto=new Cryptography(config);
    String decryption=crypto.decrypt(output);
    System.out.print(String.format("Decrypted Check:-\n%s\n-----\n",decryption));
  }

  public Cryptography(Config config){
    StandardPBEStringEncryptor actualEncryptor=new StandardPBEStringEncryptor();
    actualEncryptor.setPassword(config.getString(CFG_CKEY));
    actualEncryptor.setStringOutputType("hexadecimal");
    actualEncryptor.initialize();
    encryptor=actualEncryptor;
  }

  /** Only used for command line encryption tool.
    * Prompts the user and captures a string.
    */
  private String getInput(){
    Scanner scanner=new Scanner(System.in);
    System.out.print("Enter password to encrypt:> ");
    return scanner.nextLine();
  }

  /** Perform the jasypt encryption using the stored key.
    */
  public String encrypt(String plaintext){
    return encryptor.encrypt(plaintext);
  }

  /** Perform the jasypt decryption using the stored key.
    */
  public String decrypt(String cyphertext){
    return encryptor.decrypt(cyphertext);
  }

}
