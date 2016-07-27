package com.wci.umls.server.jpa.content;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.LongBridge;

import com.wci.umls.server.helpers.Note;
import com.wci.umls.server.model.content.Concept;

/**
 * JPA and JAXB enabled implementation of a {@link Note} connected to a {@link Concept}.
 */
@Entity
@Table(name = "concept_notes")
@Audited
@Indexed
@XmlRootElement(name = "conceptNote")
public class ConceptNoteJpa extends AbstractNote {

  /** The concept. */
  @ManyToOne(targetEntity = ConceptJpa.class, optional = false)
  private Concept concept = null;

  /**
   * Instantiates a new concept note jpa.
   */
  public ConceptNoteJpa() {
    // n/a
  }

  /**
   * Instantiates a new concept note jpa.
   *
   * @param note the note
   */
  public ConceptNoteJpa(ConceptNoteJpa note) {
    super(note);
    this.concept = note.getConcept();
  }

  /**
   * Gets the concept.
   *
   * @return the concept
   */
  @XmlTransient
  public Concept getConcept() {
    return this.concept;
  }

  /**
   * Sets the concept.
   *
   * @param concept the new concept
   */
  public void setConcept(Concept concept) {
    this.concept = concept;
  }

  /**
   * Returns the concept id.
   *
   * @return the concept id
   */
  @XmlElement
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getConceptId() {
    return (concept != null) ? concept.getId() : 0;
  }
  
  /**
   * Returns the concept name.
   *
   * @return the concept name
   */
  @XmlElement
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public String getConceptName() {
    return (concept != null) ? concept.getName() : "";
  }
  
  /**
   * Returns the concept name.
   *
   * @return the concept name
   */
  @XmlElement
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getConceptTerminologyId() {
    return (concept != null) ? concept.getTerminologyId() : "";
  }
  
  /**
   * Sets the concept id.
   *
   * @param conceptId the concept id
   */
  @SuppressWarnings("unused")
  private void setConceptId(Long conceptId) {
    if (concept == null) {
      concept = new ConceptJpa();
    }
    concept.setId(conceptId);
  }

  /* see superclass */
  @Override
  public String toString() {
    return "ConceptNoteJpa [conceptId=" + getConceptId() + "] " + super.toString();
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((getConceptId() == null) ? 0 : getConceptId().hashCode());
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
    ConceptNoteJpa other = (ConceptNoteJpa) obj;
    if (getConceptId() == null) {
      if (other.getConceptId() != null)
        return false;
    } else if (!getConceptId().equals(other.getConceptId()))
      return false;
    return true;
  }
  
  
}
