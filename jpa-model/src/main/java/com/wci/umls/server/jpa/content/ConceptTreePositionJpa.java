/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.LongBridge;

import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptTreePosition;

/**
 * JPA-enabled implementation of {@link ConceptTreePosition}.
 */
@Entity
@Table(name = "concept_tree_positions", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "terminologyVersion", "id"
}))
@Audited
@Indexed
@XmlRootElement(name = "conceptTreePosition")
public class ConceptTreePositionJpa extends AbstractTreePosition<Concept>
    implements ConceptTreePosition {

  /** The concept. */
  @ManyToOne(targetEntity = ConceptJpa.class, fetch = FetchType.EAGER, optional = false)
  @JoinColumn(nullable = false)
  private Concept node;

  /**
   * Instantiates an empty {@link ConceptTreePositionJpa}.
   */
  public ConceptTreePositionJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ConceptTreePositionJpa} from the specified
   * parameters.
   *
   * @param treepos the treepos
   * @param deepCopy the deep copy
   */
  public ConceptTreePositionJpa(ConceptTreePosition treepos, boolean deepCopy) {
    super(treepos, deepCopy);
    node = treepos.getNode();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.TreePosition#getNode()
   */
  @XmlTransient
  @Override
  public Concept getNode() {
    return node;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.TreePosition#setNode(com.wci.umls.server
   * .model.content.ComponentHasAttributesAndName)
   */
  @Override
  public void setNode(Concept concept) {
    this.node = concept;
  }

  /**
   * Returns the node id. For JAXB.
   *
   * @return the node id
   */
  @XmlElement
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @FieldBridge(impl = LongBridge.class)
  public Long getNodeId() {
    return node == null ? null : node.getId();
  }

  /**
   * Sets the node id. For JAXB.
   *
   * @param id the node id
   */
  public void setNodeId(Long id) {
    if (node == null) {
      node = new ConceptJpa();
    }
    node.setId(id);
  }

  /**
   * Returns the node name. For JAXB.
   *
   * @return the node name
   */
  @Field(index = Index.YES, store = Store.NO, analyze = Analyze.YES, analyzer = @Analyzer(definition = "noStopWord"))
  public String getNodeName() {
    return node == null ? null : node.getName();
  }

  /**
   * Sets the node name. For JAXB.
   *
   * @param name the node name
   */
  public void setNodeName(String name) {
    if (node == null) {
      node = new ConceptJpa();
    }
    node.setName(name);
  }

  /**
   * Returns the node terminology id. For JAXB.
   *
   * @return the node terminology id
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getNodeTerminologyId() {
    return node == null ? null : node.getTerminologyId();
  }

  /**
   * Sets the node terminology id. For JAXB.
   *
   * @param terminologyId the node terminology id
   */
  public void setNodeTerminologyId(String terminologyId) {
    if (node == null) {
      node = new ConceptJpa();
    }
    node.setTerminologyId(terminologyId);
  }

  /**
   * Returns the node terminology. For JAXB.
   *
   * @return the node terminology
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getNodeTerminology() {
    return node == null ? null : node.getTerminology();
  }

  /**
   * Sets the node terminology. For JAXB.
   *
   * @param terminology the node terminology
   */
  public void setNodeTerminology(String terminology) {
    if (node == null) {
      node = new ConceptJpa();
    }
    node.setTerminology(terminology);
  }

  /**
   * Returns the node terminology version. For JAXB.
   *
   * @return the node terminology version
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getNodeTerminologyVersion() {
    return node == null ? null : node.getTerminologyVersion();
  }

  /**
   * Sets the node terminology version. For JAXB.
   *
   * @param terminologyVersion the node terminology version
   */
  public void setNodeTerminologyVersion(String terminologyVersion) {
    if (node == null) {
      node = new ConceptJpa();
    }
    node.setTerminologyVersion(terminologyVersion);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractTreePosition#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime
            * result
            + ((node == null || node.getTerminologyId() == null) ? 0 : node
                .getTerminologyId().hashCode());
    return result;
  }

  /**
   * CUSTOM for concept id.
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
    ConceptTreePositionJpa other = (ConceptTreePositionJpa) obj;
    if (node == null) {
      if (other.node != null)
        return false;
    } else if (node.getTerminologyId() == null) {
      if (other.node != null && other.node.getTerminologyId() != null)
        return false;
    } else if (!node.getTerminologyId().equals(other.node.getTerminologyId()))
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractTreePosition#toString()
   */
  @Override
  public String toString() {
    return "ConceptTreePositionJpa [concept=" + node + "]";
  }

}
