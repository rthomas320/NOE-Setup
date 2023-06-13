package com.nteligen.hq.dhs.siaft.processors;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MsOfficeHelper
{
  /**
   * Detects if a given office document is protected by a password or not.
   * Supported formats: Word, Excel and PowerPoint (both legacy and OpenXml).
   *
   * @param stream Office Document stream
   * @param byteCount number of bytes in the stream
   * @return True if document is protected by a password, false otherwise
   * @throws IOException if there is a problem with the stream
  */
  public static boolean isPasswordProtected(InputStream stream, int byteCount) throws IOException
  {
    int streamSize = byteCount;
    // minimum file size for office file is 4k

    if (streamSize < 4096)
    {
      return false;
    }

    // read file header
    byte[] compObjHeader = new byte[32];
    stream.read(compObjHeader, 0, 32);

    // check if we have plain zip file
    if (compObjHeader[0] == 'P' && compObjHeader[1] == 'K')
    {
      // this is a plain OpenXml document (not encrypted)
      return false;
    }

    // check compound object magic bytes
    int val1 = compObjHeader[0] & 0xFF;
    int val2 = compObjHeader[1] & 0xFF;
    if (val1 != 0xD0 || val2 != 0xCF)
    {
      // unknown document format
      return false;
    }

    int sectionSizePower = compObjHeader[30];
    if (sectionSizePower < 8 || sectionSizePower > 16)
    {
      // invalid section size
      return false;
    }
    int sectionSize = 2 << (sectionSizePower - 1);

    final int defaultScanLength = 32768;
    int scanLength = Math.min(defaultScanLength, streamSize);

    // read header part for scan
    byte[] header = new byte[scanLength];
    stream.read(header, 0, scanLength);

    // check if we detected password protection
    if (scanForPassword(header, sectionSize))
    {
      return true;
    }

    // if not, try to scan footer as well
    int scanStart = 0;
    if (scanLength > 32768)
    {
      scanStart = streamSize - sectionSize;
    }

    // read footer part for scan
    byte[] footer = new byte[scanLength];
    stream.read(footer, scanStart, scanLength);

    // finally return the result
    return scanForPassword(footer, sectionSize);
  }

  static boolean scanForPassword(byte[] buffer, int sectionSize)
  {
    final String afterNamePadding = "\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0";
    String bufferString = new String(buffer, 0, buffer.length, StandardCharsets.US_ASCII);

    // try to detect password protection used in new OpenXml documents
    // by searching for "EncryptedPackage" or "EncryptedSummary" streams
    final String encryptedPackageName = "E\0n\0c\0r\0y\0p\0t\0e\0d\0P\0a\0c\0k\0a\0g\0e"
      + afterNamePadding;
    final String encryptedSummaryName = "E\0n\0c\0r\0y\0p\0t\0e\0d\0S\0u\0m\0m\0a\0r\0y"
      + afterNamePadding;
    if (bufferString.contains(encryptedPackageName)
        || bufferString.contains(encryptedSummaryName))
    {
      return true;
    }
    return false;
  }

}
