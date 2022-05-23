/*
 *    Copyright 2017 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.helpers.Note;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.jpa.helpers.CollectionToCsvBridge;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.ComponentHistory;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.ConceptSubsetMember;
import com.wci.umls.server.model.content.ConceptTreePosition;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.meta.IdType;

/**
 * JPA and JAXB enabled implementation of {@link Concept}.
 */
@Entity
@Table(name = "concepts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {
        "terminologyId", "terminology", "version", "id"
    }), @UniqueConstraint(columnNames = {
        "terminology", "version", "id"
    })
})

@Audited
@XmlRootElement(name = "concept")
@Indexed
public class ConceptJpa extends AbstractAtomClass implements Concept {

  /** The definitions. */
  @IndexedEmbedded(targetElement = DefinitionJpa.class, includeEmbeddedObjectId=true)
  @CollectionTable(name = "concepts_definitions", joinColumns = @JoinColumn(name = "concepts_id"))
  @OneToMany(targetEntity = DefinitionJpa.class)
  private List<Definition> definitions = null;

  /** The relationships. */
  @OneToMany(mappedBy = "from", targetEntity = ConceptRelationshipJpa.class)
  private List<ConceptRelationship> relationships = null;

  /** The inverse relationships. */
  @OneToMany(mappedBy = "to", targetEntity = ConceptRelationshipJpa.class)
  private List<ConceptRelationship> inverseRelationships = null;

  /** The tree positions. */
  @OneToMany(mappedBy = "node", targetEntity = ConceptTreePositionJpa.class)
  private List<ConceptTreePosition> treePositions = null;

  /** The component histories. */
  @IndexedEmbedded(targetElement = ComponentHistoryJpa.class, includeEmbeddedObjectId=true)
  @CollectionTable(name = "concepts_component_histories", joinColumns = @JoinColumn(name = "concepts_id"))
  @OneToMany(targetEntity = ComponentHistoryJpa.class)
  private List<ComponentHistory> componentHistories = null;

  /** The semantic type components. */
  @IndexedEmbedded(targetElement = SemanticTypeComponentJpa.class, includeEmbeddedObjectId=true)
  @CollectionTable(name = "concepts_semantic_type_components", joinColumns = @JoinColumn(name = "concepts_id"))
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
  @Fetch(FetchMode.JOIN)
  @JoinColumn(nullable = true)
  private List<String> labels;

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

  /** The last approved. */
  @Column(nullable = true)
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastApproved;

  /** The last approved by. */
  @Column(nullable = true)
  private String lastApprovedBy;

  /** The descriptions. */
  @ManyToMany(targetEntity = AtomJpa.class)
  @CollectionTable(name = "concepts_atoms", joinColumns = @JoinColumn(name = "concepts_id"))
  @IndexedEmbedded(targetElement = AtomJpa.class)
  private List<Atom> atoms = null;
  
  /** The attributes. */
  @OneToMany(targetEntity = AttributeJpa.class)
  @JoinColumn(name = "attributes_id")
  @JoinTable(name = "concepts_attributes",
      joinColumns = @JoinColumn(name = "attributes_id"),
      inverseJoinColumns = @JoinColumn(name = "concepts_id"))
  private List<Attribute> attributes = null;
  
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
   * @param collectionCopy the deep copy
   */
  public ConceptJpa(Concept concept, boolean collectionCopy) {
    super(concept, collectionCopy);
    anonymous = concept.isAnonymous();
    fullyDefined = concept.isFullyDefined();
    usesRelationshipIntersection = concept.getUsesRelationshipIntersection();
    usesRelationshipUnion = concept.getUsesRelationshipUnion();
    lastApproved = concept.getLastApproved();
    lastApprovedBy = concept.getLastApprovedBy();
    labels = new ArrayList<>(concept.getLabels());

    if (collectionCopy) {
      definitions = new ArrayList<>(concept.getDefinitions());
      relationships = new ArrayList<>(concept.getRelationships());
      inverseRelationships = new ArrayList<>(concept.getInverseRelationships());
      semanticTypes = new ArrayList<>(concept.getSemanticTypes());
      members = new ArrayList<>(concept.getMembers());
      componentHistories = new ArrayList<>(concept.getComponentHistory());
      treePositions = new ArrayList<>(concept.getTreePositions());
      notes = new ArrayList<>(concept.getNotes());
      atoms = new ArrayList<>(concept.getAtoms());
      for (final Attribute attribute : concept.getAttributes()) {
          getAttributes().add(new AttributeJpa(attribute));
      }
    }
  }

