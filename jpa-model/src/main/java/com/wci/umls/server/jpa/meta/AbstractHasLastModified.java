/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.meta;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.envers.Audited;

import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.model.meta.Abbreviation;

/**
 * Abstract implementation of {@link Abbreviation} for use with JPA.
 */
@Audited
@MappedSuperclass
public abstract class AbstractHasLastModified implements HasLastModified {

  /** The id. */
  @Id
  @GeneratedValue
  private Long id;

  /** the timestamp. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date timestamp = new Date();

  /** The last modified. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastModified = new Date();

  /** The last modified. */
  @Column(nullable = false)
  private String lastModifiedBy;

  /**
   * Instantiates an empty {@link AbstractHasLastModified}.
   */
  protected AbstractHasLastModified() {
    // do nothing
  }

  /**
   * Instantiates a {@link AbstractHasLastModified} from the specified
   * parameters.
   *
   * @param object the object
   */
  protected AbstractHasLastModified(HasLastModified object) {
    id = object.getId();
    timestamp = object.getTimestamp();
    lastModified = object.getLastModified();
    lastModifiedBy = object.getLastModifiedBy();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasId#getId()
   */
  @Override
  public Long getId() {
    return this.id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasId#setId(java.lang.Long)
   */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasLastModified#getTimestamp()
   */
  @Override
  public Date getTimestamp() {
    return timestamp;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.HasLastModified#setTimestamp(java.util.Date)
   */
  @Override
  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasLastModified#getLastModified()
   */
  @Override
  public Date getLastModified() {
    return lastModified;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.HasLastModified#setLastModified(java.util.Date)
   */
  @Override
  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasLastModified#getLastModifiedBy()
   */
  @Override
  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.HasLastModified#setLastModifiedBy(java.lang
   * .String)
   */
  @Override
  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

}
