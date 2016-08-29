/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.model.meta.IdType;

/**
 * JAXB enabled implementation of {@link ComponentInfo}.
 */
@XmlRootElement(name = "componentInfo")
public class ComponentInfoJpa implements ComponentInfo {

  /** The id. */
  private Long id;

  /** The timestamp. */
  private Date timestamp = null;

  /** The terminology. */
  private String terminology;

  /** The terminology id. */
  private String terminologyId;

  /** The version. */
  private String version;

  /** The name. */
  private String name;

  /** The type. */
  private IdType type;

  /**
   * Instantiates a new user favorite jpa.
   */
  public ComponentInfoJpa() {

  }

  /**
   * Constructor from a ~~ (double tilda) delimited string
   * Type~~Terminology~~Version~~TerminologyId~~Name~~LastModified Note: Double
   * tilda required as single tildas appear in component names.
   *
   * @param delimitedString the delimited string
   */
  public ComponentInfoJpa(String delimitedString) {
    String[] fields = delimitedString.split("~~");

    this.setType(IdType.valueOf(fields[0]));
    this.setTerminology(fields[1]);
    this.setVersion(fields[2]);
    this.setTerminologyId(fields[3]);
    this.setName(fields[4]);

    Date date = new Date();
    date.setTime(Long.parseLong(fields[5]));
    this.setTimestamp(date);
  }

  /**
   * Instantiates a new component info JPA object.
   *
   * @param componentInfo the component info
   * @throws Exception the exception
   */
  public ComponentInfoJpa(ComponentInfo componentInfo) throws Exception {
    super();

    this.id = componentInfo.getId();
    this.terminology = componentInfo.getTerminology();
    this.terminologyId = componentInfo.getTerminologyId();
    this.version = componentInfo.getVersion();
    this.name = componentInfo.getName();
    this.type = componentInfo.getType();
  }

  /**
   * Instantiates a {@link ComponentInfoJpa} from the specified parameters.
   *
   * @param id the id
   * @param terminology the terminology
   * @param terminologyId the terminology id
   * @param version the version
   * @param name the name
   * @param type the type
   * @throws Exception the exception
   */
  public ComponentInfoJpa(Long id, String terminology, String terminologyId,
      String version, String name, IdType type) throws Exception {
    super();

    this.id = id;
    this.terminology = terminology;
    this.terminologyId = terminologyId;
    this.version = version;
    this.name = name;
    this.type = type;
  }

  /* see superclass */
  @Override
  public Long getId() {
    return id;
  }

  /* see superclass */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /* see superclass */
  @Override
  public Date getTimestamp() {
    return timestamp;
  }

  /* see superclass */
  @Override
  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  /* see superclass */
  @Override
  public String getTerminology() {
    return terminology;
  }

  /* see superclass */
  @Override
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  /* see superclass */
  @Override
  public String getTerminologyId() {
    return terminologyId;
  }

  /* see superclass */
  @Override
  public void setTerminologyId(String terminologyId) {
    this.terminologyId = terminologyId;
  }

  /* see superclass */
  @Override
  public String getVersion() {
    return version;
  }

  /* see superclass */
  @Override
  public void setVersion(String version) {
    this.version = version;
  }

  /* see superclass */
  @Override
  public String getName() {
    return this.name;
  }

  /* see superclass */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /* see superclass */
  @Override
  public void setType(IdType type) {
    this.type = type;

  }

  /* see superclass */
  @Override
  public IdType getType() {
    return this.type;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result = prime * result
        + ((terminologyId == null) ? 0 : terminologyId.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ComponentInfoJpa other = (ComponentInfoJpa) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (terminology == null) {
      if (other.terminology != null)
        return false;
    } else if (!terminology.equals(other.terminology))
      return false;
    if (terminologyId == null) {
      if (other.terminologyId != null)
        return false;
    } else if (!terminologyId.equals(other.terminologyId))
      return false;
    if (type != other.type)
      return false;
    if (version == null) {
      if (other.version != null)
        return false;
    } else if (!version.equals(other.version))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "ComponentInfoJpa [id=" + id + ", timestamp=" + timestamp
        + ", terminology=" + terminology + ", terminologyId=" + terminologyId
        + ", version=" + version + ", name=" + name + ", type=" + type + "]";
  }

}
