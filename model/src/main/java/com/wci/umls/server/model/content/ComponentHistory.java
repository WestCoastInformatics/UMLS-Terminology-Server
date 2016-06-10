/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

/**
 * Represents a terminology component.
 */
public interface ComponentHistory extends Component {

  /**
   * Gets the referenced concept.
   *
   * @return the referenced concept
   */
  public Concept getReferencedConcept();

  /**
   * Sets the referenced concept.
   *
   * @param referencedConcept the new referenced concept
   */
  public void setReferencedConcept(Concept referencedConcept);

  /**
   * Gets the reason.
   *
   * @return the reason
   */
  public String getReason();

  /**
   * Sets the reason.
   *
   * @param reason the new reason
   */
  public void setReason(String reason);

  /**
   * Gets the relationship type.
   *
   * @return the relationship type
   */
  public String getRelationshipType();

  /**
   * Sets the relationship type.
   *
   * @param relationshipType the new relationship type
   */
  public void setRelationshipType(String relationshipType);

  /**
   * Gets the additional relationship type.
   *
   * @return the additional relationship type
   */
  public String getAdditionalRelationshipType();

  /**
   * Sets the additional relationship type.
   *
   * @param relationshipType the new additional relationship type
   */
  public void setAdditionalRelationshipType(String relationshipType);

  /**
   * Returns the associated release.
   *
   * @return the associated release
   */
  public String getAssociatedRelease();

  /**
   * Sets the associated release.
   *
   * @param associatedRelease the associated release
   */
  public void setAssociatedRelease(String associatedRelease);
}