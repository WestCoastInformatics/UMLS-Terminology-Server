/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.HasLastModified;

/**
 * Abstract implementation of {@link HasLastModified} for use with JPA.
 */
@Audited
@MappedSuperclass
public abstract class AbstractHasLastModified implements HasLastModified {

  /**
   * The id. - leave for subclasses because id generators may need to be
   * different
   */

  /** the timestamp. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  protected Date timestamp = null;

  /** The last modified. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  protected Date lastModified = null;

  /** The last modified. */
  @Column(nullable = false)
  protected String lastModifiedBy;

  /**
   * Instantiates an empty {@link AbstractHasLastModified}.
   */
  public AbstractHasLastModified() {
    // do nothing
  }

  /**
   * Instantiates a {@link AbstractHasLastModified} from the specified
   * parameters.
   *
   * @param component the component
   */
  public AbstractHasLastModified(HasLastModified component) {
    setId(component.getId());
    timestamp = new Date();
    lastModified = component.getLastModified();
    lastModifiedBy = component.getLastModifiedBy();
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
  public Date getLastModified() {
    return lastModified;
  }

  /**
   * Returns the last modified in yyyymmdd format.
   *
   * @return the last modified yyyymmdd
   */
  @Field(name = "lastModifiedYYYYMMDD", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  private String getLastModifiedYYYYMMDD() {
    return lastModified == null ? null
        : ConfigUtility.DATE_FORMAT.format(lastModified);
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

}
