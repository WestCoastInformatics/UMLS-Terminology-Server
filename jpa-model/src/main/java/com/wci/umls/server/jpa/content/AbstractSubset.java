/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.content.Subset;

/**
 * Abstract JPA-enabled implementation of {@link Subset}.
 */
@Audited
@MappedSuperclass
@XmlSeeAlso({
    AtomSubsetJpa.class, ConceptSubsetJpa.class
})
public abstract class AbstractSubset extends AbstractComponentHasAttributes
    implements Subset {

  /** The name. */
  @Column(nullable = false)
  private String name;

  /** The description. */
  @Column(nullable = false, length = 4000)
  private String description;


  /** The branched to. */
  @Column(nullable = true)
  private String branchedTo;

  /**
   * Instantiates an empty {@link AbstractSubset}.
   */
  public AbstractSubset() {
    // do nothing
  }

  /**
   * Instantiates a {@link AbstractSubset} from the specified parameters.
   *
   * @param subset the subset
   * @param deepCopy the deep copy
   */
  public AbstractSubset(Subset subset, boolean deepCopy) {
    super(subset, deepCopy);
    name = subset.getName();
    description = subset.getDescription();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Subset#getName()
   */
  @Override
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Subset#setName(java.lang.String)
   */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Subset#getDescription()
   */
  @Override
  public String getDescription() {
    return description;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Subset#setDescription(java.lang.String)
   */
  @Override
  public void setDescription(String description) {
    this.description = description;
  }



  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Subset#getBranchedTo()
   */
  @Override
  public String getBranchedTo() {
    return branchedTo;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Subset#setBranchedTo(java.lang.String)
   */
  @Override
  public void setBranchedTo(String branchedTo) {
    this.branchedTo = branchedTo;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractComponent#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.content.AbstractComponent#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    AbstractSubset other = (AbstractSubset) obj;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractComponent#toString()
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + " [name=" + name + ", description=" + description
        + "]";
  }

}
