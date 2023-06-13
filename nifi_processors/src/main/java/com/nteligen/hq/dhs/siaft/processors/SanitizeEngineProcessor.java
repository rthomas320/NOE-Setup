package com.nteligen.hq.dhs.siaft.processors;

import com.nteligen.hq.dhs.siaft.exceptions.FileNotFoundException;
import com.nteligen.hq.dhs.siaft.exceptions.SIAFTFatalProcessException;
import com.nteligen.hq.dhs.siaft.exceptions.SanitizationException;

import org.apache.commons.lang.StringUtils;
import org.apache.nifi.annotation.behavior.SideEffectFree;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.flowfile.attributes.CoreAttributes;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.stream.io.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.Set;

@SideEffectFree
@Tags({ "SanitizeEngineProcessor", "SIAFT" })
@CapabilityDescription("An HTTP client processor which interacts with a Sanitization Engine."
        + "The process utilizes SFTP to drop off a file for sanitization"
        + "and to pick up sanitized files. The host/server name, user,"
        + "password drop off path, and pickup path are configurable.")
@WritesAttributes(
        {
                @WritesAttribute(attribute = SanitizeEngineProcessor.PROCESS_SUCCESS_ATTR,
                        description = "This indicates that the sanitization process was "
                                + "successful or not."),
                @WritesAttribute(attribute = SanitizeEngineProcessor.PROCESS_MD5_ATTR,
                        description = "The post processed MD5 of the file."),
                @WritesAttribute(attribute = SanitizeEngineProcessor.PROCESS_MIME_ATTR,
                        description = "The post processed mime type of the file.")
        })

public class SanitizeEngineProcessor extends SIAFTBaseRetryProcessor
{
  public static final String PROCESS_SUCCESS_ATTR = "process_success";
  public static final String PROCESS_MD5_ATTR = "process_md5";
  public static final String PROCESS_MIME_ATTR = "process_mime";
  public static final SuccessBehavior SUCCESS_BEHAVIOR = new SuccessBehavior();
  public static final UpdateDatabaseBehavior UPDATE_DATABASE_BEHAVIOR =
          new UpdateDatabaseBehavior();
  public static final SanitizeEngineBehavior SANITIZER_BEHAVIOR = new SanitizeEngineBehavior();
  //public SanitizeEngineProcessor sanitizeEngine;

  private ComponentLog log;


  @Override
  protected Set<SIAFTBehaviorRetrievable> getBehaviors()
  {
    Set<SIAFTBehaviorRetrievable> behaviors = super.getBehaviors();
    behaviors.add(SUCCESS_BEHAVIOR);
    behaviors.add(UPDATE_DATABASE_BEHAVIOR);
    behaviors.add(SANITIZER_BEHAVIOR);
    return behaviors;
  }

  @Override
  protected void initInternal()
  {
    log = getLogger();
  }

