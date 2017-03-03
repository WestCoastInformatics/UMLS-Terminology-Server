/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

/**
 * Represents a terminology component.
 */
public interface ComponentHistory extends Component {

  /**
   * Returns the referenced terminology id.
   *
   * @return the referenced terminology id
   */
  public String getReferencedTerminologyId();

  /**
   * Sets the referenced terminology id.
   *
   * @param referencedTerminologyId the referenced terminology id
   */
  public void setReferencedTerminologyId(String referencedTerminologyId);

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