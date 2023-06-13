package com.nteligen.hq.dhs.siaft.processors;

/**
 * Provides a common interface for session factories to provide new sessions.
 */
public interface SessionFactory
{
  /**
   * Generate a new DropoffSession for file processing.
   * @return A DropoffSession that can be use for file processing.
   */
  public abstract DropoffSession getSession();
}
