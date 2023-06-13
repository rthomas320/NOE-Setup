package com.nteligen.hq.dhs.siaft.exceptions;

/**
 * This is the base class exception that all exceptions for siaft custom java code should be
 * extending from.
 */
public abstract class SIAFTException extends Exception
{
  public SIAFTException()
  {
    super();
  }

  public SIAFTException(String message)
  {
    super(message);
  }

  public SIAFTException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
