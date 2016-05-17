package com.wci.umls.server.helpers;

import com.wci.umls.server.model.meta.IdType;

/**
 * The Component Informational Pointer Object Represents information needed to
 * resolve a component supported by IdType
 */
public interface ComponentInfo
    extends HasTerminologyId, HasLastModified, HasName {

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
