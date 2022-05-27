package com.wci.umls.server.jpa.inversion;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.EncodingType;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.SortableField;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.LongBridge;

import com.wci.umls.server.Project;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.model.inversion.SourceIdRange;

/**
 * JPA and JAXB enabled implementation of {@link SourceIdRange}.
 */
@Entity
@Table(name = "source_id_ranges", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminology"
}))
@Audited
@Indexed
@XmlRootElement(name = "sourceIdRange")
public class SourceIdRangeJpa implements SourceIdRange {

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

  /** The last modified. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date timestamp = new Date();

  /** The versioned terminology. */
  @Column(nullable = false)
  private String terminology;
  
  /** The begin source id. */
  @Column(nullable = false)
  private Long beginSourceId;
  
  /** The end source id. */
  @Column(nullable = false)
  private Long endSourceId;
  
  /** The project. */
  @ManyToOne(targetEntity = ProjectJpa.class, optional = false)
  private Project project;
  
  public SourceIdRangeJpa() {
    // do nothing
  }
  
  public SourceIdRangeJpa(SourceIdRange sourceIdRange) {
    super();
    id = sourceIdRange.getId();
    lastModified = sourceIdRange.getLastModified();
    lastModifiedBy = sourceIdRange.getLastModifiedBy();
    timestamp = sourceIdRange.getTimestamp();
    terminology = sourceIdRange.getTerminology();
    project = sourceIdRange.getProject();
  }

  
  /* see superclass */
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  @DateBridge(resolution = Resolution.SECOND, encoding = EncodingType.STRING)
  @SortableField
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
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getTerminology() {
    return terminology;
  }

  /* see superclass */
  @Override
  public void setTerminology(String vsab) {
    this.terminology = vsab;
  }

  /* see superclass */
  @Override
  @XmlTransient
  public Project getProject() {
    return project;
  }

  /* see superclass */
  @Override
  public void setProject(Project project) {
    this.project = project;
  }

  @Override
  public void setBeginSourceId(Long beginSourceId) {
    this.beginSourceId = beginSourceId;    
  }

  @Override
  public Long getBeginSourceId() {
    return beginSourceId;
  }

  @Override
  public void setEndSourceId(Long endSourceId) {
    this.endSourceId = endSourceId;
  }

  @Override
  public Long getEndSourceId() {
    return endSourceId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((beginSourceId == null) ? 0 : beginSourceId.hashCode());
    result =
        prime * result + ((endSourceId == null) ? 0 : endSourceId.hashCode());
    result = prime * result + ((project == null) ? 0 : project.hashCode());
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());

    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SourceIdRangeJpa other = (SourceIdRangeJpa) obj;
    if (beginSourceId == null) {
      if (other.beginSourceId != null)
        return false;
    } else if (!beginSourceId.equals(other.beginSourceId))
      return false;
    if (endSourceId == null) {
      if (other.endSourceId != null)
        return false;
    } else if (!endSourceId.equals(other.endSourceId))
      return false;
    if (project == null) {
      if (other.project != null)
        return false;
    } else if (!project.equals(other.project))
      return false;
    if (terminology == null) {
      if (other.terminology != null)
        return false;
    } else if (!terminology.equals(other.terminology))
      return false;


    return true;
  }

  @Override
  public String toString() {
    return "SourceIdRangeJpa [id=" + id + ", lastModified=" + lastModified
        + ", lastModifiedBy=" + lastModifiedBy + ", timestamp=" + timestamp
        + ", vsab=" + terminology 
        + ", beginSourceId=" + beginSourceId + ", endSourceId=" + endSourceId
        + ", project=" + project + "]";
  }

 
}
