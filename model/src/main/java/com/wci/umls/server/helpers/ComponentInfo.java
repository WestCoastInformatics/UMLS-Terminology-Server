package com.wci.umls.server.helpers;

import java.util.Date;

import com.wci.umls.server.model.meta.IdType;

/**
 * Represents a pointer to some kind of component.
 */
public interface ComponentInfo extends HasTerminologyId, HasId {

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
   * @throws Exception the exception
   */
  public IdType getType() throws Exception;

  /**
   * Returns the name.
   *
   * @return the name
   */
  public String getName();

  /**
   * Sets the name.
   *
   * @param name the name
   */
  public void setName(String name);
}
