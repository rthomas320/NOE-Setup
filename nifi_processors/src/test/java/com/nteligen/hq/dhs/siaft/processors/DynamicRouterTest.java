package com.nteligen.hq.dhs.siaft.processors;

import static org.junit.Assert.assertTrue;

import org.apache.nifi.components.PropertyValue;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DynamicRouterTest
{
  /**
   * Submits a flowfile to the DynamicRouter for the first time. Tests that the
   * Dynamic Router is able to lookup the proper pipeline and forward the flowfile
   * onto the next stage.
   * @result Attribute SIAFTUtils.PIPELINE is non-null.
   */
  @org.junit.Test()
  public void testEntryPointSet()
  {
    InputStream content = new ByteArrayInputStream("foo".getBytes());

    TestRunner runner = TestRunners.newTestRunner(new DynamicRouter());

    Map<String,String> attributes = new HashMap<String,String>();

    // set the policy entry point
    attributes.put(SIAFTUtils.ENTRY, "pdf");

    runner.enqueue(content, attributes);
    runner.run(1);
    runner.assertQueueEmpty();

    List<MockFlowFile> results = runner.getFlowFilesForRelationship(
        "Reversing_Labs");

    assertTrue(results.size() > 0);

    MockFlowFile result = results.get(0);
    result.assertAttributeEquals(SIAFTUtils.PIPELINE,
                                 "MetaDefender,Reversing_Labs,Landing_Zone");

    // assert entry is consumed
    result.assertAttributeEquals(SIAFTUtils.ENTRY, "");
  }

  /**
   * Simulates a flowfile being sent to the DynamicRouter from one of the analysis
   * processors. Dynamic Router looks up the next stage of the pipeline and
   * forwards the flowfile to that relationship.
   * @result First stage in attribute SIAFTUtils.PIPELINE is removed.
   * @result Relationship specified in pipeline contains flowfile.
   */
  @org.junit.Test()
  public void testReturnFromAnalysis()
  {
    InputStream content = new ByteArrayInputStream("foo".getBytes());

    TestRunner runner = TestRunners.newTestRunner(new DynamicRouter());

    Map<String,String> attributes = new HashMap<String,String>();

    // set the policy entry point
    attributes.put(SIAFTUtils.ENTRY, "pdf");


    runner.enqueue(content, attributes);
    runner.run(1);
    runner.assertQueueEmpty();

    List<MockFlowFile> results = runner.getFlowFilesForRelationship(
        "Reversing_Labs");

    assertTrue(results.size() > 0);

    MockFlowFile result = results.get(0);
    result.assertAttributeEquals(SIAFTUtils.PIPELINE,
                                 "MetaDefender,Reversing_Labs,Landing_Zone");
  }

  /**
   * Submits a flowfile to the DynamicRouter for the first time. The
   * Dynamic Router is provided a bad entry that does not exist. Tests
   * that the flow file is placed onto the error pipeline.
   * @result Attribute SIAFTUtils.PIPELINE is set to the "error" pipeline.
   */
  @org.junit.Test()
  public void testEntryDoesNotExist()
  {
    InputStream content = new ByteArrayInputStream("foo".getBytes());

    TestRunner runner = TestRunners.newTestRunner(new DynamicRouter());

    Map<String,String> attributes = new HashMap<String,String>();

    // set the policy entry point
    attributes.put(SIAFTUtils.ENTRY, "garbage");

    runner.enqueue(content, attributes);
    runner.run(1);
    runner.assertQueueEmpty();

    List<MockFlowFile> results = runner.getFlowFilesForRelationship(
        "Landing_Zone");

    assertTrue(results.size() > 0);

    PropertyValue property = runner.getProcessContext()
        .getProperty(DynamicRouter.ERROR_KEY);
    Pipeline expected = new Pipeline(property.getValue());
    // The front component will be removed by this point, as it has been through
    // the Dynamic Router once.
    expected.pop();

    MockFlowFile result = results.get(0);

    // assert pipeline is now empty, as it was passed to the error pipeline
    result.assertAttributeEquals(SIAFTUtils.PIPELINE, expected.toString());

    // assert entry is consumed
    result.assertAttributeEquals(SIAFTUtils.ENTRY, "");
  }

  /**
   * Simulates a user adding a relationships via the "relationships" property.
   * The value is updated to "foo,bar".
   * @result The relationships set is updated to reflect changes.
   */
  @org.junit.Test()
  public void testAddRelationship()
  {
    TestRunner runner = TestRunners.newTestRunner(new DynamicRouter());

    // simulate a user modifying the property value
    runner.setProperty(DynamicRouter.RELATIONSHIP_PROPERTY_NAME, "foo,bar");

    ProcessContext context = runner.getProcessContext();
    Set<Relationship> relationships = context.getAvailableRelationships();

    // expect relationships with these names are created
    Relationship foo = new Relationship.Builder().name("foo").build();
    Relationship bar = new Relationship.Builder().name("bar").build();

    assertTrue(relationships.contains(foo));
    assertTrue(relationships.contains(bar));
  }

  /**
   * Simulates a user removing a relationships via the "relationships" property.
   * The value is changed from "foo,bar" to "foo".
   * @result The relationships set is updated to reflect removed "bar"
   *         relationship.
   */
  @org.junit.Test()
  public void testRemoveRelationship()
  {
    TestRunner runner = TestRunners.newTestRunner(new DynamicRouter());
    ProcessContext context = runner.getProcessContext();
    final Relationship foo = new Relationship.Builder().name("foo").build();
    final Relationship bar = new Relationship.Builder().name("bar").build();

    Set<Relationship> relationships;

    // set the initial state
    runner.setProperty(DynamicRouter.RELATIONSHIP_PROPERTY_NAME, "foo,bar");
    relationships = context.getAvailableRelationships();

    assertTrue(relationships.contains(foo));
    assertTrue(relationships.contains(bar));

    // simulate a user removing the "bar" relationship
    runner.setProperty(DynamicRouter.RELATIONSHIP_PROPERTY_NAME, "foo");
    relationships = context.getAvailableRelationships();

    assertTrue(relationships.contains(foo));
    assertTrue(!relationships.contains(bar));
  }
}
