/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.meta;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.PropertyChain;

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
  @ManyToMany(targetEntity = AdditionalRelationshipTypeJpa.class)
  @JoinTable(name = "projects_chains_chain", joinColumns = @JoinColumn(name = "chain_id"), inverseJoinColumns = @JoinColumn(name = "type_id"))
  private List<AdditionalRelationshipType> chain;

  /** The result. */
  @ManyToOne(targetEntity = AdditionalRelationshipTypeJpa.class, optional = false)
  private AdditionalRelationshipType result;

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
  public List<AdditionalRelationshipType> getChain() {
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
  public void setChain(List<AdditionalRelationshipType> chain) {
    this.chain = chain;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.PropertyChain#getResult()
   */
  @Override
  public AdditionalRelationshipType getResult() {
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.PropertyChain#setResult(com.wci.umls.server
   * .model.meta.AdditionalRelationshipType)
   */
  @Override
  public void setResult(AdditionalRelationshipType result) {
    this.result = result;
  }

}
