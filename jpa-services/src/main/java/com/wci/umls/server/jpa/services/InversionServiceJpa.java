/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.util.List;
import java.util.Properties;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;

import com.wci.umls.server.Project;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.content.SourceIdRangeList;
import com.wci.umls.server.jpa.helpers.content.SourceIdRangeListJpa;
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
    int numberOfIds, Long beginId) throws Exception {
    Logger.getLogger(getClass())
        .debug("Inversion Service - update sourceIdRange - " + sourceIdRange);
    // Get the max id previously assigned to any source
    final javax.persistence.Query query = manager
        .createQuery("select max(a.endSourceId) from SourceIdRangeJpa a");
    try {
      Long beginSourceId = beginId;
      
      // if begin id not indicated (not SNOMED)
      if (beginSourceId == null) {
        @SuppressWarnings("unchecked")
        final List<Object> m = query.getResultList();

        // create a new SourceIdRange with the previous max id incremented by one
        beginSourceId = (Long) (m.get(0)) + 1L;
      // SNOMED case with beginSourceId indicated
      } else {
        @SuppressWarnings("unchecked")
        final List<Object> m = query.getResultList();

        if (beginSourceId < (Long) (m.get(0)) + 1L) {
          throw new LocalException("Specified begin id must be greater than " + m.get(0));
        }
      }
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
  public SourceIdRangeList getSourceIdRange(Project project, String terminology) throws Exception {

    final javax.persistence.Query query =
        manager.createQuery("select id from SourceIdRangeJpa a where "
            + "terminology like :terminology");
    try {
      query.setParameter("terminology", "%" + terminology + "%");
      @SuppressWarnings("unchecked")
      final List<Object> list = query.getResultList();
      SourceIdRangeListJpa sourceIdRangeList = new SourceIdRangeListJpa();
      
      for (final Object entry : list) {
        final Long id = Long.valueOf(entry.toString());
        sourceIdRangeList.getObjects().add(getSourceIdRange(id));
      }
      sourceIdRangeList.setTotalCount(sourceIdRangeList.getObjects().size());
      return sourceIdRangeList;

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
    int numberOfIds, long beginId) throws Exception {

    // Get the max id previously assigned to any source
    final javax.persistence.Query query = manager
        .createQuery("select max(a.endSourceId) from SourceIdRangeJpa a");
    try {
      // create a new SourceIdRange with the previous max id incremented by one
      SourceIdRangeJpa sourceIdRange = new SourceIdRangeJpa();
      Long beginSourceId = new Long(beginId);
      
      // if begin id not indicated (not SNOMED)
      if (beginSourceId == 0L) {
        @SuppressWarnings("unchecked")
        final List<Object> m = query.getResultList();

        beginSourceId = 0L;
        if (m != null && m.get(0) != null) {
          beginSourceId = (Long) (m.get(0)) + 1L;
        }
      // SNOMED case with beginSourceId indicated
      } else {
        @SuppressWarnings("unchecked")
        final List<Object> m = query.getResultList();

        if (beginSourceId < (Long) (m.get(0)) + 1L) {
          throw new LocalException("Specified begin id must be greater than " + m.get(0));
        }
      }
      sourceIdRange.setBeginSourceId(beginSourceId);
      sourceIdRange.setEndSourceId(beginSourceId + numberOfIds - 1L);
      sourceIdRange.setProject(project);
      sourceIdRange.setTerminology(terminology);

      addHasLastModified(sourceIdRange);
      return sourceIdRange;

    } catch (NoResultException e) {
      return null;
    }
  }
}
