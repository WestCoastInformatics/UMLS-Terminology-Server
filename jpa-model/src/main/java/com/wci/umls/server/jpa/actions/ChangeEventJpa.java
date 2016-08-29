/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.actions;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.jpa.ComponentInfoJpa;
import com.wci.umls.server.model.actions.ChangeEvent;

/**
 * JAXB enabled implementation of a {@link ChangeEvent}.
 */
@XmlRootElement(name = "change")
public class ChangeEventJpa implements ChangeEvent {

  /** The id. */
  private Long id;

  /** The timestamp. */
  private Date timestamp;

  /** The last modified. */
  private Date lastModified;

  /** The last modified by. */
  private String lastModifiedBy;

  /** The name. */
  private String name;

  /** The session id. */
  private String sessionId;

  /** The type. */
  private String type;

  /** The object id. */
  private Long objectId;

  /** The container. */
  private ComponentInfo container;

  /**
   * Instantiates an empty {@link ChangeEventJpa}.
   */
  public ChangeEventJpa() {
    // n/a
  }

  /**
   * Instantiates a {@link ChangeEventJpa} from the specified parameters.
   *
   * @param event the event
   */
  public ChangeEventJpa(ChangeEvent event) {
    id = event.getId();
    timestamp = event.getTimestamp();
    lastModified = event.getLastModified();
    lastModifiedBy = event.getLastModifiedBy();
    name = event.getName();
    sessionId = event.getSessionId();
    type = event.getType();
    objectId = event.getObjectId();
    container = event.getContainer();
  }

  /**
   * Instantiates a {@link ChangeEventJpa} from the specified parameters.
   *
   * @param name the name
   * @param sessionId the session id
   * @param type the type
   * @param objectId the object id
   * @param container the container
   * @throws Exception the exception
   */
  public ChangeEventJpa(String name, String sessionId, String type,
      Long objectId, ComponentInfo container) throws Exception {
    this.objectId = objectId;
    timestamp = new Date();
    this.name = name;
    this.sessionId = sessionId;
    this.type = type;
    this.container = new ComponentInfoJpa(container);
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
  public Date getLastModified() {
    return lastModified;
  }

  /* see superclass */
  @Override
  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  /* see superclass */
  @Override
  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  /* see superclass */
  @Override
  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  /* see superclass */
  @Override
  public String getName() {
    return name;
  }

  /* see superclass */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /* see superclass */
  @Override
  public String getSessionId() {
    return sessionId;
  }

  /* see superclass */
  @Override
  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  /* see superclass */
  @Override
  public String getType() {
    return type;
  }

  /* see superclass */
  @Override
  public void setType(String type) {
    this.type = type;
  }

  /* see superclass */
  @Override
  public Long getObjectId() {
    return objectId;
  }

  /* see superclass */
  @Override
  public void setObjectId(Long objectId) {
    this.objectId = objectId;
  }

  /* see superclass */
  @Override
  @XmlElement(type = ComponentInfoJpa.class)
  public ComponentInfo getContainer() {
    return container;
  }

  /* see superclass */
  @Override
  public void setContainer(ComponentInfo container) {
    this.container = container;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
    result = prime * result + ((objectId == null) ? 0 : objectId.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((container == null) ? 0 : container.hashCode());
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
    ChangeEventJpa other = (ChangeEventJpa) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (sessionId == null) {
      if (other.sessionId != null)
        return false;
    } else if (!sessionId.equals(other.sessionId))
      return false;
    if (objectId == null) {
      if (other.objectId != null)
        return false;
    } else if (!objectId.equals(other.objectId))
      return false;
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
      return false;
    if (container == null) {
      if (other.container != null)
        return false;
    } else if (!container.equals(other.container))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "ChangeEventJpa [id=" + id + ", timestamp=" + timestamp
        + ", lastModified=" + lastModified + ", lastModifiedBy="
        + lastModifiedBy + ", name=" + name + ", sessionId=" + sessionId
        + ", type=" + type + ", objectId=" + objectId + ", container="
        + container + "]";
  }

}
