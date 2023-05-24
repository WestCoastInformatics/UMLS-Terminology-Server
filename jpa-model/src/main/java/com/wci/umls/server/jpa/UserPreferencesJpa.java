/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;

import com.wci.umls.server.User;
import com.wci.umls.server.UserPreferences;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.jpa.helpers.PrecedenceListJpa;

/**
 * JPA and JAXB enabled implementation of {@link UserPreferences}.
 */
@Entity
@Table(name = "user_preferences")
//@Audited
@XmlRootElement(name = "userPreferences")
public class UserPreferencesJpa implements UserPreferences {

  /** The id. */
  @TableGenerator(name = "EntityIdGenUser", table = "table_generator_users", pkColumnValue = "Entity", initialValue = 50)
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGenUser")
  private Long id;

  /** The user name. */
  @OneToOne(targetEntity = UserJpa.class)
  private User user;

  /** The lastProjectId. */
  @Column(nullable = true)
  private Long lastProjectId;

  /** The module id. */
  @Column(nullable = true)
  private String feedbackEmail;

  /** The lastTab. */
  @Column(nullable = true)
  private String lastTab;

  /** The lastProjectRole. */
  @Enumerated(value = EnumType.STRING)
  @Column(nullable = true)
  private UserRole lastProjectRole;

  /** The lastTerminology. */
  @Column(nullable = true)
  private String lastTerminology;

  /** The prec list. */
  @OneToOne(targetEntity = PrecedenceListJpa.class, optional = true)
  private PrecedenceList precedenceList = null;

  /** The favorites. */
  @ElementCollection
  private List<String> favorites = null;

  /** The properties. */
  @ElementCollection
  @MapKeyColumn(length = 100)
  @Column(nullable = true, length = 4000)
  private Map<String, String> properties;

  /**
   * Instantiates an empty {@link UserPreferencesJpa}.
   */
  public UserPreferencesJpa() {
    // n/a
  }

  /**
   * Instantiates a new user jpa.
   *
   * @param prefs the user preferences
   */
  public UserPreferencesJpa(UserPreferences prefs) {
    super();
    id = prefs.getId();
    user = prefs.getUser();
    feedbackEmail = prefs.getFeedbackEmail();
    lastTab = prefs.getLastTab();
    lastProjectId = prefs.getLastProjectId();
    lastTerminology = prefs.getLastTerminology();
    lastProjectRole = prefs.getLastProjectRole();
    precedenceList = prefs.getPrecedenceList();
    favorites = new ArrayList<>(prefs.getFavorites());
    properties = new HashMap<>(prefs.getProperties());
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
    return user == null ? null : user.getId();
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
   * Returns the last tab accessed.
   *
   * @return the lastTab
   */
  @Override
  public String getLastTab() {
    return lastTab;
  }

  /**
   * Sets the last tab accessed.
   *
   * @param lastTab the last tab accessed
   */
  @Override
  public void setLastTab(String lastTab) {
    this.lastTab = lastTab;
  }

  /* see superclass */
  @Override
  public String getLastTerminology() {
    return lastTerminology;
  }

  /* see superclass */
  @Override
  public void setLastTerminology(String lastTerminology) {
    this.lastTerminology = lastTerminology;
  }

  /**
   * Returns the last project ID accessed.
   *
   * @return the lastProjectId
   */
  @Override
  public Long getLastProjectId() {
    return lastProjectId;
  }

  /**
   * Sets the last project accessed.
   *
   * @param lastProjectId the last project id
   */
  @Override
  public void setLastProjectId(Long lastProjectId) {
    this.lastProjectId = lastProjectId;
  }

  /* see superclass */
  @Override
  public UserRole getLastProjectRole() {
    return lastProjectRole;
  }

  /* see superclass */
  @Override
  public void setLastProjectRole(UserRole lastProjectRole) {
    this.lastProjectRole = lastProjectRole;

  }

  /* see superclass */
  @Override
  public String getFeedbackEmail() {
    return feedbackEmail;
  }

  /* see superclass */
  @Override
  public void setFeedbackEmail(String feedbackEmail) {
    this.feedbackEmail = feedbackEmail;
  }

  /* see superclass */
  @XmlTransient
  @Override
  public PrecedenceList getPrecedenceList() {
    return precedenceList;
  }

  /* see superclass */
  @Override
  public void setPrecedenceList(PrecedenceList precedenceList) {
    this.precedenceList = precedenceList;
  }

  /**
   * Returns the precedence list id.
   *
   * @return the precedence list id
   */
  public Long getPrecedenceListId() {
    return precedenceList != null ? precedenceList.getId() : null;
  }

  /**
   * Sets the precedence list id.
   *
   * @param id the precedence list id
   */
  public void setPrecedenceListId(Long id) {
    if (id != null) {
      if (precedenceList == null) {
        precedenceList = new PrecedenceListJpa();
      }
      precedenceList.setId(id);
    }
  }

  /* see superclass */
  @Override
  public void setFavorites(List<String> favorites) {
    this.favorites = favorites;
  }

  /* see superclass */
  @Override
  public List<String> getFavorites() {
    return this.favorites;
  }

  /* see superclass */
  @Override
  public Map<String, String> getProperties() {
    if (properties == null) {
      properties = new HashMap<>();
    }
    return properties;
  }

  /* see superclass */
  @Override
  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((lastTab == null) ? 0 : lastTab.hashCode());
    result =
        prime * result + ((properties == null) ? 0 : properties.hashCode());
    result = prime * result
        + ((lastTerminology == null) ? 0 : lastTerminology.hashCode());
    result = prime * result
        + ((feedbackEmail == null) ? 0 : feedbackEmail.hashCode());
    result = prime * result
        + ((lastProjectId == null) ? 0 : lastProjectId.hashCode());
    result = prime * result
        + ((lastProjectRole == null) ? 0 : lastProjectRole.hashCode());
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

    if (user == null) {
      if (other.user != null)
        return false;
    } else if (!user.getUserName().equals(other.user.getUserName()))
      return false;
    if (lastTab == null) {
      if (other.lastTab != null)
        return false;
    } else if (!lastTab.equals(other.lastTab))
      return false;
    if (lastTerminology == null) {
      if (other.lastTerminology != null)
        return false;
    } else if (!lastTerminology.equals(other.lastTerminology))
      return false;
    if (lastProjectId == null) {
      if (other.lastProjectId != null)
        return false;
    } else if (!lastProjectId.equals(other.lastProjectId))
      return false;
    if (lastProjectRole == null) {
      if (other.lastProjectRole != null)
        return false;
    } else if (!lastProjectRole.equals(other.lastProjectRole))
      return false;
    if (feedbackEmail == null) {
      if (other.feedbackEmail != null)
        return false;
    } else if (!feedbackEmail.equals(other.feedbackEmail))
      return false;
    if (properties == null) {
      if (other.properties != null)
        return false;
    } else if (!properties.equals(other.properties))
      return false;
    // if (precedenceList == null) {
    // if (other.precedenceList != null)
    // return false;
    // } else if (!precedenceList.equals(other.precedenceList))
    // return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "UserPreferencesJpa [id=" + id + ", user=" + user
        + ", lastTerminology=" + lastTerminology + ", lastProjectId="
        + lastProjectId + ", lastProjectRole=" + lastProjectRole + ", lastTab="
        + lastTab + ", feedbackEmail=" + feedbackEmail + ", precedenceList="
        + precedenceList + properties + "]";
  }

}
