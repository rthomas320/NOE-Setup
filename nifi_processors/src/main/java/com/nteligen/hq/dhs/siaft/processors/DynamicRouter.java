package com.nteligen.hq.dhs.siaft.processors;

import com.nteligen.hq.dhs.siaft.exceptions.SIAFTFatalProcessException;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.annotation.behavior.DynamicProperty;
import org.apache.nifi.annotation.behavior.DynamicRelationship;
import org.apache.nifi.annotation.behavior.SideEffectFree;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.PropertyValue;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@SideEffectFree
@Tags({"SIAFT", "Dynamic Router"})
@CapabilityDescription("The Dynamic Router acts as a broker for movement of "
                     + "data through all stages of the pipeline.")
@DynamicProperty(name = "relationships", value = "A Comma-Separated List",
                 description = "Specify the list of analyzers, sanitizers, and "
                 + "landing zones associated with the Dynamic Router.")
@DynamicRelationship(name = "Relationships From Dynamic Property",
                     description = "Relationships specified within the "
                     + "comma-separated Relationships property.")
public class DynamicRouter extends AbstractProcessor
{
  private List<PropertyDescriptor> properties;
  private final AtomicReference<Map<String, Relationship>> relationships = new AtomicReference<>();

  public static final String DEFAULT_RELATIONSHIPS = "Analyzer1,Sanitizer1,Analyzer2,Sanitizer2"
      + "Analyzer3,Sanitizer3,Landing_Zone";
  public static final String DEFAULT_PIPELINE = "Sanitizer1,Sanitizer2,"
      + "Sanitizer3,Landing_Zone";
  public static final String DEFAULT_ERROR_PIPELINE = "Landing_Zone";
  public static final String ERROR_KEY = "error";
  public static final String RELATIONSHIP_PROPERTY_NAME = "relationships";

  public static final PropertyDescriptor RELATIONSHIPS = new PropertyDescriptor.Builder()
      .name("relationships")
      .description("List of available routing relationships. Add a value for "
                   + "each possible route that a flowfile can take from the "
                   + "Dynamic Router.")
      .required(true)
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .defaultValue(DEFAULT_RELATIONSHIPS)
      .build();

  public static final PropertyDescriptor PDF_PIPELINE = new PropertyDescriptor.Builder()
      .name("pdf")
      .description("The Pipeline Configuration")
      .required(true)
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .defaultValue(DEFAULT_PIPELINE)
      .build();

  public static final PropertyDescriptor DOC_PIPELINE = new PropertyDescriptor.Builder()
      .name("doc")
      .description("The Pipeline Configuration")
      .required(true)
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .defaultValue(DEFAULT_PIPELINE)
      .build();

  public static final PropertyDescriptor DOCX_PIPELINE = new PropertyDescriptor.Builder()
      .name("docx")
      .description("The Pipeline Configuration")
      .required(true)
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .defaultValue(DEFAULT_PIPELINE)
      .build();

  public static final PropertyDescriptor DOCM_PIPELINE = new PropertyDescriptor.Builder()
      .name("docm")
      .description("The Pipeline Configuration")
      .required(true)
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .defaultValue(DEFAULT_PIPELINE)
      .build();

  public static final PropertyDescriptor XLS_PIPELINE = new PropertyDescriptor.Builder()
      .name("xls")
      .description("The Pipeline Configuration")
      .required(true)
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .defaultValue(DEFAULT_PIPELINE)
      .build();

  public static final PropertyDescriptor XLSX_PIPELINE = new PropertyDescriptor.Builder()
      .name("xlsx")
      .description("The Pipeline Configuration")
      .required(true)
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .defaultValue(DEFAULT_PIPELINE)
      .build();

  public static final PropertyDescriptor XLSM_PIPELINE = new PropertyDescriptor.Builder()
      .name("xlsm")
      .description("The Pipeline Configuration")
      .required(true)
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .defaultValue(DEFAULT_PIPELINE)
      .build();

  public static final PropertyDescriptor PPT_PIPELINE = new PropertyDescriptor.Builder()
      .name("ppt")
      .description("The Pipeline Configuration")
      .required(true)
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .defaultValue(DEFAULT_PIPELINE)
      .build();

  public static final PropertyDescriptor PPTX_PIPELINE = new PropertyDescriptor.Builder()
      .name("pptx")
      .description("The Pipeline Configuration")
      .required(true)
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .defaultValue(DEFAULT_PIPELINE)
      .build();

  public static final PropertyDescriptor PPTM_PIPELINE = new PropertyDescriptor.Builder()
      .name("pptm")
      .description("The Pipeline Configuration")
      .required(true)
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .defaultValue(DEFAULT_PIPELINE)
      .build();

  public static final PropertyDescriptor PNG_PIPELINE = new PropertyDescriptor.Builder()
      .name("png")
      .description("The Pipeline Configuration")
      .required(true)
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .defaultValue(DEFAULT_PIPELINE)
      .build();

