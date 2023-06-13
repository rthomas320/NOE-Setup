package com.nteligen.hq.dhs.siaft.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nteligen.hq.dhs.siaft.IntegrationTests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Performs tests of the SftpSession class, which creates active SFTP
 * sessions with localhost. Assumes an active SFTP server is running on
 * localhost at the standard port (22) with username and password
 * "vagrant".
 */
@Category(IntegrationTests.class)
public class SftpSessionTest
{
  String dropoffPath = "/tmp";
  String pickupPath = "/tmp";
  String filename = "foo.txt";
  Path testFile = Paths.get(pickupPath, filename);

  String content = "foo";
  Properties properties;

  String user = "vagrant";
  String host = "localhost";
  String password = "vagrant";
  int port = 22;


  /**
   * Set up test pre-conditions.
   * @throws IOException There was an error setting up the test.
   */
  @Before
  public void setup() throws IOException
  {
    Files.deleteIfExists(testFile);
    Files.createFile(testFile);
    Files.write(testFile, content.getBytes());

    properties = new Properties();
    properties.setProperty("StrictHostKeyChecking", "no");
  }

  @After
  public void cleanup() throws IOException
  {
    Files.deleteIfExists(testFile);
  }

  /**
   * Checks the positive and negative cases for the session.exists() method.
   */
  @Test
  public void testExists() throws Exception
  {
    try (SftpSession session = new SftpSession(user, host, password, port, properties))
    {
      session.setDropoff(dropoffPath);
      session.setPickup(pickupPath);
      session.connect();

      assertTrue(session.exists(filename));

      Files.deleteIfExists(testFile);
      assertFalse(session.exists(filename));
    }
  }

  /**
   * Reads a file across an SFTP session. Validates that the file content
   * returned is correct.
   */
  @Test
  public void testRead() throws Exception
  {
    try (SftpSession session = new SftpSession(user, host, password, port, properties))
    {
      session.setDropoff(dropoffPath);
      session.setPickup(pickupPath);
      session.connect();
      assertTrue(session.exists(filename));

      OutputStream os = new ByteArrayOutputStream();
      session.read(os, filename);
      assertEquals(content, os.toString());
    }
  }

  /**
   * Writes a file across an SFTP session. Validates that the written file
   * content is correct.
   */
  @Test
  public void testWrite() throws Exception
  {
    try (SftpSession session = new SftpSession(user, host, password, port, properties))
    {
      session.setDropoff(dropoffPath);
      session.setPickup(pickupPath);
      session.connect();

      // ensure file does not exist
      Files.deleteIfExists(testFile);

      assertFalse(session.exists(filename));

      InputStream is = new ByteArrayInputStream(content.getBytes());
      session.write(is, filename);

      // assert file was created
      assertTrue(session.exists(filename));

      // verify file content
      OutputStream os = new ByteArrayOutputStream();
      session.read(os, filename);
      assertEquals(content, os.toString());
    }
  }

}
