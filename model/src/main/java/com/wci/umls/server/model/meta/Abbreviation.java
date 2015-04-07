package com.wci.umls.server.model.meta;

/**
 * Represents a piece of data with an abbreviation and an expanded form.
 */
public interface Abbreviation {

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
   * Returns the abbreviation.
   * 
   * @return the abbreviation
   */
  public String getAbbreviation();

  /**
   * Returns the expanded form.
   * 
   * @return the expanded form
   */
  public String getExpandedForm();

  /**
   * Sets the abbreviation.
   * 
   * @param abbreviation the abbreviation
   */
  public void setAbbreviation(String abbreviation);

  /**
   * Sets the expanded form.
   * 
   * @param expandedForm the expanded form
   */
  public void setExpandedForm(String expandedForm);
}
