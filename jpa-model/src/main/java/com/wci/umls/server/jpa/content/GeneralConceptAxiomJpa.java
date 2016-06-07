/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.GeneralConceptAxiom;

/**
 * JPA and JAXB enabled implementation of {@link GeneralConceptAxiom}.
 */
@Entity
@Table(name = "general_concept_axioms", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "version", "id"
}))
@Audited
@Indexed
@XmlRootElement(name = "generalConceptAxiom")
public class GeneralConceptAxiomJpa extends AbstractComponent implements
    GeneralConceptAxiom {

  /** The lhs concept. */
  @ManyToOne(targetEntity = ConceptJpa.class, fetch = FetchType.EAGER, optional = false)
  @JoinColumn(nullable = false)
  private Concept leftHandSide;

  /** the rhs concept. */
  @ManyToOne(targetEntity = ConceptJpa.class, fetch = FetchType.EAGER, optional = false)
  @JoinColumn(nullable = false)
  private Concept rightHandSide;

  /** The equivalent. */
  @Column(nullable = false)
  private boolean equivalent;

  /** The sub class. */
  @Column(nullable = false)
  private boolean subClass;

  /**
   * Instantiates an empty {@link GeneralConceptAxiomJpa}.
   */
  public GeneralConceptAxiomJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link GeneralConceptAxiomJpa} from the specified
   * parameters.
   *
   * @param gca the gca
   */
  public GeneralConceptAxiomJpa(GeneralConceptAxiom gca) {
    super(gca);
    leftHandSide = gca.getLeftHandSide();
    rightHandSide = gca.getRightHandSide();
  }

  /* see superclass */
  @Override
  @XmlElement(type = ConceptJpa.class)
  public Concept getLeftHandSide() {
    return leftHandSide;
  }

  /* see superclass */
  @Override
  public void setLeftHandSide(Concept leftHandSide) {
    this.leftHandSide = leftHandSide;

  }

  /* see superclass */
  @Override
  @XmlElement(type = ConceptJpa.class)
  public Concept getRightHandSide() {
    return rightHandSide;
  }

  /* see superclass */
  @Override
  public void setRightHandSide(Concept rightHandSide) {
    this.rightHandSide = rightHandSide;
  }

  /* see superclass */
  @Override
  public boolean isEquivalent() {
    return equivalent;
  }

  /* see superclass */
  @Override
  public void setEquivalent(boolean equivalent) {
    this.equivalent = equivalent;
  }

  /* see superclass */
  @Override
  public boolean isSubClass() {
    return subClass;
  }

  /* see superclass */
  @Override
  public void setSubClass(boolean subClass) {
    this.subClass = subClass;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (equivalent ? 1231 : 1237);
    result =
        prime * result + ((leftHandSide == null) ? 0 : leftHandSide.hashCode());
    result =
        prime * result
            + ((rightHandSide == null) ? 0 : rightHandSide.hashCode());
    result = prime * result + (subClass ? 1231 : 1237);
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
    GeneralConceptAxiomJpa other = (GeneralConceptAxiomJpa) obj;
    if (equivalent != other.equivalent)
      return false;
    if (leftHandSide == null) {
      if (other.leftHandSide != null)
        return false;
    } else if (!leftHandSide.equals(other.leftHandSide))
      return false;
    if (rightHandSide == null) {
      if (other.rightHandSide != null)
        return false;
    } else if (!rightHandSide.equals(other.rightHandSide))
      return false;
    if (subClass != other.subClass)
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "GeneralConceptAxiomJpa [leftHandSide=" + leftHandSide
        + ", rightHandSide=" + rightHandSide + ", equivalent=" + equivalent
        + ", subClass=" + subClass + "]";
  }

}
