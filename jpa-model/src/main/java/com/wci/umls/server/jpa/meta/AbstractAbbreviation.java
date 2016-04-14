/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.meta;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.hibernate.envers.Audited;

import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.model.meta.Abbreviation;

/**
 * Abstract implementation of {@link Abbreviation} for use with JPA.
 */
@Audited
@MappedSuperclass
public abstract class AbstractAbbreviation extends AbstractHasLastModified
    implements Abbreviation {

  /** The published flag. */
  @Column(nullable = false)
  private boolean published = false;

  /** The publishable flag. */
  @Column(nullable = false)
  private boolean publishable = false;

  /** The abbreviation. */
  @Column(nullable = false)
  private String abbreviation;

  /** The expandedForm. */
  @Column(nullable = false, length = 4000)
  private String expandedForm;

  /** The terminology. */
  @Column(nullable = false)
  private String terminology;

  /** The expandedForm. */
  @Column(nullable = false)
  private String version;

  /** The branch. */
  @Column(nullable = true)
  private String branch = Branch.ROOT;

  /**
   * Instantiates an empty {@link AbstractAbbreviation}.
   */
  protected AbstractAbbreviation() {
    // do nothing
  }

  /**
   * Instantiates a {@link AbstractAbbreviation} from the specified parameters.
   *
   * @param abbreviation the abbreviation
   */
  protected AbstractAbbreviation(Abbreviation abbreviation) {
    super(abbreviation);
    this.abbreviation = abbreviation.getAbbreviation();
    expandedForm = abbreviation.getExpandedForm();
    terminology = abbreviation.getTerminology();
    version = abbreviation.getVersion();
    branch = abbreviation.getBranch();
    publishable = abbreviation.isPublishable();
    published = abbreviation.isPublished();
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
  public boolean isPublishable() {
    return publishable;
  }

  /* see superclass */
  @Override
  public void setPublishable(boolean publishable) {
    this.publishable = publishable;
  }

  /* see superclass */
  @Override
  public String getBranch() {
    return branch;
  }

  /* see superclass */
  @Override
  public void setBranch(String branch) {
    this.branch = branch;
  }

  /* see superclass */
  @Override
  public String getAbbreviation() {
    return abbreviation;
  }

  /* see superclass */
  @Override
  public String getExpandedForm() {
    return expandedForm;
  }

  /* see superclass */
  @Override
  public void setAbbreviation(String abbreviation) {
    this.abbreviation = abbreviation;
  }

  /* see superclass */
  @Override
  public void setExpandedForm(String expandedForm) {
    this.expandedForm = expandedForm;
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((abbreviation == null) ? 0 : abbreviation.hashCode());
    result =
        prime * result + ((expandedForm == null) ? 0 : expandedForm.hashCode());
    result = prime * result + (publishable ? 1231 : 1237);
    result = prime * result + (published ? 1231 : 1237);
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
    AbstractAbbreviation other = (AbstractAbbreviation) obj;
    if (abbreviation == null) {
      if (other.abbreviation != null)
        return false;
    } else if (!abbreviation.equals(other.abbreviation))
      return false;
    if (expandedForm == null) {
      if (other.expandedForm != null)
        return false;
    } else if (!expandedForm.equals(other.expandedForm))
      return false;
    if (publishable != other.publishable)
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
    return getAbbreviation() + " = " + getExpandedForm();
  }

}
