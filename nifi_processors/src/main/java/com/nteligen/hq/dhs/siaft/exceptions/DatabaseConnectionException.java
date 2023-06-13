package com.nteligen.hq.dhs.siaft.exceptions;

/**
 * This exception represents that a problem occurred when trying to access data from a source.
 */
public class DatabaseConnectionException extends SIAFTException
{
  public DatabaseConnectionException()
  {
    super();
  }

  public DatabaseConnectionException(String message)
  {
    super(message);
  }

  public DatabaseConnectionException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
