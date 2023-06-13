package com.nteligen.hq.dhs.siaft.exceptions;

/**
 * This exception represents that a problem occurred when trying to detect the mime type of a file.
 */
public class MimeTypeDetectionException extends SIAFTException
{
  public MimeTypeDetectionException()
  {
    super();
  }

  public MimeTypeDetectionException(String message)
  {
    super(message);
  }

  public MimeTypeDetectionException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
