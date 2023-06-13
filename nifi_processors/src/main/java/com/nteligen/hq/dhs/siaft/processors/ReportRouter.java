package com.nteligen.hq.dhs.siaft.processors;

import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.SideEffectFree;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.stream.io.StreamUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SideEffectFree
@Tags({"SIAFT", "ReportRouter"})
@CapabilityDescription("The ReportRouter retrieves analysis reports"
                       + " and routes them to MAEC converter processors.")
@ReadsAttributes(
    {
      @ReadsAttribute(attribute = "originalUUID",
          description = "The UUID associated with the original flowfile"),
      @ReadsAttribute(attribute = SIAFTUtils.ANALYSIS_ENGINE_ID ,
           description = "The ID associated with Dynamic Analysis")
      })
@WritesAttributes(
    {
      @WritesAttribute(attribute = "siaft.RouteReport.Action",
                       description = "The action performed on the flowfile")
    })

public class ReportRouter extends AbstractProcessor
{
  public static final String FILE_ACTION = "siaft.RouteReport.Action";

  public static final Relationship RouteMAECReport = new Relationship.Builder()
      .name("routeMAECReport")
      .description("This relationship will route the VxStream MAEC analysis report to the"
                   + " MAEC report aggregator process.")
      .build();

  public static final Relationship RouteCylanceReport = new Relationship.Builder()
      .name("routeCylanceReport")
      .description("This relationship will route the Cylance analysis report to the"
                   + " Cylance MAEC converter process.")
      .build();

  public static final Relationship RouteGlasswallReport = new Relationship.Builder()
      .name("routeGlasswallReport")
      .description("This relationship will route the Glasswall analysis report to the"
                   + " Glasswall MAEC converter process.")
      .build();

  public static final Relationship RouteReversingLabsReport = new Relationship.Builder()
      .name("routeReversingLabsReport")
      .description("This relationship will route the ReversingLabs analysis report to the"
                   + " ReversingLabs MAEC converter process.")
      .build();

  public static final PropertyDescriptor ReportFileParentPath = new PropertyDescriptor.Builder()
      .name("reportFileParentPath")
      .description("The parent path wto here the report files are saved.")
      .required(true)
      .addValidator(StandardValidators.FILE_EXISTS_VALIDATOR)
      .defaultValue("")
      .build();

  public static final PropertyDescriptor CylanceEngineID = new PropertyDescriptor.Builder()
      .name("cylanceEngineID")
      .description("The engine ID assinged to the cylance analysis engine. Use '0' if cylance"
                    + " engine is not used")
      .required(false)
      .addValidator(StandardValidators.NUMBER_VALIDATOR )
      .defaultValue("0")
      .build();

  public static final PropertyDescriptor GlasswallEngineID = new PropertyDescriptor.Builder()
      .name("glasswallEngineID")
      .description("The engine ID assinged to the glasswall analysis engine. Use '0' if"
                    + " glasswall engine is not used")
      .required(false)
      .addValidator(StandardValidators.NUMBER_VALIDATOR )
      .defaultValue("0")
      .build();

  public static final PropertyDescriptor ReversingLabsEngineID = new PropertyDescriptor.Builder()
      .name("reversingLabsEngineID")
      .description("The engine ID assinged to the reversinglabs analysis engine. Use '0' if"
                    + " reversinglabs engine is not used")
      .required(false)
      .addValidator(StandardValidators.NUMBER_VALIDATOR )
      .defaultValue("0")
      .build();

  @Override
  public List<PropertyDescriptor> getSupportedPropertyDescriptors()
  {
    List<PropertyDescriptor> properties = new ArrayList<>();
    properties.add(ReportFileParentPath);
    properties.add(CylanceEngineID);
    properties.add(GlasswallEngineID);
    properties.add(ReversingLabsEngineID);

    return properties;
  }

  @Override
  public Set<Relationship> getRelationships()
  {
    return new HashSet<>(Arrays.asList(RouteMAECReport, RouteCylanceReport, RouteGlasswallReport,
                                       RouteReversingLabsReport));
  }

