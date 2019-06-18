/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKeyClass;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.EnumBridge;
import org.hibernate.search.bridge.builtin.LongBridge;

import com.wci.umls.server.Project;
import com.wci.umls.server.User;
import com.wci.umls.server.UserPreferences;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.jpa.helpers.MapIdBridge;
import com.wci.umls.server.jpa.helpers.ProjectRoleBridge;
import com.wci.umls.server.jpa.helpers.ProjectRoleMapAdapter;

/**
 * JPA and JAXB enabled implementation of {@link User}.
 */
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = {
    "userName"
}))
@Audited
@Indexed
@XmlRootElement(name = "user")
public class UserJpa implements User {

  /** The id. */
  @TableGenerator(name = "EntityIdGenUser", table = "table_generator_users", pkColumnValue = "Entity", initialValue = 50)
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGenUser")
  private Long id;

  /** The user name. */
  @Column(nullable = false, unique = true)
  private String userName;

  /** The name. */
  @Column(nullable = false)
  private String name;

  /** The team. */
  @Column(nullable = true)
  private String team;

  /** The email. */
  @Column(nullable = false)
  private String email;

  /** The editor level. */
  @Column(nullable = false)
  private int editorLevel = 0;
  
  /** The last login */
  @Column(nullable = true)
  private Date lastLogin = null;

  /** The application role. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole applicationRole;

  /** The auth token. */
  @Transient
  private String authToken;
  
  /** Api usage counter */
  @Column(nullable = true)
  private Long apiUsageCount = 0L;
  
  /** User login counter */
  @Column(nullable = true)
  private Long loginCount = 0L;
  
  @Column(nullable = true)
  private Boolean emailVerified = false;
  
  /** The user token. */
  @Column(nullable = true)
  private String userToken = null;

  /** The user preferences. */
  @OneToOne(mappedBy = "user", targetEntity = UserPreferencesJpa.class, optional = true)
  private UserPreferences userPreferences;

  /** The project role map. */
  @ElementCollection
  @MapKeyClass(value = ProjectJpa.class)
  @Enumerated(EnumType.STRING)
  @CollectionTable(name = "user_project_role_map")
  @MapKeyJoinColumn(name = "project_id")
  @Column(name = "role")
  private Map<Project, UserRole> projectRoleMap;

  /**
   * The default constructor.
   */
  public UserJpa() {
  }

  /**
   * Instantiates a new user jpa.
   *
   * @param user the user
   */
  public UserJpa(User user) {
    super();
    id = user.getId();
    userName = user.getUserName();
    name = user.getName();
    team = user.getTeam();
    email = user.getEmail();
    editorLevel = user.getEditorLevel();
    lastLogin = user.getLastLogin();
    applicationRole = user.getApplicationRole();
    authToken = user.getAuthToken();
    userPreferences = user.getUserPreferences();
    apiUsageCount = user.getApiUsageCount();
    loginCount = user.getLoginCount();
    emailVerified = user.getEmailVerified();
    userToken = user.getUserToken();
    projectRoleMap = new HashMap<>(user.getProjectRoleMap());
  }

  /* see superclass */
  @Override
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getId() {
    return id;
  }

  /* see superclass */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getUserName() {
    return userName;
  }

  /* see superclass */
  @Override
  public void setUserName(String username) {
    this.userName = username;
  }

