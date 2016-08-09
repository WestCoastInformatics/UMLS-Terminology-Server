/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server;

import java.util.List;

import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.helpers.HasName;

/**
 * Represents a tuple for configuring one field of an algorithm config. TODO:
 * the Jpa layer can have a factory for creating stock fields (like "query" and
 */
public interface AlgorithmParameter extends HasName, HasLastModified {

  /**
   * The Enum Type.
   */
  public static enum Type {

    /** boolean type. */
    BOOLEAN,
    /** string type. */
    STRING,
    /** text type */
    TEXT,
    /** enum type. */
    ENUM,
    /** directory type. */
    DIRECTORY,
    /** file type */
    FILE;
  }

  /**
   * Returns the field name.
   *
   * @return the field name
   */
  public String getFieldName();

  /**
   * Sets the field name.
   *
   * @param fieldName the field name
   */
  public void setFieldName(String fieldName);

  /**
   * Returns the type.
   *
   * @return the type
   */
  public Type getType();

  /**
   * Sets the type.
   *
   * @param type the type
   */
  public void setType(Type type);

  /**
   * Returns the length.
   *
   * @return the length
   */
  public int getLength();

  /**
   * Sets the length.
   *
   * @param length the length
   */
  public void setLength(int length);

  /**
   * Returns the values.
   *
   * @return the values
   */
  public List<String> getValues();

  /**
   * Sets the values.
   *
   * @param values the values
   */
  public void setValues(List<String> values);

  /**
   * Returns the value.
   *
   * @return the value
   */
  public String getValue();

  /**
   * Sets the value.
   *
   * @param value the value
   */
  public void setValue(String value);

  /**
   * Returns the placeholder.
   *
   * @return the placeholder
   */
  public String getPlaceholder();

  /**
   * Sets the placeholder.
   *
   * @param placehodler the placeholder
   */
  public void setPlaceholder(String placehodler);

  /**
   * Returns the description.
   *
   * @return the description
   */
  public String getDescription();

  /**
   * Sets the description.
   *
   * @param description the description
   */
  public void setDescription(String description);

}