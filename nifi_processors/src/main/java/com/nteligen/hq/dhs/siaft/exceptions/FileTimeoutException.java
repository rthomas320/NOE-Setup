package com.nteligen.hq.dhs.siaft.exceptions;

/**
 * This exception represents that the file timeout was reached and processing
 * ended prematurely.
 */
public class FileTimeoutException extends SIAFTException
{

  public FileTimeoutException()
  {
    super();
  }

  public FileTimeoutException(String message)
  {
    super(message);
  }

  public FileTimeoutException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
