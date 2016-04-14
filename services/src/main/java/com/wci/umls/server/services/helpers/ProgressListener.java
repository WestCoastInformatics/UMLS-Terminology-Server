/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.services.helpers;

/**
 * Generically listens for progress updates.
 *
 * @see ProgressEvent
 */
public interface ProgressListener {

  /**
   * Update progress.
   *
   * @param pe the pe
   */
  public void updateProgress(ProgressEvent pe);

}