  /**
   * Instantiates a {@link ConceptJpa} from the specified parameters.
   *
   * @param result the result
   */
  public ConceptJpa(SearchResult result) {
    setName(result.getValue());
    setId(result.getId());
    setTerminology(result.getTerminology());
    setTerminologyId(result.getTerminologyId());
    setVersion(result.getVersion());
    setWorkflowStatus(result.getWorkflowStatus());
  }

  /* see superclass */
  @XmlElement(type = AtomJpa.class)
  @Override
  public List<Atom> getAtoms() {
    if (atoms == null) {
      atoms = new ArrayList<>();
    }
    return atoms;
  }

  /* see superclass */
  @Override
  public void setAtoms(List<Atom> atoms) {
    this.atoms = atoms;
  }
  
  /* see superclass */
  @Override
  @XmlElement(type = AttributeJpa.class)
  public List<Attribute> getAttributes() {
    if (attributes == null) {
      attributes = new ArrayList<>(1);
    }
    return attributes;
  }

  /* see superclass */
  @Override
  public void setAttributes(List<Attribute> attributes) {
    this.attributes = attributes;
  }

  /* see superclass */
  @Override
  public Attribute getAttributeByName(String name) {
    for (final Attribute attribute : getAttributes()) {
      // If there are more than one, this just returns the first.
      if (attribute.getName().equals(name)) {
        return attribute;
      }
    }
    return null;
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

  @XmlElement(type = ConceptRelationshipJpa.class)
  @Override
  public List<ConceptRelationship> getRelationships() {
    if (relationships == null) {
      relationships = new ArrayList<>(1);
    }
    return relationships;
  }

  /* see superclass */
  @XmlTransient
  @Override
  public List<ConceptRelationship> getInverseRelationships() {
    if (inverseRelationships == null) {
      inverseRelationships = new ArrayList<>(1);
    }
    return inverseRelationships;
  }

  @Override
  public void setRelationships(List<ConceptRelationship> relationships) {
    this.relationships = relationships;

  }

  @XmlTransient
  @Override
  public List<ConceptTreePosition> getTreePositions() {
    if (treePositions == null) {
      treePositions = new ArrayList<>(1);
    }
    return treePositions;
  }

  /* see superclass */
  @Override
  public void setTreePositions(List<ConceptTreePosition> treePositions) {
    this.treePositions = treePositions;

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
    if (labels == null) {
      labels = new ArrayList<>();
    }
    return labels;
  }

  /* see superclass */
  @Override
  public void setLabels(List<String> labels) {
    this.labels = labels;

  }

  /* see superclass */
  @Override
  public void setNotes(List<Note> notes) {
    this.notes = notes;

  }

  /* see superclass */
  @Override
  @XmlElement(type = ConceptNoteJpa.class)
  public List<Note> getNotes() {
    if (this.notes == null) {
      this.notes = new ArrayList<>(1);
    }
    return this.notes;
  }

  /* see superclass */
  @Override
  public void setType(IdType type) {
    // N/A
  }

  /* see superclass */
  @Override
  public IdType getType() {
    return IdType.CONCEPT;
  }

  /**
   * Returns the last approved.
   *
   * @return the last approved
   */
  @Override
  public Date getLastApproved() {
    return lastApproved;
  }

  /**
   * Sets the last approved.
   *
   * @param lastApproved the last approved
   */
  @Override
  public void setLastApproved(Date lastApproved) {
    this.lastApproved = lastApproved;
  }

  /**
   * Returns the last approved by.
   *
   * @return the last approved by
   */
  @Override
  public String getLastApprovedBy() {
    return lastApprovedBy;
  }

  /**
   * Sets the last approved by.
   *
   * @param lastApprovedBy the last approved by
   */
  @Override
  public void setLastApprovedBy(String lastApprovedBy) {
    this.lastApprovedBy = lastApprovedBy;
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

  /* see superclass */
  @Override
  @XmlElement(type = ComponentHistoryJpa.class)
  public List<ComponentHistory> getComponentHistory() {
    if (componentHistories == null) {
      componentHistories = new ArrayList<ComponentHistory>();
    }
    return componentHistories;
  }

  /* see superclass */
  @Override
  public void setComponentHistory(List<ComponentHistory> componentHistory) {
    this.componentHistories = componentHistory;
  }

}