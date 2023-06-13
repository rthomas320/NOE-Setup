package com.nteligen.hq.dhs.siaft.processors;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.fail;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import com.nteligen.hq.dhs.siaft.exceptions.MimeTypeDetectionException;
import org.apache.commons.io.FileUtils;
import org.apache.nifi.flowfile.attributes.CoreAttributes;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Test;
import org.powermock.api.easymock.PowerMock;
import org.powermock.reflect.Whitebox;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WorkflowSelectorTest
{

  private static final String TEST_RESOURCES = "src/test/resources/wfs";

  private List<MockFlowFile> testEntrySetTestFile(File testFile, String expectedEntry)
  {
    InputStream content = null;
    try
    {
      content = new ByteArrayInputStream(FileUtils.readFileToByteArray(testFile));
    }
    catch (IOException ex)
    {
      fail("unable to read input file");
    }

    Map<String,String> attributes = new HashMap<String,String>();
    attributes.put(CoreAttributes.FILENAME.key(), testFile.getName());

    TestRunner runner = TestRunners.newTestRunner(new WorkflowSelector());

    runner.enqueue(content, attributes);
    runner.run(1);
    runner.assertQueueEmpty();

    List<MockFlowFile> results = runner.getFlowFilesForRelationship(
        WorkflowSelector.SUCCESS_BEHAVIOR.successRelationship);

    MockFlowFile result = results.get(0);
    result.assertAttributeEquals(SIAFTUtils.ENTRY,
                                 expectedEntry);
    return results;
  }

  /**
   * Submit a flow file with bad uuid.
   * @result Triggers an exception.
   */
  @Test
  public void testBadUUIDJobId()
  {
    System.out.println("The following test should produce an exception...");

    InputStream content = new ByteArrayInputStream("foo".getBytes());

    // generate a test runner to mock the processor in a flow
    TestRunner runner = TestRunners.newTestRunner(new WorkflowSelector());

    Map<String,String> attributes = new HashMap<String,String>();

    // set the uuid to garbage
    attributes.put("uuid", "bar");

    runner.enqueue(content, attributes);
    runner.run(1);
    runner.assertQueueEmpty();

    List<MockFlowFile> results = runner.getFlowFilesForRelationship(WorkflowSelector
            .SUCCESS_BEHAVIOR.successRelationship);

    MockFlowFile result = results.get(0);
    result.assertAttributeEquals(SIAFTUtils.ENTRY, DynamicRouter.ERROR_PIPELINE.getName());
  }

  /**
   * Submit a flow file with bad uuid.
   * @result Triggers an exception.
   */
  @Test
  public void testEmptyJobId()
  {
    System.out.println("The following test should produce an exception...");

    InputStream content = new ByteArrayInputStream("foo".getBytes());

    // generate a test runner to mock the processor in a flow
    TestRunner runner = TestRunners.newTestRunner(new WorkflowSelector());

    Map<String,String> attributes = new HashMap<String,String>();

    // set the uuid to garbage
    attributes.put("uuid", "");

    runner.enqueue(content, attributes);
    runner.run(1);
    runner.assertQueueEmpty();

    List<MockFlowFile> results = runner.getFlowFilesForRelationship(WorkflowSelector
            .SUCCESS_BEHAVIOR.successRelationship);

    MockFlowFile result = results.get(0);
    result.assertAttributeEquals(SIAFTUtils.ENTRY, DynamicRouter.ERROR_PIPELINE.getName());
  }

  /**
   * Tests that the WorkflowSelector properly sends to the error pipeline when the mimetype fails to
   * get the type of file.
   * @result SIAFTUtils.ENTRY is set to the error pipeline
   */
  @Test()
  public void testFileMimeTypeExceptionFromValidExtension() throws MimeTypeDetectionException
  {
    File testFile = new File(TEST_RESOURCES + "/pdf/foo.pdf");
    InputStream content = null;
    try
    {
      content = new ByteArrayInputStream(FileUtils.readFileToByteArray(testFile));
    }
    catch (IOException ex)
    {
      fail("unable to read input file");
    }

    Map<String,String> attributes = new HashMap<String,String>();
    attributes.put(CoreAttributes.FILENAME.key(), testFile.getName());
    WorkflowSelector workflowSelector = new WorkflowSelector();

    final TestRunner runner = TestRunners.newTestRunner(workflowSelector);
    //set the mimedetector that is intialized during the init() method to a mock
    MimeDetector mockMimeDetector = PowerMock.createMock(MimeDetector.class);
    Whitebox.setInternalState(workflowSelector, "mimeDetector", mockMimeDetector);
    expect(mockMimeDetector.validExtension(anyObject(), anyObject()))
            .andThrow(new MimeTypeDetectionException(""));
    replayAll();
    runner.enqueue(content, attributes);
    runner.run(1);
    runner.assertQueueEmpty();

    List<MockFlowFile> results = runner.getFlowFilesForRelationship(WorkflowSelector
            .SUCCESS_BEHAVIOR.successRelationship);

    MockFlowFile result = results.get(0);
    result.assertAttributeEquals(SIAFTUtils.ENTRY, DynamicRouter.ERROR_PIPELINE.getName());
    verifyAll();
  }

  /**
   * Tests that the WorkflowSelector properly sends to the error pipeline when the mimetype fails to
   * get the type of file.
   * @result SIAFTUtils.ENTRY is set to the error pipeline
   */
  @Test()
  public void testFileMimeTypeExceptionFromGetMimeType() throws MimeTypeDetectionException
  {
    File testFile = new File(TEST_RESOURCES + "/pdf/foo.pdf");
    InputStream content = null;
    try
    {
      content = new ByteArrayInputStream(FileUtils.readFileToByteArray(testFile));
    }
    catch (IOException ex)
    {
      fail("unable to read input file");
    }

    Map<String,String> attributes = new HashMap<String,String>();
    attributes.put(CoreAttributes.FILENAME.key(), testFile.getName());
    WorkflowSelector workflowSelector = new WorkflowSelector();

    final TestRunner runner = TestRunners.newTestRunner(workflowSelector);
    //set the mimedetector that is intialized during the init() method to a mock
    MimeDetector mockMimeDetector = PowerMock.createMock(MimeDetector.class);
    Whitebox.setInternalState(workflowSelector, "mimeDetector", mockMimeDetector);
    expect(mockMimeDetector.validExtension(anyObject(), anyObject())).andReturn(true);
    expect(mockMimeDetector.getMimeType(anyObject(), anyObject()))
            .andThrow(new MimeTypeDetectionException("")).once();
    replayAll();
    runner.enqueue(content, attributes);
    runner.run(1);
    runner.assertQueueEmpty();

    List<MockFlowFile> results = runner.getFlowFilesForRelationship(WorkflowSelector
            .SUCCESS_BEHAVIOR.successRelationship);

    MockFlowFile result = results.get(0);
    result.assertAttributeEquals(SIAFTUtils.ENTRY, DynamicRouter.ERROR_PIPELINE.getName());
    verifyAll();
  }

  /**
   * Tests that the WorkflowSelector properly labels a pdf file without an
   * extension to the error pipeline.
   * @result SIAFTUtils.ENTRY is set to the error pipeline
   */
  @Test()
  public void testFileNoExtension()
  {
    File testFile = new File(TEST_RESOURCES + "/pdf/foo");
    testEntrySetTestFile(testFile, DynamicRouter.ERROR_PIPELINE.getName());
  }

  /**
   * Tests that the WorkflowSelector properly labels a docx file masquerading as
   * a pptx to the error pipeline.
   * @result SIAFTUtils.ENTRY is set to the error pipeline
   */
  @Test()
  public void testFileMasquerading()
  {
    // TODO(DS-529): Tika is unable to catch this case
    // testEntrySetTestFile(new File(TEST_RESOURCES + "/pptx/docx_masquerading.pptx"),
    //                      DynamicRouter.ERROR_PIPELINE.getName());

    testEntrySetTestFile(new File(TEST_RESOURCES + "/xls/pdf_masquerading.xls"),
                         DynamicRouter.ERROR_PIPELINE.getName());
  }

  /**
   * Tests that the WorkflowSelector properly labels a pdf for the pdf pipeline.
   * @result SIAFTUtils.ENTRY is set to the pdf pipeline
   */
  @Test()
  public void testPdfFileType()
  {
    File testFile = new File(TEST_RESOURCES + "/pdf/foo.pdf");
    List<MockFlowFile> results = testEntrySetTestFile(testFile,
                                                      DynamicRouter.PDF_PIPELINE.getName());
    MockFlowFile result = results.get(0);
    result.assertAttributeEquals(SIAFTUtils.MIME_TYPE, "pdf");

  }

  /**
   * Tests that the WorkflowSelector properly labels a doc for the doc pipeline.
   * @result SIAFTUtils.ENTRY is set to the doc pipeline
   */
  @Test()
  public void testDocFileType()
  {
    File testFile = new File(TEST_RESOURCES + "/doc/foo.doc");
    List<MockFlowFile> results = testEntrySetTestFile(testFile,
                                                      DynamicRouter.DOC_PIPELINE.getName());
    MockFlowFile result = results.get(0);
    result.assertAttributeEquals(SIAFTUtils.MIME_TYPE, "doc");

  }

  /**
   * Tests that the WorkflowSelector properly labels a docx for the docx pipeline.
   * @result SIAFTUtils.ENTRY is set to the docx pipeline
   */
  @Test()
  public void testDocxFileType()
  {
    File testFile = new File(TEST_RESOURCES + "/docx/foo.docx");
    List<MockFlowFile> results = testEntrySetTestFile(testFile,
                                                      DynamicRouter.DOCX_PIPELINE.getName());
    MockFlowFile result = results.get(0);
    result.assertAttributeEquals(SIAFTUtils.MIME_TYPE, "docx");

  }

  /**
   * Tests that the WorkflowSelector properly labels a docm for the docm pipeline.
   * @result SIAFTUtils.ENTRY is set to the docm pipeline
   */
  @Test()
  public void testDocmFileType()
  {
    File testFile = new File(TEST_RESOURCES + "/docm/foo.docm");
    List<MockFlowFile> results = testEntrySetTestFile(testFile,
                                                      DynamicRouter.DOCM_PIPELINE.getName());
    MockFlowFile result = results.get(0);
    result.assertAttributeEquals(SIAFTUtils.MIME_TYPE, "docm");

  }

  /**
   * Tests that the WorkflowSelector properly labels an xls for the xls pipeline.
   * @result SIAFTUtils.ENTRY is set to the xls pipeline
   */
  @Test()
  public void testXlsFileType()
  {
    File testFile = new File(TEST_RESOURCES + "/xls/foo.xls");
    List<MockFlowFile> results = testEntrySetTestFile(testFile,
                                                      DynamicRouter.XLS_PIPELINE.getName());
    MockFlowFile result = results.get(0);
    result.assertAttributeEquals(SIAFTUtils.MIME_TYPE, "xls");

  }

  /**
   * Tests that the WorkflowSelector properly labels an xlsx for the xlsx pipeline.
   * @result SIAFTUtils.ENTRY is set to the xlsx pipeline
   */
  @Test()
  public void testXlsxFileType()
  {
    File testFile = new File(TEST_RESOURCES + "/xlsx/foo.xlsx");
    List<MockFlowFile> results = testEntrySetTestFile(testFile,
                                                      DynamicRouter.XLSX_PIPELINE.getName());
    MockFlowFile result = results.get(0);
    result.assertAttributeEquals(SIAFTUtils.MIME_TYPE, "xlsx");

  }

  /**
   * Tests that the WorkflowSelector properly labels an xlsm for the xlsm pipeline.
   * @result SIAFTUtils.ENTRY is set to the xlsm pipeline
   */
  @Test()
  public void testXlsmFileType()
  {
    File testFile = new File(TEST_RESOURCES + "/xlsm/foo.xlsm");
    List<MockFlowFile> results = testEntrySetTestFile(testFile,
                                                      DynamicRouter.XLSM_PIPELINE.getName());
    MockFlowFile result = results.get(0);
    result.assertAttributeEquals(SIAFTUtils.MIME_TYPE, "xlsm");

  }

  /**
   * Tests that the WorkflowSelector properly labels a ppt for the ppt pipeline.
   * @result SIAFTUtils.ENTRY is set to the ppt pipeline
   */
  @Test()
  public void testPptFileType()
  {
    File testFile = new File(TEST_RESOURCES + "/ppt/foo.ppt");
    List<MockFlowFile> results = testEntrySetTestFile(testFile,
                                                      DynamicRouter.PPT_PIPELINE.getName());
    MockFlowFile result = results.get(0);
    result.assertAttributeEquals(SIAFTUtils.MIME_TYPE, "ppt");

  }

  /**
   * Tests that the WorkflowSelector properly labels an pptx for the pptx pipeline.
   * @result SIAFTUtils.ENTRY is set to the pptx pipeline
   */
  @Test()
  public void testPptxFileType()
  {
    File testFile = new File(TEST_RESOURCES + "/pptx/foo.pptx");
    List<MockFlowFile> results = testEntrySetTestFile(testFile,
                                                      DynamicRouter.PPTX_PIPELINE.getName());
    MockFlowFile result = results.get(0);
    result.assertAttributeEquals(SIAFTUtils.MIME_TYPE, "pptx");

  }

  /**
   * Tests that the WorkflowSelector properly labels a pptm for the pptm pipeline.
   * @result SIAFTUtils.ENTRY is set to the pptm pipeline
   */
  @Test()
  public void testPptmFileType()
  {
    File testFile = new File(TEST_RESOURCES + "/pptm/foo.pptm");
    List<MockFlowFile> results = testEntrySetTestFile(testFile,
                                                      DynamicRouter.PPTM_PIPELINE.getName());
    MockFlowFile result = results.get(0);
    result.assertAttributeEquals(SIAFTUtils.MIME_TYPE, "pptm");

  }

}