  @Override
  public void onTrigger(ProcessContext context, ProcessSession session)
  {

    String rptFilePath =
        context.getProperty(ReportRouter.ReportFileParentPath).toString();
    String cylanceID =
        context.getProperty(ReportRouter.CylanceEngineID).toString();
    String glasswallID =
        context.getProperty(ReportRouter.GlasswallEngineID).toString();
    String reversingLabsID =
        context.getProperty(ReportRouter.ReversingLabsEngineID).toString();

    try
    {
      getLogger().trace("Processing session " + session);
      FlowFile flowFile = session.get();
      getLogger().trace("Processing flowfile " + flowFile);

      String analysisEngineID = flowFile.getAttribute(SIAFTUtils.ANALYSIS_ENGINE_ID);
      String flowFileUUID = flowFile.getAttribute("originalUUID");
      String maecReportFile = Paths.get(rptFilePath, analysisEngineID).toString();
      maecReportFile = Paths.get(maecReportFile, flowFileUUID).toString();
      String cylanceReportFile = Paths.get(rptFilePath, cylanceID).toString();
      cylanceReportFile = Paths.get(cylanceReportFile, flowFileUUID).toString();
      String glasswallReportFile = Paths.get(rptFilePath, glasswallID).toString();
      glasswallReportFile = Paths.get(glasswallReportFile, flowFileUUID).toString();
      String reversingLabsReportFile = Paths.get(rptFilePath, reversingLabsID).toString();
      reversingLabsReportFile = Paths.get(reversingLabsReportFile, flowFileUUID).toString();

      getLogger().debug("Create new child flowfiles to load reports as payload.");
      if (!cylanceID.equals("0"))
      {
        FlowFile cylanceFlowFile = session.create(flowFile);
        File cylanceFile = new File(cylanceReportFile);
        if (cylanceFile.exists() && (cylanceFlowFile != null))
        {
          getLogger().debug("New child flowfile exists.");
          try (OutputStream outStream = session.write(cylanceFlowFile))
          {
            try (InputStream inStream = new FileInputStream(cylanceFile))
            {
              getLogger().debug("Loading Cylance report file to new child flowFile");
              StreamUtils.copy(inStream, outStream);
            }
          }
          catch (IOException ex)
          {
            throw new IOException("Error retreiving Cylance report file", ex);
          }
          getLogger().debug("Send child flowFile with Cylance report file paylod to "
                            + "RouteCylanceReport relationship");
        }
        else
        {
          getLogger().debug("Send child flowFile with empty Cylance report file paylod to "
                            + "RouteCylanceReport relationship");
        }
        session.putAttribute(cylanceFlowFile, SIAFTUtils.ANALYSIS_ENGINE_ID, cylanceID);
        session.putAttribute(cylanceFlowFile, ReportRouter.FILE_ACTION,
                             "Cylance_Report_To_MAEC_Converter");
        session.transfer(cylanceFlowFile, ReportRouter.RouteCylanceReport);
      }
      if (!glasswallID.equals("0"))
      {
        FlowFile glasswallFlowFile = session.create(flowFile);
        File glasswallFile = new File(glasswallReportFile);
        if (glasswallFile.exists() && (glasswallFlowFile != null))
        {
          getLogger().debug("New child flowfile exists.");
          try (OutputStream outStream = session.write(glasswallFlowFile))
          {
            try (InputStream inStream = new FileInputStream(glasswallFile))
            {
              getLogger().debug("Loading Glasswall report file to new child flowFile");
              StreamUtils.copy(inStream, outStream);
            }
          }
          catch (IOException ex)
          {
            throw new IOException("Error retreiving Glasswall report file", ex);
          }
          getLogger().debug("Send child flowFile with Glasswall report file paylod to "
                            + "RouteGlasswallReport relationship");
        }
        else
        {
          getLogger().debug("Send child flowFile with empty Glasswall report file paylod to "
                            + "RouteGlasswallReport relationship");
        }
        session.putAttribute(glasswallFlowFile, SIAFTUtils.ANALYSIS_ENGINE_ID, glasswallID);
        session.putAttribute(glasswallFlowFile, ReportRouter.FILE_ACTION,
                             "Glasswall_Report_To_MAEC_Converter");
        session.transfer(glasswallFlowFile, ReportRouter.RouteGlasswallReport);
      }
      if (!reversingLabsID.equals("0"))
      {
        FlowFile rlabsFlowFile = session.create(flowFile);
        File rlabsFile = new File(reversingLabsReportFile);
        if (rlabsFile.exists() && (rlabsFlowFile != null))
        {
          getLogger().debug("New child flowfile exists.");
          try (OutputStream outStream = session.write(rlabsFlowFile))
          {
            try (InputStream inStream = new FileInputStream(rlabsFile))
            {
              getLogger().debug("Loading ReversingLabs report file to new child flowFile");
              StreamUtils.copy(inStream, outStream);
            }
          }
          catch (IOException ex)
          {
            throw new IOException("Error retreiving ReversingLabs report file", ex);
          }
          getLogger().debug("Send child flowFile with ReversingLabs report file paylod to "
                            + "RouteReversingLabsReport relationship");
        }
        else
        {
          getLogger().debug("Send child flowFile with empty ReversingLabs report file paylod to "
                            + "RouteGlasswallReport relationship");
        }
        session.putAttribute(rlabsFlowFile, SIAFTUtils.ANALYSIS_ENGINE_ID, reversingLabsID);
        session.putAttribute(rlabsFlowFile, ReportRouter.FILE_ACTION,
                             "ReversingLabs_Report_To_MAEC_Converter");
        session.transfer(rlabsFlowFile, ReportRouter.RouteReversingLabsReport);
      }
      getLogger().debug("Send flowFile with VxStream MAEC report to RouteMAECReport relationship");
      session.putAttribute(flowFile, ReportRouter.FILE_ACTION, "MAEC_Report_To_Aggregator");
      session.transfer(flowFile, ReportRouter.RouteMAECReport);

    }
    catch (InvalidPathException | IOException | ProcessException ex)
    {
      getLogger().debug("An error occurred " + ex);
    }

  }
}
