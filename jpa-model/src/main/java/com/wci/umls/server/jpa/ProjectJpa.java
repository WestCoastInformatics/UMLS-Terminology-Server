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
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
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
}))
@Audited
@Indexed
@XmlRootElement(name = "project")
public class ProjectJpa implements Project {

  /** The id. */
  @Id
  @GeneratedValue
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

  /** The terminology version. */
  @Column(nullable = false)
  private String terminologyVersion;

  /** The leads. */
  @ManyToMany(targetEntity = UserJpa.class, fetch = FetchType.LAZY)
  @JoinTable(name = "projects_leads", joinColumns = @JoinColumn(name = "projects_id"), inverseJoinColumns = @JoinColumn(name = "users_id"))
  @IndexedEmbedded(targetElement = UserJpa.class)
  private Set<User> leads = new HashSet<>();

  /** The authors. */
  @ManyToMany(targetEntity = UserJpa.class, fetch = FetchType.LAZY)
  @JoinTable(name = "projects_authors", joinColumns = @JoinColumn(name = "projects_id"), inverseJoinColumns = @JoinColumn(name = "users_id"))
  @IndexedEmbedded(targetElement = UserJpa.class)
  private Set<User> authors = new HashSet<>();

  /** The administrators. */
  @ManyToMany(targetEntity = UserJpa.class, fetch = FetchType.EAGER)
  @JoinTable(name = "projects_administrators", joinColumns = @JoinColumn(name = "projects_id"), inverseJoinColumns = @JoinColumn(name = "users_id"))
  @IndexedEmbedded(targetElement = UserJpa.class)
  private Set<User> administrators = new HashSet<>();

  /** The concepts in scope for this project. */
  @ElementCollection
  @CollectionTable(name = "projects_scope_concepts", joinColumns = @JoinColumn(name = "id"))
  @Column(nullable = true)
  private Set<String> scopeConcepts = new HashSet<>();

