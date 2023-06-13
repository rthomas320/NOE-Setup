package com.nteligen.hq.dhs.siaft.processors;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class SIAFTUtils
{
  public static final String JOB_ID = "uuid";
  public static final String UUID = "siaft.uuid";
  public static final String ENTRY = "siaft.entry";
  public static final String PIPELINE = "siaft.pipeline";
  public static final String FILE_ATTR_ID = "siaft.fileAttributesId";
  public static final String SANITIZE_ENGINE_ID = "siaft.sanitizeEngineId";
  public static final String ANALYSIS_ENGINE_ID = "siaft.analysisEngineId";
  public static final String UNPROCESSED_FILE = "siaft.unprocessedFile";
  public static final String UNPROCESSED_REASON = "siaft.unprocessedReason";
  public static final String MIME_TYPE = "siaft.mimeType";
  public static final String SKIPPED_FILE_COUNT = "siaft.skippedFileCount";

  public static String readFile(String path, Charset encoding) throws IOException
  {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  public static String readFile(URI path, Charset encoding) throws IOException
  {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  /**
   * Calculates the MD5 for an inputstream.
   * @param inputStream the inputstream
   * @return the string of the MD5 of the input stream
   * @throws IOException if there is an IOException
   * @throws NoSuchAlgorithmException If the algorithm isn't found
   */
  public static String calcMD5(InputStream inputStream) throws IOException, NoSuchAlgorithmException
  {
    MessageDigest md = MessageDigest.getInstance("MD5");
    final AtomicReference<String> md5sumHolder = new AtomicReference<>(null);
    byte[] bytes = IOUtils.toByteArray(inputStream);
    md.update(bytes, 0, bytes.length);

    byte[] hash = md.digest();
    md5sumHolder.set(Hex.encodeHexString(hash));
    String md5sum = md5sumHolder.get();
    return md5sum;
  }

  /**
   * THis will take in a map of attribute key value pairs and return a list of values that have a
   * key name that matches the base name
   *
   * <p>Example:
   * attributes = {[{"key.0":"value1"}, {"somethingelse":"value2"}, {"key.1":"value3"}]}
   * baseName = "key.";
   *
   * <p>Returned list = ["value1", "value3"]
   *
   * @param attributes the attributes
   * @param baseName the basename to search the keys for.
   * @return the list of values that had keys that matches the basename
   */
  public static List<String> getAttributeList(Map<String, String> attributes, String baseName)
  {
    List<String> attributeValues = new ArrayList<>();
    for (Map.Entry<String, String> attributeEntry : attributes.entrySet())
    {
      if (attributeEntry.getKey().startsWith(baseName))
      {
        attributeValues.add(attributeEntry.getValue());
      }
    }
    return attributeValues;
  }
}
