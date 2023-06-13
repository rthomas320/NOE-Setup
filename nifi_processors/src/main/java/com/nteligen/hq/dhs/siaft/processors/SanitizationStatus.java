package com.nteligen.hq.dhs.siaft.processors;

/**
 * This represents the status of the sanitization process on a file.
 */
public enum SanitizationStatus
{
  /**
   * This represents that the sanitization engine process succeeded and the file was modified to
   * remove detected malicious content. Undetected malicious content may still be present.
   */
  SANITIZED("Sanitized"),
  /**
   * This represents that the sanitization engine process succeeded and the file was NOT
   * modified meaning that no detected malicious content was removed. Undetected malicious
   * content may still be present.
   */
  NOT_MODIFIED("File not modified"),
  /**
   * This represents that the sanitization engine process failed. The original file will be
   * returned. This does not indicate whether the file contained malicious content or not.
   * Detected AND undetected malicious content will still be present since the
   * original file is returned.
   */
  NOT_SANITIZED("Sanitization Failed"),
  /**
   * This represents that the nifi sanitization process failed. The original file will be
   * returned. This does not indicate whether the file contained malicious content or not.
   * Detected AND undetected malicious content will still be present since the original file is
   * returned.
   */
  SANITIZATION_FAILURE("Process Failure");

  private String value;

  SanitizationStatus(String value)
  {
    this.value = value;
  }

  @Override
  public String toString()
  {
    return value;
  }
}
