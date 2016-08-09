/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.actions;

import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.helpers.HasName;
import com.wci.umls.server.model.content.Component;

/**
 * Generically represents a change event for a model object.
 *
 * @param <T> the
 */
public interface ChangeEvent<T extends Component> extends HasName,
    HasLastModified {

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
   * Returns the old value.
   *
   * @return the old value
   */
  public T getOldValue();

  /**
   * Sets the old value.
   *
   * @param oldValue the old value
   */
  public void setOldValue(T oldValue);

  /**
   * Returns the new value.
   *
   * @return the new value
   */
  public T getNewValue();

  /**
   * Sets the new value.
   *
   * @param newValue the new value
   */
  public void setNewValue(T newValue);

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
