package com.nteligen.hq.dhs.siaft.processors;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SftpBehavior implements SIAFTBehaviorRetrievable
{
  public final PropertyDescriptor hostIp = new PropertyDescriptor.Builder()
      .name("Host IP Address")
      .description("The IP address of the SFTP server")
      .required(true)
      .defaultValue("127.0.0.1")
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .build();

  public final PropertyDescriptor port = new PropertyDescriptor.Builder()
      .name("SFTP Port")
      .description("The SFTP port for the server")
      .required(true)
      .defaultValue("22")
      .addValidator(StandardValidators.PORT_VALIDATOR)
      .build();

  public final PropertyDescriptor timeout = new PropertyDescriptor.Builder()
      .name("Timeout")
      .description("The SFTP connection timeout period (seconds)")
      .required(true)
      .defaultValue("300")
      .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
      .build();

  public final PropertyDescriptor dropoffPath = new PropertyDescriptor.Builder()
      .name("Dropoff Path")
      .description("The file drop path for the server")
      .required(true)
      .defaultValue("/home/nifi/data/in")
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .build();

  public final PropertyDescriptor pickupPath = new PropertyDescriptor.Builder()
      .name("Pickup Path")
      .description("The file retrieve path for the server")
      .required(true)
      .defaultValue("/home/nifi/data/out")
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .build();

  public final PropertyDescriptor username = new PropertyDescriptor.Builder()
      .name("Username")
      .description("The username to use for the SFTP session")
      .required(true)
      .defaultValue("vagrant")
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .build();

  public final PropertyDescriptor password = new PropertyDescriptor.Builder()
      .name("Password")
      .description("The password to use for the SFTP session")
      .required(true)
      .defaultValue("vagrant")
      .sensitive(true)
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .build();

  @Override
  public List<PropertyDescriptor> getProperties()
  {
    List<PropertyDescriptor> properties = new ArrayList<>();
    properties.add(hostIp);
    properties.add(port);
    properties.add(timeout);
    properties.add(dropoffPath);
    properties.add(pickupPath);
    properties.add(username);
    properties.add(password);

    return properties;
  }

  @Override
  public Set<Relationship> getRelationships()
  {
    return Collections.emptySet();
  }

}
