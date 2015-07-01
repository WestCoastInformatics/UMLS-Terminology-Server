/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

/**
 * Represents a mapping between two {@link MapObject}s.
 */
public interface Mapping extends ComponentHasAttributes {

  /**
   * Returns the label.
   * 
   * @return the label
   */
  public String getLabel();

  /**
   * Sets the label.
   * 
   * @param label the label
   */
  public void setLabel(String label);

  /**
   * Returns the additional label.
   * 
   * @return the additional label
   */
  public String getAdditionalLabel();

  /**
   * Sets the additional label.
   * 
   * @param additionalLabel the additional label
   */
  public void setAdditionalLabel(String additionalLabel);

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
   * Returns the subset identifier.
   * 
   * @return the subset identifier
   */
  public String getSubsetIdentifier();

  /**
   * Sets the subset identifier.
   * 
   * @param subsetId the subset identifier
   */
  public void setSubsetIdentifier(String subsetId);

  /**
   * Returns the type.
   * 
   * @return the type
   */
  public String getType();

  /**
   * Sets the type.
   * 
   * @param type the type
   */
  public void setType(String type);

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
   * Returns the restriction.
   * 
   * @return the restriction
   */
  public String getRestriction();

  /**
   * Sets the restriction.
   * 
   * @param restriction the restriction
   */
  public void setRestriction(String restriction);

  /**
   * Returns the map from.
   * 
   * @return the map from
   */
  public MapObject getMapFrom();

  /**
   * Sets the map from.
   * 
   * @param from the map from
   */
  public void setMapFrom(MapObject from);

  /**
   * Returns the map to.
   * 
   * @return the map to
   */
  public MapObject getMapTo();

  /**
   * Sets the map to.
   * 
   * @param to the map to
   */
  public void setMapTo(MapObject to);

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
