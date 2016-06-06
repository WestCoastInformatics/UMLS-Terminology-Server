/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.helpers.Note;
import com.wci.umls.server.jpa.helpers.CollectionToCsvBridge;
import com.wci.umls.server.model.content.ComponentHistory;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.ConceptSubsetMember;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.meta.IdType;

/**
 * JPA-enabled implementation of {@link Concept}.
 */
@Entity
@Table(name = "concepts", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "version", "id"
}))
@Audited
@XmlRootElement(name = "concept")
@Indexed
public class ConceptJpa extends AbstractAtomClass implements Concept {

  /** The definitions. */
  @OneToMany(targetEntity = DefinitionJpa.class)
  private List<Definition> definitions = null;

  /** The relationships. */
  @OneToMany(mappedBy = "from", targetEntity = ConceptRelationshipJpa.class)
  private List<ConceptRelationship> relationships = null;

  @OneToMany(mappedBy = "referencedConcept", targetEntity = ComponentHistoryJpa.class)
  private List<ComponentHistory> componentHistories = null;

  /** The semantic type components. */
  @IndexedEmbedded(targetElement = SemanticTypeComponentJpa.class)
  @OneToMany(targetEntity = SemanticTypeComponentJpa.class)
  private List<SemanticTypeComponent> semanticTypes = null;

  /** The members. */
  @OneToMany(mappedBy = "member", targetEntity = ConceptSubsetMemberJpa.class)
  private List<ConceptSubsetMember> members = null;

  /** The notes. */
  @OneToMany(mappedBy = "concept", targetEntity = ConceptNoteJpa.class)
  @IndexedEmbedded(targetElement = ConceptNoteJpa.class)
  private List<Note> notes = new ArrayList<>();

  /** The concept terminology id map. */
  @ElementCollection(fetch = FetchType.EAGER)
  // consider this: @Fetch(sFetchMode.JOIN)
  @Column(nullable = true)
  List<String> labels;

  /** The fully defined. */
  @Column(nullable = false)
  private boolean fullyDefined = false;

  /** The anonymous. */
  @Column(nullable = false)
  private boolean anonymous = false;

  /** The uses relationships intersection flag. */
  @Column(nullable = false)
  private boolean usesRelationshipIntersection = true;

  /** The uses relationships union flag. */
  @Column(nullable = false)
  private boolean usesRelationshipUnion = false;