  @Override
  public void onTriggerInternal(final ProcessContext context,
                                final ProcessSession session,
                                FlowFile flowFile)
          throws ProcessException, SIAFTFatalProcessException
  {
    getLogger().trace("Processing session " + session);

    if (flowFile == null)
    {
      return;
    }

    getLogger().trace("Processing flowfile " + flowFile);

    final ComponentLog logger = getLogger();

    // Set Sanitizing Service Name
    String sanitizeService = context.getProperty(SANITIZER_BEHAVIOR.SANITIZE_SERVICE ).getValue();
    if (sanitizeService.isEmpty())
    {
      sanitizeService = "not set";
    }
    log.debug("VM Sanitizing Engine Service is " + sanitizeService);

    // Set Host Server
    String hostServer = context.getProperty(SANITIZER_BEHAVIOR.HOST_SERVER).getValue();

    if (hostServer.isEmpty())
    {
      hostServer = "not set";
    }
    logger.debug("Host Server is " + hostServer);

    // Set Host SFTP port
    String sftpPort = context.getProperty(SANITIZER_BEHAVIOR.SFTP_PORT).getValue();

    if (sftpPort.isEmpty())
    {
      sftpPort = "not set";
    }
    logger.debug("SFTP Port is " + sftpPort);

    // Set SFTP Connection Timeout
    String timeOut = context.getProperty(SANITIZER_BEHAVIOR.TIME_OUT).getValue();

    if (timeOut.isEmpty())
    {
      timeOut = "not set";
    }
    logger.debug("SFTP timeout is " + timeOut);

    // Set Sanitize Engine file drop path
    String inputPath = context.getProperty(SANITIZER_BEHAVIOR.INPUT_PATH).getValue();

    if (inputPath.isEmpty())
    {
      inputPath = "/home/nifi/data/in";
    }
    logger.debug("File put path is " + inputPath);

    // Set Sanitize Engine file retrieve path
    String outputPath = context.getProperty(SANITIZER_BEHAVIOR.OUTPUT_PATH).getValue();

    if (outputPath.isEmpty())
    {
      outputPath = "/home/nifi/data/out";
    }
    logger.debug("File get path is" + outputPath);

    // Set Sanitize Engine User ID
    String userId = context.getProperty(SANITIZER_BEHAVIOR.USER_ID).getValue();

    if (userId.isEmpty())
    {
      userId = "not set";
    }
    logger.debug("User ID is " + userId);

    // Set Sanitize Engine SFTP Password
    String password = context.getProperty(SANITIZER_BEHAVIOR.PASSWORD).getValue();

    if (password.isEmpty())
    {
      password = "not set";
    }
    logger.debug("Password is set");

    String useHostKey = context.getProperty(SANITIZER_BEHAVIOR.USE_HOST_KEY).getValue();

    // Set Host Key Value
    String hostKey = "none";
    if ("true".equals(useHostKey))
    {
      hostKey  = context.getProperty(SANITIZER_BEHAVIOR.HOST_KEY_FILE).getValue();
    }

    String engineName = context.getProperty(SANITIZER_BEHAVIOR.SANITIZE_ENGINE)
            .getValue();
    if (StringUtils.isEmpty(engineName))
    {
      engineName = SANITIZER_BEHAVIOR.SANITIZE_ENGINE.getDefaultValue();
    }
    log.debug("Sanitize engine name : " + engineName);
    session.putAttribute(flowFile, "sanitize_engine_name", engineName);

    // Instantiate SFTP class
    SIAFTSftp siaftSftp = new SIAFTSftp(hostServer, sftpPort, timeOut,
        inputPath, outputPath, userId, password, useHostKey, hostKey);

    //set some initial values
    final String extension = SanitizerUtils.getMimeType(session, flowFile);
    final String preProcessMD5 = SanitizerUtils.getMd5(session, flowFile);
    final String uniqueFileName = flowFile.getAttribute(CoreAttributes.UUID.key()) + extension;
    SanitizationStatus  status = SanitizationStatus.NOT_SANITIZED;

    try
    {
      session.putAttribute(flowFile, "SFTP_put_path", hostServer + inputPath);
      session.getProvenanceReporter().send(flowFile, hostServer + inputPath);

      sanitizeFile(session, flowFile, hostServer, sanitizeService,
        uniqueFileName, siaftSftp);

      status = retrieveFile(session, flowFile,
        Paths.get(outputPath, uniqueFileName).toString(),
        siaftSftp);
    }
    catch (SanitizationException ex)
    {
      log.error("Failed to process file.", ex);
      session.putAttribute(flowFile,
              PROCESS_SUCCESS_ATTR,
              SanitizationStatus.SANITIZATION_FAILURE.toString());
      session.putAttribute(flowFile, "sanitize_engine_name", engineName);
/*      session.putAttribute(flowFile,
              SIAFTUtils.SANITIZE_ENGINE_ID,
              ((Long) sanitizeEngine.getSanitizeEngineId()).toString());*/
    }
    catch (SIAFTFatalProcessException ex)
    {
      log.error("Fatal error during sanitization processing.", ex);
      session.penalize(flowFile);
      throw ex;
    }
    finally
    {
      deleteFile(Paths.get(outputPath, uniqueFileName).toString(), siaftSftp);
      closeSftpSession(siaftSftp);
    }
    session.putAttribute(flowFile, "sftp_get_path", hostServer + outputPath);
    session.putAttribute(flowFile, PROCESS_SUCCESS_ATTR,
      status.toString());
    final String postProcessMD5 = updateFlowFileWithMimeandMd5(session, flowFile);
    if ((status.equals(SanitizationStatus.SANITIZED)) && (preProcessMD5.equals(postProcessMD5)))
    {
      status = SanitizationStatus.NOT_MODIFIED;
    }

    // send the flow file to the Sanitize database processor
    FlowFile updateDb = session.clone(flowFile, 0, 0);
    session.transfer(updateDb, UPDATE_DATABASE_BEHAVIOR.updateDatabaseRelationship);
    session.transfer(flowFile, SUCCESS_BEHAVIOR.successRelationship);
    session.getProvenanceReporter().fetch(flowFile, hostServer + outputPath);
  }

