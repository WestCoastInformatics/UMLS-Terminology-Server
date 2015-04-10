/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

import java.util.List;

import com.wci.umls.server.model.meta.IdentifierType;

/**
 * Represents one end of a {@link Mapping}.
 */
public interface MapObject extends Component {

  /**
   * Returns the expression.
   * 
   * @return the expression
   */
  public String getExpression();

  /**
   * Sets the expression.
   * 
   * @param expression the expression
   */
  public void setExpression(String expression);

  /**
   * Returns the type.
   * 
   * @return the type
   */
  public IdentifierType getType();

  /**
   * Sets the type.
   * 
   * @param type the type
   */
  public void setType(IdentifierType type);

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
   * Returns the to mappings.
   * 
   * @return the to mappings
   */
  public List<Mapping> getToMappings();

  /**
   * Sets the to mappings.
   * 
   * @param toMappings the to mappings
   */
  public void setToMappings(List<Mapping> toMappings);

  /**
   * Adds the to mapping.
   * 
   * @param mapping the mapping
   */
  public void addToMapping(Mapping mapping);

  /**
   * Returns the from mappings.
   * 
   * @return the from mappings
   */
  public List<Mapping> getFromMappings();

  /**
   * Sets the from mappings.
   * 
   * @param fromMappings the from mappings
   */
  public void setFromMappings(List<Mapping> fromMappings);

  /**
   * Adds the from mappings.
   * 
   * @param mapping the mapping
   */
  public void addFromMapping(Mapping mapping);

}
