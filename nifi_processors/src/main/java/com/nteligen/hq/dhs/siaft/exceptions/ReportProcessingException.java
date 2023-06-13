package com.nteligen.hq.dhs.siaft.exceptions;

/**
 * This exception represents that a problem occurred when trying to process an
 * analysis report.
 */
public class ReportProcessingException extends SIAFTException
{
  public ReportProcessingException()
  {
    super();
  }

  public ReportProcessingException(String message)
  {
    super(message);
  }

  public ReportProcessingException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
