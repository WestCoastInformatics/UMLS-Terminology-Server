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
   * Returns the id.
   *
   * @return the id
   */
  public Long getId();
  
  /**
   * Sets the id.
   *
   * @param id the id
   */
  public void setId(Long id);

  /**
   * Indicates whether to find only active content.
   *
   * @return the find active only
   */
  public boolean getFindActiveOnly();

  /**
   * Sets the find active only flag.
   *
   * @param activeOnly the find active only
   */
  public void setFindActiveOnly(boolean activeOnly);

  /**
   * Indicates whether to find only inactive content.
   *
   * @return the find inactive only
   */
  public boolean getFindInactiveOnly();

  /**
   * Sets the find inactive only flag.
   *
   * @param inactiveOnly the find inactive only
   */
  public void setFindInactiveOnly(boolean inactiveOnly);

  /**
   * Returns the find module id.
   *
   * @return the find by module id
   */
  public String getFindByModuleId();

  /**
   * Sets the find by module id.
   *
   * @param moduleId the find by module id
   */
  public void setFindByModuleId(String moduleId);

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
   * Indicates whether to find matches themselves (vs only descendants)
   *
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
  public boolean getFindPrimitiveOnly();

  /**
   * Sets the find primitive only flag.
   *
   * @param primitiveOnly the find primitive only
   */
  public void setFindPrimitiveOnly(boolean primitiveOnly);

  /**
   * Indicates whether to find only fully defined content.
   *
   * @return the find fully defined only
   */
  public boolean getFindDefinedOnly();

  /**
   * Sets the find fully defined only flag.
   *
   * @param fullyDefinedOnly the find fully defined only
   */
  public void setFindDefinedOnly(boolean fullyDefinedOnly);

  /**
   * Returns the source id of relationships for which matches among destination
   * ids will be included.
   *
   * @return the find by relationship source id
   */
  public String getFindBySourceId();

  /**
   * Returns the type id of relationships for which matches among source or
   * destination ids will be included.
   *
   * @return the find by relationship type id
   */
  public String getFindByRelationshipTypeId();

  /**
   * Returns the source id of relationships for which matches among source ids
   * will be included.
   *
   * @return the find by relationship destination id
   */
  public String getFindByDestinationId();

  /**
   * Indicates whether the specified source or destination id of a relationship
   * criteria should be searched for just that id or also its descendats.
   *
   * @return the find by relationship descendants
   */
  public boolean getFindByRelationshipDescendants();

  /**
   * Indicates that the search should return source concepts connected by the
   * specified type id to the specified destination id and (optionally) all of
   * its descendants.
   *
   * @param typeId the type id
   * @param destinationId the destination id
   * @param descendants the descendants
   */
  public void setFindSourceOfRelationship(String typeId, String destinationId,
    boolean descendants);

  /**
   * Indicates that the search should return destination concepts connected by
   * the specified type id to the specified source id and (optionally) all of
   * its descendants.
   *
   * @param typeId the type id
   * @param sourceId the source id
   * @param descendants the descendants
   */
  public void setFindDestinationOfRelationship(String typeId, String sourceId,
    boolean descendants);

}
