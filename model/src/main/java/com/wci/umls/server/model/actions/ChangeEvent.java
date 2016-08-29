/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.actions;

import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.helpers.HasName;

/**
 * Generically represents a change event for a model object.
 */
public interface ChangeEvent extends HasName, HasLastModified {

  /**
   * Returns the session id.
   *
   * @return the session id
   */
  public String getSessionId();

  /**
   * Sets the session id.
   *
   * @param sessionId the session id
   */
  public void setSessionId(String sessionId);

  /**
   * Returns the type.
   *
   * @return the type
   */
  public String getType();

  /**
   * Sets the type.
   *
   * @param type the type
   */
  public void setType(String type);

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
   * Returns the container.
   *
   * @return the container
   */
  public ComponentInfo getContainer();

  /**
   * Sets the container.
   *
   * @param container the container
   */
  public void setContainer(ComponentInfo container);

  // TODO: add the moleuclar action id
}
