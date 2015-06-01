/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.ConceptSubsetMember;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.SemanticTypeComponent;

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

  /** The semantic type components. */
  @IndexedEmbedded
  @OneToMany(targetEntity = SemanticTypeComponentJpa.class)
  private List<SemanticTypeComponent> semanticTypes = null;

  /** The members. */
  @OneToMany(mappedBy = "member", targetEntity = ConceptSubsetMemberJpa.class)
  private List<ConceptSubsetMember> members = null;

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

    if (deepCopy) {
      for (Definition definition : concept.getDefinitions()) {
        addDefinition(new DefinitionJpa(definition, deepCopy));
      }
      for (ConceptRelationship relationship : concept.getRelationships()) {
        addRelationship(new ConceptRelationshipJpa(relationship, deepCopy));
      }
      for (SemanticTypeComponent sty : concept.getSemanticTypes()) {
        addSemanticType(new SemanticTypeComponentJpa(sty));
      }
      for (ConceptSubsetMember member : concept.getMembers()) {
        addMember(new ConceptSubsetMemberJpa(member, deepCopy));
      }
    }
  }

  /**
   * Returns the definitions.
   *
   * @return the definitions
   */
  @XmlElement(type = DefinitionJpa.class, name = "definition")
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
   * Adds the definition.
   *
   * @param definition the definition
   */
  @Override
  public void addDefinition(Definition definition) {
    if (definitions == null) {
      definitions = new ArrayList<>(1);
    }
    definitions.add(definition);

  }

  /**
   * Removes the definition.
   *
   * @param definition the definition
   */
  @Override
  public void removeDefinition(Definition definition) {
    if (definitions == null) {
      definitions = new ArrayList<>(1);
    }
    definitions.remove(definition);

  }

  /**
   * Returns the relationships.
   *
   * @return the relationships
   */
  @XmlElement(type = ConceptRelationshipJpa.class, name = "relationship")
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
   * Adds the relationship.
   *
   * @param relationship the relationship
   */
  @Override
  public void addRelationship(ConceptRelationship relationship) {
    if (relationships == null) {
      relationships = new ArrayList<>(1);
    }
    relationships.add(relationship);
  }

  /**
   * Removes the relationship.
   *
   * @param relationship the relationship
   */
  @Override
  public void removeRelationship(ConceptRelationship relationship) {
    if (relationships == null) {
      relationships = new ArrayList<>(1);
    }
    relationships.remove(relationship);
  }

  /**
   * Indicates whether or not fully defined is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  @Override
  @Field(name="fullyDefined", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  @Field(name="anonymous", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  
  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Concept#getSemanticTypes()
   */
  @XmlElement(type = SemanticTypeComponentJpa.class, name = "semanticType")
  @Override
  public List<SemanticTypeComponent> getSemanticTypes() {
    if (semanticTypes == null) {
      semanticTypes = new ArrayList<>(1);
    }
    return semanticTypes;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Concept#setSemanticTypes(java.util.List)
   */
  @Override
  public void setSemanticTypes(List<SemanticTypeComponent> semanticTypes) {
    this.semanticTypes = semanticTypes;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Concept#addSemanticType(com.wci.umls.
   * server.model.content.SemanticTypeComponent)
   */
  @Override
  public void addSemanticType(SemanticTypeComponent semanticType) {
    if (semanticTypes == null) {
      semanticTypes = new ArrayList<>(1);
    }
    semanticTypes.add(semanticType);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Concept#removeSemanticType(com.wci.umls
   * .server.model.content.SemanticTypeComponent)
   */
  @Override
  public void removeSemanticType(SemanticTypeComponent semanticType) {
    if (semanticTypes == null) {
      semanticTypes = new ArrayList<>(1);
    }
    semanticTypes.remove(semanticType);
  }


  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Concept#getUsesRelationshipIntersection()
   */
  @Override
  public boolean getUsesRelationshipIntersection() {
    return usesRelationshipIntersection;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Concept#setUsesRelationshipIntersection
   * (boolean)
   */
  @Override
  public void setUsesRelationshipIntersection(
    boolean usesRelationshipIntersection) {
    this.usesRelationshipIntersection = usesRelationshipIntersection;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Concept#getUsesRelationshipUnion()
   */
  @Override
  public boolean getUsesRelationshipUnion() {
    return usesRelationshipUnion;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Concept#setUsesRelationshipUnion(boolean)
   */
  @Override
  public void setUsesRelationshipUnion(boolean usesRelationshipUnion) {
    this.usesRelationshipUnion = usesRelationshipUnion;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasMembers#getMembers()
   */
  @XmlElement(type = ConceptSubsetMemberJpa.class, name = "member")
  @Override
  public List<ConceptSubsetMember> getMembers() {
    if (members == null) {
      members = new ArrayList<ConceptSubsetMember>();
    }
    return members;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasMembers#setMembers(java.util.List)
   */
  @Override
  public void setMembers(List<ConceptSubsetMember> members) {
    this.members = members;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.HasMembers#addMember(com.wci.umls.server.model
   * .content.SubsetMember)
   */
  @Override
  public void addMember(ConceptSubsetMember member) {
    if (members == null) {
      members = new ArrayList<ConceptSubsetMember>();
    }
    members.add(member);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.HasMembers#removeMember(com.wci.umls.server
   * .model.content.SubsetMember)
   */
  @Override
  public void removeMember(ConceptSubsetMember member) {
    if (members == null) {
      members = new ArrayList<ConceptSubsetMember>();
    }
    members.remove(member);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractAtomClass#hashCode()
   */
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.content.AbstractAtomClass#equals(java.lang.Object)
   */
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


}
