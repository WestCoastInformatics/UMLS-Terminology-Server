/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.Project;
import com.wci.umls.server.User;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.jpa.helpers.MapValueToCsvBridge;
import com.wci.umls.server.jpa.helpers.PrecedenceListJpa;
import com.wci.umls.server.jpa.helpers.UserMapUserNameBridge;
import com.wci.umls.server.jpa.helpers.UserRoleBridge;
import com.wci.umls.server.jpa.helpers.UserRoleMapAdapter;

/**
 * JPA and JAXB enabled implementation of {@link Project}.
 */
@Entity
@Table(name = "projects", uniqueConstraints = @UniqueConstraint(columnNames = {
    "name", "description"
}))
@Audited
@Indexed
@XmlRootElement(name = "project")
public class ProjectJpa implements Project {

  /** The id. */
  @TableGenerator(name = "EntityIdGen", table = "table_generator", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;

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

  /** The description. */
  @Column(nullable = false)
  private String description;

  /** Whether this project is viewable by public roles. */
  @Column(nullable = false)
  private boolean isPublic = false;

  /** The team based. */
  @Column(nullable = false)
  private boolean teamBased = false;

  /** The terminology. */
  @Column(nullable = false)
  private String terminology;

  /** The branch. */
  @Column(nullable = true)
  private String branch;

  /** The module id. */
  @Column(nullable = true)
  private String feedbackEmail;

  /** The workflow path. */
  @Column(nullable = false)
  private String workflowPath;

  /** The role map. */
  @ElementCollection
  @MapKeyClass(value = UserJpa.class)
  @Enumerated(EnumType.STRING)
  @MapKeyJoinColumn(name = "user_id")
  @Column(name = "role")
  @CollectionTable(name = "project_user_role_map")
  private Map<User, UserRole> userRoleMap;

  /** The validation checks. */
  @Column(nullable = true)
  @ElementCollection
  @CollectionTable(name = "project_validation_checks")
  private List<String> validationChecks = new ArrayList<>();

  /** The prec list. */
  @OneToOne(targetEntity = PrecedenceListJpa.class, optional = true)
  private PrecedenceList precedenceList;

  /** The semantic type category map. */
  @ElementCollection
  @Column(nullable = false)
  private Map<String, String> semanticTypeCategoryMap = new HashMap<>();

  /** The valid categories. */
  @Column(nullable = true)
  @ElementCollection
  @CollectionTable(name = "valid_categories")
  private List<String> validCategories = new ArrayList<>();

  /**
   * Instantiates an empty {@link ProjectJpa}.
   */
  public ProjectJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ProjectJpa} from the specified parameters.
   *
   * @param project the project
   */
  public ProjectJpa(Project project) {
    super();
    id = project.getId();
    lastModified = project.getLastModified();
    lastModifiedBy = project.getLastModifiedBy();
    name = project.getName();
    description = project.getDescription();
    isPublic = project.isPublic();
    teamBased = project.isTeamBased();
    terminology = project.getTerminology();
    branch = project.getBranch();
    userRoleMap = project.getUserRoleMap();
    feedbackEmail = project.getFeedbackEmail();
    precedenceList = project.getPrecedenceList();
    validCategories = project.getValidCategories();
    semanticTypeCategoryMap = project.getSemanticTypeCategoryMap();
  }

  /* see superclass */
  @Override
  public Long getId() {
    return this.id;
  }

  /* see superclass */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public Date getLastModified() {
    return lastModified;
  }

  /* see superclass */
  @Override
  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  /* see superclass */
  @Override
  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getTerminology() {
    return terminology;
  }

