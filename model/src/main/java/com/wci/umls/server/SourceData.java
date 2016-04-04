/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server;

import java.util.List;

import com.wci.umls.server.helpers.HasId;
import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.helpers.HasName;
import com.wci.umls.server.helpers.HasTerminology;

/**
 * Generically represents a collection of source data files and a Handler used
 * for those files.
 */
public interface SourceData extends HasId, HasTerminology, HasLastModified, HasName {

  /**
   * Load status for a {@link SourceData}.
   */
  public enum Status {
    /** The unknown. */
    UNKNOWN,

    /** The new. */
    NEW,

    /** The loading. */
    LOADING,

    /** The REMOVING. */
    REMOVING,

    /** The failed. */
    LOADING_FAILED,

    /** The loading complete. */
    LOADING_COMPLETE,

    /** The REMOVING failed. */
    REMOVING_FAILED,

    /** The REMOVING complete. */
    REMOVING_COMPLETE

  }

  /**
   * Sets the source data files.
   *
   * @param sourceDataFiles the source data files
   */
  public void setSourceDataFiles(List<SourceDataFile> sourceDataFiles);

  /**
   * Gets the source data files.
   *
   * @return the source data files
   */
  public List<SourceDataFile> getSourceDataFiles();

  /**
   * Sets the config file key for the Handler.
   *
   * @param Handler the Handler
   */
  public void setHandler(String Handler);

  /**
   * Gets the config file key for the Handler.
   *
   * @return the Handler
   */
  public String getHandler();

  /**
   * Returns the Handler status.
   *
   * @return the Handler status
   */
  public SourceData.Status getStatus();

  /**
   * Sets the Handler status.
   *
   * @param status the enumerated status
   */
  public void setStatus(SourceData.Status status);

  /**
   * Gets the status text.
   *
   * @return the status text
   */
  public String getStatusText();

  /**
   * Sets the status text.
   *
   * @param statusText the new status text
   */
  public void setStatusText(String statusText);

  /**
   * Returns the description.
   *
   * @return the description
   */
  /* see superclass */
  public String getDescription();

  /**
   * Sets the description.
   *
   * @param description the new description
   */
  public void setDescription(String description);

  /**
   * Load.
   */
  public void load();

  /**
   * Remove.
   */
  public void remove();

  /**
   * Gets the release version.
   *
   * @return the release version
   */
  public String getReleaseVersion();

  /**
   * Sets the release version.
   *
   * @param releaseVersion the new release version
   */
  public void setReleaseVersion(String releaseVersion);

  /**
   * Add source data file.
   *
   * @param sourceDataFile the source data file
   */
  public void addSourceDataFile(SourceDataFile sourceDataFile);

  /**
   * Remove source data file.
   *
   * @param sourceDataFile the source data file
   */
  public void removeSourceDataFile(SourceDataFile sourceDataFile);

}
