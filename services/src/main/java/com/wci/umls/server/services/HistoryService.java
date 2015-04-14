/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services;

import java.util.Date;

import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.ReleaseInfoList;
import com.wci.umls.server.helpers.content.AtomList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Relationship;

/**
 * Generically represents a service for asking questions about content history.
 */
public interface HistoryService extends ContentService {
////
////  /**
////   * Find concepts modified since date.
////   *
////   * @param terminology the terminology
////   * @param date the date
////   * @param pfs the pfs parameter
////   * @return the search result list
////   * @throws Exception the exception
////   */
////  public ConceptList findConceptsModifiedSinceDate(String terminology,
////    Date date, PfsParameter pfs) throws Exception;
////
////  /**
////   * Finds all concept revisions for the specified date range.
////   *
////   * @param id the id
////   * @param startDate the start date
////   * @param endDate the end date
////   * @param pfs the pfs parameter
////   * @return the concept revisions
////   * @throws Exception the exception
////   */
////  public ConceptList findConceptRevisions(Long id, Date startDate,
////    Date endDate, PfsParameter pfs) throws Exception;
////
////  /**
////   * Find concept release revision.
////   *
////   * @param id the id
////   * @param release the release
////   * @return the atom list
////   * @throws Exception the exception
////   */
////  public Concept findConceptReleaseRevision(Long id, Date release)
////    throws Exception;
////
////  /**
////   * Find atoms modified since date.
////   *
////   * @param terminology the terminology
////   * @param date the date
////   * @param pfs the pfs parameter
////   * @return the search result list
////   * @throws Exception the exception
////   */
////  public AtomList findAtomsModifiedSinceDate(String terminology, Date date,
////    PfsParameter pfs) throws Exception;
////
////  /**
////   * Finds all atom revisions for the specified date range.
////   *
////   * @param id the id
////   * @param startDate the start date
////   * @param endDate the end date
////   * @param pfs the pfs parameter
////   * @return the atom list
////   * @throws Exception the exception
////   */
////  public AtomList findAtomRevisions(Long id, Date startDate, Date endDate,
////    PfsParameter pfs) throws Exception;
////
////  /**
////   * Find atom release revision.
////   *
////   * @param id the id
////   * @param release the release
////   * @return the atom list
////   * @throws Exception the exception
////   */
////  public Atom findAtomReleaseRevision(Long id, Date release) throws Exception;
////
////  /**
////   * Find relationships modified since date.
////   *
////   * @param terminology the terminology
////   * @param date the date
////   * @param pfs the pfs parameter
////   * @return the search result list
////   * @throws Exception the exception
////   */
////  public RelationshipList findRelationshipsModifiedSinceDate(
////    String terminology, Date date, PfsParameter pfs) throws Exception;
//
//  /**
//   * Find relationship release revision.
//   *
//   * @param id the id
//   * @param release the release
//   * @return the atom list
//   * @throws Exception the exception
//   */
//  public Relationship<? extends Component, ? extends Component> findRelationshipReleaseRevision(
//    Long id, Date release) throws Exception;
//
//  /**
//   * Finds all relationship revisions for the specified date range.
//   *
//   * @param id the id
//   * @param startDate the start date
//   * @param endDate the end date
//   * @param pfs the pfs parameter
//   * @return the relationship list
//   * @throws Exception the exception
//   */
//  public RelationshipList findRelationshipRevisions(Long id, Date startDate,
//    Date endDate, PfsParameter pfs) throws Exception;
//
//  /**
//   * Returns concepts changed since certain date – performs a "deep" search for
//   * all concepts where it or any of its components have changed in the relevant
//   * period.
//   *
//   * @param terminology the terminology
//   * @param date the date
//   * @param pfs the pfs parameter
//   * @return the concepts deep modified since date
//   * @throws Exception the exception
//   */
//  public ConceptList findConceptsDeepModifiedSinceDate(String terminology,
//    Date date, PfsParameter pfs) throws Exception;

  /**
   * Returns the release history.
   *
   * @param terminology the terminology
   * @return the release history
   * @throws Exception the exception
   */
  public ReleaseInfoList getReleaseHistory(String terminology) throws Exception;

  /**
   * Returns the current published release info.
   *
   * @param terminology the terminology
   * @return the current release info
   * @throws Exception the exception
   */
  public ReleaseInfo getCurrentReleaseInfo(String terminology) throws Exception;

  /**
   * Returns the previous published release info.
   *
   * @param terminology the terminology
   * @return the previous release info
   * @throws Exception the exception
   */
  public ReleaseInfo getPreviousReleaseInfo(String terminology)
    throws Exception;

  /**
   * Gets the planned release info. (planned not published)
   *
   * @param terminology the terminology
   * @return the planned release info
   * @throws Exception the exception
   */
  public ReleaseInfo getPlannedReleaseInfo(String terminology) throws Exception;

  /**
   * Returns the release info.
   *
   * @param terminology the terminology
   * @param name the name
   * @return the release info
   * @throws Exception the exception
   */
  public ReleaseInfo getReleaseInfo(String terminology, String name)
    throws Exception;

  /**
   * Adds the release info.
   *
   * @param releaseInfo the release info
   * @return the release info
   * @throws Exception the exception
   */
  public ReleaseInfo addReleaseInfo(ReleaseInfo releaseInfo) throws Exception;

  /**
   * Updates release info.
   *
   * @param releaseInfo the release info
   * @throws Exception the exception
   */
  public void updateReleaseInfo(ReleaseInfo releaseInfo) throws Exception;

  /**
   * Removes the release info.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeReleaseInfo(Long id) throws Exception;

}