/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

/**
 * Represents a position in a hierarchical tree of codes. The ancestor path
 * will be a delimiter-separated value of code terminology ids.
 */
public interface CodeTreePosition extends TreePosition {

  /**
   * Returns the code id.
   *
   * @return the code id
   */
  public String getCodeId();

  /**
   * Sets the code id.
   *
   * @param codeId the code id
   */
  public void setCodeId(String codeId);

}