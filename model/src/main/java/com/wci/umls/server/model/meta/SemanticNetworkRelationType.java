package com.wci.umls.server.model.meta;

import java.util.List;

/**
 * Represents a semantic network relation (like "isa").
 */
public interface SemanticNetworkRelationType {

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
   * Returns the inverse label.
   * 
   * @return the inverse label
   */
  public String getInverseLabel();

  /**
   * Sets the inverse label.
   * 
   * @param inverse the inverse label
   */
  public void setInverseLabel(String inverse);

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
   * Returns the semantic network relation type relations.
   * 
   * @return the semantic network relation type relations
   */
  public List<SemanticNetworkRelationTypeRelation> getRelations();

  /**
   * Returns the inverse semantic network relation type relations.
   * 
   * @return the inverse semantic network relation type relations
   */
  public List<SemanticNetworkRelationTypeRelation> getInverseRelations();

  /**
   * Adds the relation.
   * 
   * @param rel the relation
   */
  public void addRelation(SemanticNetworkRelationTypeRelation rel);

  /**
   * Removes the relation.
   * 
   * @param rel the relation
   */
  public void removeRelation(SemanticNetworkRelationTypeRelation rel);

  /**
   * Adds the inverse relation.
   * 
   * @param rel the inverse relation
   */
  public void addInverseRelation(SemanticNetworkRelationTypeRelation rel);

  /**
   * Removes the inverse relation.
   * 
   * @param rel the inverse relation
   */
  public void removeInverseRelation(SemanticNetworkRelationTypeRelation rel);

  /**
   * Sets the semantic network relation type relations.
   * 
   * @param relations the semantic network relation type relations
   */
  public void setRelations(List<SemanticNetworkRelationTypeRelation> relations);

  /**
   * Sets the inverse semantic network relation type relations.
   * 
   * @param inverseRelations the inverse semantic network relation type
   *          relations
   */
  public void setInverseRelations(
    List<SemanticNetworkRelationTypeRelation> inverseRelations);

  /**
   * Returns the semantic network relation type parents.
   * 
   * @return the semantic network relation type parents
   */
  public List<SemanticNetworkRelationType> getParents();

  /**
   * Returns the semantic network relation type children.
   * 
   * @return the semantic network relation type children
   */
  public List<SemanticNetworkRelationType> getChildren();

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
