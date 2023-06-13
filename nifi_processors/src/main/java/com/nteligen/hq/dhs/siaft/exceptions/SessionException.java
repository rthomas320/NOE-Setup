package com.nteligen.hq.dhs.siaft.exceptions;

/**
 * This exception represents that a communication problem occurred within
 * a processing session.
 */
public class SessionException extends Exception
{
  public SessionException()
  {
    super();
  }

  public SessionException(String message)
  {
    super(message);
  }

  public SessionException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
