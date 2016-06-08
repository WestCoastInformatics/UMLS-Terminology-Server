/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.EnumBridge;

import com.wci.umls.server.SourceData;
import com.wci.umls.server.SourceDataFile;

/**
 * JPA and JAXB enabled implementation of {@link SourceData}.
 */
@Entity
@Table(name = "source_data", uniqueConstraints = @UniqueConstraint(columnNames = {
    "name", "terminology", "version"
}))
@Audited
@Indexed
@XmlRootElement(name = "data")
public class SourceDataJpa implements SourceData {

  /** The id. Set initial value to 5 to bypass entries in import.sql */
  @TableGenerator(name = "EntityIdGen", table = "table_generator", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;

  /** The file name. */
  @Column(nullable = false, unique = true)
  private String name;

  /** The source data description. */
  @Column(nullable = true, unique = false)
  private String terminology;

  /** The source data description. */
  @Column(nullable = true, unique = false)
  private String version;

  /** The source data description. */
  @Column(nullable = true, unique = false)
  private String releaseVersion;

  /** The source data description. */
  @Column(nullable = true, unique = false, length = 4000)
  private String description;

  /** The timestamp. */
  @Column(nullable = false, unique = false)
  private Date timestamp = new Date();

  /** The last modified. */
  @Column(nullable = false, unique = false)
  private Date lastModified;

  /** The last modified by. */
  @Column(nullable = false, unique = false)
  private String lastModifiedBy;

  /** The data files. */
  @OneToMany(targetEntity = SourceDataFileJpa.class, orphanRemoval = true)
  private List<SourceDataFile> sourceDataFiles = new ArrayList<>();

  /** The status. */
  @Column(nullable = true, unique = false)
  private SourceData.Status status = SourceData.Status.NEW;

  /** The status text. */
  @Column(nullable = true, unique = false, length = 4000)
  private String statusText;

  /**
   * The handler key from the config file.
   */
  @Column(nullable = true, unique = false, length = 4000)
  private String handler;

  /** The handler status. */
  @Column(nullable = true)
  @Enumerated(EnumType.STRING)
  private SourceData.Status handlerStatus;

  /**
   * Instantiates a new source data file jpa.
   */
  public SourceDataJpa() {
    // N/A
  }

  /**
   * Instantiates a new source data jpa.
   *
   * @param sourceData the source data
   * @param deepCopy the deep copy
   */
  public SourceDataJpa(SourceData sourceData, boolean deepCopy) {
    super();
    this.id = sourceData.getId();
    this.name = sourceData.getName();
    this.lastModified = sourceData.getLastModified();
    this.lastModifiedBy = sourceData.getLastModifiedBy();
    this.description = sourceData.getDescription();
    this.handler = sourceData.getHandler();
    this.status = sourceData.getStatus();
    this.statusText = sourceData.getStatusText();
    this.timestamp = sourceData.getTimestamp();
    for (final SourceDataFile s : sourceData.getSourceDataFiles()) {
      this.sourceDataFiles.add(new SourceDataFileJpa(s, deepCopy));
    }
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

  /**
   * Gets the last modified.
   *
   * @return the last modified
   */
  @Override
  public Date getLastModified() {
    return this.lastModified;
  }

  /**
   * Sets the last modified.
   *
   * @param lastModified the new last modified
   */
  @Override
  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  /**
   * Gets the last modified by.
   *
   * @return the last modified by
   */
  @Override
  public String getLastModifiedBy() {
    return this.lastModifiedBy;
  }

  /**
   * Sets the last modified by.
   *
   * @param lastModifiedBy the new last modified by
   */
  @Override
  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public Long getId() {
    return this.id;
  }

  /**
   * Sets the id.
   *
   * @param id the new id
   */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  @Override
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "nameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  public String getName() {
    return this.name;
  }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public String getHandler() {
    return this.handler;
  }

  /* see superclass */
  @Override
  public void setHandler(String handler) {
    this.handler = handler;
  }

  /* see superclass */
  @Field(bridge = @FieldBridge(impl = EnumBridge.class), index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public SourceData.Status getStatus() {
    return handlerStatus;
  }

  /* see superclass */
  @Override
  public void setStatus(SourceData.Status handlerStatus) {
    this.handlerStatus = handlerStatus;
  }

  /* see superclass */
  @Override
  public String getStatusText() {
    return statusText;
  }

  /* see superclass */
  @Override
  public void setStatusText(String statusText) {
    this.statusText = statusText;
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return description;
  }

  /* see superclass */
  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  /* see superclass */
  @Override
  @XmlElement(type = SourceDataFileJpa.class)
  public List<SourceDataFile> getSourceDataFiles() {
    return this.sourceDataFiles;
  }

  /* see superclass */
  @Override
  public void setSourceDataFiles(List<SourceDataFile> sourceDataFiles) {
    this.sourceDataFiles = sourceDataFiles;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((handler == null) ? 0 : handler.hashCode());
    result =
        prime * result
            + ((handlerStatus == null) ? 0 : handlerStatus.hashCode());
    result =
        prime * result + ((lastModified == null) ? 0 : lastModified.hashCode());
    result =
        prime * result
            + ((lastModifiedBy == null) ? 0 : lastModifiedBy.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result =
        prime * result
            + ((sourceDataFiles == null) ? 0 : sourceDataFiles.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
    result =
        prime * result + ((statusText == null) ? 0 : statusText.hashCode());
    result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
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
    SourceDataJpa other = (SourceDataJpa) obj;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (handler == null) {
      if (other.handler != null)
        return false;
    } else if (!handler.equals(other.handler))
      return false;
    if (handlerStatus != other.handlerStatus)
      return false;
    if (lastModified == null) {
      if (other.lastModified != null)
        return false;
    } else if (!lastModified.equals(other.lastModified))
      return false;
    if (lastModifiedBy == null) {
      if (other.lastModifiedBy != null)
        return false;
    } else if (!lastModifiedBy.equals(other.lastModifiedBy))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (sourceDataFiles == null) {
      if (other.sourceDataFiles != null)
        return false;
    } else if (!sourceDataFiles.equals(other.sourceDataFiles))
      return false;
    if (status != other.status)
      return false;
    if (statusText == null) {
      if (other.statusText != null)
        return false;
    } else if (!statusText.equals(other.statusText))
      return false;
    if (timestamp == null) {
      if (other.timestamp != null)
        return false;
    } else if (!timestamp.equals(other.timestamp))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "SourceDataJpa [id=" + id + ", name=" + name + ", description="
        + description + ", timestamp=" + timestamp + ", lastModified="
        + lastModified + ", lastModifiedBy=" + lastModifiedBy
        + ", sourceDataFiles=" + sourceDataFiles + ", status=" + status
        + ", statusText=" + statusText + ", handler=" + handler
        + ", handlerStatus=" + handlerStatus + "]";
  }

  /* see superclass */
  @Override
  public String getTerminology() {
    return this.terminology;
  }

  /* see superclass */
  @Override
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  /* see superclass */
  @Override
  public String getVersion() {
    return this.version;
  }

  /* see superclass */
  @Override
  public void setVersion(String version) {
    this.version = version;
  }

  /* see superclass */
  @Override
  public String getReleaseVersion() {
    return this.releaseVersion;
  }

  /* see superclass */
  @Override
  public void setReleaseVersion(String releaseVersion) {
    this.releaseVersion = releaseVersion;
  }

}
