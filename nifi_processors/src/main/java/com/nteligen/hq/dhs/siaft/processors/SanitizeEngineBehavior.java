package com.nteligen.hq.dhs.siaft.processors;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SanitizeEngineBehavior implements SIAFTBehaviorRetrievable
{
  private List<PropertyDescriptor> properties;
  private Set<Relationship> relationships;

  //TODO make the default property values more generic DS-580
  public static final PropertyDescriptor SANITIZE_ENGINE = new PropertyDescriptor.Builder()
      .name("Sanitize Engine")
      .description("The name of the Sanitization Engine to be used by this processor")
      .required(true)
      .defaultValue("Glasswall")
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .build();

  public static final PropertyDescriptor SANITIZE_SERVICE = new PropertyDescriptor.Builder()
      .name("Sanitize Service")
      .description("The name of the Sanitization Service running on the VM")
      .required(true)
      .defaultValue("glasswalld")
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .build();

  public static final PropertyDescriptor HOST_SERVER = new PropertyDescriptor.Builder()
      .name("Host Server")
      .description("The name the remote server for this Sanitization Engine")
      .required(true)
      .defaultValue("10.105.5.188")
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .build();

  public static final PropertyDescriptor SFTP_PORT = new PropertyDescriptor.Builder()
      .name("SFTP Port")
      .description("The SFTP port for the remote server for this Sanitization Engine")
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
      .description("The file drop path for this Sanitization Engine")
      .required(true)
      .defaultValue("/home/nifi/data/in")
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .build();

  public static final PropertyDescriptor OUTPUT_PATH = new PropertyDescriptor.Builder()
      .name("Output Path")
      .description("The file retrieve path for this Sanitization Engine")
      .required(true)
      .defaultValue("/home/nifi/data/out")
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .build();

  // TODO take out default value
  public static final PropertyDescriptor USER_ID = new PropertyDescriptor.Builder()
      .name("User ID")
      .description("The user ID key to connect to Sanitization server")
      .required(true)
      .defaultValue("root")
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .build();

  // TODO take out default value
  public static final PropertyDescriptor PASSWORD = new PropertyDescriptor.Builder()
      .name("Password")
      .description("The password to connect to Sanitization server")
      .required(true)
      .defaultValue("vagrant")
      .sensitive(true)
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .build();

  public static final PropertyDescriptor USE_HOST_KEY = new PropertyDescriptor.Builder()
      .name("Use HOST Key")
      .description("Use an SFTP private key to connect to Sanitization server")
      .required(false)
      .defaultValue("false")
      .allowableValues("true", "false")
      .build();

  public static final PropertyDescriptor HOST_KEY_FILE = new PropertyDescriptor.Builder()
      .name("Host Key File")
      .description("The SFTP private key to connect to Sanitization server")
      .required(false)
      .addValidator(StandardValidators.FILE_EXISTS_VALIDATOR)
      .build();

  public static final Relationship SUCCESS = new Relationship.Builder()
      .name("SUCCESS")
      .description("Success forwards the file after sanitization")
      .build();

  public static final Relationship UPDATE_DB = new Relationship.Builder()
      .name("UPDATE_DB")
      .description("Update_DB forks to a database processor after attempting sanitization")
      .build();

  @Override
  public List<PropertyDescriptor> getProperties()
  {
    return Arrays.asList(
      SANITIZE_ENGINE,
      SANITIZE_SERVICE,
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
