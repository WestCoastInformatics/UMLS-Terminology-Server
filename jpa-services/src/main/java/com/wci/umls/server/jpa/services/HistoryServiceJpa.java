/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;

import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.helpers.ReleaseInfoList;
import com.wci.umls.server.jpa.ReleaseInfoJpa;
import com.wci.umls.server.jpa.helpers.ReleaseInfoListJpa;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.HistoryService;

/**
 * JPA enabled implementation of {@link ContentService}.
 */
public class HistoryServiceJpa extends ContentServiceJpa implements
    HistoryService {

  /**
   * Instantiates an empty {@link HistoryServiceJpa}.
   *
   * @throws Exception the exception
   */
  public HistoryServiceJpa() throws Exception {
    super();

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.services.HistoryService#getReleaseHistory(java.lang.String
   * )
   */
  @SuppressWarnings("unchecked")
  @Override
  public ReleaseInfoList getReleaseHistory(String terminology) throws Exception {
    Logger.getLogger(getClass()).debug(
        "History Service - get release history " + terminology);
    javax.persistence.Query query =
        manager
            .createQuery("select a from ReleaseInfoJpa a where terminology = :terminology order by a.effectiveTime");
    /*
     * Try to retrieve the single expected result If zero or more than one
     * result are returned, log error and set result to null
     */
    try {
      query.setParameter("terminology", terminology);
      List<ReleaseInfo> releaseInfos = query.getResultList();
      ReleaseInfoList releaseInfoList = new ReleaseInfoListJpa();
      releaseInfoList.setObjects(releaseInfos);
      return releaseInfoList;
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.services.HistoryService#getCurrentReleaseInfo(java.lang
   * .String)
   */
  @Override
  public ReleaseInfo getCurrentReleaseInfo(String terminology) throws Exception {
    Logger.getLogger(getClass()).debug(
        "History Service - get current release info " + terminology);
    List<ReleaseInfo> results = getReleaseHistory(terminology).getObjects();
    // get max release that is published and not planned
    for (int i = results.size() - 1; i >= 0; i--) {
      if (results.get(i).isPublished() && !results.get(i).isPlanned()
          && results.get(i).getTerminology().equals(terminology)) {
        return results.get(i);
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.services.HistoryService#getPreviousReleaseInfo(java.lang
   * .String)
   */
  @Override
  public ReleaseInfo getPreviousReleaseInfo(String terminology)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "History Service - get previous release info " + terminology);
    List<ReleaseInfo> results = getReleaseHistory(terminology).getObjects();
    // get one before the max release that is published
    for (int i = results.size() - 1; i >= 0; i--) {
      if (results.get(i).isPublished() && !results.get(i).isPlanned()
          && results.get(i).getTerminology().equals(terminology)) {
        if (i > 0) {
          return results.get(i - 1);
        } else {
          return null;
        }
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.services.HistoryService#getPlannedReleaseInfo(java.lang
   * .String)
   */
  @Override
  public ReleaseInfo getPlannedReleaseInfo(String terminology) throws Exception {
    Logger.getLogger(getClass()).debug(
        "History Service - get planned release info " + terminology);
    List<ReleaseInfo> results = getReleaseHistory(terminology).getObjects();
    // get one before the max release that is published
    for (int i = results.size() - 1; i >= 0; i--) {
      if (!results.get(i).isPublished() && results.get(i).isPlanned()
          && results.get(i).getTerminology().equals(terminology)) {
        return results.get(i);
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.services.HistoryService#getReleaseInfo(java.lang.String,
   * java.lang.String)
   */
  @Override
  public ReleaseInfo getReleaseInfo(String terminology, String name)
    throws ParseException {
    Logger.getLogger(getClass()).debug(
        "History Service - get release info " + terminology + ", " + name);
    javax.persistence.Query query =
        manager.createQuery("select r from ReleaseInfoJpa r "
            + "where name = :name " + "and terminology = :terminology");
    /*
     * Try to retrieve the single expected result If zero or more than one
     * result are returned, log error and set result to null
     */
    try {
      query.setParameter("name", name);
      query.setParameter("terminology", terminology);
      return (ReleaseInfo) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.services.HistoryService#addReleaseInfo(org.ihtsdo.otf
   * .ts.helpers.ReleaseInfo)
   */
  @Override
  public ReleaseInfo addReleaseInfo(ReleaseInfo releaseInfo) throws Exception {
    Logger.getLogger(getClass()).debug(
        "History Service - add release info " + releaseInfo.getName());
    if (lastModifiedFlag) {
      releaseInfo.setLastModified(new Date());
    }
    try {
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.persist(releaseInfo);
        tx.commit();
      } else {
        manager.persist(releaseInfo);
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }

    return releaseInfo;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.services.HistoryService#updateReleaseInfo(org.ihtsdo.
   * otf.ts.helpers.ReleaseInfo)
   */
  @Override
  public void updateReleaseInfo(ReleaseInfo releaseInfo) throws Exception {
    Logger.getLogger(getClass()).debug(
        "History Service - update release info " + releaseInfo.getName());
    if (lastModifiedFlag) {
      releaseInfo.setLastModified(new Date());
    }
    try {
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.merge(releaseInfo);
        tx.commit();
      } else {
        manager.merge(releaseInfo);
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.services.HistoryService#removeReleaseInfo(java.lang.Long)
   */
  @Override
  public void removeReleaseInfo(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "History  Service - remove release info " + id);
    tx = manager.getTransaction();
    // retrieve this release info
    ReleaseInfo releaseInfo = manager.find(ReleaseInfoJpa.class, id);
    try {
      if (getTransactionPerOperation()) {
        // remove description
        tx.begin();
        if (manager.contains(releaseInfo)) {
          manager.remove(releaseInfo);
        } else {
          manager.remove(manager.merge(releaseInfo));
        }
        tx.commit();
      } else {
        if (manager.contains(releaseInfo)) {
          manager.remove(releaseInfo);
        } else {
          manager.remove(manager.merge(releaseInfo));
        }
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }

  }

}
