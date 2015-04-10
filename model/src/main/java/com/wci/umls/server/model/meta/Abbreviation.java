package com.wci.umls.server.model.meta;

import com.wci.umls.server.helpers.HasId;
import com.wci.umls.server.helpers.HasTerminology;

/**
 * Represents a piece of data with an abbreviation and an expanded form.
 */
public interface Abbreviation extends HasTerminology, HasId {

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