  /**
   * Instantiates an empty {@link ConceptJpa}.
   */
  public ConceptJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ConceptJpa} from the specified parameters.
   *
   * @param concept the concept
   * @param deepCopy the deep copy
   */
  public ConceptJpa(Concept concept, boolean deepCopy) {
    super(concept, deepCopy);
    anonymous = concept.isAnonymous();
    fullyDefined = concept.isFullyDefined();
    usesRelationshipIntersection = concept.getUsesRelationshipIntersection();
    usesRelationshipUnion = concept.getUsesRelationshipUnion();
    if (concept.getLabels() != null) {
      labels = new ArrayList<>(concept.getLabels());
    }

    if (deepCopy) {
      for (Definition definition : concept.getDefinitions()) {
        getDefinitions().add(new DefinitionJpa(definition, deepCopy));
      }
      for (ConceptRelationship relationship : concept.getRelationships()) {
        getRelationships().add(
            new ConceptRelationshipJpa(relationship, deepCopy));
      }
      for (SemanticTypeComponent sty : concept.getSemanticTypes()) {
        getSemanticTypes().add(new SemanticTypeComponentJpa(sty));
      }
      for (ConceptSubsetMember member : concept.getMembers()) {
        getMembers().add(new ConceptSubsetMemberJpa(member, deepCopy));
      }
    }
  }

  /**
   * Returns the definitions.
   *
   * @return the definitions
   */
  @XmlElement(type = DefinitionJpa.class)
  @Override
  public List<Definition> getDefinitions() {
    if (definitions == null) {
      definitions = new ArrayList<>(1);
    }
    return definitions;
  }

  /**
   * Sets the definitions.
   *
   * @param definitions the definitions
   */
  @Override
  public void setDefinitions(List<Definition> definitions) {
    this.definitions = definitions;
  }

  /**
   * Returns the relationships.
   *
   * @return the relationships
   */
  @XmlElement(type = ConceptRelationshipJpa.class)
  @Override
  public List<ConceptRelationship> getRelationships() {
    if (relationships == null) {
      relationships = new ArrayList<>(1);
    }
    return relationships;
  }

  /**
   * Sets the relationships.
   *
   * @param relationships the relationships
   */
  @Override
  public void setRelationships(List<ConceptRelationship> relationships) {
    this.relationships = relationships;

  }

  /**
   * Indicates whether or not fully defined is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  @Override
  @Field(name = "fullyDefined", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public boolean isFullyDefined() {
    return fullyDefined;
  }

  /**
   * Sets the fully defined.
   *
   * @param fullyDefined the fully defined
   */
  @Override
  public void setFullyDefined(boolean fullyDefined) {
    this.fullyDefined = fullyDefined;
  }

  /**
   * Indicates whether or not the concept is anonymous.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  @Override
  @Field(name = "anonymous", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public boolean isAnonymous() {
    return anonymous;
  }

  /**
   * Sets the anonymous flag.
   *
   * @param anonymous the anonymous flag
   */
  @Override
  public void setAnonymous(boolean anonymous) {
    this.anonymous = anonymous;
  }

  /* see superclass */
  @XmlElement(type = SemanticTypeComponentJpa.class)
  @Override
  public List<SemanticTypeComponent> getSemanticTypes() {
    if (semanticTypes == null) {
      semanticTypes = new ArrayList<>(1);
    }
    return semanticTypes;
  }

  /* see superclass */
  @Override
  public void setSemanticTypes(List<SemanticTypeComponent> semanticTypes) {
    this.semanticTypes = semanticTypes;
  }

  /* see superclass */
  @Override
  public boolean getUsesRelationshipIntersection() {
    return usesRelationshipIntersection;
  }

  /* see superclass */
  @Override
  public void setUsesRelationshipIntersection(
    boolean usesRelationshipIntersection) {
    this.usesRelationshipIntersection = usesRelationshipIntersection;
  }

  /* see superclass */
  @Override
  public boolean getUsesRelationshipUnion() {
    return usesRelationshipUnion;
  }

  /* see superclass */
  @Override
  public void setUsesRelationshipUnion(boolean usesRelationshipUnion) {
    this.usesRelationshipUnion = usesRelationshipUnion;
  }

  /* see superclass */
  @XmlElement(type = ConceptSubsetMemberJpa.class)
  @Override
  public List<ConceptSubsetMember> getMembers() {
    if (members == null) {
      members = new ArrayList<ConceptSubsetMember>();
    }
    return members;
  }

  /* see superclass */
  @Override
  public void setMembers(List<ConceptSubsetMember> members) {
    this.members = members;
  }

  /* see superclass */
  @Override
  @FieldBridge(impl = CollectionToCsvBridge.class)
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public List<String> getLabels() {
    return labels;
  }

  /* see superclass */
  @Override
  public void setLabels(List<String> labels) {
    this.labels = labels;

  }
  

  @Override
  public void setNotes(List<Note> notes) {
    this.notes = notes;

  }

  @Override
  @XmlElement(type = ConceptNoteJpa.class)
  public List<Note> getNotes() {
    if (this.notes == null) {
      this.notes = new ArrayList<>(1);
    }
    return this.notes;
  }

  @Override
  public void setType(IdType type) {
   // N/A
    
  }

  @Override
  public IdType getType() {
    return IdType.CONCEPT;
  }


  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (anonymous ? 1231 : 1237);
    result = prime * result + (fullyDefined ? 1231 : 1237);
    result = prime * result + (usesRelationshipIntersection ? 1231 : 1237);
    result = prime * result + (usesRelationshipUnion ? 1231 : 1237);
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
    ConceptJpa other = (ConceptJpa) obj;
    if (fullyDefined != other.fullyDefined)
      return false;
    if (anonymous != other.anonymous)
      return false;
    if (usesRelationshipIntersection != other.usesRelationshipIntersection)
      return false;
    if (usesRelationshipUnion != other.usesRelationshipUnion)
      return false;
    return true;
  }

  @Override
  public List<ComponentHistory> getComponentHistory() {
    return componentHistories;
  }

  @Override
  public void setComponentHistory(List<ComponentHistory> componentHistory) {
    this.componentHistories = componentHistory;
  }

}