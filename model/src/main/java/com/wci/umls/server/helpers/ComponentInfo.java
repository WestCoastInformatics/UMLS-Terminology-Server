package com.wci.umls.server.helpers;

import java.util.Date;

import com.wci.umls.server.model.meta.IdType;

/**
 * Represents a pointer to some kind of component.
 */
public interface ComponentInfo extends HasTerminologyId, HasName, HasId {

  /**
   * Returns the timestamp.
   *
   * @return the timestamp
   */
  public Date getTimestamp();

  /**
   * Sets the timestamp.
   *
   * @param timestamp the timestamp
   */
  public void setTimestamp(Date timestamp);

  /**
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(IdType type);

  /**
   * Gets the type.
   *
   * @return the type
   */
  public IdType getType();
}
