package com.wci.umls.server.helpers;

import java.util.List;

/**
 * The Interface HasNotes.
 */
public interface HasNotes  {

  /**
   * Sets the notes.
   *
   * @param notes the new notes
   */
  public void setNotes(List<Note> notes);
  
  /**
   * Gets the notes.
   *
   * @return the notes
   */
  public List<Note> getNotes();
  
}
