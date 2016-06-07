/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.LongBridge;

import com.wci.umls.server.helpers.LogEntry;
import com.wci.umls.server.model.meta.LogActivity;

/**
 * The JPA and JAXB enabled implementation of the log entry object.
 */
@Entity
@Table(name = "log_entries")
@Indexed
@XmlRootElement(name = "logEntry")
public class LogEntryJpa implements LogEntry {

  /** The id. */
  @TableGenerator(name = "EntityIdGen", table = "table_generator", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;

  /** The last modified. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastModified = new Date();

  /** The last modified. */
  @Column(nullable = false)
  private String lastModifiedBy;

  /** The message. */
  @Column(nullable = false, length = 4000)
  private String message;

  /** The object id. */
  @Column(nullable = true)
  private Long objectId;

  /** The project id. */
  @Column(nullable = true)
  private Long projectId;

  /** The terminology. */
  @Column(nullable = true)
  private String terminology;

  /** The version. */
  @Column(nullable = true)
  private String version;

  /** The from id type. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = true)
  private LogActivity activity;

  /** the timestamp. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date timestamp = null;

  /**
   * The default constructor.
   */

  public LogEntryJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link LogEntryJpa} from the specified parameters.
   *
   * @param logEntry the log entry
   */
  public LogEntryJpa(LogEntry logEntry) {
    id = logEntry.getId();
    lastModified = logEntry.getLastModified();
    lastModifiedBy = logEntry.getLastModifiedBy();
    message = logEntry.getMessage();
    objectId = logEntry.getObjectId();
    projectId = logEntry.getProjectId();
    terminology = logEntry.getTerminology();
    version = logEntry.getVersion();
    activity = logEntry.getActivity();
    timestamp = logEntry.getTimestamp();
  }

  /* see superclass */
  @Override
  public Long getId() {
    return this.id;
  }

  /* see superclass */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public String getMessage() {
    return message;
  }

  /* see superclass */
  @Override
  public void setMessage(String message) {
    if (message.length() > 4000) {
      this.message = message.substring(1, 4000);
    } else {
      this.message = message;
    }
  }

  /* see superclass */
  @Override
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getProjectId() {
    return projectId;
  }

  /* see superclass */
  @Override
  public void setProjectId(Long projectId) {
    this.projectId = projectId;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public LogActivity getActivity() {
    return activity;
  }

  /* see superclass */
  @Override
  public void setActivity(LogActivity activity) {
    this.activity = activity;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((activity == null) ? 0 : activity.hashCode());
    result = prime * result + ((message == null) ? 0 : message.hashCode());
    result = prime * result + ((objectId == null) ? 0 : objectId.hashCode());
    result = prime * result + ((projectId == null) ? 0 : projectId.hashCode());
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
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
    LogEntryJpa other = (LogEntryJpa) obj;
    if (activity != other.activity)
      return false;
    if (message == null) {
      if (other.message != null)
        return false;
    } else if (!message.equals(other.message))
      return false;
    if (objectId == null) {
      if (other.objectId != null)
        return false;
    } else if (!objectId.equals(other.objectId))
      return false;
    if (projectId == null) {
      if (other.projectId != null)
        return false;
    } else if (!projectId.equals(other.projectId))
      return false;
    if (terminology == null) {
      if (other.terminology != null)
        return false;
    } else if (!terminology.equals(other.terminology))
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
    return "LogEntryJpa [id=" + id + ", lastModified=" + lastModified
        + ", lastModifiedBy=" + lastModifiedBy + ", message=" + message
        + ", objectId=" + objectId + ", projectId=" + projectId
        + ", terminology=" + terminology + ", version=" + version
        + ", activity=" + activity + ", timestamp=" + timestamp + "]";
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

}
