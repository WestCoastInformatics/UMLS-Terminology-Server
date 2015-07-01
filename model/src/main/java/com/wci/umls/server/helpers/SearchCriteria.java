/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

/**
 * Represents semantic search criteria. NOTE, some combinations of these flags
 * may be erroneous (such as finding active AND inactive only content).
 */
public interface SearchCriteria {

  /**
   * Indicates whether to find descendants of matches.
   *
   * @return the find descendants
   */
  public boolean getFindDescendants();

  /**
   * Sets the find descendants flag.
   *
   * @param descendants the find descendants
   */
  public void setFindDescendants(boolean descendants);

  /**
   * Indicates whether to find matches themselves (vs only descendants) This
   * flag only makes sense in the context of the descendants flag.
   * @return the find self
   */
  public boolean getFindSelf();

  /**
   * Sets the find self flag.
   *
   * @param self the find self
   */
  public void setFindSelf(boolean self);

  /**
   * Indicates whether to find only primitive content.
   *
   * @return the find primitive only
   */
  public boolean getPrimitiveOnly();

  /**
   * Sets the find primitive only flag.
   *
   * @param primitiveOnly the find primitive only
   */
  public void setPrimitiveOnly(boolean primitiveOnly);

  /**
   * Indicates whether to find only fully defined content.
   *
   * @return the find fully defined only
   */
  public boolean getDefinedOnly();

  /**
   * Sets the find fully defined only flag.
   *
   * @param fullyDefinedOnly the find fully defined only
   */
  public void setDefinedOnly(boolean fullyDefinedOnly);

  /**
   * Returns the from id of relationships for which matches among to ids will be
   * included.
   *
   * @return the find by relationship from id
   */
  public String getRelationshipFromId();

  /**
   * Sets the relationship from id.
   *
   * @param id the relationship from id
   */
  public void setRelationshipFromId(String id);

  /**
   * Returns the type id of relationships for which matches among from or to ids
   * will be included.
   *
   * @return the find by relationship type
   */
  public String getRelationshipType();

  /**
   * Sets the relationship type.
   *
   * @param type the relationship type
   */
  public void setRelationshipType(String type);

  /**
   * Returns the to id of relationships for which matches among from ids will be
   * included.
   *
   * @return the find by relationship to id
   */
  public String getRelationshipToId();

  /**
   * Sets the relationship to id.
   *
   * @param id the relationship to id
   */
  public void setRelationshipToId(String id);

  /**
   * Indicates whether the specified from or to id of a relationship criteria
   * should be searched for just that id or also its descendants.
   *
   * @return the find by relationship descendants
   */
  public boolean getRelationshipDescendantsFlag();

  /**
   * Indicates that the search should return from concepts connected by the
   * specified type id to the specified to id and (optionally) all of its
   * descendants.
   *
   * @param type the type id
   * @param toId the to id
   * @param descendants the descendants
   */
  public void setFindFromByRelationshipTypeAndTo(String type, String toId,
    boolean descendants);

  /**
   * Indicates that the search should return to concepts connected by the
   * specified type id to the specified from id and (optionally) all of its
   * descendants.
   *
   * @param type the type id
   * @param fromId the from id
   * @param descendants the descendants
   */
  public void setFindToByRelationshipFromAndType(String type, String fromId,
    boolean descendants);

}
