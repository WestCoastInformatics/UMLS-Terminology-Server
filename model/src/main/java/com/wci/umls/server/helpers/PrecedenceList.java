package com.wci.umls.server.helpers;

import java.util.List;

import com.wci.umls.server.model.meta.TermType;

/**
 * Represents an ordered list of {@link TermType}s for use in computing atom
 * ranks.
 */
public interface PrecedenceList {

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
