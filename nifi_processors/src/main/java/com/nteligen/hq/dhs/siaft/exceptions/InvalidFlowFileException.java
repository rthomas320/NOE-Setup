package com.nteligen.hq.dhs.siaft.exceptions;

/**
 * This exception represents that the flow file / attributes were malformed in
 * some way.
 */
public class InvalidFlowFileException extends SIAFTException
{
  public InvalidFlowFileException()
  {
    super();
  }

  public InvalidFlowFileException(String message)
  {
    super(message);
  }

  public InvalidFlowFileException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
