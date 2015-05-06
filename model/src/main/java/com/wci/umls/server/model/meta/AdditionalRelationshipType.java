/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

/**
 * Represents a further specification of a relation.
 * @see RelationshipType
 */
public interface AdditionalRelationshipType extends Abbreviation {

  /**
   * Returns the inverse type.
   * 
   * @return the inverse type
   */
  public AdditionalRelationshipType getInverseType();

  /**
   * Sets the inverse type.
   * 
   * @param inverse the inverse type
   */
  public void setInverseType(AdditionalRelationshipType inverse);
  
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
   * Indicates whether or not equivalent classes is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isEquivalentClasses();
  
  /**
   * Sets the equivalent classes.
   *
   * @param flag the equivalent classes
   */
  public void setEquivalentClasses(boolean flag);
  
  /**
   * Returns the super type.
   *
   * @return the super type
   */
  public AdditionalRelationshipType getSuperType();
  
  /**
   * Sets the super type.
   *
   * @param inverse the super type
   */
  public void setSuperType(AdditionalRelationshipType inverse);

  /**
   * Returns the equivalent type.
   *
   * @return the equivalent type
   */
  public AdditionalRelationshipType getEquivalentType();
  
  /**
   * Sets the equivalent type.
   *
   * @param inverse the equivalent type
   */
  public void setEquivalentType(AdditionalRelationshipType inverse);

  /**
   * Indicates whether or not transitive is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isTransitive();
  
  /**
   * Sets the transitive.
   *
   * @param flag the transitive
   */
  public void setTransitive(boolean flag);
  
  /**
   * Indicates whether or not reflexive is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isReflexive();
  
  /**
   * Sets the reflexive.
   *
   * @param flag the reflexive
   */
  public void setReflexive(boolean flag);
  
  /**
   * Indicates whether or not irreflexive is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isIrreflexive();
  
  /**
   * Sets the irreflexive.
   *
   * @param flag the irreflexive
   */
  public void setIrreflexive(boolean flag);
  
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
   * Indicates whether or not inverse functional is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isInverseFunctional();
  
  /**
   * Sets the inverse functional.
   *
   * @param flag the inverse functional
   */
  public void setInverseFunctional(boolean flag);
  
  /**
   * Indicates whether or not symmetric is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isSymmetric();
  
  /**
   * Sets the symmetric.
   *
   * @param flag the symmetric
   */
  public void setSymmetric(boolean flag);
  
  /**
   * Indicates whether or not asymmetric is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isAsymmetric();
  
  /**
   * Sets the asymmetric.
   *
   * @param flag the asymmetric
   */
  public void setAsymmetric(boolean flag);

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
  

  /**
   * Indicates whether or not grouping type is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isGroupingType();

  /**
   * Sets the grouping type.
   *
   * @param groupingType the grouping type
   */
  public void setGroupingType(boolean groupingType);
}
