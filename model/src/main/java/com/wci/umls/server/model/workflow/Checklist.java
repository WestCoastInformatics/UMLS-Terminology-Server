/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.workflow;

import java.util.List;

import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.helpers.HasProject;

/**
 * Represents a checklist which is a collection of tracking records without
 * participating in workflow.
 */
public interface Checklist extends HasLastModified, HasProject {

  /**
   * Gets the tracking records.
   *
   * @return the tracking records
   */
  public List<TrackingRecord> getTrackingRecords();

  /**
   * Sets the tracking records.
   *
   * @param records the new tracking records
   */
  public void setTrackingRecords(List<TrackingRecord> records);

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName();

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name);

  /**
   * Gets the description.
   *
   * @return the description
   */
  public String getDescription();

  /**
   * Sets the description.
   *
   * @param description the new description
   */
  public void setDescription(String description);


}