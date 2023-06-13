package com.nteligen.hq.dhs.siaft.processors;

import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;

import org.junit.Before;
import org.junit.Test;

//import java.io.ByteArrayInputStream;
//import java.io.InputStream;

public class SanitizeEngineProcessorTest
{

  private TestRunner testRunner;

  @Before
  public void init()
  {
    testRunner = TestRunners.newTestRunner(SanitizeEngineProcessor.class);
  }

  @Test
  public void testProcessor()
  {

  }
}
