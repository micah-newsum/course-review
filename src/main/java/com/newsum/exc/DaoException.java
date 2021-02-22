package com.newsum.exc;

public class DaoException extends Exception {
  private Exception originalException;

  public DaoException(Exception originalException, String message){
    super(message);
    this.originalException = originalException;
  }
}
