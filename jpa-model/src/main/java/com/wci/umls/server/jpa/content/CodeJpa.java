/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.CodeRelationship;

/**
 * JPA-enabled implementation of {@link Code}.
 */
@Entity
@Table(name = "codes", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "terminologyVersion", "id"
}))
@Audited
@Indexed
@XmlRootElement(name = "code")
public class CodeJpa extends AbstractAtomClass implements Code {

  /** The relationships. */
  @OneToMany(mappedBy = "from", orphanRemoval = true, targetEntity = CodeRelationshipJpa.class)
  private List<CodeRelationship> relationships = new ArrayList<>();

  /**
   * Instantiates an empty {@link CodeJpa}.
   */
  public CodeJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link CodeJpa} from the specified parameters.
   *
   * @param code the code
   * @param deepCopy the deep copy
   */
  public CodeJpa(Code code, boolean deepCopy) {
    super(code, deepCopy);
    if (deepCopy) {
      for (CodeRelationship relationship : code.getRelationships()) {
        addRelationship(new CodeRelationshipJpa(relationship, deepCopy));
      }
    }
  }

  /**
   * Returns the relationships.
   *
   * @return the relationships
   */
  @XmlElement(type = CodeRelationshipJpa.class, name = "relationship")
  @Override
  public List<CodeRelationship> getRelationships() {
    if (relationships == null) {
      relationships = new ArrayList<>();
    }
    return relationships;
  }

  /**
   * Sets the relationships.
   *
   * @param relationships the relationships
   */
  @Override
  public void setRelationships(List<CodeRelationship> relationships) {
    this.relationships = relationships;

  }

  /**
   * Adds the relationship.
   *
   * @param relationship the relationship
   */
  @Override
  public void addRelationship(CodeRelationship relationship) {
    if (relationships == null) {
      relationships = new ArrayList<>();
    }
    relationships.add(relationship);
  }

  /**
   * Removes the relationship.
   *
   * @param relationship the relationship
   */
  @Override
  public void removeRelationship(CodeRelationship relationship) {
    if (relationships == null) {
      relationships = new ArrayList<>();
    }
    relationships.remove(relationship);
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.jpa.content.AbstractAtomClass#toString()
   */
  @Override
  public String toString() {
    return "CodeJpa [isObsolete()=" + isObsolete()
        + ", getTerminologyVersion()=" + getTerminologyVersion()
        + ", getTerminology()=" + getTerminology() + ", getTerminologyId()="
        + getTerminologyId() + "]";
  }
}
