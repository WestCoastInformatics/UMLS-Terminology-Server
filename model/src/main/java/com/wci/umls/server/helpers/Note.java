/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

/**
 * The Interface Note.
 */
public interface Note extends HasLastModified {

  /**
   * Sets the note.
   *
   * @param note the new note
   */
  public void setNote(String note);

  /**
   * Gets the note.
   *
   * @return the note
   */
  public String getNote();

}
