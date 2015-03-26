package com.wci.umls.server.model.meta;

/**
 * Relations between semantic network relation labels.
 */
public interface SemanticNetworkRelationTypeRelation {

  /**
   * Returns the semantic network relation type.
   * 
   * @return the semantic network relation type
   */
  public SemanticNetworkRelationType getSemanticNetworkRelationType();

  /**
   * Sets the semantic network relation type.
   * 
   * @param semanticNetworkRelationType the semantic network relation label
   */
  public void setSemanticNetworkRelationType(
    SemanticNetworkRelationType semanticNetworkRelationType);

  /**
   * Returns the related semantic network relation type.
   * 
   * @return the related semantic network relation type
   */
  public SemanticNetworkRelationType getRelatedSemanticNetworkRelationType();

  /**
   * Sets the related semantic network relation type.
   * 
   * @param relatedSemanticNetworkRelationType the related semantic network
   *          relation type
   */
  public void setRelatedSemanticNetworkRelationType(
    SemanticNetworkRelationType relatedSemanticNetworkRelationType);

  /**
   * Returns the Relation type.
   * 
   * @return the Relation type
   */
  public SemanticNetworkRelationType getRelationType();

  /**
   * Sets the Relation type.
   * 
   * @param relationType the relation type
   */
  public void setRelationType(SemanticNetworkRelationType relationType);

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
