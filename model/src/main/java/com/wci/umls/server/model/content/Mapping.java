/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.RelationshipType;

/**
 * Represents a mapping between a from object and a to object.
 */
public interface Mapping extends ComponentHasAttributes {

  /**
   * Gets the from id type.
   *
   * @return the from id type
   */
  public IdType getFromIdType();

  /**
   * Sets the from id type.
   *
   * @param fromIdType the new from id type
   */
  public void setFromIdType(IdType fromIdType);

  /**
   * Gets the to id type.
   *
   * @return the to id type
   */
  public IdType getToIdType();

  /**
   * Sets the to id type.
   *
   * @param toIdType the new to id type
   */
  public void setToIdType(IdType toIdType);

  /**
   * Gets the from terminology id.
   *
   * @return the from terminology id
   */
  public String getFromTerminologyId();

  /**
   * Sets the from terminology id.
   *
   * @param id the new from terminology id
   */
  public void setFromTerminologyId(String id);

  /**
   * Gets the to terminology id.
   *
   * @return the to terminology id
   */
  public String getToTerminologyId();

  /**
   * Sets the to terminology id.
   *
   * @param id the new to terminology id
   */
  public void setToTerminologyId(String id);

  /**
   * Gets the relationship type.
   *
   * @return the relationship type
   */
  public String getRelationshipType();

  /**
   * Sets the relationship type.
   *
   * @param relType the new relationship type
   */
  public void setRelationshipType(String relType);

  /**
   * Gets the additional relationship type.
   *
   * @return the additional relationship type
   */
  public String getAdditionalRelationshipType();

  /**
   * Sets the additional relationship type.
   *
   * @param addRelType the new additional relationship type
   */
  public void setAdditionalRelationshipType(
    String addRelType);

  /**
   * Returns the rank.
   * 
   * @return the rank
   */
  public String getRank();

  /**
   * Sets the rank.
   * 
   * @param rank the rank
   */
  public void setRank(String rank);

  /**
   * Gets the group.
   *
   * @return the group
   */
  public String getGroup();

  /**
   * Sets the group.
   *
   * @param group the new group
   */
  public void setGroup(String group);

  /**
   * Returns the rule.
   * 
   * @return the rule
   */
  public String getRule();

  /**
   * Sets the rule.
   * 
   * @param rule the rule
   */
  public void setRule(String rule);

  /**
   * Gets the advice.
   *
   * @return the advice
   */
  public String getAdvice();

  /**
   * Sets the advice.
   *
   * @param advice the new advice
   */
  public void setAdvice(String advice);

  /**
   * Returns the map set.
   * 
   * @return the map set
   */
  public MapSet getMapSet();

  /**
   * Sets the map set.
   * 
   * @param mapSet the map set
   */
  public void setMapSet(MapSet mapSet);

}