  /**
  * This will take the file out of the session's flowfile and send it to metadefender for
  * sanitization. It will then wait for the sanitization to be done and then process the results.
  * @param session the nifi session
  * @param flowFile the nifi flowfile
  * @param mdUrl the metadefender url
  * @param userAgent the user agent
  * @throws SanitizationException when a problem occcurred sanitizing the file
  * @throws SIAFTFatalProcessException When a serious problem occurred during the sanitization
  *      processing of the file that resulted in the flowfile attributes and/or the flowfile
  *      contents not trusted. A session rollback() is recommended.
  */
  private void sanitizeFile(ProcessSession session, FlowFile flowFile, String hostServer,
      String sanitizeService, String fileName, SIAFTSftp siaftSftp)
        throws SanitizationException, SIAFTFatalProcessException
  {
    //Test that host server is accessible
    try
    {
      InetAddress inet = InetAddress.getByName(hostServer);
      inet.isReachable(5000) ;
    }
    catch (IOException ioe)
    {
      throw new SanitizationException("Host server " + hostServer + " is unreachable", ioe);
    }

    try
    {
      siaftSftp.checkEngineStatus(sanitizeService);
    }
    catch (IOException ioe)
    {
      throw new SanitizationException("Sanitizing service " + sanitizeService
          + " is not verified for host " + hostServer, ioe);
    }

    // Send File to Sanitizer
    try (InputStream in = session.read(flowFile))
    {
      siaftSftp.put(fileName, in);
    }
    catch (IOException ex)
    {
      throw new SanitizationException("Failed to send file "
              + flowFile.getAttribute(CoreAttributes.FILENAME.key())
              + " to  host server " + hostServer, ex);
    }
  }

  private String updateFlowFileWithMimeandMd5(ProcessSession session, FlowFile flowFile)
  {
    // Get the MD5 of the processed file
    final String md5Value = SanitizerUtils.getMd5(session, flowFile);
    session.putAttribute(flowFile, PROCESS_MD5_ATTR, md5Value);

    // Get the MIME type of the processed file
    final String mimeType = SanitizerUtils.getMimeType(session, flowFile);
    session.putAttribute(flowFile, PROCESS_MIME_ATTR, mimeType);

    // update the attributes in the flowfile
    session.getProvenanceReporter().modifyAttributes(flowFile);
    return md5Value;
  }

  private boolean getSanitizeStatus(String fileName, int waitTime, SIAFTSftp siaftSftp)
  {
    int loopCounter = 0;
    int maxLoops = (waitTime * 60) / 5;
    do
    {
      // Check if the file is in the output directory
      try
      {
        if (siaftSftp.exists(fileName))
        {
          return true;
        }
        else
        {
          Thread.sleep(5000);
          ++loopCounter;
        }
      }
      catch (Exception se)
      {
        //do nothing
      }
    } while (loopCounter < maxLoops);
    return false;
  }

  private SanitizationStatus retrieveFile(ProcessSession session, FlowFile flowFile,
    String fileName, SIAFTSftp siaftSftp) throws SIAFTFatalProcessException,
    SanitizationException
  {
    // Maximum wait time in minutes
    int maxWaitTime = 5;
    SanitizationStatus status = SanitizationStatus.SANITIZED;

    // Check Sanitization Status
    boolean isComplete = getSanitizeStatus(fileName, maxWaitTime, siaftSftp);

    // Only attempt to get the processed file if there is a modified file to retrieve.
    // If a sanitized file was produced get it an replace the context of the original flowFile
    if (isComplete)
    {
      try (OutputStream outStream = session.write(flowFile))
      {
        // Retrieve the sanitized file
        try (InputStream inStream = siaftSftp.getInputStream(fileName))
        {
          StreamUtils.copy(inStream, outStream);
        }
      }
      catch (FileNotFoundException fnfe)
      {
        throw new SanitizationException("Sanitized file not found", fnfe);
      }
      catch (IOException ex) // need a fault barrier here because
      {
        throw new SIAFTFatalProcessException("Failed to write to flowFile Output stream for "
                + flowFile.getAttribute(CoreAttributes.FILENAME.key()), ex);
      }
    }
    else // the original file content will be passed along in the flowfile
    {
      status = SanitizationStatus.NOT_SANITIZED;
    }
    return status;
  }

  private void deleteFile(String fileName, SIAFTSftp siaftSftp)
  {
    try
    {
      siaftSftp.deleteFile(fileName);
    }
    catch (Exception ex)
    {
      //do nothing if the file is missing since
      //that is the purpose of the delete
    }
  }

  private void closeSftpSession(SIAFTSftp siaftSftp)
  {
    siaftSftp.closeSession();
  }

}
