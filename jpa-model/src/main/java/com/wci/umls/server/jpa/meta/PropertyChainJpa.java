/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.meta;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.meta.PropertyChain;
import com.wci.umls.server.model.meta.RelationshipType;

/**
 * JPA-enabled implementation of {@link PropertyChain}.
 */
@Entity
@Table(name = "property_chains", uniqueConstraints = @UniqueConstraint(columnNames = {
  "abbreviation"
}))
@Audited
@XmlRootElement(name = "propertyChain")
public class PropertyChainJpa extends AbstractAbbreviation implements
    PropertyChain {

  /** The chain. */
  @ElementCollection
  @Column(nullable = false)
  private List<RelationshipType> chain;

  /** The result. */
  @ManyToOne(targetEntity = RelationshipTypeJpa.class, optional = false)
  private RelationshipType result;

  /**
   * Instantiates an empty {@link PropertyChainJpa}.
   */
  public PropertyChainJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link PropertyChainJpa} from the specified parameters.
   *
   * @param chain the chain
   */
  public PropertyChainJpa(PropertyChain chain) {
    super(chain);
    this.chain = chain.getChain();
    result = chain.getResult();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.PropertyChain#getChain()
   */
  @Override
  public List<RelationshipType> getChain() {
    if (chain == null) {
      chain = new ArrayList<>();
    }
    return chain;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.PropertyChain#setChain(java.util.List)
   */
  @Override
  public void setChain(List<RelationshipType> chain) {
    this.chain = chain;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.PropertyChain#getResult()
   */
  @Override
  public RelationshipType getResult() {
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.PropertyChain#setResult(com.wci.umls.server
   * .model.meta.RelationshipType)
   */
  @Override
  public void setResult(RelationshipType result) {
    this.result = result;
  }

}
