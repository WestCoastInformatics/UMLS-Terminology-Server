/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.Project;
import com.wci.umls.server.User;

/**
 * JPA enabled implementation of {@link Project}.
 */
@Entity
@Table(name = "projects", uniqueConstraints = @UniqueConstraint(columnNames = {
    "name", "description"
}) )
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
  @Column(unique = false, nullable = false)
  private boolean isPublic = false;

  /** The terminology. */
  @Column(nullable = false)
  private String terminology;

  /** The version. */
  @Column(nullable = false)
  private String version;

  /** The leads. */
  @ManyToMany(targetEntity = UserJpa.class, fetch = FetchType.EAGER)
  @JoinTable(name = "projects_leads", joinColumns = @JoinColumn(name = "projects_id") , inverseJoinColumns = @JoinColumn(name = "users_id") )
  @IndexedEmbedded(targetElement = UserJpa.class)
  private Set<User> leads = new HashSet<>();

  /** The authors. */
  @ManyToMany(targetEntity = UserJpa.class, fetch = FetchType.EAGER)
  @JoinTable(name = "projects_authors", joinColumns = @JoinColumn(name = "projects_id") , inverseJoinColumns = @JoinColumn(name = "users_id") )
  @IndexedEmbedded(targetElement = UserJpa.class)
  private Set<User> authors = new HashSet<>();

  /** The administrators. */
  @ManyToMany(targetEntity = UserJpa.class, fetch = FetchType.EAGER)
  @JoinTable(name = "projects_administrators", joinColumns = @JoinColumn(name = "projects_id") , inverseJoinColumns = @JoinColumn(name = "users_id") )
  @IndexedEmbedded(targetElement = UserJpa.class)
  private Set<User> administrators = new HashSet<>();

  /** The concepts in scope for this project. */
  @ElementCollection
  @CollectionTable(name = "projects_scope_concepts", joinColumns = @JoinColumn(name = "id") )
  @Column(nullable = true)
  private Set<String> scopeConcepts = new HashSet<>();

  /** The concepts excludes from scope of this project. */
  @ElementCollection
  @CollectionTable(name = "projects_scope_excludes_concepts", joinColumns = @JoinColumn(name = "id") )
  @Column(nullable = true)
  private Set<String> scopeExcludesConcepts = new HashSet<>();

  /** Indicates if descendants of the scope are included in the scope. */
  @Column(unique = false, nullable = false)
  private boolean scopeDescendantsFlag = false;

  /**
   * Indicates if descendants of the excludes scope are excludes from the scope.
   */
  @Column(unique = false, nullable = false)
  private boolean scopeExcludesDescendantsFlag = false;

  /** The concepts excludes from scope of this project. */
  @ElementCollection
  @Column(nullable = true)
  private Set<String> actionWorkflowStatusValues = new HashSet<>();

  /** The branch. */
  @Column(nullable = true)
  private String branch;

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
    terminology = project.getTerminology();
    version = project.getVersion();
    leads = new HashSet<>(project.getLeads());
    authors = new HashSet<>(project.getAuthors());
    administrators = new HashSet<>(project.getAdministrators());
    scopeConcepts = new HashSet<>(project.getScopeConcepts());
    scopeDescendantsFlag = project.getScopeDescendantsFlag();
    scopeExcludesConcepts = new HashSet<>(project.getScopeExcludesConcepts());
    scopeExcludesDescendantsFlag = project.getScopeExcludesDescendantsFlag();
    branch = project.getBranch();
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

  /**
   * Returns the object id. For JAXB.
   *
   * @return the object id
   */
  public String getObjectId() {
    return id == null ? "" : id.toString();
  }

  /**
   * Sets the object id. For JAXB.
   *
   * @param id the object id
   */
  public void setObjectId(String id) {
    this.id = Long.parseLong(id);
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
  @XmlElement(type = UserJpa.class)
  public Set<User> getLeads() {
    return leads;
  }

  /* see superclass */
  @Override
  public void setLeads(Set<User> leads) {
    this.leads = leads;
  }

  /* see superclass */
  @Override
  public void addLead(User lead) {
    leads.add(lead);
  }

  /* see superclass */
  @Override
  public void removeLead(User lead) {
    leads.remove(lead);
  }

  /* see superclass */
  @Override
  @XmlElement(type = UserJpa.class)
  public Set<User> getAuthors() {
    return authors;
  }

  /* see superclass */
  @Override
  public void setAuthors(Set<User> authors) {
    this.authors = authors;
  }

  /* see superclass */
  @Override
  public void addAuthor(User author) {
    authors.add(author);
  }

  /* see superclass */
  @Override
  public void removeAuthor(User author) {
    authors.remove(author);
  }

  /* see superclass */
  @Override
  @XmlElement(type = UserJpa.class)
  public Set<User> getAdministrators() {
    return administrators;
  }

  /* see superclass */
  @Override
  public void setAdministrators(Set<User> administrators) {
    this.administrators = administrators;
  }

  /* see superclass */
  @Override
  public void addAdministrator(User administrator) {
    administrators.add(administrator);
  }

  /* see superclass */
  @Override
  public void removeAdministrator(User administrator) {
    administrators.remove(administrator);
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
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getVersion() {
    return version;
  }

  /* see superclass */
  @Override
  public void setVersion(String version) {
    this.version = version;
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
  public Set<String> getScopeConcepts() {
    return scopeConcepts;
  }

  /* see superclass */
  @Override
  public void setScopeConcepts(Set<String> scopeConcepts) {
    this.scopeConcepts = scopeConcepts;
  }

  /* see superclass */
  @Override
  public void addScopeConcept(String terminologyId) {
    this.scopeConcepts.add(terminologyId);
  }

  /* see superclass */
  @Override
  public void removeScopeConcept(String terminologyId) {
    this.scopeConcepts.remove(terminologyId);
  }

  /* see superclass */
  @Override
  public boolean getScopeDescendantsFlag() {
    return scopeDescendantsFlag;
  }

  /* see superclass */
  @Override
  public void setScopeDescendantsFlag(boolean flag) {
    scopeDescendantsFlag = flag;
  }

  /* see superclass */
  @Override
  public Set<String> getScopeExcludesConcepts() {
    return scopeExcludesConcepts;
  }

  /* see superclass */
  @Override
  public void setScopeExcludesConcepts(Set<String> scopeExcludesConcepts) {
    this.scopeExcludesConcepts = scopeExcludesConcepts;
  }

  /* see superclass */
  @Override
  public void addScopeExcludesConcept(String terminologyId) {
    this.scopeExcludesConcepts.add(terminologyId);
  }

  /* see superclass */
  @Override
  public void removeScopeExcludesConcept(String terminologyId) {
    this.scopeExcludesConcepts.remove(terminologyId);
  }

  /* see superclass */
  @Override
  public boolean getScopeExcludesDescendantsFlag() {
    return scopeExcludesDescendantsFlag;
  }

  /* see superclass */
  @Override
  public void setScopeExcludesDescendantsFlag(boolean flag) {
    scopeExcludesDescendantsFlag = flag;
  }

  /* see superclass */
  @Override
  public Set<String> getActionWorkflowStatusValues() {
    return actionWorkflowStatusValues;
  }

  /* see superclass */
  @Override
  public void setActionWorkflowStatusValues(
    Set<String> actionWorkflowStatusValues) {
    this.actionWorkflowStatusValues = actionWorkflowStatusValues;
  }

  /* see superclass */
  @Override
  public String toString() {
    return getName() + " " + getId();
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result
        + ((scopeConcepts == null) ? 0 : scopeConcepts.hashCode());
    result = prime * result + (scopeDescendantsFlag ? 1231 : 1237);
    result = prime * result + ((scopeExcludesConcepts == null) ? 0
        : scopeExcludesConcepts.hashCode());
    result = prime * result + (scopeExcludesDescendantsFlag ? 1231 : 1237);
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
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
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (scopeConcepts == null) {
      if (other.scopeConcepts != null)
        return false;
    } else if (!scopeConcepts.equals(other.scopeConcepts))
      return false;
    if (scopeDescendantsFlag != other.scopeDescendantsFlag)
      return false;
    if (scopeExcludesConcepts == null) {
      if (other.scopeExcludesConcepts != null)
        return false;
    } else if (!scopeExcludesConcepts.equals(other.scopeExcludesConcepts))
      return false;
    if (scopeExcludesDescendantsFlag != other.scopeExcludesDescendantsFlag)
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
    return true;
  }
}
