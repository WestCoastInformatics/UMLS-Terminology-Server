package com.wci.umls.server.model.meta;

/**
 * Represents a language without a locale.
 */
public interface Language extends Abbreviation {

  /**
   * Returns the iSO code.
   * 
   * @return the iSO code
   */
  public String getISOCode();

  /**
   * Returns the iS o3 code.
   * 
   * @return the iS o3 code
   */
  public String getISO3Code();

}
