/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

/**
 * Represents a semantic type from the semantic network.
 */
public interface SemanticType extends Abbreviation {

  /**
   * Returns the value.
   * 
   * @return the value
   */
  public String getValue();

  /**
   * Sets the value.
   * 
   * @param value the value
   */
  public void setValue(String value);

  /**
   * Returns the definition.
   * 
   * @return the definition
   */
  public String getDefinition();

  /**
   * Sets the definition.
   * 
   * @param definition the definition
   */
  public void setDefinition(String definition);

  /**
   * Returns the example.
   * 
   * @return the example
   */
  public String getExample();

  /**
   * Sets the example.
   * 
   * @param example the example
   */
  public void setExample(String example);

  /**
   * Returns the unique identifier.
   * 
   * @return the unique identifier
   */
  public String getTypeId();

  /**
   * Sets the unique identifier.
   * 
   * @param ui the unique identifier
   */
  public void setTypeId(String ui);

  /**
   * Returns the non human flag.
   * 
   * @return the non human flag
   */
  public boolean isNonHuman();

  /**
   * Sets the non human flag.
   * 
   * @param nonHuman the non human flag
   */
  public void setNonHuman(boolean nonHuman);

  /**
   * Returns the tree number.
   * 
   * @return the tree number
   */
  public String getTreeNumber();

  /**
   * Sets the tree number.
   * 
   * @param treeNumber the tree number
   */
  public void setTreeNumber(String treeNumber);

  /**
   * Returns the usage note.
   * 
   * @return the usage note
   */
  public String getUsageNote();

  /**
   * Sets the usage note.
   * 
   * @param usageNote the usage note
   */
  public void setUsageNote(String usageNote);

}
