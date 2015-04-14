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
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.Concept;
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
  @OneToMany(orphanRemoval = true, targetEntity = DefinitionJpa.class)
  private List<Definition> definitions = null;

  /** The relationships. */
  @OneToMany(orphanRemoval = true, targetEntity = AtomRelationshipJpa.class)
  private List<AtomRelationship> relationships = null;

  /** The concept terminology id map. */
  @ElementCollection
  @CollectionTable(name = "atom_concept_map", joinColumns = @JoinColumn(name = "atom_id"))
  @Column(nullable = false)
  Map<String, String> conceptTerminologyIdMap;

  /** The code id. */
  @Column(nullable = false)
  private String codeId;

  /** The descriptor id. */
  @Column(nullable = true)
  private String descriptorId;

  /** The language. */
  @Column(nullable = false)
  private String language;

  /** The lexical class id. */
  @Column(nullable = false)
  private String lexicalClassId;

  /** The string class id. */
  @Column(nullable = false)
  private String stringClassId;

  /** The term. */
  @Column(nullable = false, length = 4000)
  private String term;

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
    conceptTerminologyIdMap = atom.getConceptTerminologyIdMap();
    descriptorId = atom.getDescriptorId();
    language = atom.getLanguage();
    lexicalClassId = atom.getLexicalClassId();
    stringClassId = atom.getStringClassId();
    term = atom.getTerm();
    termType = atom.getTermType();
    workflowStatus = atom.getWorkflowStatus();

    if (deepCopy) {
      for (Definition definition : atom.getDefinitions()) {
        addDefinition(definition);
      }
      for (AtomRelationship relationship : atom.getRelationships()) {
        addRelationship(relationship);
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
      definitions = new ArrayList<>();
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
    if (relationships  == null) {
      relationships = new ArrayList<>();
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
   * @see com.wci.umls.server.model.content.Atom#getConceptTerminologyIdMap()
   */
  @Override
  @XmlTransient
  public Map<String, String> getConceptTerminologyIdMap() {
    return conceptTerminologyIdMap;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Atom#setConceptTerminologyIdMap(java.
   * util.Map)
   */
  @Override
  public void setConceptTerminologyIdMap(
    Map<String, String> conceptTerminologyIdMap) {
    this.conceptTerminologyIdMap = conceptTerminologyIdMap;
  }

  /**
   * Returns the concepts list. For JAXB
   *
   * @return the concepts list
   */
  @XmlElement
  public KeyValuePairList getConceptsList() {
    KeyValuePairList kvpl = new KeyValuePairList();
    if (conceptTerminologyIdMap != null) {
      for (String key : conceptTerminologyIdMap.keySet()) {
        KeyValuePair kvp = new KeyValuePair();
        kvp.setKey(key);
        kvp.setValue(conceptTerminologyIdMap.get(key));
        kvpl.addKeyValuePair(kvp);
      }
    }
    return kvpl;
  }

  /**
   * Sets the concepts list.
   *
   * @param list the concepts list
   */
  public void setConceptsList(KeyValuePairList list) {
    if (list != null) {
      conceptTerminologyIdMap = new HashMap<>();
      for (KeyValuePair pair : list.getKeyValuePairList()) {
        conceptTerminologyIdMap.put(pair.getKey(), pair.getValue());
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Atom#getCodeId()
   */
  @Override
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
   * @see com.wci.umls.server.model.content.Atom#getLanguage()
   */
  @Override
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
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "termSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  @Analyzer(definition = "noStopWord")
  public String getTerm() {
    return term;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Atom#setTerm(java.lang.String)
   */
  @Override
  public void setTerm(String term) {
    this.term = term;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Atom#getTermType()
   */
  @Override
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
      definitions = new ArrayList<>();
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
      definitions = new ArrayList<>();
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
      relationships = new ArrayList<>();
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
      relationships = new ArrayList<>();
    }
    relationships.remove(relationship);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Atom#addConcept(com.wci.umls.server.model
   * .content.Concept)
   */
  @Override
  public void addConcept(Concept concept) {
    if (conceptTerminologyIdMap == null) {
      conceptTerminologyIdMap = new HashMap<>();
    }
    conceptTerminologyIdMap.put(concept.getTerminology(),
        concept.getTerminologyId());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Atom#removeConcept(com.wci.umls.server
   * .model.content.Concept)
   */
  @Override
  public void removeConcept(Concept concept) {
    if (conceptTerminologyIdMap == null) {
      conceptTerminologyIdMap = new HashMap<>();
    }
    conceptTerminologyIdMap.remove(concept.getTerminology());
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractComponent#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((codeId == null) ? 0 : codeId.hashCode());
    result =
        prime * result + ((descriptorId == null) ? 0 : descriptorId.hashCode());
    result = prime * result + ((language == null) ? 0 : language.hashCode());
    result =
        prime * result
            + ((lexicalClassId == null) ? 0 : lexicalClassId.hashCode());
    result =
        prime * result
            + ((stringClassId == null) ? 0 : stringClassId.hashCode());
    result = prime * result + ((term == null) ? 0 : term.hashCode());
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
    if (term == null) {
      if (other.term != null)
        return false;
    } else if (!term.equals(other.term))
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
    return "AtomJpa [conceptTerminologyIdMap=" + conceptTerminologyIdMap
        + ", codeId=" + codeId + ", descriptorId=" + descriptorId
        + ", language=" + language + ", lexicalClassId=" + lexicalClassId
        + ", stringClassId=" + stringClassId + ", term=" + term + ", termType="
        + termType + "]";
  }

}
