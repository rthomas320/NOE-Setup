package com.nteligen.hq.dhs.siaft.processors;

import com.nteligen.hq.dhs.siaft.exceptions.SessionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

/**
 * A LocalSession can be used to submit and retrieve files for processing to a
 * service running on the local system. This is accomplished via file copies /
 * deletes.
 */
public class LocalSession implements DropoffSession
{
  private int timeout;
  private String workDir;

  public LocalSession(String workDir)
  {
    this.timeout = 60;
    this.workDir = workDir;
  }

  @Override
  public void connect() throws SessionException
  {
    // do nothing
  }

  @Override
  public boolean exists(String filename)
  {
    Path localPath = Paths.get(workDir, filename);
    return Files.exists(localPath);
  }

  @Override
  public void delete(String filename) throws SessionException
  {
    Path localPath = Paths.get(workDir, filename);
    try
    {
      Files.deleteIfExists(localPath);
    }
    catch (IOException ioe)
    {
      throw new SessionException("Unable to delete file", ioe);
    }
  }

  @Override
  public void write(InputStream in, String filename) throws SessionException
  {
    try
    {
      Files.copy(in, Paths.get(workDir, filename),
                 StandardCopyOption.REPLACE_EXISTING);
    }
    catch (IOException ioe)
    {
      throw new SessionException("Unable to copy file", ioe);
    }
  }

  @Override
  public void read(OutputStream os, String filename) throws SessionException
  {
    try
    {
      Files.copy(Paths.get(workDir, filename), os);
    }
    catch (IOException ioe)
    {
      throw new SessionException("Unable to read file", ioe);
    }
  }

  @Override
  public Map getReportAttributes()
  {
    return null;
  }

  @Override
  public String getReportExtension()
  {
    return ".xml";
  }

  @Override
  public void close() throws SessionException
  {
    // do nothing
  }

  @Override
  public int getTimeout()
  {
    return timeout;
  }

}
