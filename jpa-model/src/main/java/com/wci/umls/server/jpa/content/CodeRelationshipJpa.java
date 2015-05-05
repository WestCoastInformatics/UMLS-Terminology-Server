/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.CodeRelationship;

/**
 * JPA-enabled implementation of {@link CodeRelationship}.
 */
@Entity
@Table(name = "code_relationships", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "terminologyVersion", "id"
}))
@Audited
@XmlRootElement(name = "codeRelationship")
public class CodeRelationshipJpa extends AbstractRelationship<Code, Code>
    implements CodeRelationship {

  /** The from code. */
  @ManyToOne(targetEntity = CodeJpa.class, optional = false)
  @JoinColumn(nullable = false)
  private Code from;

  /** the to code. */
  @ManyToOne(targetEntity = CodeJpa.class, optional = false)
  @JoinColumn(nullable = false)
  private Code to;

  /** The alternate terminology ids. */
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "coderel_alt_terminology_ids", joinColumns = @JoinColumn(name = "relationship_id"))
  @Column(nullable = true)
  private Map<String, String> alternateTerminologyIds;

  /**
   * Instantiates an empty {@link CodeRelationshipJpa}.
   */
  public CodeRelationshipJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link CodeRelationshipJpa} from the specified parameters.
   *
   * @param relationship the relationship
   * @param deepCopy the deep copy
   */
  public CodeRelationshipJpa(CodeRelationship relationship, boolean deepCopy) {
    super(relationship, deepCopy);
    to = relationship.getTo();
    from = relationship.getFrom();
    alternateTerminologyIds = relationship.getAlternateTerminologyIds();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Relationship#getFrom()
   */
  @Override
  @XmlTransient
  public Code getFrom() {
    return from;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Relationship#setFrom(com.wci.umls.server
   * .model.content.Component)
   */
  @Override
  public void setFrom(Code component) {
    this.from = component;
  }

  /**
   * Returns the from id. For JAXB.
   *
   * @return the from id
   */
  public Long getFromId() {
    return from == null ? null : from.getId();
  }

  /**
   * Sets the from id.
   *
   * @param id the from id
   */
  public void setFromId(Long id) {
    if (from == null) {
      from = new CodeJpa();
    }
    from.setId(id);
  }

  /**
   * Returns the from term. For JAXB.
   *
   * @return the from term
   */
  public String getFromDefaultPreferredName() {
    return from == null ? null : from.getDefaultPreferredName();
  }

  /**
   * Sets the from term.
   *
   * @param term the from term
   */
  public void setFromDefaultPreferredName(String term) {
    if (from == null) {
      from = new CodeJpa();
    }
    from.setDefaultPreferredName(term);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Relationship#getTo()
   */
  @Override
  @XmlTransient
  public Code getTo() {
    return to;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Relationship#setTo(com.wci.umls.server
   * .model.content.Component)
   */
  @Override
  public void setTo(Code component) {
    this.to = component;
  }

  /**
   * Returns the to id. For JAXB.
   *
   * @return the to id
   */
  public Long getToId() {
    return to == null ? null : to.getId();
  }

  /**
   * Sets the to id.
   *
   * @param id the to id
   */
  public void setToId(Long id) {
    if (to == null) {
      to = new CodeJpa();
    }
    to.setId(id);
  }

  /**
   * Returns the to term. For JAXB.
   *
   * @return the to term
   */
  public String getToDefaultPreferredName() {
    return to == null ? null : to.getDefaultPreferredName();
  }

  /**
   * Sets the to term.
   *
   * @param term the to term
   */
  public void setToDefaultPreferredName(String term) {
    if (to == null) {
      to = new CodeJpa();
    }
    to.setDefaultPreferredName(term);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasAlternateTerminologyIds#
   * getAlternateTerminologyIds()
   */
  @Override
  public Map<String, String> getAlternateTerminologyIds() {
    if (alternateTerminologyIds == null) {
      alternateTerminologyIds = new HashMap<>();
    }
    return alternateTerminologyIds;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasAlternateTerminologyIds#
   * setAlternateTerminologyIds(java.util.Map)
   */
  @Override
  public void setAlternateTerminologyIds(
    Map<String, String> alternateTerminologyIds) {
    this.alternateTerminologyIds = alternateTerminologyIds;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasAlternateTerminologyIds#
   * putAlternateTerminologyId(java.lang.String, java.lang.String)
   */
  @Override
  public void putAlternateTerminologyId(String terminology, String terminologyId) {
    if (alternateTerminologyIds == null) {
      alternateTerminologyIds = new HashMap<>();
    }
    alternateTerminologyIds.put(terminology, terminologyId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasAlternateTerminologyIds#
   * removeAlternateTerminologyId(java.lang.String)
   */
  @Override
  public void removeAlternateTerminologyId(String terminology) {
    if (alternateTerminologyIds == null) {
      alternateTerminologyIds = new HashMap<>();
    }
    alternateTerminologyIds.remove(terminology);

  }

  /**
   * CUSTOM to support alternateTerminologyIds.
   *
   * @return the int
   * @see com.wci.umls.server.jpa.content.AbstractRelationship#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((from == null) ? 0 : from.hashCode());
    result = prime * result + ((to == null) ? 0 : to.hashCode());
    result =
        prime
            * result
            + ((alternateTerminologyIds == null) ? 0 : alternateTerminologyIds
                .toString().hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.content.AbstractRelationship#equals(java.lang.Object
   * )
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    CodeRelationshipJpa other = (CodeRelationshipJpa) obj;
    if (from == null) {
      if (other.from != null)
        return false;
    } else if (!from.equals(other.from))
      return false;
    if (to == null) {
      if (other.to != null)
        return false;
    } else if (!to.equals(other.to))
      return false;
    if (alternateTerminologyIds == null) {
      if (other.alternateTerminologyIds != null)
        return false;
    } else if (!alternateTerminologyIds.equals(other.alternateTerminologyIds))
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractRelationship#toString()
   */
  @Override
  public String toString() {
    return "CodeRelationshipJpa [from=" + from.getTerminologyId() + ", to="
        + to.getTerminologyId() + "]";
  }

}
