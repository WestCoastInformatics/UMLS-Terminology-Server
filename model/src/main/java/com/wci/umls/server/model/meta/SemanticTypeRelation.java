package com.wci.umls.server.model.meta;

/**
 * Relations between semantic types.
 */
public interface SemanticTypeRelation {

  /**
   * Returns the semantic type.
   * 
   * @return the semantic type
   */
  public SemanticType getSemanticType();

  /**
   * Sets the semantic type.
   * 
   * @param semanticType the semantic type
   */
  public void setSemanticType(SemanticType semanticType);

  /**
   * Returns the related semantic type.
   * 
   * @return the related semantic type
   */
  public SemanticType getRelatedSemanticType();

  /**
   * Sets the related semantic type.
   * 
   * @param relatedSemanticType the related semantic type
   */
  public void setRelatedSemanticType(SemanticType relatedSemanticType);

  /**
   * Returns the Relation type.
   * 
   * @return the Relation type
   */
  public SemanticNetworkRelationType getRelationType();

  /**
   * Sets the Relation type.
   * 
   * @param RelationType the Relation type
   */
  public void setRelationType(SemanticNetworkRelationType RelationType);

  /**
   * Indicates whether or not defined is the case.
   * 
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isDefined();

  /**
   * Sets the defined.
   * 
   * @param defined the defined
   */
  public void setDefined(boolean defined);

  /**
   * Indicates whether or not blocked is the case.
   * 
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isBlocked();

  /**
   * Sets the blocked.
   * 
   * @param blocked the blocked
   */
  public void setBlocked(boolean blocked);

  /**
   * Indicates whether or not inherited is the case.
   * 
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isInherited();

  /**
   * Sets the inherited.
   * 
   * @param inherited the inherited
   */
  public void setInherited(boolean inherited);

  /**
   * Returns the id.
   * 
   * @return the id
   */
  public String getId();
}
