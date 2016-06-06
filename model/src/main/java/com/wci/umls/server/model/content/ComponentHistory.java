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
   * Gets the release.
   *
   * @return the release
   */
  public String getRelease();
  
  /**
   * Sets the release.
   *
   * @param release the new release
   */
  public void setRelease(String release);

  /**
   * Gets the referenced concept id.
   *
   * @return the referenced concept id
   */
  public Long getReferencedConceptId();

  /**
   * Sets the referenced concept id.
   *
   * @param id the new referenced concept id
   */
  public void setReferencedConceptId(Long id);

  /**
   * Gets the referenced concept terminology id.
   *
   * @return the referenced concept terminology id
   */
  public String getReferencedConceptTerminologyId();

  /**
   * Sets the referenced concept terminology id.
   *
   * @param terminologyId the new referenced concept terminology id
   */
  public void setReferencedConceptTerminologyId(String terminologyId);

  /**
   * Gets the referenced concept terminology.
   *
   * @return the referenced concept terminology
   */
  public String getReferencedConceptTerminology();

  /**
   * Sets the referenced concept terminology.
   *
   * @param terminology the new referenced concept terminology
   */
  public void setReferencedConceptTerminology(String terminology);

  /**
   * Gets the referenced concept version.
   *
   * @return the referenced concept version
   */
  public String getReferencedConceptVersion();

  /**
   * Sets the referenced concept version.
   *
   * @param version the new referenced concept version
   */
  public void setReferencedConceptVersion(String version);

  /**
   * Gets the referenced concept name.
   *
   * @return the referenced concept name
   */
  public String getReferencedConceptName();

  /**
   * Sets the referenced concept name.
   *
   * @param term the new referenced concept name
   */
  public void setReferencedConceptName(String term);

}