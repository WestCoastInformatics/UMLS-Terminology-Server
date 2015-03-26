package com.wci.umls.server.model.meta;

/**
 * Represents a piece of data with an abbreviation and an expanded form.
 */
public interface Abbreviation {

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
