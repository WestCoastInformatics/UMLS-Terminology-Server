/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;

import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.helpers.ReleaseInfoList;
import com.wci.umls.server.helpers.content.ComponentHistoryList;
import com.wci.umls.server.jpa.ReleaseInfoJpa;
import com.wci.umls.server.jpa.content.ComponentHistoryJpa;
import com.wci.umls.server.jpa.helpers.ReleaseInfoListJpa;
import com.wci.umls.server.jpa.helpers.content.ComponentHistoryListJpa;
import com.wci.umls.server.model.content.ComponentHistory;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.HistoryService;

/**
 * JPA and JAXB enabled implementation of {@link ContentService}.
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

  /* see superclass */
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

  /* see superclass */
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

  /* see superclass */
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

  /* see superclass */
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

  /* see superclass */
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

  /* see superclass */
  @Override
  public ReleaseInfo addReleaseInfo(ReleaseInfo releaseInfo) throws Exception {
    Logger.getLogger(getClass()).debug(
        "History Service - add release info " + releaseInfo.getName());

    return addHasLastModified(releaseInfo);
  }

  /* see superclass */
  @Override
  public void updateReleaseInfo(ReleaseInfo releaseInfo) throws Exception {
    Logger.getLogger(getClass()).debug(
        "History Service - update release info " + releaseInfo.getName());
    updateHasLastModified(releaseInfo);
  }

  /* see superclass */
  @Override
  public void removeReleaseInfo(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "History  Service - remove release info " + id);
    removeHasLastModified(id, ReleaseInfoJpa.class);

  }

  /* see superclass */
  @Override
  public ComponentHistory addComponentHistory(ComponentHistory componentHistory)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add componentHistory " + componentHistory);

    // Add component
    return addComponent(componentHistory);
  }

  /* see superclass */
  @Override
  public ComponentHistory getComponentHistory(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get componentHistory " + id);
    return getComponent(id, ComponentHistoryJpa.class);
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public ComponentHistoryList getComponentHistory(String terminologyId,
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get componentHistorys " + terminologyId + "/"
            + terminology + "/" + version);
    final List<ComponentHistory> componentHistories =
        getComponents(terminologyId, terminology, version,
            ComponentHistoryJpa.class);
    if (componentHistories == null) {
      return null;
    }
    final ComponentHistoryList list = new ComponentHistoryListJpa();
    list.setTotalCount(componentHistories.size());
    list.setObjects(componentHistories);
    return list;
  }

  /* see superclass */
  @Override
  public ComponentHistory getComponentHistory(String terminologyId,
    String terminology, String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get componentHistory " + terminologyId + "/"
            + terminology + "/" + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        ComponentHistoryJpa.class);
  }

  /* see superclass */
  @Override
  public void updateComponentHistory(ComponentHistory componentHistory)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update componentHistory " + componentHistory);

    // update component
    updateComponent(componentHistory);

  }

  /* see superclass */
  @Override
  public void removeComponentHistory(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove componentHistory " + id);
    // Remove the component
    removeComponent(id, ComponentHistoryJpa.class);

  }
}
