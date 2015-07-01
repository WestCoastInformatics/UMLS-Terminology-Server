/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

/**
 * Represents the name of an attribute.
 */
public interface AttributeName extends Abbreviation {

  /**
   * Indicates whether or not universal quantification is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isUniversalQuantification();

  /**
   * Sets the universal quantification.
   *
   * @param flag the universal quantification
   */
  public void setUniversalQuantification(boolean flag);

  /**
   * Indicates whether or not existential quantification is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isExistentialQuantification();

  /**
   * Sets the existential quantification.
   *
   * @param flag the existential quantification
   */
  public void setExistentialQuantification(boolean flag);

  /**
   * Returns the super type.
   *
   * @return the super type
   */
  public AttributeName getSuperName();

  /**
   * Sets the super type.
   *
   * @param superName the super name
   */
  public void setSuperName(AttributeName superName);

  /**
   * Returns the equivalent type.
   *
   * @return the equivalent type
   */
  public AttributeName getEquivalentName();

  /**
   * Sets the equivalent type.
   *
   * @param equivalentName the equivalent name
   */
  public void setEquivalentName(AttributeName equivalentName);

  /**
   * Indicates whether or not functional is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isFunctional();

  /**
   * Sets the functional.
   *
   * @param flag the functional
   */
  public void setFunctional(boolean flag);

  /**
   * Returns the domain id.
   *
   * @return the domain id
   */
  public String getDomainId();

  /**
   * Sets the domain id.
   *
   * @param domainId the domain id
   */
  public void setDomainId(String domainId);

  /**
   * Returns the range id.
   *
   * @return the range id
   */
  public String getRangeId();

  /**
   * Sets the range id.
   *
   * @param rangeId the range id
   */
  public void setRangeId(String rangeId);

}
