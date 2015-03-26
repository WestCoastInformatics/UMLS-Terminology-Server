package com.wci.umls.server.model.meta;

import java.util.List;

/**
 * Represents a semantic type from the semantic network.
 */
public interface SemanticType extends Comparable<SemanticType> {

  /**
   * Returns the abbreviation.
   * 
   * @return the abbreviation
   */
  public String getAbbreviation();

  /**
   * Sets the abbreviation.
   * 
   * @param abbreviation the abbreviation
   */
  public void setAbbreviation(String abbreviation);

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
  public String getUi();

  /**
   * Sets the unique identifier.
   * 
   * @param ui the unique identifier
   */
  public void setUi(String ui);

  /**
   * Returns the non human.
   * 
   * @return the non human
   */
  public String getNonHuman();

  /**
   * Sets the non human.
   * 
   * @param nonHuman the non human
   */
  public void setNonHuman(String nonHuman);

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

  /**
   * Returns the semantic type group.
   * 
   * @return the semantic type group
   */
  public SemanticTypeGroup getSemanticTypeGroup();

  /**
   * Sets the semantic type group.
   * 
   * @param group the semantic type group
   */
  public void setSemanticTypeGroup(SemanticTypeGroup group);

  /**
   * Returns the semantic type relations.
   * 
   * @return the semantic type relations
   */
  public List<SemanticTypeRelation> getRelations();

  /**
   * Returns the inverse semantic type relations.
   * 
   * @return the inverse semantic type relations
   */
  public List<SemanticTypeRelation> getInverseRelations();

  /**
   * Adds the relation.
   * 
   * @param rel the relation
   */
  public void addRelation(SemanticTypeRelation rel);

  /**
   * Removes the relation.
   * 
   * @param rel the relation
   */
  public void removeRelation(SemanticTypeRelation rel);

  /**
   * Adds the inverse relation.
   * 
   * @param rel the inverse relation
   */
  public void addInverseRelation(SemanticTypeRelation rel);

  /**
   * Removes the inverse relation.
   * 
   * @param rel the inverse relation
   */
  public void removeInverseRelation(SemanticTypeRelation rel);

  /**
   * Sets the semantic type relations.
   * 
   * @param relations the semantic type relations
   */
  public void setRelations(List<SemanticTypeRelation> relations);

  /**
   * Sets the inverse semantic type relations.
   * 
   * @param inverseRelations the inverse semantic type relations
   */
  public void setInverseRelations(List<SemanticTypeRelation> inverseRelations);

  /**
   * Returns the semantic type parents.
   * 
   * @return the semantic type parents
   */
  public List<SemanticType> getParents();

  /**
   * Returns the semantic type children.
   * 
   * @return the semantic type children
   */
  public List<SemanticType> getChildren();

  /**
   * Returns the semantic type descendants.
   * 
   * @return the semantic type descendants
   */
  public List<SemanticType> getDescendants();

  /**
   * Returns the child count. This is a computable shorthand for knowing whether
   * this node of a tree has child nodes.
   * 
   * @return the child count
   */
  public int getChildCount();

  /**
   * Sets the child count. This is a computable shorthand for knowing whether
   * this node of a tree has child nodes.
   * 
   * @param ct the child count
   */
  public void setChildCount(int ct);

}
