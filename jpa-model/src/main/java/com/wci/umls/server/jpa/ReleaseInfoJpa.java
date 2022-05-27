/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.ReleaseProperty;

/**
 * JPA and JAXB enabled implementation of a {@link ReleaseInfo}.
 */
@Entity
@Table(name = "release_infos", uniqueConstraints = {
    @UniqueConstraint(columnNames = {
        "name", "terminology"
    })
})
@Audited
@XmlRootElement(name = "releaseInfo")
public class ReleaseInfoJpa implements ReleaseInfo {

  /** The id. */
  @TableGenerator(name = "EntityIdGen", table = "table_generator", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;

  /** The name. */
  @Column(nullable = false, length = 255)
  private String name;

  /** The description. */
  @Column(nullable = false, length = 4000)
  private String description;

  /** The effective time. */
  @Temporal(TemporalType.TIMESTAMP)
  private Date effectiveTime;

  /** The release begin date. */
  @Temporal(TemporalType.TIMESTAMP)
  private Date releaseBeginDate;

  /** The release finish date. */
  @Temporal(TemporalType.TIMESTAMP)
  private Date releaseFinishDate;

  /** The planned flag. */
  @Column(nullable = false)
  private boolean planned;

  /** The published flag. */
  @Column(nullable = false)
  private boolean published;

  /** The terminology. */
  @Column(nullable = false)
  private String terminology;

  /** The version. */
  @Column(nullable = false)
  private String version;

  /** The last modified by. */
  @Column(nullable = false)
  private String lastModifiedBy;

  /** The last modified. */
  @Column(nullable = false)
  private Date lastModified = new Date();

  /** The release properties. */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true, targetEntity = ReleasePropertyJpa.class)
  @JoinColumn(name = "properties_id")
  @JoinTable(name = "release_infos_release_properties",
      inverseJoinColumns = @JoinColumn(name = "properties_id"),
      joinColumns = @JoinColumn(name = "release_infos_id"))
  private List<ReleaseProperty> properties;

  /** the timestamp. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date timestamp = null;
  
  /**
   * Instantiates an empty {@link ReleaseInfoJpa}.
   */
  public ReleaseInfoJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ReleaseInfoJpa} from the specified parameters.
   *
   * @param releaseInfo the release info
   */
  public ReleaseInfoJpa(ReleaseInfo releaseInfo) {
    id = releaseInfo.getId();
    lastModified = releaseInfo.getLastModified();
    lastModifiedBy = releaseInfo.getLastModifiedBy();
    name = releaseInfo.getName();
    description = releaseInfo.getDescription();
    releaseBeginDate = releaseInfo.getReleaseBeginDate();
    releaseFinishDate = releaseInfo.getReleaseFinishDate();
    planned = releaseInfo.isPlanned();
    published = releaseInfo.isPublished();
    terminology = releaseInfo.getTerminology();
    version = releaseInfo.getVersion();
    properties = new ArrayList<>(releaseInfo.getProperties());
    timestamp = releaseInfo.getTimestamp();
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
  public Date getReleaseBeginDate() {
    return releaseBeginDate;
  }

  /* see superclass */
  @Override
  public void setReleaseBeginDate(Date releaseBeginDate) {
    this.releaseBeginDate = releaseBeginDate;

  }

  /* see superclass */
  @Override
  public Date getReleaseFinishDate() {
    return releaseFinishDate;
  }

  /* see superclass */
  @Override
  public void setReleaseFinishDate(Date releaseFinishDate) {
    this.releaseFinishDate = releaseFinishDate;
  }

  /* see superclass */
  @Override
  public boolean isPlanned() {
    return planned;
  }

  /* see superclass */
  @Override
  public void setPlanned(boolean planned) {
    this.planned = planned;
  }

  /* see superclass */
  @Override
  public boolean isPublished() {
    return published;
  }

  /* see superclass */
  @Override
  public void setPublished(boolean published) {
    this.published = published;
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + (planned ? 1231 : 1237);
    result = prime * result + (published ? 1231 : 1237);
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  @XmlElement(type = ReleasePropertyJpa.class)
  public List<ReleaseProperty> getProperties() {
    return properties;
  }

  /* see superclass */
  @Override
  public void setProperties(List<ReleaseProperty> properties) {
    this.properties = properties;
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
    ReleaseInfoJpa other = (ReleaseInfoJpa) obj;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (planned != other.planned)
      return false;
    if (published != other.published)
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
    return name + ", " + description + ", " + effectiveTime + ", " + planned
        + ", " + published + ", " + terminology + ", " + version + ", " + timestamp;
  }

}