  public static final PropertyDescriptor JPG_PIPELINE = new PropertyDescriptor.Builder()
      .name("jpg")
      .description("The Pipeline Configuration")
      .required(true)
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .defaultValue(DEFAULT_PIPELINE)
      .build();

  public static final PropertyDescriptor GIF_PIPELINE = new PropertyDescriptor.Builder()
      .name("gif")
      .description("The Pipeline Configuration")
      .required(true)
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .defaultValue(DEFAULT_PIPELINE)
      .build();

  public static final PropertyDescriptor BMP_PIPELINE = new PropertyDescriptor.Builder()
          .name("bmp")
          .description("The Pipeline Configuration")
          .required(true)
          .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
          .defaultValue(DEFAULT_PIPELINE)
          .build();

  public static final PropertyDescriptor TIFF_PIPELINE = new PropertyDescriptor.Builder()
          .name("tif")
          .description("The Pipeline Configuration")
          .required(true)
          .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
          .defaultValue(DEFAULT_PIPELINE)
          .build();

  public static final PropertyDescriptor TXT_PIPELINE = new PropertyDescriptor.Builder()
          .name("txt")
          .description("The Pipeline Configuration")
          .required(true)
          .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
          .defaultValue(DEFAULT_PIPELINE)
          .build();

  public static final PropertyDescriptor ERROR_PIPELINE = new PropertyDescriptor.Builder()
      .name("error")
      .description("The Pipeline Configuration")
      .required(true)
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .defaultValue(DEFAULT_ERROR_PIPELINE)
      .build();

  @Override
  public void init(final ProcessorInitializationContext context)
  {
    final List<PropertyDescriptor> properties = new ArrayList<>();
    properties.add(RELATIONSHIPS);
    properties.add(PDF_PIPELINE);
    properties.add(DOC_PIPELINE);
    properties.add(DOCX_PIPELINE);
    properties.add(DOCM_PIPELINE);
    properties.add(XLS_PIPELINE);
    properties.add(XLSX_PIPELINE);
    properties.add(XLSM_PIPELINE);
    properties.add(PPT_PIPELINE);
    properties.add(PPTX_PIPELINE);
    properties.add(PPTM_PIPELINE);
    properties.add(PNG_PIPELINE);
    properties.add(JPG_PIPELINE);
    properties.add(GIF_PIPELINE);
    properties.add(BMP_PIPELINE);
    properties.add(TIFF_PIPELINE);
    properties.add(TXT_PIPELINE);
    properties.add(ERROR_PIPELINE);
    this.properties = Collections.unmodifiableList(properties);

    // trigger the callback to populate default relationships
    this.onPropertyModified(RELATIONSHIPS, null, RELATIONSHIPS.getDefaultValue());
  }

  /**
   * When a user changes the property values for a processor, this method is
   * called for each modified property. In response, checks to see if the
   * "relationships" property was the one modified. If changed, the list of
   * available relationships is altered to be that of the comma delimited
   * property value.
   *
   * @param descriptor The PropertyDescriptor that was modified.
   * @param oldValue The original property value.
   * @param newValue The updated value. Expects a comma delimited list of
   *                 strings representing the available relationships for the
   *                 processor (e.g. "foo,bar,baz").
   */
  @Override
  public void onPropertyModified(final PropertyDescriptor descriptor,
                                 final String oldValue, final String newValue)
  {
    if (descriptor.equals(RELATIONSHIPS))
    {

      getLogger().debug("Relationships list modified, old value=" + oldValue
                        + " new value=" + newValue);

      String[] elements = newValue.split(",");
      Arrays.parallelSetAll(elements, (index) -> elements[index].trim());
      List<String> propNames = Arrays.asList(elements);

      final Map<String, Relationship> updatedRelationships = new HashMap<>();
      for (String propName : propNames)
      {
        updatedRelationships.put(propName, new Relationship.Builder().name(propName).build());
      }

      updatedRelationships.put("Failure", new Relationship.Builder().name("Failure").build());

      this.relationships.set(updatedRelationships);
    }
  }

  /**
   * Establishes the pipeline for the given flowfile based on the provided entry
   * point. The pipeline is written out to the SIAFTUtils.PIPELINE flowfile
   * attribute.
   *
   * @param entry    The entry point for the policy, which is a three letter file
   *                 extension indicating file type. This indicates key identifies
   *                 the pipeline to be used during processing.
   * @param flowfile The flowfile currently being processed.
   * @return none
   */
  private FlowFile populatePipeline(String entry, final ProcessSession session,
                                    FlowFile flowfile, Pipeline pipeline)
  {
    flowfile = session.putAttribute(flowfile, SIAFTUtils.PIPELINE, pipeline.toString());

    // delete entry so that subsequent sessions do not repeat this step
    flowfile = session.putAttribute(flowfile, SIAFTUtils.ENTRY, "");

    return flowfile;
  }

