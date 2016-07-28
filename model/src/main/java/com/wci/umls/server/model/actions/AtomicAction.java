/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.actions;

import com.wci.umls.server.helpers.HasId;
import com.wci.umls.server.model.meta.IdType;

/**
 * The Interface AtomicAction.
 */
public interface AtomicAction extends HasId {

  /**
   * Gets the molecular action.
   *
   * @return the molecular action
   */
  public MolecularAction getMolecularAction();

  /**
   * Sets the molecular action.
   *
   * @param molecularAction the new molecular action
   */
  public void setMolecularAction(MolecularAction molecularAction);

  /**
   * Gets the id type.
   *
   * @return the id type
   */
  public IdType getIdType();

  /**
   * Sets the id type.
   *
   * @param idType the new id type
   */
  public void setIdType(IdType idType);

  /**
   * Gets the field.
   *
   * @return the field
   */
  public String getField();

  /**
   * Sets the field.
   *
   * @param field the new field
   */
  public void setField(String field);

  /**
   * Gets the old value.
   *
   * @return the old value
   */
  public String getOldValue();

  /**
   * Sets the old value.
   *
   * @param oldValue the new old value
   */
  public void setOldValue(String oldValue);

  /**
   * Gets the new value.
   *
   * @return the new value
   */
  public String getNewValue();

  /**
   * Sets the new value.
   *
   * @param newValue the new new value
   */
  public void setNewValue(String newValue);

  /**
   * Gets the object id.
   *
   * @return the object id
   */
  public Long getObjectId();

  /**
   * Sets the object id.
   *
   * @param id the new object id
   */
  public void setObjectId(Long id);

  /**
   * Returns the class name.
   *
   * @return the class name
   */
  public String getClassName();

  /**
   * Sets the class name.
   *
   * @param className the class name
   */
  public void setClassName(String className);

}
