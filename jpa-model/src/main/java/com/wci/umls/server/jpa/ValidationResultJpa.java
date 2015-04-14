/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.ValidationResult;

/**
 * JPA enabled implementation of {@link ValidationResult}.
 */
@XmlRootElement
public class ValidationResultJpa implements ValidationResult {

  /** The errors. */
  private Set<String> errors = new HashSet<>();

  /** The warnings. */
  private Set<String> warnings = new HashSet<>();
  
  /**  The comments. */
  private Set<String> comments = new HashSet<>();

  /**
   * Instantiates an empty {@link ValidationResultJpa}.
   */
  public ValidationResultJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ValidationResultJpa} from the specified parameters.
   *
   * @param result the result
   */
  public ValidationResultJpa(ValidationResult result) {
    this.errors = new HashSet<>(result.getErrors());
    this.warnings = new HashSet<>(result.getWarnings());
    this.comments = new HashSet<>(result.getComments());
    }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.helpers.ValidationResult#isValid()
   */
  @Override
  public boolean isValid() {
    return errors.size() == 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.helpers.ValidationResult#getErrors()
   */
  @XmlElement(type = String.class, name = "error")
  @Override
  public Set<String> getErrors() {
    return errors;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.mapping.helpers.ValidationResult#setErrors(java.util.Set)
   */
  @Override
  public void setErrors(Set<String> errors) {
    this.errors = errors;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.mapping.helpers.ValidationResult#addError(java.lang.String)
   */
  @Override
  public void addError(String error) {
    this.errors.add(error);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.mapping.helpers.ValidationResult#addErrors(java.util.Set)
   */
  @Override
  public void addErrors(Set<String> errorSet) {
    if (this.errors != null) {
      this.errors.addAll(errorSet);
    } else {
      this.errors = new HashSet<>(errorSet);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.mapping.helpers.ValidationResult#removeError(java.lang.String
   * )
   */
  @Override
  public void removeError(String error) {
    this.errors.remove(error);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.helpers.ValidationResult#getWarnings()
   */
  @XmlElement(type = String.class, name = "warning")
  @Override
  public Set<String> getWarnings() {
    return warnings;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.mapping.helpers.ValidationResult#setWarnings(java.util.Set)
   */
  @Override
  public void setWarnings(Set<String> warnings) {
    this.warnings = warnings;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.mapping.helpers.ValidationResult#addWarning(java.lang.String
   * )
   */
  @Override
  public void addWarning(String warning) {
    this.warnings.add(warning);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.mapping.helpers.ValidationResult#addWarnings(java.util.Set)
   */
  @Override
  public void addWarnings(Set<String> warningSet) {
    if (this.warnings != null)
      this.warnings.addAll(warningSet);
    else
      this.warnings = new HashSet<>(warningSet);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.mapping.helpers.ValidationResult#removeWarning(java.lang
   * .String)
   */
  @Override
  public void removeWarning(String warning) {
    this.warnings.remove(warning);
  }

  /* (non-Javadoc)
   * @see org.ihtsdo.otf.ts.ValidationResult#getComments()
   */
  @Override
  public Set<String> getComments() {
    return comments;
  }

  /* (non-Javadoc)
   * @see org.ihtsdo.otf.ts.ValidationResult#setComments(java.util.Set)
   */
  @Override
  public void setComments(Set<String> comments) {
    this.comments = comments;
  }

  /* (non-Javadoc)
   * @see org.ihtsdo.otf.ts.ValidationResult#removeComment(java.lang.String)
   */
  @Override
  public void removeComment(String comment) {
    comments.remove(comment);
  }

  /* (non-Javadoc)
   * @see org.ihtsdo.otf.ts.ValidationResult#addComment(java.lang.String)
   */
  @Override
  public void addComment(String comment) {
    comments.add(comment);
  }

  /* (non-Javadoc)
   * @see org.ihtsdo.otf.ts.ValidationResult#addComment(java.util.Set)
   */
  @Override
  public void addComment(Set<String> comments) {
    comments.addAll(comments);
  }
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.helpers.ValidationResult#merge(org.ihtsdo.otf.ts.helpers
   * .ValidationResult)
   */
  @Override
  public void merge(ValidationResult validationResult) {

    this.errors.addAll(validationResult.getErrors());
    this.warnings.addAll(validationResult.getWarnings());
    this.comments.addAll(validationResult.getComments());

  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ERRORS: " + errors + ", WARNINGS: " + warnings + ", COMMENTS: " + comments;
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
    result = prime * result + ((errors == null) ? 0 : errors.hashCode());
    result = prime * result + ((warnings == null) ? 0 : warnings.hashCode());
    result = prime * result + ((comments == null) ? 0 : comments.hashCode());
    return result;
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
    ValidationResultJpa other = (ValidationResultJpa) obj;
    if (errors == null) {
      if (other.errors != null)
        return false;
    } else if (!errors.equals(other.errors))
      return false;
    if (warnings == null) {
      if (other.warnings != null)
        return false;
    } else if (!warnings.equals(other.warnings))
      return false;
    if (comments == null) {
      if (other.comments != null)
        return false;
    } else if (!comments.equals(other.comments))
      return false;
    return true;
  }


}
