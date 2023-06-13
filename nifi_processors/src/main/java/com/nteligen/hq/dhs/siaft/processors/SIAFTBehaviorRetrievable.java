package com.nteligen.hq.dhs.siaft.processors;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.Relationship;

import java.util.List;
import java.util.Set;

public interface SIAFTBehaviorRetrievable
{
  List<PropertyDescriptor> getProperties();

  Set<Relationship> getRelationships();
}
