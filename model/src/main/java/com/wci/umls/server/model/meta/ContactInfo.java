/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

/**
 * Represents contact information for a person or organization.
 */
public interface ContactInfo {

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
   * Returns the value. Used for legacy data where contact information fields
   * are not normalized or structured.
   * 
   * @return the value
   */
  public String getValue();

  /**
   * Sets the value. Used for legacy data where contact information fields are
   * not normalized or structured.
   * 
   * @param value the value
   */
  public void setValue(String value);

  /**
   * Returns the address1.
   * 
   * @return the address1
   */
  public String getAddress1();

  /**
   * Sets the address1.
   * 
   * @param address1 the address1
   */
  public void setAddress1(String address1);

  /**
   * Returns the address2.
   * 
   * @return the address2
   */
  public String getAddress2();

  /**
   * Sets the address2.
   * 
   * @param address2 the address2
   */
  public void setAddress2(String address2);

  /**
   * Returns the city.
   * 
   * @return the city
   */
  public String getCity();

  /**
   * Sets the city.
   * 
   * @param city the city
   */
  public void setCity(String city);

  /**
   * Returns the country.
   * 
   * @return the country
   */
  public String getCountry();

  /**
   * Sets the country.
   * 
   * @param country the country
   */
  public void setCountry(String country);

  /**
   * Returns the email.
   * 
   * @return the email
   */
  public String getEmail();

  /**
   * Sets the email.
   * 
   * @param email the email
   */
  public void setEmail(String email);

  /**
   * Returns the fax.
   * 
   * @return the fax
   */
  public String getFax();

  /**
   * Sets the fax.
   * 
   * @param fax the fax
   */
  public void setFax(String fax);

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
   * Returns the organization.
   * 
   * @return the organization
   */
  public String getOrganization();

  /**
   * Sets the organization.
   * 
   * @param organization the organization
   */
  public void setOrganization(String organization);

  /**
   * Returns the state or province.
   * 
   * @return the state or province
   */
  public String getStateOrProvince();

  /**
   * Sets the state or province.
   * 
   * @param stateOrProvince the state or province
   */
  public void setStateOrProvince(String stateOrProvince);

  /**
   * Returns the telephone.
   * 
   * @return the telephone
   */
  public String getTelephone();

  /**
   * Sets the telephone.
   * 
   * @param telephone the telephone
   */
  public void setTelephone(String telephone);

  /**
   * Returns the title.
   * 
   * @return the title
   */
  public String getTitle();

  /**
   * Sets the title.
   * 
   * @param title the title
   */
  public void setTitle(String title);

  /**
   * Returns the url.
   * 
   * @return the url
   */
  public String getUrl();

  /**
   * Sets the url.
   * 
   * @param url the url
   */
  public void setUrl(String url);

  /**
   * Returns the zip code.
   * 
   * @return the zip code
   */
  public String getZipCode();

  /**
   * Sets the zip code.
   * 
   * @param zipCode the zip code
   */
  public void setZipCode(String zipCode);

}