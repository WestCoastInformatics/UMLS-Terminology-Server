/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.jpa.meta.TermTypeJpa;
import com.wci.umls.server.model.meta.TermType;

/**
 * JPA-enabled implementation of {@link PrecedenceList}. This is a list of TTYs
 * used for a particular context. Individual editors can have their own TTY
 * perspectives, projects can have their own TTY perspectives, and the release
 * can have its own TTY perspective. This mechanism is used to determine which
 * atoms represent preferred names.
 */
@Entity
@Table(name = "precedence_list")
@XmlRootElement(name = "precedenceList")
public class PrecedenceListJpa implements PrecedenceList {

  /** The id. */
  @Id
  @GeneratedValue
  private Long id;

  /** The name. */
  @Column(nullable = false)
  private String name;

  /** The default list. */
  @Column(nullable = false)
  private boolean defaultList = false;

  /** The descriptions. */
  @ManyToMany(targetEntity = TermTypeJpa.class)
  private List<TermType> termTypes = null;

  /**
   * Instantiates an empty {@link PrecedenceListJpa}.
   */
  protected PrecedenceListJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link PrecedenceListJpa} from the specified parameters.
   *
   * @param precedenceList the precedence list
   */
  protected PrecedenceListJpa(PrecedenceList precedenceList) {
    id = precedenceList.getId();
    name = precedenceList.getName();
    termTypes = precedenceList.getTermTypes();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.PrecedenceList#getId()
   */
  @Override
  @XmlTransient
  public Long getId() {
    return this.id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.PrecedenceList#setId(java.lang.Long)
   */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.PrecedenceList#isDefaultList()
   */
  @Override
  public boolean isDefaultList() {
    return defaultList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.PrecedenceList#setDefaultList(boolean)
   */
  @Override
  public void setDefaultList(boolean defaultList) {
    this.defaultList = defaultList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.PrecedenceList#getName()
   */
  @Override
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.PrecedenceList#setName(java.lang.String)
   */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.PrecedenceList#setTermTypes(java.util.List)
   */
  @Override
  public void setTermTypes(List<TermType> termTypes) {
    this.termTypes = termTypes;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.PrecedenceList#getTermTypes()
   */
  @Override
  public List<TermType> getTermTypes() {
    return termTypes;
  }

  // TODO
  // isDlEnabled?
  // isreflexive, is transitive, is functional, etc.
  // domain/range
  // is non-grouping
  // property chains.

}