  /**
   * Determine which Relationship to use as the destination for the flow file.
   *
   * @param session  A hook for the processor session.
   * @param context  The current processor context
   * @param flowfile The flow file that is currently being processed.
   * @return Relationship
   */
  private Relationship getNextStage(final ProcessSession session,
                                    final ProcessContext context,
                                    FlowFile flowfile)
                                    throws SIAFTFatalProcessException
  {
    String entry = flowfile.getAttribute(SIAFTUtils.ENTRY);

    // Having an entry point defined signifies the flowfile came from
    // the WFS. Read the Pipeline Config and store it within an attribute.
    if (!StringUtils.isEmpty(entry))
    {
      getLogger().debug("Looking up pipeline for entry [" + entry + "]");
      Map<String,Pipeline> pipelineMap = readPipelineConfig(context);

      Pipeline pipeline = pipelineMap.get(entry);

      // if the entry does not exist send file along error pipeline
      if (null == pipeline)
      {
        pipeline = pipelineMap.get(ERROR_PIPELINE.getName());
      }

      flowfile = session.putAttribute(flowfile, SIAFTUtils.UNPROCESSED_FILE,
          Boolean.toString(true));
      flowfile = populatePipeline(entry, session, flowfile, pipeline);
    }

    getLogger().debug("Determining next stage of the pipeline");
    Pipeline pipeline = new Pipeline(flowfile.getAttribute(SIAFTUtils.PIPELINE));

    getLogger().debug("Pipeline currently set to [" + pipeline + "]");

    // pop stage off the pipeline stack to reflect progress
    String nextStage = pipeline.pop();
    getLogger().debug("Next stage will be [" + nextStage + "]");

    getLogger().debug("Updating pipeline attribute [" + pipeline + "]");
    flowfile = session.putAttribute(flowfile, SIAFTUtils.PIPELINE, pipeline.toString());

    Relationship dest = (relationships.get()).get(nextStage);
    if (null == dest)
    {
      getLogger().error("The relationship [" + nextStage
        + "] does not exist in the defined Relationships ["
        + relationships.toString() + "]");
      throw new SIAFTFatalProcessException("Pipeline for [" + entry
        + "] contains an error. The relationship [" + nextStage
        + "] does not exist in Relationship list [" + relationships.toString() + "]");
    }
    return dest;
  }

  /**
   * Reads the current processor properties defining the various pipelines.
   *
   * @param context  The current processor context
   * @return A map of pipeline names to Pipeline objects representing the
   *         current configuration.
   */
  private Map<String,Pipeline> readPipelineConfig(final ProcessContext context)
  {
    Map<String,Pipeline> pipelineMap = new HashMap<String,Pipeline>();

    Map<PropertyDescriptor,String> properties = context.getProperties();

    for (Map.Entry<PropertyDescriptor,String> pair : properties.entrySet())
    {
      PropertyValue propertyValue = context.getProperty(pair.getKey().getName());

      Pipeline pipeline = new Pipeline(propertyValue.getValue());
      pipelineMap.put(pair.getKey().getName(), pipeline);
    }

    return pipelineMap;
  }

  @Override
  public void onTrigger(final ProcessContext context,
                        final ProcessSession session)
    throws ProcessException
  {
    getLogger().trace("Processing session " + session);
    FlowFile flowfile = session.get();
    getLogger().trace("Processing flowfile " + flowfile);
    try
    {
      if (flowfile == null)
      {
        getLogger().info("Flowfile is null");
        context.yield(); //This ensures that the yield duration is honored
        throw new ProcessException("DynamicRouter detected a null FlowFile");
      }
      else
      {
        getLogger().debug("Processing file '" + flowfile.getAttribute("filename") + "' with uuid "
                          + flowfile.getAttribute(SIAFTUtils.JOB_ID));
      }
      Relationship dest = getNextStage(session, context, flowfile);

      session.transfer(flowfile, dest);
    }
    catch (SIAFTFatalProcessException ex)
    {
      getLogger().error("Dynamic Router failed to process file.", ex);
      session.transfer(flowfile, (relationships.get()).get("Failure"));
    }
    catch (Exception ex) // fault barrier
    {
      context.yield(); //This ensures that the yield duration is honored
      if (session == null)
      {
        getLogger().error("Unable to penalize flowflow because session null.", ex);
      }
      else
      {
        if (flowfile == null)
        {
          getLogger().error("Unable to penalize flowflow because flowfile null.", ex);
        }
        else
        {
          getLogger().error("Penalizing flowfile because an error occurred.",  ex);
          session.penalize(flowfile);
        }
      }
      throw new ProcessException("DynamicRouter failed to process file.", ex);
    }
  }

  @Override
  public Set<Relationship> getRelationships()
  {
    return new HashSet<Relationship>(relationships.get().values());
  }

  @Override
  public final List<PropertyDescriptor> getSupportedPropertyDescriptors()
  {
    return properties;
  }

  @Override
  protected PropertyDescriptor getSupportedDynamicPropertyDescriptor(
      final String propertyDescriptorName)
  {
    return new PropertyDescriptor.Builder()
        .required(false)
        .name(propertyDescriptorName)
        .dynamic(true)
        .build();
  }
}
