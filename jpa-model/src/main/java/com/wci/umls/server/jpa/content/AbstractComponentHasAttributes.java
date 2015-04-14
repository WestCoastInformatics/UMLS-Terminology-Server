/**
 * Copyright 2015 West Coast Informatics, LLC
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

  /** The attributes. */
  @OneToMany(targetEntity = AttributeJpa.class)
  // @IndexedEmbedded(targetElement = AttributeJpa.class)
  private List<Attribute> attributes = null;

  /**
   * Instantiates an empty {@link AbstractComponentHasAttributes}.
   */
  public AbstractComponentHasAttributes() {
    // do nothing
  }

  /**
   * Instantiates a {@link AbstractComponentHasAttributes} from the specified
   * parameters.
   *
   * @param component the component
   * @param deepCopy the deep copy
   */
  public AbstractComponentHasAttributes(ComponentHasAttributes component,
      boolean deepCopy) {
    super(component);

    if (deepCopy) {
      for (Attribute attribute : component.getAttributes()) {
        addAttribute(new AttributeJpa(attribute));
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Component#getAttributes()
   */
  @Override
  @XmlElement(type = AttributeJpa.class, name = "attribute")
  public List<Attribute> getAttributes() {
    if (attributes == null) {
      attributes = new ArrayList<>();
    }
    return attributes;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Component#setAttributes(java.util.List)
   */
  @Override
  public void setAttributes(List<Attribute> attributes) {
    this.attributes = attributes;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Component#addAttribute(com.wci.umls.server
   * .model.content.Attribute)
   */
  @Override
  public void addAttribute(Attribute attribute) {
    if (attributes == null) {
      attributes = new ArrayList<>();
    }
    attributes.add(attribute);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Component#removeAttribute(com.wci.umls
   * .server.model.content.Attribute)
   */
  @Override
  public void removeAttribute(Attribute attribute) {
    if (attributes == null) {
      attributes = new ArrayList<>();
    }
    attributes.remove(attribute);
  }

}
