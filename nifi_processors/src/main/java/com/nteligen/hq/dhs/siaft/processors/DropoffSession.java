package com.nteligen.hq.dhs.siaft.processors;

import com.nteligen.hq.dhs.siaft.exceptions.SessionException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Provides a common interface for submitting and retrieving files to/from a
 * file processing host. This is done in a "dropoff" and polling fashion, where
 * a file is submitted and the client must poll and ouptut location until
 * processing is complete.
 */
public interface DropoffSession extends AutoCloseable
{
  /**
   * Connect to the processing host.
   * @throws SessionException There was a problem connecting to the processing host.
   */
  public void connect() throws SessionException;

  /**
   * Checks if a file with the given name exits. If the file exists, it
   * signifies that processing is complete.
   * @param filename The name of the file to check.
   * @return True if the file exists, otherwise false.
   */
  public boolean exists(String filename);

  /**
   * Delete the file with the given name if it exists.
   * @param filename The name of the file to delete.
   * @throws SessionException Indicators there was a problem deleting the file.
   */
  public void delete(String filename) throws SessionException;

  /**
   * Writes the given file content (as an input stream) to the processing host.
   * @param in The file content.
   * @param filename The name of the file.
   * @throws SessionException Indicates there was a problem submitting the file
   *                          to the processing host.
   */
  public void write(InputStream in, String filename) throws SessionException;

  /**
   * Reads the file with the given filename on the processing host.
   * @param os       The output stream where the file content is to be written.
   * @param filename The name of the file to read.
   * @throws SessionException There was a problem reading the given file.
   */
  public void read(OutputStream os, String filename) throws SessionException;

  /**
   * Get the analysis report attributes
   * @return HashMap.
   */

  public Map<String, String> getReportAttributes();

  /**
   * Get the analysis report extension type.
   * @return The extension type.
   */
  public String getReportExtension();

  /**
   * Close the session.
   */
  @Override
  public void close() throws SessionException;

  /**
   * Get the session timeout (seconds).
   * @return The session timeout in seconds.
   */
  public int getTimeout();

}
