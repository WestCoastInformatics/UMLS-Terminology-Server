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
  @ManyToOne(targetEntity = LanguageJpa.class, fetch = FetchType.EAGER, optional = true)
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
    polyhierarchy = rootTerminology.isPolyhierarchy();
  }

  /* see superclass */
  @Override
  public String getTerminology() {
    return terminology;
  }

  /* see superclass */
  @Override
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  /* see superclass */
  @Override
  @XmlElement(type = ContactInfoJpa.class, name = "acquisitionContact")
  public ContactInfo getAcquisitionContact() {
    return acquisitionContact;
  }

  /* see superclass */
  @Override
  public void setAcquisitionContact(ContactInfo acquisitionContact) {
    this.acquisitionContact = acquisitionContact;
  }

  /* see superclass */
  @Override
  @XmlElement(type = ContactInfoJpa.class, name = "contentContact")
  public ContactInfo getContentContact() {
    return contentContact;
  }

  /* see superclass */
  @Override
  public void setContentContact(ContactInfo contentContact) {
    this.contentContact = contentContact;
  }

  /* see superclass */
  @Override
  public void setPolyhierarchy(boolean polyhierarchy) {
    this.polyhierarchy = polyhierarchy;
  }

  /* see superclass */
  @Override
  public String getFamily() {
    return family;
  }

  /* see superclass */
  @Override
  public void setFamily(String family) {
    this.family = family;
  }

  /* see superclass */
  @Override
  public String getHierarchicalName() {
    return hierarchicalName;
  }

  /* see superclass */
  @Override
  public void setHierarchicalName(String hierarchicalName) {
    this.hierarchicalName = hierarchicalName;
  }

  /* see superclass */
  @Override
  @XmlElement(type = LanguageJpa.class, name = "language")
  public Language getLanguage() {
    return language;
  }

  /* see superclass */
  @Override
  public void setLanguage(Language language) {
    this.language = language;
  }

  /* see superclass */
  @Override
  @XmlElement(type = ContactInfoJpa.class, name = "licenseContact")
  public ContactInfo getLicenseContact() {
    return licenseContact;
  }

  /* see superclass */
  @Override
  public void setLicenseContact(ContactInfo licenseContact) {
    this.licenseContact = licenseContact;
  }

  /* see superclass */
  @Override
  public String getPreferredName() {
    return preferredName;
  }

  /* see superclass */
  @Override
  public void setPreferredName(String preferredName) {
    this.preferredName = preferredName;
  }

  /* see superclass */
  @Override
  public int getRestrictionLevel() {
    return restrictionLevel;
  }

  /* see superclass */
  @Override
  public void setRestrictionLevel(int restrictionLevel) {
    this.restrictionLevel = restrictionLevel;
  }

  /* see superclass */
  @Override
  public String getShortName() {
    return shortName;
  }

  /* see superclass */
  @Override
  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  /* see superclass */
  @Override
  @XmlElement(type = String.class, name = "syName")
  public List<String> getSynonymousNames() {
    if (synonymousNames == null) {
      synonymousNames = new ArrayList<>();
    }
    return synonymousNames;
  }

  /* see superclass */
  @Override
  public void setSynonymousNames(List<String> synonymousNames) {
    this.synonymousNames = synonymousNames;
  }

  /* see superclass */
  @Override
  public boolean isPolyhierarchy() {
    return polyhierarchy;
  }

  /* see superclass */
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

  /* see superclass */
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

  /* see superclass */
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
