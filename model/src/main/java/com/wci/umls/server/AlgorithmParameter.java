/*
 *    Copyright 2017 West Coast Informatics, LLC
 */
package com.wci.umls.server;

import java.util.List;

import com.wci.umls.server.helpers.HasName;

/**
 * Represents a tuple for configuring one field of an algorithm config. TODO:
 * the Jpa layer can have a factory for creating stock fields (like "query" and
 */
public interface AlgorithmParameter extends HasName {

  /**
   * The Enum Type.
   */
  public static enum Type {

    /** boolean type. */
    BOOLEAN,

    /** integer type. */
    INTEGER,

    /** string type. */
    STRING,

    /** text type. */
    TEXT,
    /** enum type. */
    ENUM,
    /**
     * multiple-value selection type (e.g. integrity checks) - render as table.
     */
    MULTI,
    /** directory type. */
    DIRECTORY,

    /** file type. */
    FILE,

    /** The query1. */
    QUERY_ID,

    /** The query2. */
    QUERY_ID_PAIR;
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
  public List<String> getPossibleValues();

  /**
   * Sets the values.
   *
   * @param values the values
   */
  public void setPossibleValues(List<String> values);

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

  /**
   * Returns the value. Captures the actual value of the parameter.
   *
   * @return the value
   */
  public String getValue();

  /**
   * Sets the value. Captures the actual value of the parameter.
   *
   * @param value the value
   */
  public void setValue(String value);

  /**
   * Returns the values. Captures the actual values of the parameter.
   *
   * @return the values
   */
  public List<String> getValues();

  /**
   * Sets the values. Captures the actual values of the parameter.
   *
   * @param values the values
   */
  public void setValues(List<String> values);

}