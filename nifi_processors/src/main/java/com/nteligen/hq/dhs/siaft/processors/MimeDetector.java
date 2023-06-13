package com.nteligen.hq.dhs.siaft.processors;

import com.nteligen.hq.dhs.siaft.exceptions.MimeTypeDetectionException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaMetadataKeys;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Provides MIME type checking capabilities using Apache Tika and allows for
 * checking that a file's content matches its given extension.
 */
public class MimeDetector
{
  private final TikaConfig config;
  private final Detector detector;
  private final Tika tika;
  private static final Logger log = LoggerFactory.getLogger(MimeDetector.class);

  /**
   * MimeDetector constructor.
   */
  public MimeDetector()
  {
    // setup Tika
    this.config = TikaConfig.getDefaultConfig();
    this.detector = config.getDetector();
    this.tika = new Tika(detector);
  }

  /**
   * Determines if the file type of the provided content matches the given
   * filename extension.
   *
   * @param content The file content.
   * @param filename The filename.
   * @return True if the filename extension is valid. Otherwise returns false.
   * @throws MimeTypeDetectionException There was an error detecting mime type.
   */
  public boolean validExtension(byte[] content, String filename) throws MimeTypeDetectionException
  {
    if (StringUtils.isEmpty(filename))
    {
      this.log.debug("Filename is empty, invalid.");
      return false;
    }

    String extension = FilenameUtils.getExtension(filename);
    if (StringUtils.isEmpty(extension))
    {
      this.log.debug("Filename has no extension present, invalid.");
      return false;
    }

    String type;
    type = getMimeType(content, filename);

    this.log.debug("Detected Mime Type is " + type);

    if (type.equals(extension))
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  /**
   * Determines the mime type of the provided content
   * @param content The file content.
   * @param filename The filename.
   * @return the string of the mime type
   * @throws MimeTypeDetectionException when this function fails to discover to mime type
   */
  public String getMimeType(byte[] content, String filename) throws MimeTypeDetectionException
  {
    String type;
    Metadata metadata = new Metadata();
    if (filename != null)
    {
      metadata.add(TikaMetadataKeys.RESOURCE_NAME_KEY, filename);
    }

    String mediaType = null;
    try (InputStream inStream = new ByteArrayInputStream(content))
    {
      mediaType = tika.detect(inStream, metadata);
    }
    catch (IOException ex)
    {
      throw new MimeTypeDetectionException("Failed to get mediatype.", ex);
    }
    this.log.debug("Determined media type [filename=" + filename
                      + ", mediaType=" + mediaType + "]");
    try
    {
      MimeType mimeType = config.getMimeRepository().forName(mediaType);
      type = mimeType.getExtension();
    }
    catch (MimeTypeException ex)
    {
      throw new MimeTypeDetectionException("Failed to get mime type.", ex);
    }
    // remove dot from extension
    type = type.replaceFirst(".", "");
    return type;
  }

}
