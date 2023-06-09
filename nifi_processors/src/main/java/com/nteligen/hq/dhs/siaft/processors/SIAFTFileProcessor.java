package com.nteligen.hq.dhs.siaft.processors;

import com.nteligen.hq.dhs.siaft.exceptions.FileTimeoutException;
import com.nteligen.hq.dhs.siaft.exceptions.SessionException;

import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;


public abstract class SIAFTFileProcessor extends SIAFTBaseProcessor
{
  public static final String MODIFIED_REPORT_CONTENT = "generated analysis report";

  /**
  * Writes the file to process on the provided dropoff session.
  *
  * @param dropoff  The active dropoff session to make use of.
  * @param session  The active NiFi ProcessSession.
  * @param flowFile The FlowFile containing the file to process.
  * @param filename The file path (on remote host) where the file is to be
  *                 written.
  * @throws IOException There was a problem submitting the file for processing.
  */
  protected void putFile(DropoffSession dropoff, ProcessSession session,
     FlowFile flowFile, String filename) throws IOException
  {
    try (InputStream is = session.read(flowFile))
    {
      getLogger().debug("Writing file [dest=" + filename + "]");
      dropoff.write(is, filename);
    }
    catch (SessionException ex)
    {
      throw new IOException("Error writing file on dropoff session", ex);
    }
  }

  /**
  * Transfer the report contents using the provided dropoff session and place
  * it into the provided FlowFile.
  *
  * @param dropoff The active dropoff session to make use of.
  * @param session The active NiFi ProcessSession.
  * @param flowFile The FlowFile where the results are to be placed.
  * @param filename The filename of the file to read.
  * @throws IOException THere was a problem reading the file.
  */
  private void readFile(DropoffSession dropoff, ProcessSession session,
      FlowFile flowFile, String filename) throws IOException
  {
    try (OutputStream os = session.write(flowFile))
    {
      dropoff.read(os, filename);
    }
    catch (SessionException ex)
    {
      throw new IOException("Error reading file on dropoff session", ex);
    }

  }

  protected void putReportAttributes(DropoffSession dropoff, ProcessSession session,
      FlowFile reportFlowFile)
  {
    try
    {
      Map<String, String> attributesMap =  dropoff.getReportAttributes();
      if (!attributesMap.isEmpty())
      {
        Iterator iterator = attributesMap.entrySet().iterator();
        while (iterator.hasNext())
        {
          Map.Entry<String, String> reportAttribute = (Map.Entry<String, String>) iterator.next();
          //Set report results as attributes
          session.putAttribute(reportFlowFile,  reportAttribute.getKey(),
            reportAttribute.getValue());
        }
      }
    }
    catch (NullPointerException ex)
    {
      //do nothing
    }
  }

  /**
  * Deletes the file at the given path using the provided dropoff session. If
  * no file exits, or there was any problem (e.g. permission denied) then no
  * action is taken and fails silently.
  *
  * @param dropoff  The active dropoff session to make use of.
  * @param filename The path of the file to delete.
  */
  protected void deleteFile(DropoffSession dropoff, String filename)
  {
    try
    {
      getLogger().debug("Deleting file [file=" + filename + "]");
      dropoff.delete(filename);
    }
    catch (SessionException ex)
    {
      // warn that file could not be deleted, however continue processing
      getLogger().warn("Unable to delete file [file=" + filename + "]");
    }
  }

  /**
  * Retrieves the given file. Waits until the specified file is found, or the
  * timeout is reached.
  *
  * @param dropoff        The active DropoffSession to make use of.
  * @param session        The active NiFi ProcessSession.
  * @param flowFile       The FlowFile where the processing results are to be
  *                       written.
  * @param filename       The filename of the file to retrieved.
  * @throws IOException   There was an error retrieving the processing results.
  * @throws FileTimeoutException Throws if the file cannot be obtained before
  *                              the specified timeout length;
  */
  protected void getFile(DropoffSession dropoff, ProcessSession session,
                         FlowFile flowFile, String filename)
                         throws FileTimeoutException, IOException
  {
    long startTime = System.currentTimeMillis();
    long endTime = startTime + (dropoff.getTimeout() * 1000);
    boolean timedOut = true;

    while (System.currentTimeMillis() < endTime)
    {
      getLogger().debug("Checking if processing is complete [filename=" + filename + "]");
      if (dropoff.exists(filename))
      {
        getLogger().debug("File found, reading report [filename=" + filename + "]");
        readFile(dropoff, session, flowFile, filename);
        timedOut = false;
        break;
      }
      else
      {
        try
        {
          Thread.sleep(1000);
        }
        catch (InterruptedException ex)
        {
          getLogger().error("Sleep interrupted, failing silently");
          getLogger().error(ex.getMessage());
        }
      }
    }

    if (timedOut)
    {
      throw new FileTimeoutException("File processing timed out [source="
                                     + filename + "]");
    }
  }
}
