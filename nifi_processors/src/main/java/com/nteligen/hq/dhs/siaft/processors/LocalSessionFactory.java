package com.nteligen.hq.dhs.siaft.processors;

public class LocalSessionFactory implements SessionFactory
{

  private String path;

  public LocalSessionFactory(String path)
  {
    this.path = path;
  }

  @Override
  public DropoffSession getSession()
  {
    return new LocalSession(path);
  }
}
