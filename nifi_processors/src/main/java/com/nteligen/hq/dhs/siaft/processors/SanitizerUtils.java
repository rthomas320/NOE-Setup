package com.nteligen.hq.dhs.siaft.processors;

import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.flowfile.attributes.CoreAttributes;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.stream.io.NullOutputStream;
import org.apache.nifi.stream.io.StreamUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaMetadataKeys;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicReference;

public class SanitizerUtils
{
  private static final Logger log = LoggerFactory.getLogger(SanitizerUtils.class);

  /**
  * SanitizerUtils constructor.
  *
  */
  public SanitizerUtils()
  {

  }

  /**
  * Calculates the MD5 of the FlowFile content and returns the value if
  * successful, if an exception occurs it transfers the session to failure.
  *
  * @param session
  *          The nifi ProcessSession
  * @param flowFile
  *          The nifi FlowFile
  * @return The MD5 hash value
  */
  public static String getMd5(ProcessSession session, FlowFile flowFile)
  {
    final AtomicReference<String> hashValue = new AtomicReference<>();
    hashValue.set(null);

    MessageDigest digest;
    try
    {
      digest = MessageDigest.getInstance("MD5");
    }
    catch (NoSuchAlgorithmException nsae)
    {
      log.error("Failed to process {} due to {}", new Object[] { flowFile, nsae });
      return null;
    }

    try
    {
      session.read(flowFile, new InputStreamCallback()
      {
        @Override
        public void process(final InputStream in) throws IOException
        {
          try (final DigestOutputStream digestOut = new DigestOutputStream(new NullOutputStream(),
              digest))
          {
            StreamUtils.copy(in, digestOut);
            final byte[] hash = digest.digest();
            final StringBuilder strb = new StringBuilder(hash.length * 2);
            for (int i = 0; i < hash.length; i++)
            {
              strb.append(Integer.toHexString((hash[i] & 0xFF) | 0x100).substring(1, 3));
            }
            hashValue.set(strb.toString());
            log.info("MD5 hash value is " + hashValue.get());
          }
        }
      });
    }
    catch (final ProcessException pe)
    {
      log.error("Failed to process {} due to {}", new Object[] { flowFile, pe });
      session.putAttribute(flowFile, "process_success", "MD5Failure");
      session.getProvenanceReporter().modifyAttributes(flowFile);
      return null;
    }
    return hashValue.get();
  }

  /**
   * Determines the MIME type of the FlowFile content and returns the value if
   * successful, if an exception occurs or the MIME type is unable to be
   * determined application/octet-stream is returned.
   *
   * @param session
   *          The nifi ProcessSession
   * @param flowFile
   *          The nifi FlowFile
   * @return MIME type as file extension
   */
  // Determine MIME type of the flow file content
  public static String getMimeType(ProcessSession session, FlowFile flowFile)
  {
    final TikaConfig config = TikaConfig.getDefaultConfig();
    final Detector detector = config.getDetector();

    final AtomicReference<String> mimeTypeRef = new AtomicReference<>(null);
    final String filename = flowFile.getAttribute(CoreAttributes.FILENAME.key());

    session.read(flowFile, new InputStreamCallback()
    {
      @Override
      public void process(final InputStream stream) throws IOException
      {
        try (final InputStream in = new BufferedInputStream(stream))
        {
          TikaInputStream tikaStream = TikaInputStream.get(in);
          Metadata metadata = new Metadata();

          if (filename != null)
          {
            metadata.add(TikaMetadataKeys.RESOURCE_NAME_KEY, filename);
          }
          // Get mime type
          org.apache.tika.mime.MediaType mediatype = detector.detect(tikaStream, metadata);
          mimeTypeRef.set(mediatype.toString());
        }
        catch (IOException ioe)
        {
          session.putAttribute(flowFile, "process_success", "MIMEFailure");
        }
      }
    });

    String mimeType = mimeTypeRef.get();
    String extension = "";
    try
    {
      MimeType mimetype;
      mimetype = config.getMimeRepository().forName(mimeType);
      extension = mimetype.getExtension();
    }
    catch (MimeTypeException ex)
    {
      log.warn("MIME type extension lookup failed: {}", new Object[] { ex });
    }

    // Workaround for bug in Tika - https://issues.apache.org/jira/browse/TIKA-1563
    if (mimeType != null && mimeType.equals("application/gzip") && extension.equals(".tgz"))
    {
      extension = ".gz";
    }

    if (mimeType == null)
    {
      log.debug("Unable to identify MIME Type for {}; setting to application/octet-stream",
          new Object[] { flowFile });
    }
    else
    {
      log.debug("Identified {} as having MIME Type {}", new Object[] { flowFile, mimeType });
    }
    return extension;
  }
}
