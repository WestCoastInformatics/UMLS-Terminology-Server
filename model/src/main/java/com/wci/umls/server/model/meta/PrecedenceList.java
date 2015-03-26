package com.wci.umls.server.model.meta;

import java.io.Serializable;
import java.util.List;

/**
 * Represents an ordered list of {@link TermType}s for use in computing
 * atom ranks.
 * 
 * @author Brian Carlsen (brian.a.carlsen@lmco.com)
 */
public interface PrecedenceList extends Serializable {

  /**
   * Returns the name.
   * 
   * @return the name
   */
  public String getName();

  /**
   * Sets the name.
   * 
   * @param name the name
   */
  public void setName(String name);

  /**
   * Sets the source term types.
   * 
   * @param precedence the source term types
   */
  public void setTermTypes(List<TermType> precedence);

  /**
   * Returns the source term types.
   * 
   * @return the source term types
   */
  public List<TermType> getTermTypes();
}
