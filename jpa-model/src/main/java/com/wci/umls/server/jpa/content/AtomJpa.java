/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.jpa.helpers.MapValueToCsvBridge;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.AtomSubsetMember;
import com.wci.umls.server.model.content.Definition;

/**
 * JPA-enabled implementation of {@link Atom}.
 */
@Entity
// @UniqueConstraint here is being used to create an index, not to enforce
// uniqueness
@Table(name = "atoms", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "terminologyVersion", "id"
}))
@Audited
@XmlRootElement(name = "atom")
public class AtomJpa extends AbstractComponentHasAttributes implements Atom {

  /** The definitions. */
  @OneToMany(targetEntity = DefinitionJpa.class)
  @IndexedEmbedded
  private List<Definition> definitions = null;

  /** The members. */
  @OneToMany(mappedBy = "member", targetEntity = AtomSubsetMemberJpa.class)
  private List<AtomSubsetMember> members = null;

  /** The relationships. */
  @OneToMany(mappedBy = "from", targetEntity = AtomRelationshipJpa.class)
  private List<AtomRelationship> relationships = null;

  /** The concept terminology id map. */
  @ElementCollection(fetch = FetchType.EAGER)
  // consider this: @Fetch(sFetchMode.JOIN)
  @CollectionTable(name = "concept_terminology_ids", joinColumns = @JoinColumn(name = "atom_id"))
  @Column(nullable = false)
  Map<String, String> conceptTerminologyIds;

  /** The alternate terminology ids. */
  @ElementCollection(fetch = FetchType.EAGER)
  // consider this: @Fetch(FetchMode.JOIN)
  @CollectionTable(name = "atom_alt_terminology_ids", joinColumns = @JoinColumn(name = "atom_id"))
  @Column(nullable = true)
  private Map<String, String> alternateTerminologyIds;

  /** The code id. */
  @Column(nullable = false)
  private String codeId;

  /** The descriptor id. */
  @Column(nullable = true)
  private String descriptorId;

  /** The concept id. */
  @Column(nullable = true)
  private String conceptId;

  /** The language. */
  @Column(nullable = false)
  private String language;

  /** The lexical class id. */
  @Column(nullable = false)
  private String lexicalClassId;

  /** The string class id. */
  @Column(nullable = false)
  private String stringClassId;

  /** The name. */
  @Column(nullable = false, length = 4000)
  private String name;

  /** The term type. */
  @Column(nullable = false)
  private String termType;

  /** The workflow status. */
  @Column(nullable = true)
  private String workflowStatus;

