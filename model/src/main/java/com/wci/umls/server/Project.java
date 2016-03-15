/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server;

import java.util.Date;
import java.util.Set;

/**
 * Generically represents an editing project.
 */
public interface Project {

  /**
   * Returns the id.
   * 
   * @return the id
   */
  public Long getId();

  /**
   * Sets the id.
   * 
   * @param id the id
   */
  public void setId(Long id);

  /**
   * Returns the name.
   * 
   * @return the name
   */
  public String getName();

  /**
   * Sets the name.
   * 
   * @param name the name
   */
  public void setName(String name);

  /**
   * Returns the description.
   * 
   * @return the description
   */
  public String getDescription();

  /**
   * Sets the description.
   * 
   * @param description the description
   */
  public void setDescription(String description);

  /**
   * Checks if the project is viewable by public roles.
   *
   * @return true, if is public
   */
  public boolean isPublic();

  /**
   * Sets whether the project is viewable by public roles.
   *
   * @param isPublic the new public
   */
  public void setPublic(boolean isPublic);

  /**
   * Returns the last modified.
   * 
   * @return the last modified
   */
  public Date getLastModified();

  /**
   * Sets the last modified.
   * 
   * @param lastModified the last modified
   */
  public void setLastModified(Date lastModified);

  /**
   * Returns the last modified by.
   * 
   * @return the last modified by
   */
  public String getLastModifiedBy();

  /**
   * Sets the last modified by.
   * 
   * @param lastModifiedBy the last modified by
   */
  public void setLastModifiedBy(String lastModifiedBy);

  /**
   * Returns the leads.
   * 
   * @return the leads
   */
  public Set<User> getLeads();

  /**
   * Sets the leads.
   * 
   * @param leads the leads
   */
  public void setLeads(Set<User> leads);

  /**
   * Adds the lead.
   * 
   * @param lead the lead
   */
  public void addLead(User lead);

  /**
   * Removes the lead.
   * 
   * @param lead the lead
   */
  public void removeLead(User lead);

  /**
   * Returns the administrators.
   * 
   * @return the administrators
   */
  public Set<User> getAdministrators();

  /**
   * Sets the administrators.
   * @param administrators the administrators
   */
  public void setAdministrators(Set<User> administrators);

  /**
   * Adds the administrator.
   * 
   * @param administrator a administrator
   */
  public void addAdministrator(User administrator);

  /**
   * Removes the administrator.
   * 
   * @param administrator the administrator
   */
  public void removeAdministrator(User administrator);

  /**
   * Returns the author.
   * 
   * @return the author.
   */
  public Set<User> getAuthors();

  /**
   * Sets the authors.
   * 
   * @param authors the authors
   */
  public void setAuthors(Set<User> authors);

  /**
   * Adds the author.
   * 
   * @param author the author
   */
  public void addAuthor(User author);

  /**
   * Removes the author.
   * 
   * @param author the author
   */
  public void removeAuthor(User author);

  /**
   * Returns the terminology.
   * 
   * @return the terminology
   */
  public String getTerminology();

  /**
   * Sets the terminology.
   * 
   * @param terminology the terminology
   */
  public void setTerminology(String terminology);

  /**
   * Returns the version.
   * 
   * @return the version
   */
  public String getVersion();

  /**
   * Sets the version.
   * 
   * @param version the version
   */
  public void setVersion(String version);

  /**
   * Returns the scope concepts.
   * 
   * @return the scope concepts
   */
  public Set<String> getScopeConcepts();

  /**
   * Sets the scope concepts.
   * 
   * @param scopeConcepts the scope concepts
   */
  public void setScopeConcepts(Set<String> scopeConcepts);

  /**
   * Indicates whether or not project scope includes descendants of scope
   * concepts.
   * 
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean getScopeDescendantsFlag();

  /**
   * Sets the scope descendants flag.
   * 
   * @param flag the scope descendants flag
   */
  public void setScopeDescendantsFlag(boolean flag);

  /**
   * Returns the scope excludes concepts.
   * 
   * @return the scope excludes concepts
   */
  public Set<String> getScopeExcludesConcepts();

  /**
   * Sets the scope excludes concepts.
   * 
   * @param scopeExcludesConcepts the scope excludes concepts
   */
  public void setScopeExcludesConcepts(Set<String> scopeExcludesConcepts);

  /**
   * Indicates whether or not scope excludes descendants of the scope excludes
   * concepts.
   * 
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean getScopeExcludesDescendantsFlag();

  /**
   * Sets the scope excludes descendants flag.
   * 
   * @param flag the scope excludes descendants flag
   */
  public void setScopeExcludesDescendantsFlag(boolean flag);

  /**
   * Adds the scope excludes concept.
   *
   * @param terminologyId the terminology id
   */
  public void addScopeExcludesConcept(String terminologyId);

  /**
   * Removes the scope excludes concept.
   *
   * @param terminologyId the terminology id
   */
  public void removeScopeExcludesConcept(String terminologyId);

  /**
   * Adds the scope concept.
   *
   * @param terminologyId the terminology id
   */
  public void addScopeConcept(String terminologyId);

  /**
   * Removes the scope concept.
   *
   * @param terminologyId the terminology id
   */
  public void removeScopeConcept(String terminologyId);

  /**
   * Returns the action workflow status values.
   *
   * @return the action workflow status values
   */
  public Set<String> getActionWorkflowStatusValues();

  /**
   * Sets the action workflow status values.
   *
   * @param values the action workflow status values
   */
  public void setActionWorkflowStatusValues(Set<String> values);

  /**
   * Returns the branch.
   *
   * @return the branch
   */
  public String getBranch();

  /**
   * Sets the branch.
   *
   * @param branch the branch
   */
  public void setBranch(String branch);

}