  /** The concepts excludes from scope of this project. */
  @ElementCollection
  @CollectionTable(name = "projects_scope_excludes_concepts", joinColumns = @JoinColumn(name = "id"))
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
  @CollectionTable(name = "projects_workflow_statuses", joinColumns = @JoinColumn(name = "id"))
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
    terminologyVersion = project.getTerminologyVersion();
    leads = new HashSet<>(project.getLeads());
    authors = new HashSet<>(project.getAuthors());
    administrators = new HashSet<>(project.getAdministrators());
    scopeConcepts = new HashSet<>(project.getScopeConcepts());
    scopeDescendantsFlag = project.getScopeDescendantsFlag();
    scopeExcludesConcepts = new HashSet<>(project.getScopeExcludesConcepts());
    scopeExcludesDescendantsFlag = project.getScopeExcludesDescendantsFlag();
    branch = project.getBranch();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#getId()
   */
  @Override
  public Long getId() {
    return this.id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#setId(java.lang.Long)
   */
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

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#getLastModified()
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public Date getLastModified() {
    return lastModified;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#setLastModified(java.util.Date)
   */
  @Override
  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#getLastModifiedBy()
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#setLastModifiedBy(java.lang.String)
   */
  @Override
  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#getLeads()
   */
  @Override
  @XmlElement(type = UserJpa.class, name = "lead")
  public Set<User> getLeads() {
    return leads;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#setLeads(java.util.Set)
   */
  @Override
  public void setLeads(Set<User> leads) {
    this.leads = leads;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#addLead(org.ihtsdo.otf.ts.helpers.User)
   */
  @Override
  public void addLead(User lead) {
    leads.add(lead);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#removeLead(org.ihtsdo.otf.ts.helpers.User)
   */
  @Override
  public void removeLead(User lead) {
    leads.remove(lead);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#getAuthors()
   */
  @Override
  @XmlElement(type = UserJpa.class, name = "author")
  public Set<User> getAuthors() {
    return authors;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#setAuthors(java.util.Set)
   */
  @Override
  public void setAuthors(Set<User> authors) {
    this.authors = authors;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#addAuthor(org.ihtsdo.otf.ts.helpers.User)
   */
  @Override
  public void addAuthor(User author) {
    authors.add(author);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#removeAuthor(org.ihtsdo.otf.ts.helpers.User)
   */
  @Override
  public void removeAuthor(User author) {
    authors.remove(author);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#getAdministrators()
   */
  @Override
  @XmlElement(type = UserJpa.class, name = "administrator")
  public Set<User> getAdministrators() {
    return administrators;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#setAdministrators(java.util.Set)
   */
  @Override
  public void setAdministrators(Set<User> administrators) {
    this.administrators = administrators;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.Project#addAdministrator(org.ihtsdo.otf.ts.helpers.User)
   */
  @Override
  public void addAdministrator(User administrator) {
    administrators.add(administrator);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.Project#removeAdministrator(org.ihtsdo.otf.ts.helpers
   * .User)
   */
  @Override
  public void removeAdministrator(User administrator) {
    administrators.remove(administrator);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#getTerminology()
   */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getTerminology() {
    return terminology;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#setTerminology(java.lang.String)
   */
  @Override
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#getTerminologyVersion()
   */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getTerminologyVersion() {
    return terminologyVersion;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#setTerminologyVersion(java.lang.String)
   */
  @Override
  public void setTerminologyVersion(String terminologyVersion) {
    this.terminologyVersion = terminologyVersion;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#getName()
   */
  @Override
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#getDescription()
   */
  @Override
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public String getDescription() {
    return description;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#setName(java.lang.String)
   */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#setDescription(java.lang.String)
   */
  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#isPublic()
   */
  @Override
  public boolean isPublic() {
    return isPublic;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#setPublic(boolean)
   */
  @Override
  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#getScopeConcepts()
   */
  @Override
  public Set<String> getScopeConcepts() {
    return scopeConcepts;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#setScopeConcepts(java.util.Set)
   */
  @Override
  public void setScopeConcepts(Set<String> scopeConcepts) {
    this.scopeConcepts = scopeConcepts;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#addScopeConcept(java.lang.String)
   */
  @Override
  public void addScopeConcept(String terminologyId) {
    this.scopeConcepts.add(terminologyId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#removeScopeConcept(java.lang.String)
   */
  @Override
  public void removeScopeConcept(String terminologyId) {
    this.scopeConcepts.remove(terminologyId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#getScopeDescendantsFlag()
   */
  @Override
  public boolean getScopeDescendantsFlag() {
    return scopeDescendantsFlag;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#setScopeDescendantsFlag(boolean)
   */
  @Override
  public void setScopeDescendantsFlag(boolean flag) {
    scopeDescendantsFlag = flag;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#getScopeExcludesConcepts()
   */
  @Override
  public Set<String> getScopeExcludesConcepts() {
    return scopeExcludesConcepts;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#setScopeExcludesConcepts(java.util.Set)
   */
  @Override
  public void setScopeExcludesConcepts(Set<String> scopeExcludesConcepts) {
    this.scopeExcludesConcepts = scopeExcludesConcepts;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#addScopeExcludesConcept(java.lang.String)
   */
  @Override
  public void addScopeExcludesConcept(String terminologyId) {
    this.scopeExcludesConcepts.add(terminologyId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#removeScopeExcludesConcept(java.lang.String)
   */
  @Override
  public void removeScopeExcludesConcept(String terminologyId) {
    this.scopeExcludesConcepts.remove(terminologyId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#getScopeExcludesDescendantsFlag()
   */
  @Override
  public boolean getScopeExcludesDescendantsFlag() {
    return scopeExcludesDescendantsFlag;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#setScopeExcludesDescendantsFlag(boolean)
   */
  @Override
  public void setScopeExcludesDescendantsFlag(boolean flag) {
    scopeExcludesDescendantsFlag = flag;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#getActionWorkflowStatusValues()
   */
  @Override
  public Set<String> getActionWorkflowStatusValues() {
    return actionWorkflowStatusValues;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.Project#setActionWorkflowStatusValues(java.util.Set)
   */
  @Override
  public void setActionWorkflowStatusValues(
    Set<String> actionWorkflowStatusValues) {
    this.actionWorkflowStatusValues = actionWorkflowStatusValues;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getName() + " " + getId();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.Project#getBranch()
   */
  @Override
  public String getBranch() {
    return branch;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.Project#setBranch(java.lang.String)
   */
  @Override
  public void setBranch(String branch) {
    this.branch = branch;
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
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result =
        prime * result
            + ((scopeConcepts == null) ? 0 : scopeConcepts.hashCode());
    result = prime * result + (scopeDescendantsFlag ? 1231 : 1237);
    result =
        prime
            * result
            + ((scopeExcludesConcepts == null) ? 0 : scopeExcludesConcepts
                .hashCode());
    result = prime * result + (scopeExcludesDescendantsFlag ? 1231 : 1237);
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result =
        prime
            * result
            + ((terminologyVersion == null) ? 0 : terminologyVersion.hashCode());
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
    if (terminologyVersion == null) {
      if (other.terminologyVersion != null)
        return false;
    } else if (!terminologyVersion.equals(other.terminologyVersion))
      return false;
    return true;
  }
}
