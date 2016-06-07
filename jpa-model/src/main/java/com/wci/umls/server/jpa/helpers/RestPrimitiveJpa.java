/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.RestPrimitive;

/**
 * Object to contain a primitive result.
 */
@XmlRootElement(name = "restPrimitive")
public class RestPrimitiveJpa implements RestPrimitive {

  /** The value. */
  private String value;

  /** The type. */
  private String type;

  /**
   * Instantiates an empty {@link RestPrimitiveJpa}.
   */
  public RestPrimitiveJpa() {
    // left empty
  }

  /**
   * Instantiates a {@link RestPrimitiveJpa} from the specified parameters.
   *
   * @param primitive the primitive
   */
  public RestPrimitiveJpa(RestPrimitive primitive) {
    value = primitive.getValue();
    type = primitive.getType();
  }

  /**
   * Constructor.
   * 
   * @param value the value
   * @param type the type
   */
  public RestPrimitiveJpa(String value, String type) {
    this.value = value;
    this.type = type;
  }

  /* see superclass */
  @Override
  @XmlElement
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
  public String getType() {
    return type;
  }

  /* see superclass */
  @Override
  public void setType(String type) {
    this.type = type;

  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
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
    RestPrimitiveJpa other = (RestPrimitiveJpa) obj;
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }
}
