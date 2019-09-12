/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;

import com.wci.umls.server.Project;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.inversion.SourceIdRangeJpa;
import com.wci.umls.server.model.inversion.SourceIdRange;
import com.wci.umls.server.services.InversionService;

/**
 * JPA and JAXB enabled implementation of {@link InversionService}.
 */
public class InversionServiceJpa extends HistoryServiceJpa
    implements InversionService {

  /** The config properties. */
  protected static Properties config = null;

  /**
   * Instantiates an empty {@link InversionServiceJpa}.
   *
   * @throws Exception the exception
   */
  public InversionServiceJpa() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public SourceIdRange getSourceIdRange(Long id) {
    Logger.getLogger(getClass())
        .debug("Inversion Service - get sourceIdRange " + id);
    final SourceIdRange sourceIdRange =
        manager.find(SourceIdRangeJpa.class, id);
    return sourceIdRange;
  }

  /* see superclass */
  @Override
  public SourceIdRange addSourceIdRange(SourceIdRange sourceIdRange)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Inversion Service - add sourceIdRange - " + sourceIdRange);
    return addHasLastModified(sourceIdRange);
  }

  /* see superclass */
  @Override
  public SourceIdRange updateSourceIdRange(SourceIdRange sourceIdRange,
    int numberOfIds) throws Exception {
    Logger.getLogger(getClass())
        .debug("Inversion Service - update sourceIdRange - " + sourceIdRange);
    // Get the max id previously assigned to any source
    final javax.persistence.Query query = manager
        .createQuery("select max(a.endSourceId) from SourceIdRangeJpa a");
    try {
      @SuppressWarnings("unchecked")
      final List<Object> m = query.getResultList();

      // create a new SourceIdRange with the previous max id incremented by one
      Long beginSourceId = (Long) (m.get(0)) + 1L;
      sourceIdRange.setBeginSourceId(beginSourceId);
      sourceIdRange.setEndSourceId(beginSourceId + numberOfIds - 1L);

      updateHasLastModified(sourceIdRange);

      return sourceIdRange;

    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public void removeSourceIdRange(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Inversion Service - remove sourceIdRange " + id);
    removeHasLastModified(id, SourceIdRangeJpa.class);
  }

  @Override
  public SourceIdRange getSourceIdRange(Project project, String terminology,
    String version) throws Exception {

    final javax.persistence.Query query =
        manager.createQuery("select a from SourceIdRangeJpa a where "
            + "version = :version and terminology = :terminology");
    // Try to retrieve the single expected result If zero or more than one
    // result are returned, log error and set result to null
    try {
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      @SuppressWarnings("unchecked")
      final List<Object> m = query.getResultList();
      SourceIdRangeJpa sourceIdRange = new SourceIdRangeJpa();
      sourceIdRange = (SourceIdRangeJpa) m.get(0);

      return sourceIdRange;

    } catch (NoResultException e) {
      return null;
    }
  }

  private String composeQuery(Project project, String query) throws Exception {
    final StringBuilder localQuery = new StringBuilder();
    if (query != null && !query.equals("null"))
      localQuery.append(query);
    if (!ConfigUtility.isEmpty(query)) {
      localQuery.append(" AND ");
    }
    // Support explicitly null project
    if (project == null) {
      localQuery.append("projectId:[* TO *]");
    } else {
      localQuery.append("projectId:" + project.getId());
    }
    return localQuery.toString();
  }

  @Override
  public SourceIdRange requestSourceIdRange(Project project, String terminology,
    String version, int numberOfIds) throws Exception {

    // Get the max id previously assigned to any source
    final javax.persistence.Query query = manager
        .createQuery("select max(a.endSourceId) from SourceIdRangeJpa a");
    try {
      @SuppressWarnings("unchecked")
      final List<Object> m = query.getResultList();

      // create a new SourceIdRange with the previous max id incremented by one
      SourceIdRangeJpa sourceIdRange = new SourceIdRangeJpa();
      Long beginSourceId = (Long) (m.get(0)) + 1L;
      sourceIdRange.setBeginSourceId(beginSourceId);
      sourceIdRange.setEndSourceId(beginSourceId + numberOfIds - 1L);
      sourceIdRange.setProject(project);
      sourceIdRange.setTerminology(terminology);
      sourceIdRange.setVersion(version);

      addHasLastModified(sourceIdRange);
      return sourceIdRange;

    } catch (NoResultException e) {
      return null;
    }
  }
}
