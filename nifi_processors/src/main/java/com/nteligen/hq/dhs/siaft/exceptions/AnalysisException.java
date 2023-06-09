package com.nteligen.hq.dhs.siaft.exceptions;

/**
 * This exception represents that a problem occurred during file analysis.
 */
public class AnalysisException extends Exception
{
  public AnalysisException()
  {
    super();
  }

  public AnalysisException(String message)
  {
    super(message);
  }

  public AnalysisException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
