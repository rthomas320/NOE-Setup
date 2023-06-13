package com.nteligen.hq.dhs.siaft.processors;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.ChannelSftp.LsEntrySelector;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.nteligen.hq.dhs.siaft.exceptions.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SIAFTSftp
{
  private Session jsession = null;
  private ChannelExec exec = null;
  private ChannelSftp sftp = null;
  private boolean closed = false;

  private static final Logger log = LoggerFactory.getLogger(SIAFTSftp.class);

  private String hostServer = "";
  private String sftpPort = "";
  private String timeOut = "";
  private String inputPath = "";
  private String outputPath = "";
  private String userId = "";
  private String password = "";
  private String useHostKey = "";
  private String hostKeyFile = "";

  private int connectionTimeoutMillis = 0;

  /**
   * constructor for SIAFTSftp class.
   *
   * @param hostServer
   *          The host server name or IP
   * @param sftpPort
   *          The SFTp port usually 22
   * @param timeOut
   *           The SFTP time period in seconds
   * @param inputPath
   *            The host server path to put to
   * @param outputPath
   *            The host server path to get from
   * @param userId
   *            The user ID
   * @param password
   *            The password for the user ID
   * @param useHostKey
   *             Test value true or false (comes from a nifi property)
   * @param hostKeyFile
   *              The path and name of the host key file (can be null)
   */
  public SIAFTSftp(String hostServer, String sftpPort, String timeOut,
    String inputPath, String outputPath, String userId, String password,
    String useHostKey, String hostKeyFile)
  {
    this.hostServer = hostServer;
    this.sftpPort = sftpPort;
    this.timeOut = timeOut;
    this.inputPath = inputPath;
    this.outputPath = outputPath;
    this.userId = userId;
    this.password = password;
    this.useHostKey = useHostKey;
    this.hostKeyFile = hostKeyFile;
  }

  /**
  * returns a list of file names in String form calls a function to get
  * the file list from the sanitizer server.
  *
  * @return List of file names
  * @throws IOException
  *           Thrown if an I/O error occurs
  * @throws FileNotFoundException
  *           Thrown if the file cannot be retrieved
  */
  public List<String> getListing()
    throws IOException, FileNotFoundException
  {
    final String path = outputPath;
    final int depth = 0;

    final int maxResults = 10;

    final List<String> listing = new ArrayList<String>();
    getListing(path, depth, maxResults, listing);
    return listing;
  }

  /**
  * Returns a String List of filenames from a sanitizer server.
  * Opens an sftp channel to the server if none exists.
  *
  * @param pathValue
  *          The path to look for the sanitized file
  * @param depth
  *          The (int) depth of the directories to get listings for
  * @param maxResults
  *          The (int) max number of listings to return
  * @return List of file names
  * @throws IOException
  *           Thrown if an I/O error occurs
  * @throws FileNotFoundException
  *           Thrown if the file cannot be retrieved
  */
  private void getListing(final String pathValue, final int depth, final int maxResults,
      final List<String> listing)
        throws IOException, FileNotFoundException
  {
    if (maxResults < 1 || listing.size() >= maxResults)
    {
      return;
    }
    final String path = outputPath;

    if (depth >= 100)
    {
      log.warn(this + " had to stop recursively searching directories at a recursive depth of "
          + depth + " to avoid memory issues");
      return;
    }

    final boolean ignoreDottedFiles = true;
    final boolean recurse = false;
    final String pattern = null;

    final ChannelSftp sftp = getSftpChannel();
    final boolean isPathMatch = true;

    final List<LsEntry> subDirs = new ArrayList<>();
    try
    {
      final LsEntrySelector filter = new LsEntrySelector()
      {
        @Override
        public int select(final LsEntry entry)
        {
          final String entryFilename = entry.getFilename();

          // skip over 'this directory' and 'parent directory' special
          // files regardless of ignoring dot files
          if (entryFilename.equals(".") || entryFilename.equals(".."))
          {
            return LsEntrySelector.CONTINUE;
          }

          // skip files and directories that begin with a dot if we're
          // ignoring them
          if (ignoreDottedFiles && entryFilename.startsWith("."))
          {
            return LsEntrySelector.CONTINUE;
          }

          // if is a directory and we're supposed to recurse
          if (recurse && entry.getAttrs().isDir())
          {
            subDirs.add(entry);
            return LsEntrySelector.CONTINUE;
          }

          // if is not a directory and is not a link let's add it
          if (!entry.getAttrs().isDir() && !entry.getAttrs().isLink() && isPathMatch)
          {
            if (pattern == null)
            {
              listing.add(newFilePath(entry, path));
            }
          }

          if (listing.size() >= maxResults)
          {
            return LsEntrySelector.BREAK;
          }

          return LsEntrySelector.CONTINUE;
        }

      };

      if (path == null || path.trim().isEmpty())
      {
        sftp.ls(".", filter);
      }
      else
      {
        sftp.ls(path, filter);
      }
    }
    catch (final SftpException se)
    {
      final String pathDesc = path == null ? "current directory" : path;
      switch (se.id)
      {
        case ChannelSftp.SSH_FX_NO_SUCH_FILE:
          throw new FileNotFoundException("Could not perform listing on " + pathDesc
              + " because could not find the file on the remote server", se);
        case ChannelSftp.SSH_FX_PERMISSION_DENIED:
          throw new IOException(
              "Could not perform listing on " + pathDesc + " due to insufficient permissions");
        default:
          throw new IOException("Failed to obtain file listing for " + pathDesc, se);
      }
    }

    for (final LsEntry entry : subDirs)
    {
      final String entryFilename = entry.getFilename();
      final File newFullPath = new File(path, entryFilename);
      final String newFullForwardPath = newFullPath.getPath().replace("\\", "/");

      try
      {
        getListing(newFullForwardPath, depth + 1, maxResults, listing);
      }
      catch (final IOException ioe)
      {
        log.error(
            "Unable to get listing from " + newFullForwardPath + "; skipping this subdirectory",
            ioe);
      }
    }
  }

  /**
   * Checks if the given filename exits within the pickup path on the open SFTP
   * connection.
   *
   * @param filename
   *          The file path to check.
   * @return True if the file exists, otherwise false.
   */
  public boolean exists(String filename)
  {
    try
    {
      sftp.lstat(filename);
      return true;
    }
    catch (SftpException ex)
    {
      // do nothing, signifies files does not exist
    }

    return false;
  }

  /**
   * deletes a file from sanitizer server.  Nothing is returned
   * @param remoteFileName
   *          The full path and name of the file to delete
  * @throws IOException
   *          Thrown if an I/O error occurs
   * @throws FileNotFoundException
   *          Thrown if file cannot be retrieved
   */
  public void deleteFile(final String remoteFileName)
      throws IOException, FileNotFoundException
  {
    try
    {
      sftp.rm(remoteFileName);
    }
    catch (final SftpException se)
    {
      switch (se.id)
      {
        case ChannelSftp.SSH_FX_NO_SUCH_FILE:
          log.debug("Could not find file " + remoteFileName
            + " to remove from remote SFTP Server");
          throw new FileNotFoundException("Could not find file " + remoteFileName
            + " to remove from remote SFTP Server ", se);
        default:
          log.debug("Failed to delete remote file " + remoteFileName);
          throw new IOException("Failed to delete remote file " + remoteFileName, se);
      }
      // failed to delete file do nothing
    }
  }

  /**
   * checks if a file exists on a sanitizer server.  Nothing is returned.
   * @param remoteFileName
   *          The full path and name of the file to delete
   * @return true or false
   * @throws IOException
   *          Thrown if an I/O error occurs
   */
  public boolean fileExists(final String remoteFileName) throws IOException
  {
    try
    {
      sftp.lstat(remoteFileName);
      return true;
    }
    catch (SftpException se)
    {
      // file doesn't exist do nothing
    }
    return false;
  }

  /**
   * Builds a full file name from a path and a filename.
   *
   * @param entry
   *          The sftp channel ls listing
   * @param path
   *          The remote path
   * @return fullFilePath
   */
  private String newFilePath(final LsEntry entry, String path)
  {
    if (entry == null)
    {
      return null;
    }
    final File newFullPath = new File(path, entry.getFilename());
    final String newFullForwardPath = newFullPath.getPath().replace("\\", "/");

    return newFullForwardPath;
  }

  /**
   * Returns a input stream of the sanitized file.
   *
   * @param remoteFileName
   *          The name of the sanitized file to retrieve
   * @return InputStream of the sanitized file
   * @throws IOException
   *          Thrown if an I/O error occurs
   * @throws FileNotFoundException
   *          Throw if the file cannot be retrieved
   */
  public InputStream getInputStream(final String remoteFileName)
      throws IOException, FileNotFoundException
  {
    final ChannelSftp sftp = getSftpChannel();
    String source = Paths.get(outputPath, remoteFileName).toString();
    try
    {
      return sftp.get(remoteFileName);
    }
    catch (final SftpException se)
    {
      switch (se.id)
      {
        case ChannelSftp.SSH_FX_NO_SUCH_FILE:
          log.error("Failed to process {} due to {}", new Object[] {remoteFileName, se });
          throw new FileNotFoundException(
                  "Could not find file " + remoteFileName + " on remote SFTP Server", se);
        case ChannelSftp.SSH_FX_PERMISSION_DENIED:
          log.error("Failed to process {} due to {}", new Object[] {remoteFileName, se });
          throw new IOException("Insufficient permissions to read file " + remoteFileName
                                + " from remote SFTP Server", se);
        default:
          throw new IOException("Failed to obtain file content for " + remoteFileName, se);
      }
    }
  }

  /**
   * Closes an ssh session from the calling app.
   *
   */
  public void closeSession()
  {
    close(jsession);
  }

  /**
   * Closes an ssh session.
   *
   * @param session
   *          The pointer to the open ssh/sftp session
   */
  public void close(Session session)
  {
    if (closed)
    {
      return;
    }
    closed = true;

    try
    {
      if (null != sftp)
      {
        sftp.exit();
      }
    }
    catch (final Exception ex)
    {
      log.warn("Failed to close ssh/sftp due to {}", new Object[] { ex.toString() }, ex);
    }
    sftp = null;

    try
    {
      if (null != session)
      {
        session.disconnect();
      }
    }
    catch (final Exception ex)
    {
      log.warn("Failed to close session due to {}", new Object[] { ex.toString() }, ex);
    }
    session = null;
  }

  /**
   * Opens an SSH connection to the sanitizer server. returns a pointer
   * to an ssh session if successful,.otherwise returns null
   *
   * @return jsession
   * @throws IOException
   *          Throw if an I/O error occurs
   */
  public Session getSession() throws IOException
  {
    if (jsession != null)
    {
      String sessionhost = jsession.getHost();
      String desthost = hostServer;
      if (sessionhost.equals(desthost))
      {
        // destination matches so we can keep our current session
        return jsession;
      }
      else
      {
        // this flowFile is going to a different destination, reset session
        jsession.disconnect();
        jsession = null;
      }
    }

    final JSch jsch = new JSch();
    try
    {
      final String username = userId;
      final Session session = jsch.getSession(username, hostServer, Integer.parseInt(sftpPort));

      final String useKey = useHostKey;
      final String hostKeyVal = hostKeyFile;

      final Properties properties = new Properties();
      if (useKey == "true")
      {
        if (hostKeyVal != null)
        {
          jsch.setKnownHosts(hostKeyVal);
          properties.setProperty("StrictHostKeyChecking", "yes");
        }
      }
      else
      {
        properties.setProperty("StrictHostKeyChecking", "no");
      }

      // Don't use compression
      properties.setProperty("compression.s2c", "none");
      properties.setProperty("compression.c2s", "none");

      session.setConfig(properties);

      final String pword = password;
      if (pword != null)
      {
        session.setPassword(pword);
      }

      connectionTimeoutMillis =
          Integer.parseInt(timeOut.substring(0, timeOut.lastIndexOf(" "))) * 1000;
      session.setTimeout(connectionTimeoutMillis);
      session.connect();
      this.jsession = session;
      this.closed = false;
      return jsession;
    }
    catch (JSchException je)
    {
      throw new IOException("Failed to obtain connection to remote host due to " + je.toString(),
          je);
    }
  }

  /**
  * Opens an SFTP connection to the sanitizer server. returns a pointer
  * to an sftp session if successful,.otherwise returns null.
  *
  * @return The SFTP Channel
  * @throws IOException
  *           Thrown if an I/O error occurs
  */
  public ChannelSftp getSftpChannel()
    throws IOException
  {
    try
    {
      jsession = getSession();
      if (jsession != null)
      {
        sftp = (ChannelSftp) jsession.openChannel("sftp");
        sftp.connect(connectionTimeoutMillis);
      }
      return sftp;
    }
    catch (JSchException je)
    {
      log.warn("failed to obtain an sftp  connection" );
      throw new IOException("Failed to obtain connection to remote host due to " + je.toString(),
         je);
    }
  }

  /**
  * Opens an ChannelExec connection to the sanitizer server. returns a pointer
  * to an sftp session if successful,.otherwise retrurns null.
  *
  * @return The SSH Channel
  * @throws IOException
  *           Thrown if an I/O error occurs
  */
  public ChannelExec getExecChannel()
     throws IOException
  {
    try
    {
      jsession = getSession();
      if (jsession != null)
      {
        exec = (ChannelExec) jsession.openChannel("exec");
        exec.connect(connectionTimeoutMillis);
      }
      return exec;
    }
    catch (JSchException je)
    {
      log.warn("failed to obtain an ssh shell connection" );
      throw new IOException("Failed to obtain connection to remote host due to " + je.toString(),
        je);
    }
  }

  /**
  * Performs a ps -ef for the engineServiceName on the
  * sanitizer VM returns true if the process is in the list
  * else returns false.
  *
  * @param engineServiceName
  *          The name of the Sanitizer Service
  * @return True or False
  * @throws IOException
  *           Thrown if an I/O error occurs
  */
  public boolean checkEngineStatus(String engineServiceName)
    throws IOException
  {
    boolean success = false;
    String command = "ps -ef | grep " + engineServiceName + " | grep -v grep";
    final ChannelExec exec = getExecChannel();
    try
    {
      exec.setCommand(command);
      exec.setInputStream(null);
      InputStream in = exec.getInputStream();
      exec.connect();
      if (in.available() > 0)
      {
        success = true;
      }
      exec.disconnect();
      return success;
    }
    catch (IOException ioe)
    {
      throw new IOException("Failed to execute remote command  " + ioe.toString(),
          ioe);
    }
    catch (JSchException je)
    {
      throw new IOException("Failed to execute remote command  " + je.toString(),
        je);
    }
  }

  /**
  * Tests for an SFTP connection, returns true or false.
  * @return boolean
  */
  public boolean sftpConnection()
  {
    if (sftp != null)
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  /**
  *SFTP sends the file to be sanitize to the sanitizer.  If successful
  * returns the file path, if unsuccessful, returns null.
  *
  * @param filename
  *          The file name of the file to sanitize
  * @param content
  *          The nifi input stream content to be sanitized
  * @return full path of the put file
  * @throws IOException
  *           Thrown if an I/O error occurs
  */
  public String put(final String filename, final InputStream content) throws IOException
  {
    final String path = inputPath;

    log.debug("opening sftp channel");
    final ChannelSftp sftp = getSftpChannel();

    // destination path + filename
    final String fullPath = (path == null) ? filename
        : (path.endsWith("/")) ? path + filename : path + "/" + filename;

    log.debug("destination path is " + fullPath);
    // temporary path + filename -> not using dotRename set variable to false for
    // now
    String tempFilename = null;
    if (tempFilename == null)
    {
      final boolean dotRename = false;
      tempFilename = dotRename ? "." + filename : filename;
    }
    final String tempPath = (path == null) ? tempFilename
        : (path.endsWith("/")) ? path + tempFilename : path + "/" + tempFilename;

    try
    {
      sftp.put(content, tempPath);
    }
    catch (final SftpException se)
    {
      log.error("Failed to process {} due to {}", new Object[] { tempPath, se });
      throw new IOException("Unable to put content to " + fullPath + " due to " + se, se);
    }

    if (!filename.equals(tempFilename))
    {
      try
      {
        sftp.rename(tempPath, fullPath);
      }
      catch (final SftpException se)
      {
        log.error("Failed to process {} due to {}", new Object[] { tempPath, se });
      }
      try
      {
        sftp.rm(tempPath);
        throw new IOException("Failed to rename dot-file to " + fullPath);
      }
      catch (final SftpException se1)
      {
        log.error("Failed to process {} due to {}", new Object[] { tempPath, se1 });
        throw new IOException("Failed to rename dot-file to " + fullPath
            + " and failed to delete it when attempting to clean up", se1);
      }
    }
    return fullPath;
  }
}
