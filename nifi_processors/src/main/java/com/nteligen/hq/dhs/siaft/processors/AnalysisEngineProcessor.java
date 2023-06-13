package com.nteligen.hq.dhs.siaft.processors;

import com.nteligen.hq.dhs.siaft.exceptions.FileNotFoundException;
import com.nteligen.hq.dhs.siaft.exceptions.SIAFTFatalProcessException;
import com.nteligen.hq.dhs.siaft.exceptions.AnalysisException;
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
@Tags({ "AnalysisEngineProcessor", "SIAFT" })
@CapabilityDescription("An SFTP client processor which interacts with a Analysis Engine."
        + "The process utilizes SFTP to drop off a file for analysis"
        + "and to pick up analyzed files. The host/server name, user,"
        + "password drop off path, and pickup path are configurable.")
@WritesAttributes(
        {
                @WritesAttribute(attribute = AnalysisEngineProcessor.ANALYSIS_SUCCESS,
                        description = "This indicates that the analysis process was "
                                + "successful or not."),
                @WritesAttribute(attribute = AnalysisEngineProcessor.ANALYSIS_RESULTS,
                        description = "The post processed MD5 of the file."),
                @WritesAttribute(attribute = AnalysisEngineProcessor.SANITIZE_ENGINE,
                        description = "The post processed mime type of the file.")
        })

public class AnalysisEngineProcessor extends SIAFTBaseRetryProcessor
{
    public static final String ANALYSIS_SUCCESS = "analysis_success";
    public static final String ANALYSIS_RESULTS = "analysis_results";
    public static final String SANITIZE_ENGINE = "sanitize_engine";
    public static final SuccessBehavior SUCCESS_BEHAVIOR = new SuccessBehavior();
    public static final AnalysisEngineBehavior ANALYSIS_BEHAVIOR = new AnalysisEngineBehavior();
    public static final UpdateDatabaseBehavior UPDATE_DATABASE_BEHAVIOR =
            new UpdateDatabaseBehavior();

    private ComponentLog log;


