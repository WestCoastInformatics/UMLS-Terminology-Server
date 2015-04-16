/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

import java.util.List;

import com.wci.umls.server.helpers.HasLanguage;
import com.wci.umls.server.helpers.HasLastModified;

/**
 * Represents a {@link Terminology} of data independent of version information.
 */
public interface RootTerminology extends HasLanguage, HasLastModified {

  /**
   * Returns the terminology.
   * 
   * @return the terminology
   */
  public String getTerminology();

  /**
   * Sets the terminology.
   * 
   * @param terminology the terminology
   */
  public void setTerminology(String terminology);

  /**
   * Returns the family.
   * 
   * @return the family
   */
  public String getFamily();

  /**
   * Sets the family.
   * 
   * @param family the family
   */
  public void setFamily(String family);

  /**
   * Returns the restriction level.
   * 
   * @return the restriction level
   */
  public int getRestrictionLevel();

  /**
   * Sets the restriction level.
   * 
   * @param srl the restriction level
   */
  public void setRestrictionLevel(int srl);

  /**
   * Indicates whether or not polyhierachy is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isPolyhierarchy();

  /**
   * Sets the polyhierarchy flag.
   *
   * @param polyhierarchy the polyhierarchy flag
   */
  public void setPolyhierarchy(boolean polyhierarchy);

  /**
   * Returns the acquisition contact.
   * 
   * @return the acquisition contact
   */
  public ContactInfo getAcquisitionContact();

  /**
   * Sets the acquisition contact.
   * 
   * @param acquisitionContact the acquisition contact
   */
  public void setAcquisitionContact(ContactInfo acquisitionContact);

  /**
   * Returns the content contact.
   * 
   * @return the content contact
   */
  public ContactInfo getContentContact();

  /**
   * Sets the content contact.
   * 
   * @param contentContact the content contact
   */
  public void setContentContact(ContactInfo contentContact);

  /**
   * Returns the license contact.
   * 
   * @return the license contact
   */
  public ContactInfo getLicenseContact();

  /**
   * Sets the license contact.
   * 
   * @param licenseContact the license contact
   */
  public void setLicenseContact(ContactInfo licenseContact);

  /**
   * Returns the preferred name.
   * 
   * @return the preferred name
   */
  public String getPreferredName();

  /**
   * Sets the preferred name.
   * 
   * @param preferredName the preferred name
   */
  public void setPreferredName(String preferredName);

  /**
   * Returns the synonymous names.
   * 
   * @return the synonymous names
   */
  public List<String> getSynonymousNames();

  /**
   * Sets the synonymous names.
   * 
   * @param synonymousNames the synonymous names
   */
  public void setSynonymousNames(List<String> synonymousNames);

  /**
   * Returns the short name.
   * 
   * @return the short name
   */
  public String getShortName();

  /**
   * Sets the short name.
   * 
   * @param shortName the short name
   */
  public void setShortName(String shortName);

  /**
   * Returns the hierarchical name.
   * 
   * @return the hierarchical name
   */
  public String getHierarchicalName();

  /**
   * Sets the hierarchical name.
   * 
   * @param hierarchicalName the hierarchical name
   */
  public void setHierarchicalName(String hierarchicalName);

}