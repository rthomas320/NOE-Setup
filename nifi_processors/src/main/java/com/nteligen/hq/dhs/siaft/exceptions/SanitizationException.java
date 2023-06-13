package com.nteligen.hq.dhs.siaft.exceptions;

/**
 * This exception represents that a problem occurred when trying to sanitize a file.
 */
public class SanitizationException extends SIAFTException
{
  public SanitizationException()
  {
    super();
  }

  public SanitizationException(String message)
  {
    super(message);
  }

  public SanitizationException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
