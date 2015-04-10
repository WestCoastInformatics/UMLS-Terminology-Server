/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services.helpers;

/**
 * Generically represents an update to progress information.
 */
public class ProgressEvent {

  /** The source. */
  private Object source;

  /** The pct. */
  private int pct = 0;

  /** The progress. */
  private long progress = 0;

  /** The note. */
  private String note = null;

  /**
   * Instantiates a {@link ProgressEvent} from the specified information.
   * @param source event source
   * @param pct percent finished
   * @param progress total progress value
   * @param note progress note
   */
  public ProgressEvent(Object source, int pct, long progress, String note) {
    this.pct = pct;
    this.progress = progress;
    this.note = note;
    this.source = source;
  }

  /**
   * Returns the percentage completed.
   * @return the percentage completed
   */
  public int getPercent() {
    return pct;
  }

  /**
   * Returns the scaled percentage. It maps a 0 to 100 range to the specified
   * range. E.g. If getPercent() returns 50, getScaledPercent(50,100) eturns 75.
   * @param low the low end of the scale
   * @param high the high end of the scale
   * @return the scaled percentage completed
   */
  public int getScaledPercent(int low, int high) {
    if (pct < 0) {
      return low;
    }
    if (pct > 100) {
      return high;
    }
    return (((high - low) * pct) / 100) + low;
  }

  /**
   * Returns the progress.
   * @return the progress
   */
  public long getProgress() {
    return progress;
  }

  /**
   * Returns the note.
   * @return the note
   */
  public String getNote() {
    return note;
  }

  /**
   * Returns the source.
   * @return the source
   */
  public Object getSource() {
    return source;
  }
}
