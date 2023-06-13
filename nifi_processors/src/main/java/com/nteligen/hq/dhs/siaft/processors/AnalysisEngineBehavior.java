package com.nteligen.hq.dhs.siaft.processors;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.*;

public class AnalysisEngineBehavior implements SIAFTBehaviorRetrievable
{
  /**
   * Constructor.
   *
   * @param 'Analysis Engine' The default value for the analysis engine name property.
   * @param 'Analyze Service' The default value for the analyze service property.
   * @param 'Host Server' The default value for the analysis engine host server property.
   * @param 'SFTP Port' The default value for the the server sftp port property.
   * @param 'Time Out' The default value for the analysis engine timeout property.
   * @param 'Input Path' The default value for the analysis engine input path property.
   * @param 'Output Path' The default value for the analysis engine output path property.
   * @param 'User ID' The default value for the analysis engine User ID property.
   * @param 'Password' The default value for the analysis engine User's Password property.
   * @param 'Use Host Key' Default value is false.
   * @param 'Host Key File' Default value is empty.
   */
  private List<PropertyDescriptor> properties;
  private Set<Relationship> relationships;

  //TODO make the default property values more generic DS-580
  public static final PropertyDescriptor ANALYSIS_ENGINE = new PropertyDescriptor.Builder()
          .name("Analysis Engine")
          .description("The name of the Analysis Engine to be used by this processor")
          .required(true)
          .defaultValue("analyzer1")
          .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
          .build();

  public static final PropertyDescriptor ANALYZE_SERVICE = new PropertyDescriptor.Builder()
          .name("Analysis Service")
          .description("The name of the Analysis Service running on the VM")
          .required(true)
          .defaultValue("analyzer1")
          .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
          .build();

  public static final PropertyDescriptor HOST_SERVER = new PropertyDescriptor.Builder()
          .name("Host Server")
          .description("The name the remote server for this Analysis Engine")
          .required(true)
          .defaultValue("10.105.5.188")
          .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
          .build();

  public static final PropertyDescriptor SFTP_PORT = new PropertyDescriptor.Builder()
          .name("SFTP Port")
          .description("The SFTP port for the remote server for this Analysis Engine")
          .required(true)
          .defaultValue("22")
          .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
          .build();

  public static final PropertyDescriptor TIME_OUT = new PropertyDescriptor.Builder()
          .name("Time Out")
          .description("The SFTP connection time out")
          .required(true)
          .defaultValue("30 sec")
          .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
          .build();

  public static final PropertyDescriptor INPUT_PATH = new PropertyDescriptor.Builder()
          .name("Input Path")
          .description("The file drop path for this Analysis Engine")
          .required(true)
          .defaultValue("/home/nifi/data/in")
          .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
          .build();

  public static final PropertyDescriptor OUTPUT_PATH = new PropertyDescriptor.Builder()
          .name("Output Path")
          .description("The file retrieve path for this Analysis Engine")
          .required(true)
          .defaultValue("/home/nifi/data/out")
          .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
          .build();

  // TODO take out default value
  public static final PropertyDescriptor USER_ID = new PropertyDescriptor.Builder()
          .name("User ID")
          .description("The user ID key to connect to analysis server")
          .required(true)
          .defaultValue("root")
          .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
          .build();

  // TODO take out default value
  public static final PropertyDescriptor PASSWORD = new PropertyDescriptor.Builder()
          .name("Password")
          .description("The password to connect to analysis server")
          .required(true)
          .defaultValue("vagrant")
          .sensitive(true)
          .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
          .build();

  public static final PropertyDescriptor USE_HOST_KEY = new PropertyDescriptor.Builder()
          .name("Use HOST Key")
          .description("Use an SFTP private key to connect to analysis server")
          .required(false)
          .defaultValue("false")
          .allowableValues("true", "false")
          .build();

  public static final PropertyDescriptor HOST_KEY_FILE = new PropertyDescriptor.Builder()
          .name("Host Key File")
          .description("The SFTP private key to connect to analysis server")
          .required(false)
          .addValidator(StandardValidators.FILE_EXISTS_VALIDATOR)
          .build();

  public static final Relationship SUCCESS = new Relationship.Builder()
          .name("SUCCESS")
          .description("Success forwards the file after analysis")
          .build();

  public static final Relationship UPDATE_DB = new Relationship.Builder()
          .name("UPDATE_DB")
          .description("Update_DB forks to a database processor after attempting analysis")
          .build();

  @Override
  public List<PropertyDescriptor> getProperties()
  {
    return Arrays.asList(
            ANALYSIS_ENGINE,
            ANALYZE_SERVICE,
            HOST_SERVER,
            SFTP_PORT,
            TIME_OUT,
            INPUT_PATH,
            OUTPUT_PATH,
            USER_ID,
            PASSWORD,
            USE_HOST_KEY,
            HOST_KEY_FILE);
  }

  @Override
  public Set<Relationship> getRelationships()
  {
    return Collections.EMPTY_SET;
  }

}

