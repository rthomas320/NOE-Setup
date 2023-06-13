package com.nteligen.hq.dhs.siaft.processors;

import static org.junit.Assert.assertEquals;


public class PipelineTest
{
  @org.junit.Test()
  public void testNoModification()
  {
    Pipeline pipeline = new Pipeline("A,B,C");
    String expected = "A,B,C";
    assertEquals(expected, pipeline.toString());
  }

  @org.junit.Test()
  public void testLeadingSpace()
  {
    Pipeline pipeline = new Pipeline(" A,B,C");
    String expected = "A,B,C";
    assertEquals(expected, pipeline.toString());
  }

  @org.junit.Test()
  public void testLeadingTab()
  {
    Pipeline pipeline = new Pipeline("\tA,B,C");
    String expected = "A,B,C";
    assertEquals(expected, pipeline.toString());
  }

  @org.junit.Test()
  public void testInnerSpace()
  {
    Pipeline pipeline = new Pipeline("A, B,C");
    String expected = "A,B,C";
    assertEquals(expected, pipeline.toString());
  }

  @org.junit.Test()
  public void testInnerTab()
  {
    Pipeline pipeline = new Pipeline("A,\tB,C");
    String expected = "A,B,C";
    assertEquals(expected, pipeline.toString());
  }

  @org.junit.Test()
  public void testTrailingSpace()
  {
    Pipeline pipeline = new Pipeline("A,B,C ");
    String expected = "A,B,C";
    assertEquals(expected, pipeline.toString());
  }

  @org.junit.Test()
  public void testTrailingTab()
  {
    Pipeline pipeline = new Pipeline("A,B,C\t");
    String expected = "A,B,C";
    assertEquals(expected, pipeline.toString());
  }
}
