/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.ReleaseProperty;

/**
 * JPA enabled implementation of a {@link ReleaseInfo}.
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
  @Id
  @GeneratedValue
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

  /** The terminology version. */
  @Column(nullable = false)
  private String terminologyVersion;

  /** The last modified by. */
  @Column(nullable = false)
  private String lastModifiedBy;

  /** The last modified. */
  @Column(nullable = false)
  private Date lastModified = new Date();

  /** The release properties. */
  @OneToMany(mappedBy = "releaseInfo", cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = ReleasePropertyJpa.class)
  private List<ReleaseProperty> properties;

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
    name = releaseInfo.getName();
    description = releaseInfo.getDescription();
    releaseBeginDate = releaseInfo.getReleaseBeginDate();
    releaseFinishDate = releaseInfo.getReleaseFinishDate();
    planned = releaseInfo.isPlanned();
    published = releaseInfo.isPublished();
    terminology = releaseInfo.getTerminology();
    terminologyVersion = releaseInfo.getTerminologyVersion();
  }

  /**
   * ID for XML serialization.
   *
   * @return the object id
   */
  @XmlID
  public String getObjectId() {
    return (id == null ? "" : id.toString());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.helpers.ReleaseInfo#getId()
   */
  @Override
  public Long getId() {
    return id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.helpers.ReleaseInfo#setId(java.lang.Long)
   */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.helpers.ReleaseInfo#getName()
   */
  @Override
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.helpers.ReleaseInfo#setName(java.lang.String)
   */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.helpers.ReleaseInfo#getDescription()
   */
  @Override
  public String getDescription() {
    return description;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.helpers.ReleaseInfo#setDescription(java.lang.String)
   */
  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.helpers.ReleaseInfo#getReleaseBeginDate()
   */
  @Override
  public Date getReleaseBeginDate() {
    return releaseBeginDate;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.helpers.ReleaseInfo#setReleaseBeginDate(java.util.Date)
   */
  @Override
  public void setReleaseBeginDate(Date releaseBeginDate) {
    this.releaseBeginDate = releaseBeginDate;

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.helpers.ReleaseInfo#getReleaseFinishDate()
   */
  @Override
  public Date getReleaseFinishDate() {
    return releaseFinishDate;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.helpers.ReleaseInfo#setReleaseFinishDate(java.util.Date)
   */
  @Override
  public void setReleaseFinishDate(Date releaseFinishDate) {
    this.releaseFinishDate = releaseFinishDate;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.helpers.ReleaseInfo#isPlanned()
   */
  @Override
  public boolean isPlanned() {
    return planned;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.helpers.ReleaseInfo#setPlanned(boolean)
   */
  @Override
  public void setPlanned(boolean planned) {
    this.planned = planned;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.helpers.ReleaseInfo#isPublished()
   */
  @Override
  public boolean isPublished() {
    return published;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.helpers.ReleaseInfo#setPublished(boolean)
   */
  @Override
  public void setPublished(boolean published) {
    this.published = published;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.helpers.ReleaseInfo#getTerminology()
   */
  @Override
  public String getTerminology() {
    return terminology;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.helpers.ReleaseInfo#setTerminology(java.lang.String)
   */
  @Override
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.helpers.ReleaseInfo#getTerminologyVersion()
   */
  @Override
  public String getTerminologyVersion() {
    return terminologyVersion;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.helpers.ReleaseInfo#setTerminologyVersion(java.lang.String
   * )
   */
  @Override
  public void setTerminologyVersion(String terminologyVersion) {
    this.terminologyVersion = terminologyVersion;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.ReleaseInfo#getLastModifiedBy()
   */
  @Override
  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  @Override
  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.ReleaseInfo#getLastModified()
   */
  @Override
  public Date getLastModified() {
    return lastModified;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.ReleaseInfo#setLastModified(java.util.Date)
   */
  @Override
  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
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
    result =
        prime
            * result
            + ((terminologyVersion == null) ? 0 : terminologyVersion.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.ReleaseInfo#getProperties()
   */
  @Override
  @XmlElement(type = ReleasePropertyJpa.class, name = "property")
  public List<ReleaseProperty> getProperties() {
    return properties;
  }

  @Override
  public void setProperties(List<ReleaseProperty> properties) {
    this.properties = properties;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
    if (terminologyVersion == null) {
      if (other.terminologyVersion != null)
        return false;
    } else if (!terminologyVersion.equals(other.terminologyVersion))
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return name + ", " + description + ", " + effectiveTime + ", " + planned
        + ", " + published + ", " + terminology + ", " + terminologyVersion;
  }

}
