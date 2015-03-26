package com.wci.umls.server.model.meta;

import java.util.List;

import com.wci.umls.server.helpers.HasLanguage;

/**
 * Represents a {@link Terminology} of data independent of version information.
 */
public interface RootTerminology extends Abbreviation, HasLanguage {

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
   * Returns the context type.
   * 
   * @return the context type
   */
  public String getContextType();

  /**
   * Sets the context type.
   * 
   * @param cxty the context type
   */
  public void setContextType(String cxty);

  /**
   * Returns the acquisition contact.
   * 
   * @return the acquisition contact
   */
  public ContactInformation getAcquisitionContact();

  /**
   * Sets the acquisition contact.
   * 
   * @param acquisitionContact the acquisition contact
   */
  public void setAcquisitionContact(ContactInformation acquisitionContact);

  /**
   * Returns the content contact.
   * 
   * @return the content contact
   */
  public ContactInformation getContentContact();

  /**
   * Sets the content contact.
   * 
   * @param contentContact the content contact
   */
  public void setContentContact(ContactInformation contentContact);

  /**
   * Returns the license contact.
   * 
   * @return the license contact
   */
  public ContactInformation getLicenseContact();

  /**
   * Sets the license contact.
   * 
   * @param licenseContact the license contact
   */
  public void setLicenseContact(ContactInformation licenseContact);

  /**
   * Returns the sources.
   * 
   * @return the sources
   */
  public List<Terminology> getSources();

  /**
   * Sets the sources.
   * 
   * @param sources the sources
   */
  public void setSources(List<Terminology> sources);

  /**
   * Adds the source.
   * 
   * @param source the source
   */
  public void addSource(Terminology source);

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
   * Adds a synonymous name.
   * 
   * @param synonymousName a synonymous name
   */
  public void addSynonymousName(String synonymousName);

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

  /**
   * Returns the current version.
   * 
   * @return the current version
   */
  public Terminology getCurrentVersion();

  /**
   * Returns the previous version.
   * 
   * @return the previous version
   */
  public Terminology getPreviousVersion();

}