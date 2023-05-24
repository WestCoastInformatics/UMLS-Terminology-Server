/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
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
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.SortableField;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.LongBridge;

import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomTreePosition;
import com.wci.umls.server.model.content.Attribute;

/**
 * JPA-enabled implementation of {@link AtomTreePosition}.
 */
@Entity
@Table(name = "atom_tree_positions", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "version", "id"
}))
//@Audited
@Indexed
@XmlRootElement(name = "atomTreePosition")
public class AtomTreePositionJpa extends AbstractTreePosition<Atom> implements AtomTreePosition {

  /** The atom. */
  @ManyToOne(targetEntity = AtomJpa.class, fetch = FetchType.EAGER, optional = false)
  @JoinColumn(nullable = false)
  private Atom node;

  /** The attributes. */
  @OneToMany(targetEntity = AttributeJpa.class)
  @JoinColumn(name = "attributes_id")
  @JoinTable(name = "atom_tree_positions_attributes",
      inverseJoinColumns = @JoinColumn(name = "attributes_id"),
      joinColumns = @JoinColumn(name = "atom_tree_positions_id"))
  private List<Attribute> attributes = null;

  /**
   * Instantiates an empty {@link AtomTreePositionJpa}.
   */
  public AtomTreePositionJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link AtomTreePositionJpa} from the specified parameters.
   *
   * @param treepos the treepos
   * @param collectionCopy the deep copy
   */
  public AtomTreePositionJpa(AtomTreePosition treepos, boolean collectionCopy) {
    super(treepos, collectionCopy);
    node = treepos.getNode();
    if (collectionCopy) {
      for (final Attribute attribute : treepos.getAttributes()) {
        getAttributes().add(new AttributeJpa(attribute));
      }
    }
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

  @XmlTransient
  @Override
  public Atom getNode() {
    return node;
  }

  @Override
  public void setNode(Atom atom) {
    this.node = atom;
  }

  /**
   * Returns the node id. For JAXB.
   *
   * @return the node id
   */
  @XmlElement
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
      node = new AtomJpa();
    }
    node.setId(id);
  }

  /**
   * Returns the node name. For JAXB.
   *
   * @return the node name
   */
  @Fields({
      @Field(name = "nodeName", index = Index.YES, store = Store.NO, analyze = Analyze.YES,
          analyzer = @Analyzer(definition = "noStopWord")),
      @Field(name = "nodeNameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  @SortableField(forField = "nodeNameSort")
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
      node = new AtomJpa();
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
      node = new AtomJpa();
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
      node = new AtomJpa();
    }
    node.setTerminology(terminology);
  }

  /**
   * Returns the node version. For JAXB.
   *
   * @return the node version
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getNodeVersion() {
    return node == null ? null : node.getVersion();
  }

  /**
   * Sets the node version. For JAXB.
   *
   * @param version the node version
   */
  public void setNodeVersion(String version) {
    if (node == null) {
      node = new AtomJpa();
    }
    node.setVersion(version);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((node == null || node.getTerminologyId() == null) ? 0
        : node.getTerminologyId().hashCode());
    return result;
  }

  /**
   * CUSTOM for atom id.
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
    AtomTreePositionJpa other = (AtomTreePositionJpa) obj;
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

}