  /* see superclass */
  @Override
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "nameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  public String getName() {
    return name;
  }

  /* see superclass */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getTeam() {
    return team;
  }

  /* see superclass */
  @Override
  public void setTeam(String team) {
    this.team = team;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getEmail() {
    return email;
  }

  /* see superclass */
  @Override
  public void setEmail(String email) {
    this.email = email;
  }

  /* see superclass */
  @Override
  public int getEditorLevel() {
    return editorLevel;
  }

  /* see superclass */
  @Override
  public void setEditorLevel(int editorLevel) {
    this.editorLevel = editorLevel;
  }
  
  /* see superclass */
  @Override
  public Date getLastLogin() {
    return lastLogin;
  }
  
  /* see superclass */
  @Override 
  public void setLastLogin(Date lastLogin) {
	  this.lastLogin = lastLogin;
  }
  
  

  /* see superclass */
  @Override
  @Field(bridge = @FieldBridge(impl = EnumBridge.class), index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public UserRole getApplicationRole() {
    return applicationRole;
  }

  /* see superclass */
  @Override
  public void setApplicationRole(UserRole role) {
    this.applicationRole = role;
  }

  /* see superclass */
  @Override
  public String getAuthToken() {
    return authToken;
  }

  /* see superclass */
  @Override
  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result += editorLevel;
    result = prime * result
        + ((applicationRole == null) ? 0 : applicationRole.hashCode());
    result = prime * result + ((authToken == null) ? 0 : authToken.hashCode());
    result = prime * result + ((email == null) ? 0 : email.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((team == null) ? 0 : team.hashCode());
    result = prime * result + ((userName == null) ? 0 : userName.hashCode());
    result = prime * result + ((lastLogin == null) ? 0 : lastLogin.hashCode());
    result = prime * result + ((apiUsageCount == null) ? 0 : apiUsageCount.hashCode());
    result = prime * result + ((loginCount == null) ? 0 : loginCount.hashCode());
    result = prime * result + ((emailVerified == null) ? 0 : emailVerified.hashCode());
    result = prime * result + ((userToken == null) ? 0 : userToken.hashCode());
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
    UserJpa other = (UserJpa) obj;
    if (applicationRole != other.applicationRole)
      return false;
    if (authToken == null) {
      if (other.authToken != null)
        return false;
    } else if (!authToken.equals(other.authToken))
      return false;
    if (email == null) {
      if (other.email != null)
        return false;
    } else if (!email.equals(other.email))
      return false;
    if (editorLevel != other.editorLevel)
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (team == null) {
      if (other.team != null)
        return false;
    } else if (!team.equals(other.team))
      return false;
    if (userName == null) {
      if (other.userName != null)
        return false;
    } else if (!userName.equals(other.userName))
      return false;
    if (lastLogin == null) {
        if (other.lastLogin != null)
          return false;
      } else if (!lastLogin.equals(other.lastLogin))
        return false;
    if (apiUsageCount == null) {
      if (other.apiUsageCount != null)
        return false;
    } else if (!apiUsageCount.equals(other.apiUsageCount))
      return false;
    if (loginCount == null) {
      if (other.loginCount != null)
        return false;
    } else if (!loginCount.equals(other.loginCount))
      return false;
    if (emailVerified == null) {
      if (other.emailVerified != null)
        return false;
    } else if (!emailVerified.equals(other.emailVerified))
      return false;
    if (userToken == null) {
      if (other.userToken != null)
        return false;
    } else if (!userToken.equals(other.userToken))
      return false;
    
    return true;
  }

  /* see superclass */
  @XmlElement(type = UserPreferencesJpa.class)
  @Override
  public UserPreferences getUserPreferences() {
    return userPreferences;
  }

  /* see superclass */
  @Override
  public void setUserPreferences(UserPreferences preferences) {
    this.userPreferences = preferences;
  }

  /* see superclass */
	@Override
	public String toString() {
		return "UserJpa [id=" + id + ", userName=" + userName + ", name=" + name + ", team=" + team + ", email=" + email
				+ ", applicationRole=" + applicationRole + ", authToken=" + authToken + ", editorLevel=" + editorLevel
				+ ", lastLogin=" + lastLogin + ", apiUsageCount=" + apiUsageCount + ", loginCount=" + loginCount
				+ ", emailVerified=" + emailVerified + ", userToken=" + userToken + "]";
	}

  /*
   * <pre> This supports searching both for a particular role on a particular
   * project or to determine if this user is assigned to any project. For
   * example:
   * 
   * "projectRoleMap:10ADMIN" -> finds where the user has an ADMIN role on
   * project 10 "projectAnyRole:10" -> finds where the user has any role on
   * project 10 </pre>
   */
  @XmlJavaTypeAdapter(ProjectRoleMapAdapter.class)
  @Fields({
      @Field(bridge = @FieldBridge(impl = ProjectRoleBridge.class), index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "projectAnyRole", bridge = @FieldBridge(impl = MapIdBridge.class), index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  })
  @Override
  public Map<Project, UserRole> getProjectRoleMap() {
    if (projectRoleMap == null) {
      projectRoleMap = new HashMap<>();
    }
    return projectRoleMap;
  }

  /* see superclass */
  @Override
  public void setProjectRoleMap(Map<Project, UserRole> projectRoleMap) {
    this.projectRoleMap = projectRoleMap;
  }

  /* see superclass */
	@Override
	public Long getApiUsageCount() {
		return this.apiUsageCount;
	}

	/* see superclass */
	@Override
	public void setApiUsageCount(Long apiUsageCount) {
		this.apiUsageCount = apiUsageCount;
	}
	
	/* see superclass */
	@Override
	public Long getLoginCount() {
		return this.loginCount;
	}

	/* see superclass */
	@Override
	public void setLoginCount(Long loginCount) {
		this.loginCount = loginCount;
	}
	
	/* see superclass */
	@Override
	public Boolean getEmailVerified() {
		return this.emailVerified;
	}

	/* see superclass */
	@Override
	public void setEmailVerified(Boolean emailVerified) {
		this.emailVerified = emailVerified;
	}
	
	/* see superclass */
	@Override
	public String getUserToken() {
		return this.userToken;
	}

	/* see superclass */
	@Override
	public void setUserToken(String userToken) {
		this.userToken = userToken;
	}
}
