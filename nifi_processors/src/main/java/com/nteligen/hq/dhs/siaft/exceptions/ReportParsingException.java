package com.nteligen.hq.dhs.siaft.exceptions;

/**
 * This exception represents that a problem occurred when trying to parse an
 * analysis report.
 */
public class ReportParsingException extends ReportProcessingException
{
  public ReportParsingException()
  {
    super();
  }

  public ReportParsingException(String message)
  {
    super(message);
  }

  public ReportParsingException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
