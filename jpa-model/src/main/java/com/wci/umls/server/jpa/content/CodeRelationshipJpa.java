/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.HashMap;
import java.util.Map;

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
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.jpa.helpers.MapValueToCsvBridge;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.CodeRelationship;

/**
 * JPA-enabled implementation of {@link CodeRelationship}.
 */
@Entity
@Table(name = "code_relationships", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "version", "id"
}))
@Audited
@Indexed
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
    alternateTerminologyIds =
        new HashMap<>(relationship.getAlternateTerminologyIds());
  }

  /* see superclass */
  @Override
  @XmlTransient
  public Code getFrom() {
    return from;
  }

  /* see superclass */
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
   * Returns the from terminology.
   *
   * @return the from terminology
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getFromTerminology() {
    return from == null ? null : from.getTerminology();
  }

  /**
   * Sets the from terminology.
   *
   * @param terminology the from terminology
   */
  public void setFromTerminology(String terminology) {
    if (from == null) {
      from = new CodeJpa();
    }
    from.setTerminology(terminology);
  }

  /**
   * Returns the from version.
   *
   * @return the from version
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getFromVersion() {
    return from == null ? null : from.getVersion();
  }

  /**
   * Sets the from terminology id.
   *
   * @param version the from terminology id
   */
  public void setFromVersion(String version) {
    if (from == null) {
      from = new CodeJpa();
    }
    from.setVersion(version);
  }

  /**
   * Returns the from terminology id.
   *
   * @return the from terminology id
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getFromTerminologyId() {
    return from == null ? null : from.getTerminologyId();
  }

  /**
   * Sets the from terminology id.
   *
   * @param terminologyId the from terminology id
   */
  public void setFromTerminologyId(String terminologyId) {
    if (from == null) {
      from = new CodeJpa();
    }
    from.setTerminologyId(terminologyId);
  }

  /**
   * Returns the from term. For JAXB.
   *
   * @return the from term
   */
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO, analyzer = @Analyzer(definition = "noStopWord")),
      @Field(name = "fromNameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  public String getFromName() {
    return from == null ? null : from.getName();
  }

  /**
   * Sets the from term.
   *
   * @param term the from term
   */
  public void setFromName(String term) {
    if (from == null) {
      from = new CodeJpa();
    }
    from.setName(term);
  }

  /* see superclass */
  @Override
  @XmlTransient
  public Code getTo() {
    return to;
  }

  /* see superclass */
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
   * Returns the to terminology id.
   *
   * @return the to terminology id
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getToTerminologyId() {
    return to == null ? null : to.getTerminologyId();
  }

  /**
   * Sets the to terminology id.
   *
   * @param terminologyId the to terminology id
   */
  public void setToTerminologyId(String terminologyId) {
    if (to == null) {
      to = new CodeJpa();
    }
    to.setTerminologyId(terminologyId);
  }

  /**
   * Returns the to terminology.
   *
   * @return the to terminology
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getToTerminology() {
    return to == null ? null : to.getTerminology();
  }

  /**
   * Sets the to terminology.
   *
   * @param terminology the to terminology
   */
  public void setToTerminology(String terminology) {
    if (to == null) {
      to = new CodeJpa();
    }
    to.setTerminology(terminology);
  }

  /**
   * Returns the to version.
   *
   * @return the to version
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getToVersion() {
    return to == null ? null : to.getVersion();
  }

  /**
   * Sets the to version.
   *
   * @param version the to version
   */
  public void setToVersion(String version) {
    if (to == null) {
      to = new CodeJpa();
    }
    to.setVersion(version);
  }

  /**
   * Returns the to term. For JAXB.
   *
   * @return the to term
   */
  @Fields({
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO),
    @Field(name = "toNameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
})
  public String getToName() {
    return to == null ? null : to.getName();
  }

  /**
   * Sets the to term.
   *
   * @param term the to term
   */
  public void setToName(String term) {
    if (to == null) {
      to = new CodeJpa();
    }
    to.setName(term);
  }

  /* see superclass */
  @Override
  @FieldBridge(impl = MapValueToCsvBridge.class)
  @Field(name = "alternateTerminologyIds", index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public Map<String, String> getAlternateTerminologyIds() {
    if (alternateTerminologyIds == null) {
      alternateTerminologyIds = new HashMap<>(2);
    }
    return alternateTerminologyIds;
  }

  /* see superclass */
  @Override
  public void setAlternateTerminologyIds(
    Map<String, String> alternateTerminologyIds) {
    this.alternateTerminologyIds = alternateTerminologyIds;
  }

  /* see superclass */
  @Override
  public void putAlternateTerminologyId(String terminology, String terminologyId) {
    if (alternateTerminologyIds == null) {
      alternateTerminologyIds = new HashMap<>(2);
    }
    alternateTerminologyIds.put(terminology, terminologyId);
  }

  /* see superclass */
  @Override
  public void removeAlternateTerminologyId(String terminology) {
    if (alternateTerminologyIds == null) {
      alternateTerminologyIds = new HashMap<>(2);
    }
    alternateTerminologyIds.remove(terminology);

  }

  /**
   * CUSTOM to support to/from/alternateTerminologyIds.
   *
   * @return the int
   * @see com.wci.umls.server.jpa.content.AbstractRelationship#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime
            * result
            + ((from == null || from.getTerminologyId() == null) ? 0 : from
                .getTerminologyId().hashCode());
    result =
        prime
            * result
            + ((to == null || to.getTerminologyId() == null) ? 0 : to
                .getTerminologyId().hashCode());
    result =
        prime
            * result
            + ((alternateTerminologyIds == null) ? 0 : alternateTerminologyIds
                .toString().hashCode());
    return result;
  }

  /**
   * Custom equals method for to/from.getTerminologyId
   *
   * @param obj the obj
   * @return true, if successful
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
    } else if (from.getTerminologyId() == null) {
      if (other.from != null && other.from.getTerminologyId() != null)
        return false;
    } else if (!from.getTerminologyId().equals(other.from.getTerminologyId()))
      return false;
    if (to == null) {
      if (other.to != null)
        return false;
    } else if (to.getTerminologyId() == null) {
      if (other.to != null && other.to.getTerminologyId() != null)
        return false;
    } else if (!to.getTerminologyId().equals(other.to.getTerminologyId()))
      return false;

    if (alternateTerminologyIds == null) {
      if (other.alternateTerminologyIds != null)
        return false;
    } else if (!alternateTerminologyIds.equals(other.alternateTerminologyIds))
      return false;
    return true;
  }

}
