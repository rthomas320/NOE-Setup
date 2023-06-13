package com.nteligen.hq.dhs.siaft.exceptions;

/**
 * This exception represents that a problem occurred when trying to a file that is unavailable.
 */
public class FileNotFoundException extends SIAFTException
{
  public FileNotFoundException()
  {
    super();
  }

  public FileNotFoundException(String message)
  {
    super(message);
  }

  public FileNotFoundException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