    @Override
    protected Set<SIAFTBehaviorRetrievable> getBehaviors()
    {
        Set<SIAFTBehaviorRetrievable> behaviors = super.getBehaviors();
        behaviors.add(SUCCESS_BEHAVIOR);
        behaviors.add(UPDATE_DATABASE_BEHAVIOR);
        behaviors.add(ANALYSIS_BEHAVIOR);
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

        // Set Analysis Service Name
        String analyzeService = context.getProperty(ANALYSIS_BEHAVIOR.ANALYZE_SERVICE).getValue();
        if (analyzeService.isEmpty())
        {
            analyzeService = "not set";
        }
        log.debug("VM Analysis Engine Service is " + analyzeService);

        // Set Host Server
        String hostServer = context.getProperty(ANALYSIS_BEHAVIOR.HOST_SERVER).getValue();

        if (hostServer.isEmpty())
        {
            hostServer = "not set";
        }
        logger.debug("Host Server is " + hostServer);

        // Set Host SFTP port
        String sftpPort = context.getProperty(ANALYSIS_BEHAVIOR.SFTP_PORT).getValue();

        if (sftpPort.isEmpty())
        {
            sftpPort = "not set";
        }
        logger.debug("SFTP Port is " + sftpPort);

        // Set SFTP Connection Timeout
        String timeOut = context.getProperty(ANALYSIS_BEHAVIOR.TIME_OUT).getValue();

        if (timeOut.isEmpty())
        {
            timeOut = "not set";
        }
        logger.debug("SFTP timeout is " + timeOut);

        // Set Analysis Engine file drop path
        String inputPath = context.getProperty(ANALYSIS_BEHAVIOR.INPUT_PATH).getValue();

        if (inputPath.isEmpty())
        {
            inputPath = "/home/nifi/data/in";
        }
        logger.debug("File put path is " + inputPath);

        // Set Analysis Engine file retrieve path
        String outputPath = context.getProperty(ANALYSIS_BEHAVIOR.OUTPUT_PATH).getValue();

        if (outputPath.isEmpty())
        {
            outputPath = "/home/nifi/data/out";
        }
        logger.debug("File get path is" + outputPath);

        // Set Analysis Engine User ID
        String userId = context.getProperty(ANALYSIS_BEHAVIOR.USER_ID).getValue();

        if (userId.isEmpty())
        {
            userId = "not set";
        }
        logger.debug("User ID is " + userId);

        // Set Analysis Engine SFTP Password
        String password = context.getProperty(ANALYSIS_BEHAVIOR.PASSWORD).getValue();

        if (password.isEmpty())
        {
            password = "not set";
        }
        logger.debug("Password is set");

        String useHostKey = context.getProperty(ANALYSIS_BEHAVIOR.USE_HOST_KEY).getValue();

        // Set Host Key Value
        String hostKey = "none";
        if ("true".equals(useHostKey))
        {
            hostKey  = context.getProperty(ANALYSIS_BEHAVIOR.HOST_KEY_FILE).getValue();
        }

        String engineName = context.getProperty(ANALYSIS_BEHAVIOR.ANALYSIS_ENGINE)
                .getValue();
        if (StringUtils.isEmpty(engineName))
        {
            engineName = "unknown";
        }
        log.debug("Analysis engine name : " + engineName);
        session.putAttribute(flowFile, "analysis_engine_name", engineName);

        String sanitizeEngineName = flowFile.getAttribute("sanitize_engine_name");
        if (StringUtils.isEmpty(sanitizeEngineName))
        {
            sanitizeEngineName = "unknown";
        }
        log.debug("Sanitize engine name : " + engineName);

        // Instantiate SFTP class
        SIAFTSftp siaftSftp = new SIAFTSftp(hostServer, sftpPort, timeOut,
                inputPath, outputPath, userId, password, useHostKey, hostKey);

        //set some initial values
        final String extension = SanitizerUtils.getMimeType(session, flowFile);
        final String uniqueFileName = flowFile.getAttribute(CoreAttributes.UUID.key()) + extension;
        final String reportFileName = flowFile.getAttribute(CoreAttributes.UUID.key()) + ".json";
        AnalysisStatus status = AnalysisStatus.NOT_PROCESSED;
        FlowFile updateDb = session.clone(flowFile);

        try
        {
            session.putAttribute(flowFile, "SFTP_put_path", hostServer + inputPath);
            session.getProvenanceReporter().send(flowFile, hostServer + inputPath);

            analyzeFile(session, flowFile, hostServer, analyzeService,
                    uniqueFileName, siaftSftp);

            status = retrieveFile(session, updateDb,
                    Paths.get(outputPath, reportFileName).toString(),
                    siaftSftp);
        }
        catch (AnalysisException ex)
        {
            log.error("Failed to process file.", ex);
            session.putAttribute(flowFile,
                    ANALYSIS_SUCCESS,
                    AnalysisStatus.ANALYSIS_FAILURE.toString());
            session.putAttribute(flowFile, "analysis_engine_name", engineName);
            session.putAttribute(updateDb, "analysis_engine_name", engineName);
        }
        catch (SIAFTFatalProcessException ex)
        {
            log.error("Fatal error during analysis process.", ex);
            session.penalize(flowFile);
            throw ex;
        }
        finally
        {
            deleteFile(Paths.get(outputPath, reportFileName).toString(), siaftSftp);
            closeSftpSession(siaftSftp);
        }
        session.putAttribute(flowFile, "sftp_get_path", hostServer + outputPath);
        session.putAttribute(updateDb, "sftp_get_path", hostServer + outputPath);
        session.putAttribute(flowFile, ANALYSIS_SUCCESS,  status.toString());
        session.putAttribute(updateDb, ANALYSIS_SUCCESS,  status.toString());

         //send the flow file to the Analysis database processor
        session.transfer(updateDb, UPDATE_DATABASE_BEHAVIOR.updateDatabaseRelationship);
        session.transfer(flowFile, SUCCESS_BEHAVIOR.successRelationship);
        session.getProvenanceReporter().fetch(flowFile, hostServer + outputPath);
    }

