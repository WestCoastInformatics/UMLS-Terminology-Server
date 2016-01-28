/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.meta;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.meta.Citation;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.Terminology;

/**
 * JPA-enabled implementation of {@link Terminology}.
 */
@Entity
@Table(name = "terminologies", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminology", "version"
}))
@Audited
@XmlRootElement(name = "terminology")
public class TerminologyJpa extends AbstractHasLastModified implements
    Terminology {

  /** The terminology. */
  @Column(nullable = false)
  private String terminology;

  /** The citation. */
  @OneToOne(targetEntity = CitationJpa.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = true)
  private Citation citation;

  /** The end date. */
  @Column(nullable = true)
  @Temporal(TemporalType.TIMESTAMP)
  private Date endDate;

  /** The organizing class type. */
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private IdType organizingClassType;

  /** The preferred name. */
  @Column(nullable = false, length = 3000)
  private String preferredName;

  /** The root terminology. */
  @ManyToOne(targetEntity = RootTerminologyJpa.class, fetch = FetchType.EAGER, optional = false)
  @JoinColumn(nullable = false)
  private RootTerminology rootTerminology;

  /** The start date. */
  @Column(nullable = true)
  @Temporal(TemporalType.TIMESTAMP)
  private Date startDate;

  /** The synonymous names. */
  @ElementCollection
  private List<String> synonymousNames = new ArrayList<>();

  /** The terminology version. */
  @Column(nullable = false)
  private String version;

  /** The is asserts rel direction. */
  @Column(nullable = false)
  private boolean assertsRelDirection = false;

  /** The is current. */
  @Column(nullable = false)
  private boolean current = false;

  /** The metathesaurus flag. */
  @Column(nullable = false)
  private boolean metathesaurus = false;

  /** The flag indicating whether this is a DL terminology. */
  @Column(nullable = false)
  private boolean descriptionLogicTerminology = false;

  /** The description logic profile. */
  @Column(nullable = true)
  private String descriptionLogicProfile = null;

  /**
   * Instantiates an empty {@link TerminologyJpa}.
   */
  public TerminologyJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link TerminologyJpa} from the specified parameters.
   *
   * @param terminology the terminology
   */
  public TerminologyJpa(Terminology terminology) {
    super(terminology);
    this.terminology = terminology.getTerminology();
    citation = terminology.getCitation();
    endDate = terminology.getEndDate();
    organizingClassType = terminology.getOrganizingClassType();
    preferredName = terminology.getPreferredName();
    rootTerminology = terminology.getRootTerminology();
    startDate = terminology.getStartDate();
    synonymousNames = terminology.getSynonymousNames();
    version = terminology.getVersion();
    assertsRelDirection = terminology.isAssertsRelDirection();
    current = terminology.isCurrent();
    metathesaurus = terminology.isMetathesaurus();
    descriptionLogicTerminology = terminology.isDescriptionLogicTerminology();
    descriptionLogicProfile = terminology.getDescriptionLogicProfile();
  }

  /* see superclass */
  @Override
  @XmlElement(type = CitationJpa.class)
  public Citation getCitation() {
    return citation;
  }

  /* see superclass */
  @Override
  public void setCitation(Citation citation) {
    this.citation = citation;
  }

  /* see superclass */
  @Override
  public Date getEndDate() {
    return endDate;
  }

  /* see superclass */
  @Override
  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  /* see superclass */
  @Override
  public IdType getOrganizingClassType() {
    return organizingClassType;
  }

  /* see superclass */
  @Override
  public void setOrganizingClassType(IdType organizingClassType) {
    this.organizingClassType = organizingClassType;
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
  @XmlTransient
  public RootTerminology getRootTerminology() {
    return rootTerminology;
  }

  /* see superclass */
  @Override
  public void setRootTerminology(RootTerminology rootTerminology) {
    this.rootTerminology = rootTerminology;
  }

  /**
   * Returns the root terminology id. For JAXB.
   *
   * @return the root terminology id
   */
  public Long getRootTerminologyId() {
    return rootTerminology == null ? null : rootTerminology.getId();
  }

  /**
   * Sets the root terminology id.
   *
   * @param id the root terminology id
   */
  public void setRootTerminologyId(Long id) {
    if (rootTerminology == null) {
      rootTerminology = new RootTerminologyJpa();
    }
    rootTerminology.setId(id);
  }

  /**
   * Returns the root terminology abbreviation. For JAXB
   *
   * @return the root terminology abbreviation
   */
  public String getRootTerminologyAbbreviation() {
    return rootTerminology == null ? null : rootTerminology.getTerminology();
  }

  /**
   * Sets the root terminology abbreviation.
   *
   * @param abbreviation the root terminology abbreviation
   */
  public void setRootTerminologyAbbreviation(String abbreviation) {
    if (rootTerminology == null) {
      rootTerminology = new RootTerminologyJpa();
    }
    rootTerminology.setTerminology(abbreviation);
  }

  /* see superclass */
  @Override
  public Date getStartDate() {
    return startDate;
  }

  /* see superclass */
  @Override
  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  /* see superclass */
  @Override
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
  public String getVersion() {
    return version;
  }

  /* see superclass */
  @Override
  public void setVersion(String version) {
    this.version = version;
  }

  /* see superclass */
  @Override
  public boolean isAssertsRelDirection() {
    return assertsRelDirection;
  }

  /* see superclass */
  @Override
  public void setAssertsRelDirection(boolean isAssertsRelDirection) {
    this.assertsRelDirection = isAssertsRelDirection;
  }

  /* see superclass */
  @Override
  public boolean isCurrent() {
    return current;
  }

  /* see superclass */
  @Override
  public void setCurrent(boolean isCurrent) {
    this.current = isCurrent;
  }

  /* see superclass */
  @Override
  public boolean isMetathesaurus() {
    return metathesaurus;
  }

  /* see superclass */
  @Override
  public void setMetathesaurus(boolean metathesaurus) {
    this.metathesaurus = metathesaurus;
  }

  /* see superclass */
  @Override
  public boolean isDescriptionLogicTerminology() {
    return descriptionLogicTerminology;
  }

  /* see superclass */
  @Override
  public void setDescriptionLogicTerminology(boolean flag) {
    descriptionLogicTerminology = flag;
  }

  /* see superclass */
  @Override
  public String getDescriptionLogicProfile() {
    return descriptionLogicProfile;
  }

  /* see superclass */
  @Override
  public void setDescriptionLogicProfile(String profile) {
    descriptionLogicProfile = profile;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (assertsRelDirection ? 1231 : 1237);
    result = prime * result + ((citation == null) ? 0 : citation.hashCode());
    result = prime * result + (current ? 1231 : 1237);
    result = prime * result + (metathesaurus ? 1231 : 1237);
    result = prime * result + (descriptionLogicTerminology ? 1231 : 1237);
    result =
        prime
            * result
            + ((descriptionLogicProfile == null) ? 0 : descriptionLogicProfile
                .hashCode());
    result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
    result =
        prime
            * result
            + ((organizingClassType == null) ? 0 : organizingClassType
                .hashCode());
    result =
        prime * result
            + ((preferredName == null) ? 0 : preferredName.hashCode());
    result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
    result =
        prime * result
            + ((synonymousNames == null) ? 0 : synonymousNames.hashCode());
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
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
    TerminologyJpa other = (TerminologyJpa) obj;
    if (assertsRelDirection != other.assertsRelDirection)
      return false;
    if (citation == null) {
      if (other.citation != null)
        return false;
    } else if (!citation.equals(other.citation))
      return false;
    if (current != other.current)
      return false;
    if (metathesaurus != other.metathesaurus)
      return false;
    if (descriptionLogicTerminology != other.descriptionLogicTerminology)
      return false;
    if (descriptionLogicProfile == null) {
      if (other.descriptionLogicProfile != null)
        return false;
    } else if (!descriptionLogicProfile.equals(other.descriptionLogicProfile))
      return false;
    if (endDate == null) {
      if (other.endDate != null)
        return false;
    } else if (!endDate.equals(other.endDate))
      return false;
    if (organizingClassType == null) {
      if (other.organizingClassType != null)
        return false;
    } else if (!organizingClassType.equals(other.organizingClassType))
      return false;
    if (preferredName == null) {
      if (other.preferredName != null)
        return false;
    } else if (!preferredName.equals(other.preferredName))
      return false;
    if (startDate == null) {
      if (other.startDate != null)
        return false;
    } else if (!startDate.equals(other.startDate))
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
    if (version == null) {
      if (other.version != null)
        return false;
    } else if (!version.equals(other.version))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "TerminologyJpa [terminology=" + terminology + ", citation="
        + citation + ", endDate=" + endDate + ", organizingClassType="
        + organizingClassType + ", preferredName=" + preferredName
        + ", startDate=" + startDate + ", synonymousNames=" + synonymousNames
        + ", version=" + version + ", assertsRelDirection="
        + assertsRelDirection + ", current=" + current + ", metathesaurus="
        + metathesaurus + ", descriptionLogicTerminology="
        + descriptionLogicTerminology + ", descriptionLogicProfile="
        + descriptionLogicProfile + "]";
  }

}