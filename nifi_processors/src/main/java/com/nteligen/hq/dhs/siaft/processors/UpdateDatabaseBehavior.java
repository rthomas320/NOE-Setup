package com.nteligen.hq.dhs.siaft.processors;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.Relationship;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UpdateDatabaseBehavior implements SIAFTBehaviorRetrievable
{
  public final Relationship updateDatabaseRelationship = new Relationship.Builder()
          .name("UPDATE_DB")
          .description("Update_DB forks to a database processor after attempting sanitization")
          .build();

  @Override
  public List<PropertyDescriptor> getProperties()
  {
    return Collections.EMPTY_LIST;
  }

  @Override
  public Set<Relationship> getRelationships()
  {
    return new HashSet<>(Arrays.asList(updateDatabaseRelationship));
  }
}
