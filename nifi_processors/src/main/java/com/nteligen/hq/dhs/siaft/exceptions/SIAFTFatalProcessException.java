package com.nteligen.hq.dhs.siaft.exceptions;

/**
 * This exception represents a very serious problem that happened that results in the flowfile
 * atttributes and/or the flowfile contents not being able to trusted. In these circumstances
 * we rolling back the session since we don't want to transfer a flowfile that we can't trust
 * the attribute or contents.
 */
public class SIAFTFatalProcessException extends Exception
{
  public SIAFTFatalProcessException()
  {
    super();
  }

  public SIAFTFatalProcessException(String message)
  {
    super(message);
  }

  public SIAFTFatalProcessException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
