package com.nteligen.hq.dhs.siaft.processors;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.Relationship;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReportBehavior implements SIAFTBehaviorRetrievable
{
  public final Relationship reportRelationship = new Relationship.Builder()
    .name("report")
    .description("report")
    .build();

  @Override
  public List<PropertyDescriptor> getProperties()
  {
    return Collections.emptyList();
  }

  @Override
  public Set<Relationship> getRelationships()
  {
    return new HashSet<>(Arrays.asList(reportRelationship));
  }
}
