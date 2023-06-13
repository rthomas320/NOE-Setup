package com.nteligen.hq.dhs.siaft.processors;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import com.nteligen.hq.dhs.siaft.exceptions.SessionException;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;
import java.util.ArrayList;


/**
 * Provides an interface for an SFTP session. The caller must invoke
 * <code>connect()</code> before attempting to read or write any files. The
 * class implements <code>AutoCloseable</code> so that sessions can be safely
 * closed via a try-with-resources block.
 */
public class SftpSession implements DropoffSession
{
  private static int DEFAULT_TIMEOUT = 300;

  private Session jschSession;
  private ChannelSftp channel;
  private String user;
  private String host;
  private String password;
  private int port;
  private Properties properties;
  private int timeout;
  private String dropoffPath;
  private String pickupPath;

  /**
   * Constructor.
   *
   * @param user The SFTP user name.
   * @param host The SFTP host to connect to.
   * @param password The password to use during login.
   * @param port The SFTP port number.
   * @param properties Additionaly properties that define the SFTP session.
   */
  public SftpSession(String user, String host, String password, int port,
                     Properties properties)
  {
    this.jschSession = null;
    this.channel = null;

    this.user = user;
    this.host = host;
    this.password = password;
    this.port = port;
    this.properties = properties;
    this.timeout = DEFAULT_TIMEOUT;
  }

  /**
   * Connects to the SFTP server.
   *
   * @throws SessionException There was an error connecting to the SFTP server.
   */
  @Override
  public void connect() throws SessionException
  {
    try
    {
      if (this.jschSession == null)
      {
        JSch jsch = new JSch();
        this.jschSession = jsch.getSession(user, host, port);
        this.jschSession.setPassword(password);
        this.jschSession.setConfig(properties);
      }

      if (this.jschSession != null && !this.jschSession.isConnected())
      {
        this.jschSession.connect();
      }

      this.channel = (ChannelSftp) this.jschSession.openChannel("sftp");
      if (this.channel != null && !this.channel.isConnected())
      {
        this.channel.connect();
      }
    }
    catch (JSchException je)
    {
      this.close();
      throw new SessionException("Unable to connect to host", je);
      // TODO throw
    }
  }

  /**
   * Checks if the given filename exits within the pickup path on the open SFTP
   * connection.
   *
   * @param filename The file path to check.
   * @return True if the file exists, otherwise false.
   */
  @Override
  public boolean exists(String filename)
  {
    String source = generatePickupPath(filename);
    boolean exists = false;

    try
    {
      SftpATTRS attributes = this.channel.lstat(source);
      long size = attributes.getSize();
      if (size > 0)
      {
        exists = true;
      }
    }
    catch (SftpException ex)
    {
      // do nothing, signifies files does not exist
    }

    return exists;
  }

  /**
   * Deletes the file within the pickup path if it exists on the open SFTP
   * connection.
   *
   * @param filename The path of the file to delete.
   * @throws SessionException THere was a problem deleting the file.
   */
  @Override
  public void delete(String filename) throws SessionException
  {
    String source = generatePickupPath(filename);

    try
    {
      this.channel.rm(source);
    }
    catch (SftpException ex)
    {
      throw new SessionException("Unable to delete file", ex);
    }
  }

  /**
   * Writes the given input stream to a file at the provided destination path
   * on the open SFTP connection.
   *
   * @param in       The file content.
   * @param filename The destination path of the file to write.
   * @throws SessionException There was an error creating the file.
   */
  @Override
  public void write(InputStream in, String filename) throws SessionException
  {
    String dest = generateDropoffPath(filename);
    String ren = generateDropoffPath(filename);

    if (channel == null)
    {
      throw new SessionException("Failed, channel is closed");
    }

    try
    {
      this.channel.put(in, dest);
    }
    catch (SftpException ex)
    {
      throw new SessionException("Failed to write file", ex);
    }
  }

  /**
   * Read the file at the given source path on the open SFTP connection.
   *
   * @param os       The output stream where the file content is to be written.
   * @param filename The file path of the file to read.
   * @throws SessionException There was an error reading the file.
   */
  @Override
  public void read(OutputStream os, String filename) throws SessionException
  {
    if(filename.endsWith("m")) {
      filename = filename.substring(0, filename.length() - 1) + 'x';
    }
    String source = generatePickupPath(filename);
     
    try (InputStream is = this.channel.get(source))
    {
      IOUtils.copy(is, os);
    }
    catch (SftpException ex)
    {
      throw new SessionException("Failed to read file, SFTP connection failure", ex);
    }
    catch (IOException ioe)
    {
      throw new SessionException("Failed to read file, IO failure");
    }
  }

  /**
   * Close the SFTP session.
   */
  @Override
  public void close() throws SessionException
  {
    if (this.jschSession != null && this.jschSession.isConnected())
    {
      this.jschSession.disconnect();
    }
  }

  @Override
  public HashMap<String, String> getReportAttributes()
  {
    return null;
  }

  @Override
  public String getReportExtension()
  {
    return ".xml";
  }

  public void setTimeout(int timeout)
  {
    this.timeout = timeout;
  }

  @Override
  public int getTimeout()
  {
    return timeout;
  }

  public void setDropoff(String dropoffPath)
  {
    this.dropoffPath = dropoffPath;

  }

  public void setPickup(String pickupPath)
  {
    this.pickupPath = pickupPath;
  }

  private String generatePickupPath(String filename)
  {
    Path pickupPath = Paths.get(this.pickupPath, filename);
    return pickupPath.toString();
  }

  private String generateDropoffPath(String filename)
  {
    Path dropoffPath = Paths.get(this.dropoffPath, filename);
    return dropoffPath.toString();
  }
}
