package com.wci.umls.server.helpers;

import com.wci.umls.server.model.meta.IdType;

/**
 * Represents a pointer to some kind of component.
 */
public interface ComponentInfo extends HasTerminologyId, HasLastModified,
    HasName {

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
