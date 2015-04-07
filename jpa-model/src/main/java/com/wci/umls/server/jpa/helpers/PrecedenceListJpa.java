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

  @Override
  @XmlTransient
  public Long getId() {
    return this.id;
  }

  @Override
  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void setTermTypes(List<TermType> termTypes) {
    this.termTypes = termTypes;
  }

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
