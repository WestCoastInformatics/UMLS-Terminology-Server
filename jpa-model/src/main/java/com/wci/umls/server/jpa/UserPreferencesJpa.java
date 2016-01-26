/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;

import com.wci.umls.server.User;
import com.wci.umls.server.UserPreferences;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.jpa.helpers.PrecedenceListJpa;

/**
 * JPA enabled implementation of {@link UserPreferences}.
 */
@Entity
@Table(name = "user_preferences")
@Audited
@XmlRootElement(name = "userPreferences")
public class UserPreferencesJpa implements UserPreferences {

  /** The id. */
  @Id
  @GeneratedValue
  private Long id;

  /** The user name. */
  @OneToOne(targetEntity = UserJpa.class)
  private User user;

  /** The precedence list. */
  @OneToOne(targetEntity = UserJpa.class, optional = false)
  @JoinColumn(nullable = false)
  private PrecedenceList precedenceList;

  /**
   * The default constructor.
   */
  public UserPreferencesJpa() {
  }

  /**
   * Instantiates a new user jpa.
   *
   * @param userPreferences the user preferences
   */
  public UserPreferencesJpa(UserPreferences userPreferences) {
    super();
    id = userPreferences.getId();
    user = userPreferences.getUser();
    precedenceList = userPreferences.getPrecedenceList();
  }

  /**
   * Returns the id.
   *
   * @return the id
   */
  @Override
  public Long getId() {
    return id;
  }

  /**
   * Sets the id.
   *
   * @param id the id
   */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Returns the object id. Needed for JAXB id
   *
   * @return the object id
   */
  @XmlID
  public String getObjectId() {
    return id == null ? "" : id.toString();
  }

  /**
   * Sets the object id.
   *
   * @param id the object id
   */
  public void setObjectId(String id) {
    if (id != null) {
      this.id = Long.parseLong(id);
    }
  }

  /**
   * Returns the user.
   *
   * @return the user
   */
  @XmlTransient
  @Override
  public User getUser() {
    return user;
  }

  /**
   * Sets the user.
   *
   * @param user the user
   */
  @Override
  public void setUser(User user) {
    this.user = user;
  }

  /**
   * Returns the user id.
   *
   * @return the user id
   */
  public Long getUserId() {
    return user == null ? 0L : user.getId();
  }

  /**
   * Sets the user id.
   *
   * @param id the user id
   */
  public void setUserId(Long id) {
    if (user == null) {
      user = new UserJpa();
    }
    user.setId(id);
  }

  /**
   * Returns the user name.
   *
   * @return the user name
   */
  public String getUserName() {
    return user == null ? "" : user.getUserName();
  }

  /**
   * Sets the user name.
   *
   * @param userName the user name
   */
  public void setUserName(String userName) {
    if (user == null) {
      user = new UserJpa();
    }
    user.setUserName(userName);
  }

  /**
   * Returns the precedence list.
   *
   * @return the precedence list
   */
  @XmlElement(type = PrecedenceListJpa.class)
  @Override
  public PrecedenceList getPrecedenceList() {
    return precedenceList;
  }

  /**
   * Sets the precedence list.
   *
   * @param list the precedence list
   */
  @Override
  public void setPrecedenceList(PrecedenceList list) {
    this.precedenceList = list;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result
            + ((precedenceList == null) ? 0 : precedenceList.hashCode());
    result = prime * result + ((user == null) ? 0 : user.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    UserPreferencesJpa other = (UserPreferencesJpa) obj;
    if (precedenceList == null) {
      if (other.precedenceList != null)
        return false;
    } else if (!precedenceList.equals(other.precedenceList))
      return false;
    if (user == null) {
      if (other.user != null)
        return false;
    } else if (!user.getUserName().equals(other.user.getUserName()))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "UserPreferencesJpa [id=" + id + ", user=" + user
        + ", precedenceList=" + precedenceList + "]";
  }

}
