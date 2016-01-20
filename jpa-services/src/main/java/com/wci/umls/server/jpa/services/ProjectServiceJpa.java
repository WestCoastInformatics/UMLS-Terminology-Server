/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;

import com.wci.umls.server.Project;
import com.wci.umls.server.User;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.helpers.ProjectListJpa;
import com.wci.umls.server.services.ProjectService;

/**
 * JPA enabled implementation of {@link ProjectService}.
 */
public class ProjectServiceJpa extends RootServiceJpa implements ProjectService {

  /**
   * Instantiates an empty {@link ProjectServiceJpa}.
   *
   * @throws Exception the exception
   */
  public ProjectServiceJpa() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public ConceptList findConceptsInScope(Project project, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "Project Service - get project scope - " + project);

    // TODO
    // Likely we want a more expansive scope definition that
    // can include concepts, terminologies, and/or semantic types.
    // the use cases
    // * editor only allowed to work on a certain sourcde
    // * editor only allowed to work in a particular content area
    // * editor of a terminology can only work in a subtree.

    return null;
  }

  /* see superclass */
  @Override
  public Project getProject(Long id) {
    Logger.getLogger(getClass()).debug("Project Service - get project " + id);
    final Project project = manager.find(ProjectJpa.class, id);
    handleLazyInit(project);
    return project;
  }

  /* see superclass */
  @Override
  @SuppressWarnings("unchecked")
  public ProjectList getProjects() {
    Logger.getLogger(getClass()).debug("Project Service - get projects");
    javax.persistence.Query query =
        manager.createQuery("select a from ProjectJpa a");
    try {
      final List<Project> projects = query.getResultList();
      final ProjectList projectList = new ProjectListJpa();
      projectList.setObjects(projects);
      for (final Project project : projectList.getObjects()) {
        handleLazyInit(project);
      }
      return projectList;
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public UserRole getUserRoleForProject(String username, Long projectId)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Project Service - get user role for project - " + username + ", "
            + projectId);
    final Project project = getProject(projectId);
    if (project == null) {
      throw new Exception("No project found for " + projectId);
    }

    // check admin
    for (final User user : project.getAdministrators()) {
      if (username.equals(user.getUserName())) {
        return UserRole.ADMINISTRATOR;
      }
    }

    // check lead
    for (final User user : project.getLeads()) {
      if (username.equals(user.getUserName())) {
        return UserRole.LEAD;
      }
    }

    // check author
    for (final User user : project.getAuthors()) {
      if (username.equals(user.getUserName())) {
        return UserRole.AUTHOR;
      }
    }

    return null;
  }

  /* see superclass */
  @Override
  public Project addProject(Project project) {
    Logger.getLogger(getClass()).debug(
        "Project Service - add project - " + project);
    try {
      // Set last modified date
      project.setLastModified(new Date());

      // add the project
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.persist(project);
        tx.commit();
      } else {
        manager.persist(project);
      }
      return project;
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /* see superclass */
  @Override
  public void updateProject(Project project) {
    Logger.getLogger(getClass()).debug(
        "Project Service - update project - " + project);

    try {
      // Set modification date
      project.setLastModified(new Date());

      // update
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.merge(project);
        tx.commit();
      } else {
        manager.merge(project);
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /* see superclass */
  @Override
  public void removeProject(Long id) {
    Logger.getLogger(getClass())
        .debug("Project Service - remove project " + id);
    try {
      // Get transaction and object
      tx = manager.getTransaction();
      final Project project = manager.find(ProjectJpa.class, id);

      // if project doesn't exist, return
      if (project == null)
        return;

      // Set modification date
      project.setLastModified(new Date());

      // Remove
      if (getTransactionPerOperation()) {
        // remove refset member
        tx.begin();
        if (manager.contains(project)) {
          manager.remove(project);
        } else {
          manager.remove(manager.merge(project));
        }
        tx.commit();
      } else {
        if (manager.contains(project)) {
          manager.remove(project);
        } else {
          manager.remove(manager.merge(project));
        }
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /**
   * Handle lazy initialization.
   *
   * @param project the project
   */
  @SuppressWarnings("static-method")
  private void handleLazyInit(Project project) {
    if (project == null) {
      return;
    }
    if (project.getActionWorkflowStatusValues() != null) {
      project.getActionWorkflowStatusValues().size();
    }
    if (project.getAdministrators() != null) {
      project.getAdministrators().size();
    }
    if (project.getAuthors() != null) {
      project.getAuthors().size();
    }
    if (project.getLeads() != null) {
      project.getLeads().size();
    }
    if (project.getScopeExcludesConcepts() != null) {
      project.getScopeExcludesConcepts().size();
    }
    if (project.getScopeConcepts() != null) {
      project.getScopeConcepts().size();
    }
  }

  /**
   * Returns the pfs comparator.
   *
   * @param <T> the
   * @param clazz the clazz
   * @param pfs the pfs
   * @return the pfs comparator
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  protected <T> Comparator<T> getPfsComparator(Class<T> clazz, PfsParameter pfs)
    throws Exception {
    if (pfs != null
        && (pfs.getSortField() != null && !pfs.getSortField().isEmpty())) {
      // check that specified sort field exists on Concept and is
      // a string
      final Field sortField = clazz.getField(pfs.getSortField());

      // allow the field to access the Concept values
      sortField.setAccessible(true);

      if (pfs.isAscending()) {
        // make comparator
        return new Comparator<T>() {
          @Override
          public int compare(T o1, T o2) {
            try {
              // handle dates explicitly
              if (o2 instanceof Date) {
                return ((Date) sortField.get(o1)).compareTo((Date) sortField
                    .get(o2));
              } else {
                // otherwise, sort based on conversion to string
                return (sortField.get(o1).toString()).compareTo(sortField.get(
                    o2).toString());
              }
            } catch (IllegalAccessException e) {
              // on exception, return equality
              return 0;
            }
          }
        };
      } else {
        // make comparator
        return new Comparator<T>() {
          @Override
          public int compare(T o2, T o1) {
            try {
              // handle dates explicitly
              if (o2 instanceof Date) {
                return ((Date) sortField.get(o1)).compareTo((Date) sortField
                    .get(o2));
              } else {
                // otherwise, sort based on conversion to string
                return (sortField.get(o1).toString()).compareTo(sortField.get(
                    o2).toString());
              }
            } catch (IllegalAccessException e) {
              // on exception, return equality
              return 0;
            }
          }
        };
      }

    } else {
      return null;
    }
  }

  /* see superclass */
  @Override
  public void refreshCaches() throws Exception {
    // n/a
  }

}
