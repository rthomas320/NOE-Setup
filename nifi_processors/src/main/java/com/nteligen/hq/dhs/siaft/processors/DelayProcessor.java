package com.nteligen.hq.dhs.siaft.processors;

import org.apache.nifi.annotation.behavior.SideEffectFree;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.Validator;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;


@SideEffectFree
@Tags({"Delay Processor"})
@CapabilityDescription("This will delay for a specified number of milliseconds before passing the "
        + "flowfile on to the next processor.")
public class DelayProcessor extends AbstractProcessor
{

  private List<PropertyDescriptor> properties;
  private Set<Relationship> relationships;

  public static final Relationship SUCCESS = new Relationship.Builder()
      .name("successRelationship")
      .description("Success relationship")
      .build();

  public static final PropertyDescriptor WaitForFlowFile = new PropertyDescriptor.Builder()
          .name("Wait_For_FlowFile")
          .description("This will continue to wait for a flowfile to not be null. When it is not "
                  + "null it will immediately transfer it to the success relationship. "
                  + "If the Delay Duration "
                  + "is exceeded before the flowfile is not null in the session then a null "
                  + "flowfile will be passed to the success relationship.")
          .required(true)
            .addValidator(Validator.VALID)
          .defaultValue("False")
          .build();

  public static final PropertyDescriptor DelayDurationInSeconds = new PropertyDescriptor.Builder()
          .name("Delay_Duration")
          .description("The number of seconds that this processor will sleep for before the "
                  + "flowfile is transferred to the success relationship.")
          .required(true)
          .addValidator(Validator.VALID)
          .defaultValue("10")
          .build();

  @Override
  public void init(final ProcessorInitializationContext context)
  {
    List<PropertyDescriptor> properties = new ArrayList<>();
    properties.add(WaitForFlowFile);
    properties.add(DelayDurationInSeconds);
    this.properties = Collections.unmodifiableList(properties);

    Set<Relationship> relationships = new HashSet<>();
    relationships.add(SUCCESS);
    this.relationships = Collections.unmodifiableSet(relationships);
  }

  @Override
  public void onTrigger(final ProcessContext context,
                        final ProcessSession session)
    throws NullPointerException, ProcessException
  {

    final AtomicReference<String> value = new AtomicReference<>();
    boolean waitForFlowFile = Boolean.parseBoolean(context.getProperty(WaitForFlowFile).getValue());
    getLogger().trace("waitForFlowFile : " + waitForFlowFile);
    long delayDuration = Long.parseLong(context.getProperty(DelayDurationInSeconds).getValue());
    delayDuration = Math.abs(delayDuration);
    getLogger().trace("delayDuration : " + delayDuration);

    getLogger().trace("Processing session " + session);
    FlowFile flowfile = null;
    if (waitForFlowFile)
    {
      for (int i = 0; i < delayDuration; i++)
      {
        flowfile = session.get();
        if (flowfile != null)
        {
          break;
        }
        getLogger().trace("Checking session for valid flowfile attempt(" + i + ")");
        try
        {
          Thread.sleep(1000);
        }
        catch (InterruptedException ex)
        {
          getLogger().info("flowfile recheck interrupted.");
          break;
        }
      }
    }
    else // waiting for the duration period regardless
    {
      try
      {
        getLogger().info("waiting for " + delayDuration + " seconds.");
        Thread.sleep(1000 * delayDuration);
        getLogger().debug(delayDuration + " second sleep completed. Getting flowfile "
                + "from session");
        flowfile = session.get();
      }
      catch (InterruptedException ex)
      {
        getLogger().info(delayDuration + " delay was interrupted.");
        throw new ProcessException(delayDuration + " delay was interrupted.", ex);
      }
    }
    getLogger().debug("Transferring flowfile " + flowfile);
    session.transfer(flowfile, SUCCESS);
  }

  @Override
  public Set<Relationship> getRelationships()
  {
    return relationships;
  }

  @Override
  public List<PropertyDescriptor> getSupportedPropertyDescriptors()
  {
    return properties;
  }
}
