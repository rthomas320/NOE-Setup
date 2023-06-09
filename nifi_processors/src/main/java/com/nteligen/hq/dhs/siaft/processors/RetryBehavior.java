package com.nteligen.hq.dhs.siaft.processors;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RetryBehavior implements SIAFTBehaviorRetrievable
{
  public final PropertyDescriptor returnDelayProperty = new PropertyDescriptor.Builder()
          .name("NullFlowFileReturnDelay")
          .description("The number of milliseconds that this processor will sleep for before "
                  + "returning.")
          .required(true)
          .addValidator(StandardValidators.LONG_VALIDATOR)
          .defaultValue("0")
          .build();

  public final Relationship retryRelationship = new Relationship.Builder()
          .name("Retry")
          .description("A FlowFile is routed to this relationship if the database cannot be "
                  + "updated but attempting the operation again may succeed")
          .build();

  @Override
  public List<PropertyDescriptor> getProperties()
  {
    return Arrays.asList(returnDelayProperty);
  }

  @Override
  public Set<Relationship> getRelationships()
  {
    return new HashSet<>(Arrays.asList(retryRelationship));
  }
}