    /**
     * This will take the file out of the session's flowfile and send it to analysis.
     * It will wait for the analysis to be done and then retrieve the report.
     * @param session the nifi session
     * @param flowFile the nifi flowfile
     * @throws AnalysisException when a problem occcurred sanitizing the file
     * @throws SIAFTFatalProcessException When a serious problem occurred during the sanitization
     *      processing of the file that resulted in the flowfile attributes and/or the flowfile
     *      contents not trusted. A session rollback() is recommended.
     */
    private void analyzeFile(ProcessSession session, FlowFile flowFile, String hostServer,
                              String analyzeService, String fileName, SIAFTSftp siaftSftp)
            throws AnalysisException, SIAFTFatalProcessException
    {
        //Test that host server is accessible
        try
        {
            InetAddress inet = InetAddress.getByName(hostServer);
            inet.isReachable(5000) ;
        }
        catch (IOException ioe)
        {
            throw new AnalysisException("Host server " + hostServer + " is unreachable", ioe);
        }

        try
        {
            siaftSftp.checkEngineStatus(analyzeService);
        }
        catch (IOException ioe)
        {
            throw new AnalysisException("Analysis service " + analyzeService
                    + " is not verified for host " + hostServer, ioe);
        }

        // Send File to Sanitizer
        try (InputStream in = session.read(flowFile))
        {
            siaftSftp.put(fileName, in);
        }
        catch (IOException ex)
        {
            throw new AnalysisException("Failed to send file "
                    + flowFile.getAttribute(CoreAttributes.FILENAME.key())
                    + " to  host server " + hostServer, ex);
        }
    }

    private boolean getAnalysisStatus(String reportFileName, int waitTime, SIAFTSftp siaftSftp)
    {
        int loopCounter = 0;
        int maxLoops = (waitTime * 60) / 5;
        do
        {
            // Check if the file is in the output directory
            try
            {
                if (siaftSftp.exists(reportFileName))
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

    private AnalysisStatus retrieveFile(ProcessSession session, FlowFile flowFile,
                                            String reportFileName, SIAFTSftp siaftSftp) throws SIAFTFatalProcessException,
            AnalysisException
    {
        // Maximum wait time in minutes
        int maxWaitTime = 5;
        AnalysisStatus status = AnalysisStatus.ANALYSIS_SUCCESS;

        // Check Analysis Status
        boolean isComplete = getAnalysisStatus(reportFileName, maxWaitTime, siaftSftp);

        // Only attempt to get the processed file if there is a modified file to retrieve.
        // If a sanitized file was produced get it an replace the context of the original flowFile
        if (isComplete)
        {
            try (OutputStream outStream = session.write(flowFile))
            {
                // Retrieve the sanitized file
                try (InputStream inStream = siaftSftp.getInputStream(reportFileName))
                {
                    StreamUtils.copy(inStream, outStream);
                }
            }
            catch (FileNotFoundException fnfe)
            {
                throw new AnalysisException("Analyzed file not found", fnfe);
            }
            catch (IOException ex) // need a fault barrier here because
            {
                throw new SIAFTFatalProcessException("Failed to write to flowFile Output stream for "
                        + flowFile.getAttribute(CoreAttributes.FILENAME.key()), ex);
            }
        }
        else // the original file content will be passed along in the flowfile
        {
            status = AnalysisStatus.NOT_PROCESSED;
        }
        return status;
    }

    private void deleteFile(String reportFileName, SIAFTSftp siaftSftp)
    {
        try
        {
            siaftSftp.deleteFile(reportFileName);
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

