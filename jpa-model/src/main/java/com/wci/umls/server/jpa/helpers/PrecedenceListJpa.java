/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CollectionTable;
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

import com.wci.umls.server.helpers.Branch;
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

  /** The terminology. */
  @Column(nullable = false)
  private String terminology;

  /** The expandedForm. */
  @Column(nullable = false)
  private String version;

  /** The branch. */
  @Column(nullable = true)
  private String branch = Branch.ROOT;

  /** The name. */
  @Column(nullable = false)
  private String name;

  /** The default list. */
  @Column(nullable = false)
  private boolean defaultList = false;

  /** The terminology list. */
  @ElementCollection
  @CollectionTable(name = "precedence_list_terminologies")
  @JoinColumn(nullable = false)
  private List<String> terminologies;

  /** The term types. */
  @ElementCollection
  @CollectionTable(name = "precedence_list_term_types")
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
    terminology = precedenceList.getTerminology();
    version = precedenceList.getVersion();
    branch = precedenceList.getBranch();
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
   * @see com.wci.umls.server.helpers.PrecedenceList#getBranch()
   */
  @Override
  public String getBranch() {
    return branch;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.PrecedenceList#setBranch(java.lang.String)
   */
  @Override
  public void setBranch(String branch) {
    this.branch = branch;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasTerminology#getTerminology()
   */
  @Override
  public String getTerminology() {
    return terminology;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.HasTerminology#setTerminology(java.lang.String)
   */
  @Override
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasTerminology#getVersion()
   */
  @Override
  public String getVersion() {
    return version;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.HasTerminology#setVersion(java.lang.String)
   */
  @Override
  public void setVersion(String version) {
    this.version = version;
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
    if (precedence == null) {
      return;
    }
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
      pair.setValue(termTypes.get(i));
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.PrecedenceList#addTerminologyTermType(java.
   * lang.String, java.lang.String)
   */
  @Override
  public void addTerminologyTermType(String terminology, String termType) {
    terminologies.add(terminology);
    termTypes.add(termType);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.PrecedenceList#removeTerminologyTermType(java
   * .lang.String, java.lang.String)
   */
  @Override
  public void removeTerminologyTermType(String terminology, String termType) {
    for (int i = 0; i < termTypes.size(); i++) {
      if (terminology.equals(terminologies.get(i))
          && termType.equals(termTypes.get(i))) {
        terminologies.remove(i);
        termTypes.remove(i);
        break;
      }
    }
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
        + lastModifiedBy + ", terminology=" + terminology + ", version="
        + version + ", branch=" + branch + ", name=" + name + ", defaultList="
        + defaultList + ", terminologies=" + terminologies + ", termTypes="
        + termTypes + "]";
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
    result = prime * result + ((terminology == null) ? 0 : version.hashCode());
    result = prime * result + ((version == null) ? 0 : terminology.hashCode());
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
    if (terminology == null) {
      if (other.terminology != null)
        return false;
    } else if (!terminology.equals(other.terminology))
      return false;
    if (version == null) {
      if (other.version != null)
        return false;
    } else if (!version.equals(other.version))
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
