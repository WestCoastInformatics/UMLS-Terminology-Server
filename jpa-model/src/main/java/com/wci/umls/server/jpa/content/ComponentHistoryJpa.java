package com.wci.umls.server.jpa.content;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.model.content.ComponentHistory;
import com.wci.umls.server.model.content.Concept;

/**
 * JPA-enabled implementation of {@link ComponentHistory}.
 */
@Entity
@Table(name = "component_histories", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "version", "id"
}))
@Audited
@XmlRootElement(name = "componentHistory")
public class ComponentHistoryJpa extends AbstractComponent implements
    ComponentHistory {

  /** The referenced concept. */
  @ManyToOne(targetEntity = ConceptJpa.class, optional = true)
  @JoinColumn(nullable = true, name = "referenced_concept_id")
  private Concept referencedConcept;

  /** The reason. */
  @Column(nullable = true, length = 4000)
  private String reason;

  /** The relationship type. */
  @Column(nullable = true)
  private String relationshipType;

  /** The additional relationship type. */
  @Column(nullable = true)
  private String additionalRelationshipType;

  /** The release. */
  @Column(nullable = false)
  private String associatedRelease;

  /**
   * Instantiates an empty {@link ComponentHistoryJpa}.
   */
  public ComponentHistoryJpa() {
    // n/a
  }

  /**
   * Instantiates a {@link ComponentHistoryJpa} from the specified parameters.
   *
   * @param h the h
   */
  public ComponentHistoryJpa(ComponentHistory h) {
    super(h);
    this.referencedConcept = new ConceptJpa(h.getReferencedConcept(), false);
    this.reason = h.getReason();
    relationshipType = h.getRelationshipType();
    additionalRelationshipType = h.getAdditionalRelationshipType();
    associatedRelease = h.getAssociatedRelease();
  }

  /* see superclass */
  @XmlTransient
  @Override
  public Concept getReferencedConcept() {
    return referencedConcept;
  }

  /**
   * Returns the referenced concept id.
   *
   * @return the referenced concept id
   */
  public Long getReferencedConceptId() {
    return referencedConcept == null ? null : referencedConcept.getId();
  }

  /**
   * Sets the referenced concept id.
   *
   * @param id the referenced concept id
   */
  public void setReferencedConceptId(Long id) {
    if (referencedConcept == null) {
      referencedConcept = new ConceptJpa();
    }
    referencedConcept.setId(id);
  }

  /**
   * Returns the referenced concept terminology id.
   *
   * @return the referenced concept terminology id
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getReferencedConceptTerminologyId() {
    return referencedConcept == null ? null : referencedConcept
        .getTerminologyId();
  }

  /**
   * Sets the referenced concept terminology id.
   *
   * @param terminologyId the referenced concept terminology id
   */
  public void setReferencedConceptTerminologyId(String terminologyId) {
    if (referencedConcept == null) {
      referencedConcept = new ConceptJpa();
    }
    referencedConcept.setTerminologyId(terminologyId);
  }

  /**
   * Returns the referenced concept terminology.
   *
   * @return the referenced concept terminology
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getReferencedConceptTerminology() {
    return referencedConcept == null ? null : referencedConcept
        .getTerminology();
  }

  /**
   * Sets the referenced concept terminology.
   *
   * @param terminology the referenced concept terminology
   */
  public void setReferencedConceptTerminology(String terminology) {
    if (referencedConcept == null) {
      referencedConcept = new ConceptJpa();
    }
    referencedConcept.setTerminology(terminology);
  }

  /**
   * Returns the referenced concept version.
   *
   * @return the referenced concept version
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getReferencedConceptVersion() {
    return referencedConcept == null ? null : referencedConcept.getVersion();
  }

  /**
   * Sets the referenced concept version.
   *
   * @param version the referenced concept version
   */
  public void setReferencedConceptVersion(String version) {
    if (referencedConcept == null) {
      referencedConcept = new ConceptJpa();
    }
    referencedConcept.setVersion(version);
  }

  /**
   * Returns the referenced concept name.
   *
   * @return the referenced concept name
   */
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "referencedConceptNameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  public String getReferencedConceptName() {
    return referencedConcept == null ? null : referencedConcept.getName();
  }

  /**
   * Sets the referenced concept name.
   *
   * @param term the referenced concept name
   */
  public void setReferencedConceptName(String term) {
    if (referencedConcept == null) {
      referencedConcept = new ConceptJpa();
    }
    referencedConcept.setName(term);
  }

  /* see superclass */
  @Override
  public void setReferencedConcept(Concept referencedConcept) {
    this.referencedConcept = referencedConcept;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public String getReason() {
    return reason;
  }

  /* see superclass */
  @Override
  public void setReason(String reason) {
    this.reason = reason;
  }

  /* see superclass */
  @Override
  public String getRelationshipType() {
    return relationshipType;
  }

  /* see superclass */
  @Override
  public void setRelationshipType(String relationshipType) {
    this.relationshipType = relationshipType;
  }

  /* see superclass */
  @Override
  public String getAdditionalRelationshipType() {
    return additionalRelationshipType;
  }

  /* see superclass */
  @Override
  public void setAdditionalRelationshipType(String relationshipType) {
    this.additionalRelationshipType = relationshipType;
  }

  /* see superclass */
  @Override
  public String getAssociatedRelease() {
    return associatedRelease;
  }

  /* see superclass */
  @Override
  public void setAssociatedRelease(String associatedRelease) {
    this.associatedRelease = associatedRelease;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime
            * result
            + ((additionalRelationshipType == null) ? 0
                : additionalRelationshipType.hashCode());
    result = prime * result + ((reason == null) ? 0 : reason.hashCode());
    result =
        prime * result
            + ((referencedConcept == null) ? 0 : referencedConcept.hashCode());
    result =
        prime * result
            + ((relationshipType == null) ? 0 : relationshipType.hashCode());
    result =
        prime * result
            + ((associatedRelease == null) ? 0 : associatedRelease.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    ComponentHistoryJpa other = (ComponentHistoryJpa) obj;
    if (additionalRelationshipType == null) {
      if (other.additionalRelationshipType != null)
        return false;
    } else if (!additionalRelationshipType
        .equals(other.additionalRelationshipType))
      return false;
    if (reason == null) {
      if (other.reason != null)
        return false;
    } else if (!reason.equals(other.reason))
      return false;
    if (referencedConcept == null) {
      if (other.referencedConcept != null)
        return false;
    } else if (!referencedConcept.equals(other.referencedConcept))
      return false;
    if (relationshipType == null) {
      if (other.relationshipType != null)
        return false;
    } else if (!relationshipType.equals(other.relationshipType))
      return false;
    if (associatedRelease == null) {
      if (other.associatedRelease != null)
        return false;
    } else if (!associatedRelease.equals(other.associatedRelease))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "ComponentHistoryJpa [referencedConcept=" + referencedConcept
        + ", reason=" + reason + ", relationshipType=" + relationshipType
        + ", additionalRelationshipType=" + additionalRelationshipType
        + ", release=" + associatedRelease + "]";
  }

}
