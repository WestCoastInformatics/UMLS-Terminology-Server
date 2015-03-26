package com.wci.umls.server.helpers;

import java.util.List;

import com.wci.umls.server.model.content.Attribute;

/**
 * Represents a thing that has attributes.
 */
public interface HasAttributes  {

  /**
   * Returns the attributes.
   *
   * @return the attributes
   */
  public List<Attribute> getAttributes();
  
  /**
   * Sets the attributes.
   *
   * @param attributes the attributes
   */
  public void setAttributes(List<Attribute> attributes);
  
  /**
   * Adds the attribute.
   *
   * @param attribute the attribute
   */
  public void addAttribute(Attribute attribute);
  
  /**
   * Removes the attribute.
   *
   * @param attribute the attribute
   */
  public void removeAttribute(Attribute attribute);

}
