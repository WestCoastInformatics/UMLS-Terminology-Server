/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.AlgorithmParameter;

/**
 * JAXB enabled implementation of {@link AlgorithmParameter}.
 */
@XmlRootElement(name = "parameter")
public class AlgorithmParameterJpa implements AlgorithmParameter {

  /** The name. */
  private String name;

  /** The field name. */
  private String fieldName;

  /** The placeholder. */
  private String placeholder;

  /** The type. */
  private AlgorithmParameter.Type type;

  /** The length. */
  private int length = 0;

  /** The description. */
  private String description;

  /** The possible values. */
  private List<String> possibleValues;

  /** The value. */
  private String value;

  /** The values. */
  private List<String> values;

  /**
   * Instantiates an empty {@link AlgorithmParameterJpa}.
   */
  public AlgorithmParameterJpa() {
    // n/a
  }

  /**
   * Instantiates a {@link AlgorithmParameterJpa} from the specified parameters.
   *
   * @param param the param
   */
  public AlgorithmParameterJpa(AlgorithmParameter param) {
    name = param.getName();
    fieldName = param.getFieldName();
    description = param.getDescription();
    length = param.getLength();
    placeholder = param.getPlaceholder();
    type = param.getType();
    value = param.getValue();
    values = new ArrayList<>(param.getValues());
    possibleValues = new ArrayList<>(param.getPossibleValues());
  }

  /**
   * Instantiates a {@link AlgorithmParameterJpa} from the specified parameters.
   *
   * @param name the name
   * @param fieldName the field name
   * @param desc the desc
   * @param placeholder the placeholder
   * @param length the length
   * @param type the type
   */
  public AlgorithmParameterJpa(String name, String fieldName, String desc,
      String placeholder, int length, AlgorithmParameter.Type type) {
    this.name = name;
    this.fieldName = fieldName;
    this.description = desc;
    this.placeholder = placeholder;
    this.length = length;
    this.type = type;
  }

  /* see superclass */
  @Override
  public String getName() {
    return name;
  }

  /* see superclass */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /* see superclass */
  @Override
  public String getFieldName() {
    return fieldName;
  }

  /* see superclass */
  @Override
  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  /* see superclass */
  @Override
  public String getPlaceholder() {
    return placeholder;
  }

  /* see superclass */
  @Override
  public void setPlaceholder(String placeholder) {
    this.placeholder = placeholder;
  }

  /* see superclass */
  @Override
  public AlgorithmParameter.Type getType() {
    return type;
  }

  /* see superclass */
  @Override
  public void setType(AlgorithmParameter.Type type) {
    this.type = type;
  }

  /* see superclass */
  @Override
  public int getLength() {
    return length;
  }

  /* see superclass */
  @Override
  public void setLength(int length) {
    this.length = length;
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return description;
  }

  /* see superclass */
  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  /* see superclass */
  @Override
  public List<String> getPossibleValues() {
    if (possibleValues == null) {
      possibleValues = new ArrayList<>();
    }
    return possibleValues;
  }

  /* see superclass */
  @Override
  public void setPossibleValues(List<String> values) {
    this.possibleValues = values;
  }

  /* see superclass */
  @Override
  public String getValue() {
    return value;
  }

  /* see superclass */
  @Override
  public void setValue(String value) {
    this.value = value;
  }

  /* see superclass */
  @Override
  public List<String> getValues() {
    if (values == null) {
      values = new ArrayList<>();
    }
    return values;
  }

  /* see superclass */
  @Override
  public void setValues(List<String> values) {
    this.values = values;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + length;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
    result =
        prime * result + ((placeholder == null) ? 0 : placeholder.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result
        + ((possibleValues == null) ? 0 : possibleValues.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    result = prime * result + ((values == null) ? 0 : values.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AlgorithmParameterJpa other = (AlgorithmParameterJpa) obj;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (length != other.length)
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (fieldName == null) {
      if (other.fieldName != null)
        return false;
    } else if (!fieldName.equals(other.fieldName))
      return false;
    if (placeholder == null) {
      if (other.placeholder != null)
        return false;
    } else if (!placeholder.equals(other.placeholder))
      return false;
    if (type != other.type)
      return false;
    if (possibleValues == null) {
      if (other.possibleValues != null)
        return false;
    } else if (!possibleValues.equals(other.possibleValues))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    if (values == null) {
      if (other.values != null)
        return false;
    } else if (!values.equals(other.values))
      return false;

    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "AlgorithmParameterJpa [name=" + name + ", fieldName=" + fieldName
        + ", placeholder=" + placeholder + ", type=" + type + ", length="
        + length + ", description=" + description + ", possibleValues="
        + possibleValues + ", value=" + value + ", values=" + values + "]";
  }

}