  /**
   * Instantiates an empty {@link AtomJpa}.
   */
  public AtomJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link AtomJpa} from the specified parameters.
   *
   * @param atom the atom
   */
  public AtomJpa(Atom atom) {
    this(atom, false);
  }

  /**
   * Instantiates a {@link AtomJpa} from the specified parameters.
   *
   * @param atom the atom
   * @param deepCopy the deep copy
   */
  public AtomJpa(Atom atom, boolean deepCopy) {
    super(atom, deepCopy);
    codeId = atom.getCodeId();
    conceptTerminologyIds = atom.getConceptTerminologyIds();
    alternateTerminologyIds = atom.getAlternateTerminologyIds();
    descriptorId = atom.getDescriptorId();
    conceptId = atom.getDescriptorId();
    language = atom.getLanguage();
    lexicalClassId = atom.getLexicalClassId();
    stringClassId = atom.getStringClassId();
    name = atom.getName();
    termType = atom.getTermType();
    workflowStatus = atom.getWorkflowStatus();

    if (deepCopy) {
      for (Definition definition : atom.getDefinitions()) {
        addDefinition(new DefinitionJpa(definition, deepCopy));
      }
      for (AtomRelationship relationship : atom.getRelationships()) {
        addRelationship(new AtomRelationshipJpa(relationship, deepCopy));
      }
      for (AtomSubsetMember member : atom.getMembers()) {
        addMember(new AtomSubsetMemberJpa(member, deepCopy));
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasDefinitions#getDefinitions()
   */
  @Override
  @XmlElement(type = DefinitionJpa.class, name = "definition")
  public List<Definition> getDefinitions() {
    if (definitions == null) {
      definitions = new ArrayList<>(1);
    }
    return definitions;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.HasDefinitions#setDefinitions(java.util.List)
   */
  @Override
  public void setDefinitions(List<Definition> definitions) {
    this.definitions = definitions;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasRelationships#getRelationships()
   */
  @XmlElement(type = AtomRelationshipJpa.class, name = "relationship")
  @Override
  public List<AtomRelationship> getRelationships() {
    if (relationships == null) {
      relationships = new ArrayList<>(1);
    }
    return relationships;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.HasRelationships#setRelationships(java.util
   * .List)
   */
  @Override
  public void setRelationships(List<AtomRelationship> relationships) {
    this.relationships = relationships;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Atom#getConceptTerminologyIds()
   */
  @Override
  @FieldBridge(impl = MapValueToCsvBridge.class)
  @Field(name = "conceptTerminologyIds", index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public Map<String, String> getConceptTerminologyIds() {
    if (conceptTerminologyIds == null) {
      conceptTerminologyIds = new HashMap<>(2);
    }
    return conceptTerminologyIds;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Atom#setConceptTerminologyIds(java.
   * util.Map)
   */
  @Override
  public void setConceptTerminologyIds(Map<String, String> conceptTerminologyIds) {
    this.conceptTerminologyIds = conceptTerminologyIds;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Atom#putConceptTerminologyId(java.lang
   * .String, java.lang.String)
   */
  @Override
  public void putConceptTerminologyId(String terminology, String terminologyId) {
    if (conceptTerminologyIds == null) {
      conceptTerminologyIds = new HashMap<>(2);
    }
    conceptTerminologyIds.put(terminology, terminologyId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Atom#removeConceptTerminologyId(java.
   * lang.String)
   */
  @Override
  public void removeConceptTerminologyId(String terminology) {
    if (conceptTerminologyIds == null) {
      conceptTerminologyIds = new HashMap<>(2);
    }
    conceptTerminologyIds.remove(terminology);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Atom#getCodeId()
   */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getCodeId() {
    return codeId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Atom#setCodeId(java.lang.String)
   */
  @Override
  public void setCodeId(String codeId) {
    this.codeId = codeId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Atom#getDescriptorId()
   */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getDescriptorId() {
    return descriptorId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Atom#setDescriptorId(java.lang.String)
   */
  @Override
  public void setDescriptorId(String descriptorId) {
    this.descriptorId = descriptorId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Atom#getConceptId()
   */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getConceptId() {
    return conceptId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Atom#setConceptId(java.lang.String)
   */
  @Override
  public void setConceptId(String conceptId) {
    this.conceptId = conceptId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Atom#getLanguage()
   */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getLanguage() {
    return language;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Atom#setLanguage(java.lang.String)
   */
  @Override
  public void setLanguage(String language) {
    this.language = language;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Atom#getLexicalClassId()
   */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getLexicalClassId() {
    return lexicalClassId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Atom#setLexicalClassId(java.lang.String)
   */
  @Override
  public void setLexicalClassId(String lexicalClassId) {
    this.lexicalClassId = lexicalClassId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Atom#getStringClassId()
   */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getStringClassId() {
    return stringClassId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Atom#setStringClassId(java.lang.String)
   */
  @Override
  public void setStringClassId(String stringClassId) {
    this.stringClassId = stringClassId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Atom#getTerm()
   */
  @Override
  @Fields({
      @Field(name = "name", index = Index.YES, store = Store.NO, analyze = Analyze.YES, analyzer = @Analyzer(definition = "noStopWord")),
      @Field(name = "nameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO),
      @Field(name = "edgeNGramName", index = Index.YES, store = Store.NO, analyze = Analyze.YES, analyzer = @Analyzer(definition = "autocompleteEdgeAnalyzer")),
      @Field(name = "nGramName", index = Index.YES, store = Store.NO, analyze = Analyze.YES, analyzer = @Analyzer(definition = "autocompleteNGramAnalyzer"))
  })
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Atom#setTerm(java.lang.String)
   */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Atom#getTermType()
   */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getTermType() {
    return termType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Atom#setTermType(java.lang.String)
   */
  @Override
  public void setTermType(String termType) {
    this.termType = termType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Atom#getWorkflowStatus()
   */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getWorkflowStatus() {
    return workflowStatus;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Atom#setWorkflowStatus(java.lang.String)
   */
  @Override
  public void setWorkflowStatus(String workflowStatus) {
    this.workflowStatus = workflowStatus;

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.HasDefinitions#addDefinition(com.wci.umls.server
   * .model.content.Definition)
   */
  @Override
  public void addDefinition(Definition definition) {
    if (definitions == null) {
      definitions = new ArrayList<>(1);
    }
    definitions.add(definition);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.HasDefinitions#removeDefinition(com.wci.umls
   * .server.model.content.Definition)
   */
  @Override
  public void removeDefinition(Definition definition) {
    if (definitions == null) {
      definitions = new ArrayList<>(1);
    }
    definitions.remove(definition);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.HasRelationships#addRelationship(com.wci.umls
   * .server.model.content.Relationship)
   */
  @Override
  public void addRelationship(AtomRelationship relationship) {
    if (relationships == null) {
      relationships = new ArrayList<>(1);
    }
    relationships.add(relationship);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.HasRelationships#removeRelationship(com.wci
   * .umls.server.model.content.Relationship)
   */
  @Override
  public void removeRelationship(AtomRelationship relationship) {
    if (relationships == null) {
      relationships = new ArrayList<>(1);
    }
    relationships.remove(relationship);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Component#getAlternateTerminologyIds()
   */
  @Override
  @FieldBridge(impl = MapValueToCsvBridge.class)
  @Field(name = "alternateTerminologyIds", index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public Map<String, String> getAlternateTerminologyIds() {
    if (alternateTerminologyIds == null) {
      alternateTerminologyIds = new HashMap<>(2);
    }
    return alternateTerminologyIds;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Component#setAlternateTerminologyIds(
   * java.util.Map)
   */
  @Override
  public void setAlternateTerminologyIds(
    Map<String, String> alternateTerminologyIds) {
    this.alternateTerminologyIds = alternateTerminologyIds;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Component#putAlternateTerminologyId(java
   * .lang.String, java.lang.String)
   */
  @Override
  public void putAlternateTerminologyId(String terminology, String terminologyId) {
    if (alternateTerminologyIds == null) {
      alternateTerminologyIds = new HashMap<>(2);
    }
    alternateTerminologyIds.put(terminology, terminologyId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Component#removeAlternateTerminologyId
   * (java.lang.String)
   */
  @Override
  public void removeAlternateTerminologyId(String terminology) {
    if (alternateTerminologyIds == null) {
      alternateTerminologyIds = new HashMap<>(2);
    }
    alternateTerminologyIds.remove(terminology);

  }

  /**
   * CUSTOM equals: uses .toString() on the concept terminology ids map.
   *
   * @return the int
   * @see com.wci.umls.server.jpa.content.AbstractComponent#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((codeId == null) ? 0 : codeId.hashCode());
    result = prime * result + ((conceptId == null) ? 0 : conceptId.hashCode());
    result =
        prime
            * result
            + ((conceptTerminologyIds == null) ? 0 : conceptTerminologyIds
                .toString().hashCode());
    result =
        prime
            * result
            + ((alternateTerminologyIds == null) ? 0 : alternateTerminologyIds
                .toString().hashCode());
    result =
        prime * result + ((descriptorId == null) ? 0 : descriptorId.hashCode());
    result = prime * result + ((language == null) ? 0 : language.hashCode());
    result =
        prime * result
            + ((lexicalClassId == null) ? 0 : lexicalClassId.hashCode());
    result =
        prime * result
            + ((stringClassId == null) ? 0 : stringClassId.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((termType == null) ? 0 : termType.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.content.AbstractComponent#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    AtomJpa other = (AtomJpa) obj;
    if (codeId == null) {
      if (other.codeId != null)
        return false;
    } else if (!codeId.equals(other.codeId))
      return false;
    if (conceptId == null) {
      if (other.conceptId != null)
        return false;
    } else if (!conceptId.equals(other.conceptId))
      return false;
    if (conceptTerminologyIds == null) {
      if (other.conceptTerminologyIds != null)
        return false;
    } else if (!conceptTerminologyIds.equals(other.conceptTerminologyIds))
      return false;
    if (alternateTerminologyIds == null) {
      if (other.alternateTerminologyIds != null)
        return false;
    } else if (!alternateTerminologyIds.equals(other.alternateTerminologyIds))
      return false;
    if (descriptorId == null) {
      if (other.descriptorId != null)
        return false;
    } else if (!descriptorId.equals(other.descriptorId))
      return false;
    if (language == null) {
      if (other.language != null)
        return false;
    } else if (!language.equals(other.language))
      return false;
    if (lexicalClassId == null) {
      if (other.lexicalClassId != null)
        return false;
    } else if (!lexicalClassId.equals(other.lexicalClassId))
      return false;
    if (stringClassId == null) {
      if (other.stringClassId != null)
        return false;
    } else if (!stringClassId.equals(other.stringClassId))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (termType == null) {
      if (other.termType != null)
        return false;
    } else if (!termType.equals(other.termType))
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractComponent#toString()
   */
  @Override
  public String toString() {
    return "AtomJpa [conceptTerminologyIds=" + conceptTerminologyIds
        + ", alternateTerminologyIds=" + alternateTerminologyIds + ", codeId="
        + codeId + ", descriptorId=" + descriptorId + ", conceptId="
        + conceptId + ", language=" + language + ", lexicalClassId="
        + lexicalClassId + ", stringClassId=" + stringClassId + ", name="
        + name + ", termType=" + termType + ", workflowStatus="
        + workflowStatus + "] - " + super.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasMembers#getMembers()
   */
  @Override
  @XmlElement(type = AtomSubsetMemberJpa.class, name = "member")
  public List<AtomSubsetMember> getMembers() {
    if (members == null) {
      members = new ArrayList<AtomSubsetMember>();
    }
    return members;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasMembers#setMembers(java.util.List)
   */
  @Override
  public void setMembers(List<AtomSubsetMember> members) {
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
  public void addMember(AtomSubsetMember member) {
    if (members == null) {
      members = new ArrayList<AtomSubsetMember>();
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
  public void removeMember(AtomSubsetMember member) {
    if (members == null) {
      members = new ArrayList<AtomSubsetMember>();
    }
    members.remove(member);
  }

}
