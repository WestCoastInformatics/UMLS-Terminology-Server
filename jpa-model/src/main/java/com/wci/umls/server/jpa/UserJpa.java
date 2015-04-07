package com.wci.umls.server.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wci.umls.server.User;
import com.wci.umls.server.UserRole;

/**
 * JPA enabled implementation of {@link User}.
 */
@Entity
@Table(name = "users")
@Audited
@XmlRootElement(name = "user")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserJpa implements User {

  /** The id. */
  @Id
  @GeneratedValue
  private Long id;

  /** The user name. */
  @Column(nullable = false, unique = true, length = 25)
  private String userName;

  /** The name. */
  @Column(nullable = false, length = 25)
  private String name;

  /** The email. */
  @Column(nullable = false)
  private String email;

  /** The application role. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole applicationRole;

  /**
   * The default constructor.
   */
  public UserJpa() {
  }

  /**
   * Instantiates a new user jpa.
   *
   * @param id the id
   * @param userName the user name
   * @param name the name
   * @param email the email
   * @param applicationRole the application role
   */
  public UserJpa(long id, String userName, String name, String email,
      UserRole applicationRole) {
    super();
    this.id = id;
    this.userName = userName;
    this.name = name;
    this.email = email;
    this.applicationRole = applicationRole;
  }

  /**
   * Instantiates a new user jpa.
   *
   * @param user the user
   */
  public UserJpa(User user) {
    super();
    this.id = user.getId();
    this.userName = user.getUserName();
    this.name = user.getName();
    this.email = user.getEmail();
    this.applicationRole = user.getApplicationRole();
  }

  /**
   * Return the id.
   *
   * @return the id
   */
  @Override
  public Long getId() {
    return id;
  }

  /**
   * Returns the id in string form.
   *
   * @return the id in string form
   */
  @XmlID
  @Override
  public String getObjectId() {
    return (id == null ? "" : id.toString());
  }

  @XmlIDREF
  @Override
  public void setObjectId(String objectId) {
    this.id = Long.parseLong(objectId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.model.MapUser#getUserName()
   */
  /**
   * Returns the user name.
   *
   * @return the user name
   */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getUserName() {
    return userName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.model.MapUser#setUserName(java.lang.String)
   */
  /**
   * Sets the user name.
   *
   * @param username the user name
   */
  @Override
  public void setUserName(String username) {
    this.userName = username;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.model.MapUser#getName()
   */
  /**
   * Returns the name.
   *
   * @return the name
   */
  @Override
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.model.MapUser#setName(java.lang.String)
   */
  /**
   * Sets the name.
   *
   * @param name the name
   */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.model.MapUser#getEmail()
   */
  /**
   * Returns the email.
   *
   * @return the email
   */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getEmail() {
    return email;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.model.MapUser#setEmail(java.lang.String)
   */
  /**
   * Sets the email.
   *
   * @param email the email
   */
  @Override
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Returns the application role.
   *
   * @return the application role
   */
  @Override
  public UserRole getApplicationRole() {
    return applicationRole;
  }

  /**
   * Sets the application role.
   *
   * @param role the application role
   */
  @Override
  public void setApplicationRole(UserRole role) {
    this.applicationRole = role;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {

    return this.getId() + "," + this.getUserName() + "," + this.getEmail()
        + "," + this.getName() + "," + this.getApplicationRole().getValue();
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result
            + ((applicationRole == null) ? 0 : applicationRole.hashCode());
    result = prime * result + ((email == null) ? 0 : email.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((userName == null) ? 0 : userName.hashCode());
    return result;
  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    UserJpa other = (UserJpa) obj;
    if (applicationRole != other.applicationRole)
      return false;
    if (email == null) {
      if (other.email != null)
        return false;
    } else if (!email.equals(other.email))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (userName == null) {
      if (other.userName != null)
        return false;
    } else if (!userName.equals(other.userName))
      return false;
    return true;
  }

}