  /* see superclass */
  @Override
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public String getName() {
    return name;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public String getDescription() {
    return description;
  }

  /* see superclass */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /* see superclass */
  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  /* see superclass */
  @Override
  public boolean isPublic() {
    return isPublic;
  }

  /* see superclass */
  @Override
  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  /* see superclass */
  @Override
  public boolean isTeamBased() {
    return teamBased;
  }

  /* see superclass */
  @Override
  public void setTeamBased(boolean teamBased) {
    this.teamBased = teamBased;
  }

  /* see superclass */
  @XmlJavaTypeAdapter(UserRoleMapAdapter.class)
  @Fields({
      @Field(bridge = @FieldBridge(impl = UserRoleBridge.class), index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "userAnyRole", bridge = @FieldBridge(impl = UserMapUserNameBridge.class), index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  })
  @Override
  public Map<User, UserRole> getUserRoleMap() {
    if (userRoleMap == null) {
      userRoleMap = new HashMap<>();
    }
    return userRoleMap;
  }

  /* see superclass */
  @Override
  public void setUserRoleMap(Map<User, UserRole> userRoleMap) {
    this.userRoleMap = userRoleMap;
  }

  /* see superclass */
  @Override
  public String getBranch() {
    return branch;
  }

  /* see superclass */
  @Override
  public void setBranch(String branch) {
    this.branch = branch;
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
  @XmlElement
  @Override
  public List<String> getValidationChecks() {
    if (this.validationChecks == null) {
      this.validationChecks = new ArrayList<String>();
    }
    return validationChecks;
  }

  /* see superclass */
  @Override
  public void setValidationChecks(List<String> validationChecks) {
    this.validationChecks = validationChecks;
  }

  /* see superclass */
  @XmlElement
  @Override
  public List<String> getValidCategories() {
    if (this.validCategories == null) {
      this.validCategories = new ArrayList<String>();
    }
    return validCategories;
  }

  @Override
  public void setValidCategories(List<String> validCategories) {
    this.validCategories = validCategories;
  }

  /* see superclass */
  @FieldBridge(impl = MapValueToCsvBridge.class)
  @Field(name = "semanticTypeCategoryMap", index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  @Override
  public Map<String, String> getSemanticTypeCategoryMap() {
    if (semanticTypeCategoryMap == null) {
      semanticTypeCategoryMap = new HashMap<>();
    }
    return semanticTypeCategoryMap;
  }

  /* see superclass */
  @Override
  public void setSemanticTypeCategoryMap(
    Map<String, String> semanticTypeCategoryMap) {
    this.semanticTypeCategoryMap = semanticTypeCategoryMap;
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
    if (precedenceList == null) {
      precedenceList = new PrecedenceListJpa();
    }
    precedenceList.setId(id);
  }

  /* see superclass */
  @Override
  public String getWorkflowPath() {
    return workflowPath;
  }

  /* see superclass */
  @Override
  public void setWorkflowPath(String workflowPath) {
    this.workflowPath = workflowPath;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((branch == null) ? 0 : branch.hashCode());
    result =
        prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + (isPublic ? 1231 : 1237);
    result = prime * result + (teamBased ? 1231 : 1237);
    result =
        prime * result
            + ((feedbackEmail == null) ? 0 : feedbackEmail.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result =
        prime * result + ((userRoleMap == null) ? 0 : userRoleMap.hashCode());
    // result = prime * result + ((validationChecks == null) ? 0 :
    // validationChecks.hashCode());
    // result =
    // prime * result
    // + ((precedenceList == null) ? 0 : precedenceList.hashCode());
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
    ProjectJpa other = (ProjectJpa) obj;
    if (branch == null) {
      if (other.branch != null)
        return false;
    } else if (!branch.equals(other.branch))
      return false;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (isPublic != other.isPublic)
      return false;
    if (teamBased != other.teamBased)
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
    if (userRoleMap == null) {
      if (other.userRoleMap != null)
        return false;
    } else if (!userRoleMap.equals(other.userRoleMap))
      return false;
    if (feedbackEmail == null) {
      if (other.feedbackEmail != null)
        return false;
    } else if (!feedbackEmail.equals(other.feedbackEmail))
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
    return "ProjectJpa [id=" + id + ", lastModified=" + lastModified
        + ", lastModifiedBy=" + lastModifiedBy + ", name=" + name
        + ", description=" + description + ", isPublic=" + isPublic
        + ", terminology=" + terminology + ", branch=" + branch
        + ", userRoleMap=" + userRoleMap + ", feedbackEmail=" + feedbackEmail
        + ", precedenceList=" + precedenceList + ", validationChecks="
        + validationChecks + ", workflowPath=" + workflowPath + "]";
  }

}
