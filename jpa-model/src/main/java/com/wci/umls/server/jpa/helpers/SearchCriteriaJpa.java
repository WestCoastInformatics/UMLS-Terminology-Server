/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.SearchCriteria;

/**
 * JAXB enabled implementation of a {@link SearchCriteria}.
 */
@XmlRootElement(name = "searchCriteria")
public class SearchCriteriaJpa implements SearchCriteria {

  /** The destination id. */
  private String relationshipToId;

  /** The relationship descendants. */
  private boolean relationshipDescendantsFlag;

  /** The relationship type id. */
  private String relationshipType;

  /** The source id. */
  private String relationshipFromId;

  /** The descendants. */
  private boolean findDescendants;

  /** The defined only. */
  private boolean definedOnly;

  /** The primitive only. */
  private boolean primitiveOnly;

  /** The self. */
  private boolean findSelf;

  /**
   * Instantiates an empty {@link SearchCriteriaJpa}.
   */
  public SearchCriteriaJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link SearchCriteriaJpa} from the specified parameters.
   *
   * @param searchCriteria the search criteria
   */
  public SearchCriteriaJpa(SearchCriteria searchCriteria) {
    relationshipToId = searchCriteria.getRelationshipToId();
    relationshipDescendantsFlag =
        searchCriteria.getRelationshipDescendantsFlag();
    relationshipType = searchCriteria.getRelationshipType();
    relationshipFromId = searchCriteria.getRelationshipFromId();
    definedOnly = searchCriteria.getDefinedOnly();
    findDescendants = searchCriteria.getFindDescendants();
    primitiveOnly = searchCriteria.getPrimitiveOnly();
    findSelf = searchCriteria.getFindSelf();
  }

  /* see superclass */
  @Override
  public boolean getFindDescendants() {
    return findDescendants;
  }

  /* see superclass */
  @Override
  public void setFindDescendants(boolean descendants) {
    this.findDescendants = descendants;
  }

  /* see superclass */
  @Override
  public boolean getFindSelf() {
    return findSelf;
  }

  /* see superclass */
  @Override
  public void setFindSelf(boolean self) {
    this.findSelf = self;
  }

  /* see superclass */
  @Override
  public boolean getPrimitiveOnly() {
    return primitiveOnly;
  }

  /* see superclass */
  @Override
  public void setPrimitiveOnly(boolean primitiveOnly) {
    this.primitiveOnly = primitiveOnly;
  }

  /* see superclass */
  @Override
  public boolean getDefinedOnly() {
    return definedOnly;
  }

  /* see superclass */
  @Override
  public void setDefinedOnly(boolean definedOnly) {
    this.definedOnly = definedOnly;
  }

  /* see superclass */
  @Override
  public String getRelationshipFromId() {
    return relationshipFromId;
  }

  /* see superclass */
  @Override
  public String getRelationshipType() {
    return relationshipType;
  }

  /* see superclass */
  @Override
  public String getRelationshipToId() {
    return relationshipToId;
  }

  /* see superclass */
  @Override
  public boolean getRelationshipDescendantsFlag() {
    return relationshipDescendantsFlag;
  }

  /* see superclass */
  @Override
  public void setFindFromByRelationshipTypeAndTo(String type,
    String destinationId, boolean descendants) {
    this.relationshipType = type;
    this.relationshipToId = destinationId;
    this.relationshipDescendantsFlag = descendants;
  }

  /* see superclass */
  @Override
  public void setFindToByRelationshipFromAndType(String type, String sourceId,
    boolean descendants) {
    this.relationshipType = type;
    this.relationshipFromId = sourceId;
    this.relationshipDescendantsFlag = descendants;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (definedOnly ? 1231 : 1237);
    result = prime * result + (findDescendants ? 1231 : 1237);
    result = prime * result + (findSelf ? 1231 : 1237);
    result = prime * result + (primitiveOnly ? 1231 : 1237);
    result = prime * result + (relationshipDescendantsFlag ? 1231 : 1237);
    result =
        prime
            * result
            + ((relationshipFromId == null) ? 0 : relationshipFromId.hashCode());
    result =
        prime * result
            + ((relationshipToId == null) ? 0 : relationshipToId.hashCode());
    result =
        prime * result
            + ((relationshipType == null) ? 0 : relationshipType.hashCode());
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
    SearchCriteriaJpa other = (SearchCriteriaJpa) obj;
    if (definedOnly != other.definedOnly)
      return false;
    if (findDescendants != other.findDescendants)
      return false;
    if (findSelf != other.findSelf)
      return false;
    if (primitiveOnly != other.primitiveOnly)
      return false;
    if (relationshipDescendantsFlag != other.relationshipDescendantsFlag)
      return false;
    if (relationshipFromId == null) {
      if (other.relationshipFromId != null)
        return false;
    } else if (!relationshipFromId.equals(other.relationshipFromId))
      return false;
    if (relationshipToId == null) {
      if (other.relationshipToId != null)
        return false;
    } else if (!relationshipToId.equals(other.relationshipToId))
      return false;
    if (relationshipType == null) {
      if (other.relationshipType != null)
        return false;
    } else if (!relationshipType.equals(other.relationshipType))
      return false;
    return true;
  }

  /**
   * Sets the find by destination id.
   *
   * @param findByDestinationId the find by destination id
   */
  public void setFindByDestinationId(String findByDestinationId) {
    this.relationshipToId = findByDestinationId;
  }

  /**
   * Sets the find by relationship descendants.
   *
   * @param findByRelationshipDescendants the find by relationship descendants
   */
  public void setFindByRelationshipDescendants(
    boolean findByRelationshipDescendants) {
    this.relationshipDescendantsFlag = findByRelationshipDescendants;
  }

  /**
   * Sets the find by relationship type id.
   *
   * @param findByRelationshipTypeId the find by relationship type id
   */
  public void setFindByRelationshipTypeId(String findByRelationshipTypeId) {
    this.relationshipType = findByRelationshipTypeId;
  }

  /**
   * Sets the find by source id.
   *
   * @param findBySourceId the find by source id
   */
  public void setFindBySourceId(String findBySourceId) {
    this.relationshipFromId = findBySourceId;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "SearchCriteriaJpa [relationshipToId=" + relationshipToId
        + ", relationshipDescendantsFlag=" + relationshipDescendantsFlag
        + ", relationshipType=" + relationshipType + ", relationshipFromId="
        + relationshipFromId + ", findDescendants=" + findDescendants
        + ", definedOnly=" + definedOnly + ", primitiveOnly=" + primitiveOnly
        + ", findSelf=" + findSelf + "]";
  }

  /* see superclass */
  @XmlElement
  @Override
  public void setRelationshipFromId(String relationshipFromId) {
    this.relationshipFromId = relationshipFromId;
  }

  /* see superclass */
  @XmlElement
  @Override
  public void setRelationshipType(String relationshipType) {
    this.relationshipType = relationshipType;
  }

  /* see superclass */
  @XmlElement
  @Override
  public void setRelationshipToId(String relationshipToId) {
    this.relationshipToId = relationshipToId;
  }

  /**
   * Sets the relationship descendants flag.
   *
   * @param descendants the relationship descendants flag
   */
  public void setRelationshipDescendantsFlag(boolean descendants) {
    this.relationshipDescendantsFlag = descendants;
  }

}
