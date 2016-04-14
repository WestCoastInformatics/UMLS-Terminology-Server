/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

/**
 * Represents a language without a locale.
 */
public interface Language extends Abbreviation {

  /**
   * Returns the ISO code.
   * 
   * @return the ISO code
   */
  public String getISOCode();

  /**
   * Sets the ISO code.
   *
   * @param isoCode the ISO code
   */
  public void setISOCode(String isoCode);

  /**
   * Returns the iS o3 code.
   * 
   * @return the iS o3 code
   */
  public String getISO3Code();

  /**
   * Sets the ISO 3 code.
   *
   * @param iso3Code the IS o3 code
   */
  public void setISO3Code(String iso3Code);

}
