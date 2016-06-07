/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.workflow;

import java.util.List;

import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.model.content.Concept;


/**
 * Represents a tracking record for authoring being performed for a translation
 * refset. This indicates when a concept is assigned to an author or lead.
 */
public interface TrackingRecord extends HasLastModified {

  /**
   * Returns the author users.
   *
   * @return the author users
   */
  public List<String> getAuthors();

  /**
   * Sets the author users.
   *
   * @param authors the author users
   */
  public void setAuthors(List<String> authors);

  /**
   * Returns the reviewers users.
   *
   * @return the reviewers users
   */
  public List<String> getReviewers();

  /**
   * Sets the reviewers users.
   *
   * @param reviewers the reviewers
   */
  public void setReviewers(List<String> reviewers);


  /**
   * Returns the concept.
   *
   * @return the concept
   */
  public Concept getConcept();

  /**
   * Sets the concept.
   *
   * @param concept the concept
   */
  public void setConcept(Concept concept);

  /**
   * Indicates whether or not for review is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isForReview();

  /**
   * Sets the for review.
   *
   * @param forReview the for review
   */
  public void setForReview(boolean forReview);

  /**
   * Indicates whether or not for authoring is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isForAuthoring();

  /**
   * Sets the for authoring flag.
   *
   * @param forAuthoring the for authoring flag
   */
  public void setForAuthoring(boolean forAuthoring);

  /**
   * Indicates whether or not revision is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isRevision();

  /**
   * Sets the revision flag (indicating this is a ready for publication record
   * being re-edited)
   *
   * @param revision the revision
   */
  public void setRevision(boolean revision);

  /**
   * Returns the origin revision, the initial state of the refset before
   * re-editing.
   *
   * @return the origin revision
   */
  public Integer getOriginRevision();

  /**
   * Sets the origin revision.
   *
   * @param revision the origin revision
   */
  public void setOriginRevision(Integer revision);

  /**
   * Returns the review origin revision, the initial state of the refset before
   * review, in case it needs to be unassigned after changes.
   *
   * @return the review origin revision
   */
  public Integer getReviewOriginRevision();

  /**
   * Sets the review origin revision.
   *
   * @param revision the review origin revision
   */
  public void setReviewOriginRevision(Integer revision);

}