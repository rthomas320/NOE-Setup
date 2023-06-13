package com.nteligen.hq.dhs.siaft.processors;

import com.nteligen.hq.dhs.siaft.exceptions.SIAFTFatalProcessException;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.exception.ProcessException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class SIAFTBaseRetryProcessor extends SIAFTBaseProcessor
{
  public static final RetryBehavior RETRY_BEHAVIOR = new RetryBehavior();

  @Override
  protected Set<SIAFTBehaviorRetrievable> getBehaviors()
  {
    return new HashSet<>(Arrays.asList(RETRY_BEHAVIOR));
  }

  @Override
  public final void onTrigger(final ProcessContext context,
                        final ProcessSession session)
                        throws ProcessException
  {
    FlowFile flowFile = session.get();
    if (flowFile == null)
    {
      try
      {
        long returnDelay = Long.parseLong(context
                .getProperty(RETRY_BEHAVIOR.returnDelayProperty).getValue());
        returnDelay = Math.abs(returnDelay);
        getLogger().trace("Delaying return by " + returnDelay + " seconds.");
        if (returnDelay > 0)
        {
          Thread.sleep(returnDelay);
        }
      }
      catch (NumberFormatException ex)
      {
        //do nothing. Using default returnDelay of 0
      }
      catch (InterruptedException ex)
      {
        throw new ProcessException("Return Delay Sleep interrupted");
      }
      getLogger().debug("flowFile is null. Returning without processing.");
      return;
    }
    getLogger().trace("Processing flowFile " + flowFile);
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
}
