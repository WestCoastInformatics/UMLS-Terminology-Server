/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.model.content.AtomClass;

/**
 * The Interface Note.
 *
 * @param <T> the atomclass type
 */
public interface Note<T extends AtomClass> extends HasLastModified {

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
  
  /**
   * Sets the node.
   *
   * @param note the new node
   */
  public void setNode(T note);
  
  /**
   * Gets the node.
   *
   * @return the node
   */
  public T getNode();
  
}
