/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.jpa.meta;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.meta.ContactInfo;
import com.wci.umls.server.model.meta.Language;
import com.wci.umls.server.model.meta.RootTerminology;

/**
 * JPA-enabled implementation of {@link RootTerminology}.
 */
@Entity
@Table(name = "root_terminologies", uniqueConstraints = @UniqueConstraint(columnNames = {
  "terminology"
}))
@Audited
@XmlRootElement(name = "rootTerminology")
public class RootTerminologyJpa extends AbstractHasLastModified implements
    RootTerminology {

  /** The terminology. */
  @Column(nullable = false)
  private String terminology;

  /** The acquisition contact. */
  @OneToOne(targetEntity = ContactInfoJpa.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = true)
  private ContactInfo acquisitionContact;

  /** The content contact. */
  @OneToOne(targetEntity = ContactInfoJpa.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = true)
  private ContactInfo contentContact;

  /** The polyhierarchy flag. */
  @Column(nullable = false)
  private boolean polyhierarchy;

  /** The family. */
  @Column(nullable = false)
  private String family;

  /** The hierarchical name. */
  @Column(nullable = true, length = 3000)
  private String hierarchicalName;

  /** The language. */
  @ManyToOne(targetEntity = LanguageJpa.class, fetch = FetchType.EAGER, optional = false)
  @JoinColumn(nullable = false)
  private Language language;

  /** The license contact. */
  @OneToOne(targetEntity = ContactInfoJpa.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = true)
  private ContactInfo licenseContact;

  /** The preferred name. */
  @Column(nullable = false, length = 3000)
  private String preferredName;

  /** The restriction level. */
  @Column(nullable = false)
  private int restrictionLevel;

  /** The short name. */
  @Column(nullable = true, length = 3000)
  private String shortName;

  /** The short name. */
  @ElementCollection
  @Column(nullable = true)
  private List<String> synonymousNames = new ArrayList<>();

  /**
   * Instantiates an empty {@link RootTerminologyJpa}.
   */
  public RootTerminologyJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link RootTerminologyJpa} from the specified parameters.
   *
   * @param rootTerminology the terminology
   */
  public RootTerminologyJpa(RootTerminology rootTerminology) {
    super(rootTerminology);
    terminology = rootTerminology.getTerminology();
    acquisitionContact = rootTerminology.getAcquisitionContact();
    contentContact = rootTerminology.getContentContact();
    family = rootTerminology.getFamily();
    hierarchicalName = rootTerminology.getHierarchicalName();
    language = rootTerminology.getLanguage();
    licenseContact = rootTerminology.getLicenseContact();
    preferredName = rootTerminology.getPreferredName();
    restrictionLevel = rootTerminology.getRestrictionLevel();
    shortName = rootTerminology.getShortName();
    synonymousNames = rootTerminology.getSynonymousNames();
    polyhierarchy = rootTerminology.isPolyhierachy();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.RootTerminology#getTerminology()
   */
  @Override
  public String getTerminology() {
    return terminology;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.RootTerminology#setTerminology(java.lang
   * .String)
   */
  @Override
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.RootTerminology#getAcquisitionContact()
   */
  @Override
  @XmlElement(type = ContactInfoJpa.class, name = "acquisitionContact")
  public ContactInfo getAcquisitionContact() {
    return acquisitionContact;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.RootTerminology#setAcquisitionContact(com
   * .wci.umls.server.model.meta.ContactInfo)
   */
  @Override
  public void setAcquisitionContact(ContactInfo acquisitionContact) {
    this.acquisitionContact = acquisitionContact;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.RootTerminology#getContentContact()
   */
  @Override
  @XmlElement(type = ContactInfoJpa.class, name = "contentContact")
  public ContactInfo getContentContact() {
    return contentContact;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.RootTerminology#setContentContact(com.wci
   * .umls.server.model.meta.ContactInfo)
   */
  @Override
  public void setContentContact(ContactInfo contentContact) {
    this.contentContact = contentContact;
  }

  /**
   * Indicates whether or not polyhierarchy is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isPolyhierarchy() {
    return polyhierarchy;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.RootTerminology#setPolyhierarchy(boolean)
   */
  @Override
  public void setPolyhierarchy(boolean polyhierarchy) {
    this.polyhierarchy = polyhierarchy;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.RootTerminology#getFamily()
   */
  @Override
  public String getFamily() {
    return family;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.RootTerminology#setFamily(java.lang.String)
   */
  @Override
  public void setFamily(String family) {
    this.family = family;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.RootTerminology#getHierarchicalName()
   */
  @Override
  public String getHierarchicalName() {
    return hierarchicalName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.RootTerminology#setHierarchicalName(java
   * .lang.String)
   */
  @Override
  public void setHierarchicalName(String hierarchicalName) {
    this.hierarchicalName = hierarchicalName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasLanguage#getLanguage()
   */
  @Override
  @XmlElement(type = LanguageJpa.class, name = "language")
  public Language getLanguage() {
    return language;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.HasLanguage#setLanguage(com.wci.umls.server
   * .model.meta.Language)
   */
  @Override
  public void setLanguage(Language language) {
    this.language = language;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.RootTerminology#getLicenseContact()
   */
  @Override
  @XmlElement(type = ContactInfoJpa.class, name = "licenseContact")
  public ContactInfo getLicenseContact() {
    return licenseContact;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.RootTerminology#setLicenseContact(com.wci
   * .umls.server.model.meta.ContactInfo)
   */
  @Override
  public void setLicenseContact(ContactInfo licenseContact) {
    this.licenseContact = licenseContact;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.RootTerminology#getPreferredName()
   */
  @Override
  public String getPreferredName() {
    return preferredName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.RootTerminology#setPreferredName(java.lang
   * .String)
   */
  @Override
  public void setPreferredName(String preferredName) {
    this.preferredName = preferredName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.RootTerminology#getRestrictionLevel()
   */
  @Override
  public int getRestrictionLevel() {
    return restrictionLevel;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.RootTerminology#setRestrictionLevel(int)
   */
  @Override
  public void setRestrictionLevel(int restrictionLevel) {
    this.restrictionLevel = restrictionLevel;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.RootTerminology#getShortName()
   */
  @Override
  public String getShortName() {
    return shortName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.RootTerminology#setShortName(java.lang.String
   * )
   */
  @Override
  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.RootTerminology#getSynonymousNames()
   */
  @Override
  @XmlElement(type = String.class, name = "syName")
  public List<String> getSynonymousNames() {
    return synonymousNames;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.RootTerminology#setSynonymousNames(java.
   * util.List)
   */
  @Override
  public void setSynonymousNames(List<String> synonymousNames) {
    this.synonymousNames = synonymousNames;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.RootTerminology#isPolyhierachy()
   */
  @Override
  public boolean isPolyhierachy() {
    return polyhierarchy;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime
            * result
            + ((acquisitionContact == null) ? 0 : acquisitionContact.hashCode());
    result =
        prime * result
            + ((contentContact == null) ? 0 : contentContact.hashCode());
    result = prime * result + ((family == null) ? 0 : family.hashCode());
    result =
        prime * result
            + ((hierarchicalName == null) ? 0 : hierarchicalName.hashCode());
    result = prime * result + ((language == null) ? 0 : language.hashCode());
    result =
        prime * result
            + ((licenseContact == null) ? 0 : licenseContact.hashCode());
    result = prime * result + (polyhierarchy ? 1231 : 1237);
    result =
        prime * result
            + ((preferredName == null) ? 0 : preferredName.hashCode());
    result = prime * result + restrictionLevel;
    result = prime * result + ((shortName == null) ? 0 : shortName.hashCode());
    result =
        prime * result
            + ((synonymousNames == null) ? 0 : synonymousNames.hashCode());
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RootTerminologyJpa other = (RootTerminologyJpa) obj;
    if (acquisitionContact == null) {
      if (other.acquisitionContact != null)
        return false;
    } else if (!acquisitionContact.equals(other.acquisitionContact))
      return false;
    if (contentContact == null) {
      if (other.contentContact != null)
        return false;
    } else if (!contentContact.equals(other.contentContact))
      return false;
    if (family == null) {
      if (other.family != null)
        return false;
    } else if (!family.equals(other.family))
      return false;
    if (hierarchicalName == null) {
      if (other.hierarchicalName != null)
        return false;
    } else if (!hierarchicalName.equals(other.hierarchicalName))
      return false;
    if (language == null) {
      if (other.language != null)
        return false;
    } else if (!language.equals(other.language))
      return false;
    if (licenseContact == null) {
      if (other.licenseContact != null)
        return false;
    } else if (!licenseContact.equals(other.licenseContact))
      return false;
    if (polyhierarchy != other.polyhierarchy)
      return false;
    if (preferredName == null) {
      if (other.preferredName != null)
        return false;
    } else if (!preferredName.equals(other.preferredName))
      return false;
    if (restrictionLevel != other.restrictionLevel)
      return false;
    if (shortName == null) {
      if (other.shortName != null)
        return false;
    } else if (!shortName.equals(other.shortName))
      return false;
    if (synonymousNames == null) {
      if (other.synonymousNames != null)
        return false;
    } else if (!synonymousNames.equals(other.synonymousNames))
      return false;
    if (terminology == null) {
      if (other.terminology != null)
        return false;
    } else if (!terminology.equals(other.terminology))
      return false;
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "RootTerminologyJpa [terminology=" + terminology
        + ", acquisitionContact=" + acquisitionContact + ", contentContact="
        + contentContact + ", polyhierarchy=" + polyhierarchy + ", family="
        + family + ", hierarchicalName=" + hierarchicalName + ", language="
        + language + ", licenseContact=" + licenseContact + ", preferredName="
        + preferredName + ", restrictionLevel=" + restrictionLevel
        + ", shortName=" + shortName + ", synonymousNames=" + synonymousNames
        + "]";
  }

}
