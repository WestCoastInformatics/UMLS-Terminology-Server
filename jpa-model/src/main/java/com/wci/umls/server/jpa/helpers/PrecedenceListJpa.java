/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.PrecedenceList;

/**
 * JPA-enabled implementation of {@link PrecedenceList}. This is a list of TTYs
 * used for a particular context. Individual editors can have their own TTY
 * perspectives, projects can have their own TTY perspectives, and the release
 * can have its own TTY perspective. This mechanism is used to determine which
 * atoms represent preferred names.
 */
@Entity
@Table(name = "precedence_lists")
@XmlRootElement(name = "precedenceList")
public class PrecedenceListJpa implements PrecedenceList {

  /** The id. */
  @Id
  @GeneratedValue
  private Long id;

  /** the timestamp. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date timestamp = new Date();

  /** The last modified. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastModified = new Date();

  /** The last modified. */
  @Column(nullable = false)
  private String lastModifiedBy;

  /** The name. */
  @Column(nullable = false)
  private String name;

  /** The default list. */
  @Column(nullable = false)
  private boolean defaultList = false;

  /** The terminology list. */
  @ElementCollection
  @JoinColumn(nullable = false)
  private List<String> terminologies;

  /** The term types. */
  @ElementCollection
  @JoinColumn(nullable = false)
  private List<String> termTypes;

  /**
   * Instantiates an empty {@link PrecedenceListJpa}.
   */
  public PrecedenceListJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link PrecedenceListJpa} from the specified parameters.
   *
   * @param precedenceList the precedence list
   */
  public PrecedenceListJpa(PrecedenceList precedenceList) {
    id = precedenceList.getId();
    name = precedenceList.getName();
    setPrecedence(precedenceList.getPrecedence());
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
   * com.wci.umls.server.helpers.PrecedenceList#setPrecedence(com.wci.umls.server
   * .helpers.KeyValuePairList)
   */
  @Override
  public void setPrecedence(KeyValuePairList precedence) {
    terminologies = new ArrayList<>();
    termTypes = new ArrayList<>();
    for (KeyValuePair pair : precedence.getKeyValuePairList()) {
      terminologies.add(pair.getKey());
      termTypes.add(pair.getValue());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.PrecedenceList#getPrecedence()
   */
  @Override
  public KeyValuePairList getPrecedence() {
    KeyValuePairList precedence = new KeyValuePairList();
    for (int i = 0; i < termTypes.size(); i++) {
      final KeyValuePair pair = new KeyValuePair();
      pair.setKey(terminologies.get(i));
      pair.setValue(terminologies.get(i));
      precedence.addKeyValuePair(pair);
    }
    return precedence;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasLastModified#getTimestamp()
   */
  @Override
  public Date getTimestamp() {
    return timestamp;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.HasLastModified#setTimestamp(java.util.Date)
   */
  @Override
  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasLastModified#getLastModified()
   */
  @Override
  public Date getLastModified() {
    return lastModified;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.HasLastModified#setLastModified(java.util.Date)
   */
  @Override
  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasLastModified#getLastModifiedBy()
   */
  @Override
  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.HasLastModified#setLastModifiedBy(java.lang
   * .String)
   */
  @Override
  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  /**
   * Returns the terminologies.
   *
   * @return the terminologies
   */
  public List<String> getTerminologies() {
    return terminologies;
  }

  /**
   * Sets the terminologies.
   *
   * @param terminologies the terminologies
   */
  public void setTerminologies(List<String> terminologies) {
    this.terminologies = terminologies;
  }

  /**
   * Returns the term types.
   *
   * @return the term types
   */
  public List<String> getTermTypes() {
    return termTypes;
  }

  /**
   * Sets the term types.
   *
   * @param termTypes the term types
   */
  public void setTermTypes(List<String> termTypes) {
    this.termTypes = termTypes;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "PrecedenceListJpa [id=" + id + ", timestamp=" + timestamp
        + ", lastModified=" + lastModified + ", lastModifiedBy="
        + lastModifiedBy + ", name=" + name + ", defaultList=" + defaultList
        + ", terminologies=" + terminologies + ", termTypes=" + termTypes + "]";
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (defaultList ? 1231 : 1237);
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((termTypes == null) ? 0 : termTypes.hashCode());
    result =
        prime * result
            + ((terminologies == null) ? 0 : terminologies.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PrecedenceListJpa other = (PrecedenceListJpa) obj;
    if (defaultList != other.defaultList)
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (termTypes == null) {
      if (other.termTypes != null)
        return false;
    } else if (!termTypes.equals(other.termTypes))
      return false;
    if (terminologies == null) {
      if (other.terminologies != null)
        return false;
    } else if (!terminologies.equals(other.terminologies))
      return false;
    return true;
  }

}
