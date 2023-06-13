package com.nteligen.hq.dhs.siaft.processors;

import com.nteligen.hq.dhs.siaft.exceptions.SIAFTFatalProcessException;
import org.apache.nifi.annotation.behavior.SideEffectFree;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.stream.io.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@SideEffectFree
@Tags({"SIAFT Base Processor"})
@CapabilityDescription("This is the base SIAFT Processor")
public abstract class SIAFTBaseProcessor extends AbstractProcessor
{
  // used within the ENTRY attribute to indicate the file cannot be processed
  // and should be routed to the error pipeline
  public static final String ERROR_KEY = "error";

  private List<PropertyDescriptor> properties;
  private Set<Relationship> relationships;

  /**
   * This will retrieve the behaviors this object should exhibit.
   * @return a set of behaviors
   */
  protected abstract Set<SIAFTBehaviorRetrievable> getBehaviors();

  protected List<PropertyDescriptor> getProperties()
  {
    return new ArrayList<>();
  }

  /**
   * This function is called at the end of the init() function.
   */
  protected abstract void initInternal();

  @Override
  public final void init(final ProcessorInitializationContext context)
  {
    Set<SIAFTBehaviorRetrievable> behaviors = getBehaviors();

    List<PropertyDescriptor> properties = new ArrayList<>();
    properties.addAll(getProperties());

    Set<Relationship> relationships = new HashSet<>();
    for (SIAFTBehaviorRetrievable behavior : behaviors)
    {
      properties.addAll(behavior.getProperties());
      relationships.addAll(behavior.getRelationships());
    }

    getLogger().debug("Behaviors : " + behaviors);
    this.properties = Collections.unmodifiableList(properties);
    this.relationships = Collections.unmodifiableSet(relationships);
    getLogger().debug("Properties : " + this.properties);
    getLogger().debug("Relationships : " + this.relationships);
    initInternal();
  }

  /**
   * This function is called from the onTrigger function.
   * @param context
   *          The process context
   * @param session
   *          The process session
   * @param flowFile
   *          The flowfile from the session
   * @throws ProcessException
   *          Thrown if a nifi processing error occurs
   * @throws SIAFTFatalProcessException
   *          Thrown if any other processing exception occurs
   */
  protected abstract void onTriggerInternal(final ProcessContext context,
                                            final ProcessSession session,
                                            FlowFile flowFile)
          throws ProcessException, SIAFTFatalProcessException;

  @Override
  public void onTrigger(final ProcessContext context,
                        final ProcessSession session)
    throws ProcessException
  {
    if (session == null)
    {
      return;
    }

    FlowFile flowFile = session.get();
    if (flowFile == null)
    {
      return;
    }

    getLogger().trace("Processing session " + session);
    try
    {
      onTriggerInternal(context, session, flowFile);
    }
    catch (SIAFTFatalProcessException ex)
    {
      getLogger().trace("Fatal Exception occurred with " + session
              + " rolling back the session.", ex);
      session.rollback(true);
      return;
    }
  }

  @Override
  public final Set<Relationship> getRelationships()
  {
    return relationships;
  }

  @Override
  public final List<PropertyDescriptor> getSupportedPropertyDescriptors()
  {
    return properties;
  }

  /**
   * Helper method to read the FlowFile content stream into a byte array.
   *
   * @param session The current process session.
   * @param flowFile The FlowFile to read the content from.
   *
   * @return byte array representation of the FlowFile content.
   */
  protected byte[] readContent(final ProcessSession session, final FlowFile flowFile)
  {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream((int) flowFile.getSize() + 1);
    session.read(flowFile, new InputStreamCallback()
    {
      @Override
      public void process(final InputStream in) throws IOException
      {
        StreamUtils.copy(in, baos);
      }
    });

    return baos.toByteArray();
  }
}
