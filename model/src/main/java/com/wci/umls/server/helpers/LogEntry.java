/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

/**
 * Represents a log entry.
 */
public interface LogEntry extends HasLastModified {

  /**
   * Returns the message.
   *
   * @return the message
   */
  public String getMessage();

  /**
   * Sets the message.
   *
   * @param message the message
   */
  public void setMessage(String message);

  /**
   * Returns the object id.
   *
   * @return the object id
   */
  public Long getObjectId();

  /**
   * Sets the object id.
   *
   * @param objectId the object id
   */
  public void setObjectId(Long objectId);

  /**
   * Returns the project id.
   *
   * @return the project id
   */
  public Long getProjectId();

  /**
   * Sets the project id.
   *
   * @param projectId the project id
   */
  public void setProjectId(Long projectId);

  /**
   * Gets the activity id.
   *
   * @return the activity id
   */
  public String getActivityId();

  /**
   * Sets the activity id.
   *
   * @param activityId the activity id
   */
  public void setActivityId(String activityId);

  /**
   * Gets the work id.
   *
   * @return the work id
   */
  public String getWorkId();

  /**
   * Sets the work id.
   *
   * @param workId the work id
   */
  public void setWorkId(String workId);

  /**
   * Gets the version.
   *
   * @return the version
   */
  public String getVersion();

  /**
   * Sets the version.
   *
   * @param version the new version
   */
  public void setVersion(String version);

  /**
   * Gets the terminology.
   *
   * @return the terminology
   */
  public String getTerminology();

  /**
   * Sets the terminology.
   *
   * @param terminology the new terminology
   */
  public void setTerminology(String terminology);

}
