/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.ComponentHasAttributes;

/**
 * Abstract implementation of {@link ComponentHasAttributes} for use with JPA.
 */
@Audited
@MappedSuperclass
public abstract class AbstractComponentHasAttributes extends AbstractComponent
    implements ComponentHasAttributes {
//
//  /** The attributes. */
//  @OneToMany(targetEntity = AttributeJpa.class)
//  // @IndexedEmbedded(targetElement = AttributeJpa.class)
//  private List<Attribute> attributes = null;
//
//  /**
//   * Instantiates an empty {@link AbstractComponentHasAttributes}.
//   */
//  public AbstractComponentHasAttributes() {
//    // do nothing
//  }
//
//  /**
//   * Instantiates a {@link AbstractComponentHasAttributes} from the specified
//   * parameters.
//   *
//   * @param component the component
//   * @param collectionCopy the deep copy
//   */
//  public AbstractComponentHasAttributes(ComponentHasAttributes component,
//      boolean collectionCopy) {
//    super(component);
//
//    if (collectionCopy) {
//      for (final Attribute attribute : component.getAttributes()) {
//        getAttributes().add(new AttributeJpa(attribute));
//      }
//    }
//  }
//
//  /* see superclass */
//  @Override
//  @XmlElement(type = AttributeJpa.class)
//  public List<Attribute> getAttributes() {
//    if (attributes == null) {
//      attributes = new ArrayList<>(1);
//    }
//    return attributes;
//  }
//
//  /* see superclass */
//  @Override
//  public void setAttributes(List<Attribute> attributes) {
//    this.attributes = attributes;
//  }
//
//  /* see superclass */
//  @Override
//  public Attribute getAttributeByName(String name) {
//    for (final Attribute attribute : getAttributes()) {
//      // If there are more than one, this just returns the first.
//      if (attribute.getName().equals(name)) {
//        return attribute;
//      }
//    }
//    return null;
//  }

}
