package com.nteligen.hq.dhs.siaft.processors;

import java.util.Properties;

public class SftpSessionFactory implements SessionFactory
{

  private String user;
  private String host;
  private String password;
  private int port;
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
   * @param timeout The file processing timeout (seconds).
   * @param dropoffPath The dropoff directory path on the SFTP host.
   * @param pickupPath The pickup directory path on the SFTP host.
   */
  public SftpSessionFactory(String user, String host, String password,
                            int port, int timeout, String dropoffPath,
                            String pickupPath)
  {
    this.user = user;
    this.host = host;
    this.password = password;
    this.port = port;
    this.timeout = timeout;
    this.dropoffPath = dropoffPath;
    this.pickupPath = pickupPath;
  }

  @Override
  public DropoffSession getSession()
  {
    Properties properties = new Properties();
    properties.setProperty("StrictHostKeyChecking", "no");

    SftpSession session = new SftpSession(user, host, password, port, properties);
    session.setTimeout(timeout);
    session.setDropoff(dropoffPath);
    session.setPickup(pickupPath);

    return session;
  }

}
