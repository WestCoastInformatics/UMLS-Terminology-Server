/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.jpa.helpers.MapKeyValueToCsvBridge;
import com.wci.umls.server.model.content.Subset;

/**
 * Abstract JPA and JAXB enabled implementation of {@link Subset}.
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

  /** The alternate terminology ids. */
  @ElementCollection()
  // consider this: @Fetch(FetchMode.JOIN)
  @Column(nullable = true)
  private Map<String, String> alternateTerminologyIds;

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
   * @param collectionCopy the deep copy
   */
  public AbstractSubset(Subset subset, boolean collectionCopy) {
    super(subset, collectionCopy);
    name = subset.getName();
    description = subset.getDescription();
    branchedTo = subset.getBranchedTo();
    alternateTerminologyIds =
        new HashMap<>(subset.getAlternateTerminologyIds());
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
  public String getBranchedTo() {
    return branchedTo;
  }

  /* see superclass */
  @Override
  public void setBranchedTo(String branchedTo) {
    this.branchedTo = branchedTo;
  }

  /* see superclass */
  @Override
  @FieldBridge(impl = MapKeyValueToCsvBridge.class)
  @Field(name = "alternateTerminologyIds", index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public Map<String, String> getAlternateTerminologyIds() {
    if (alternateTerminologyIds == null) {
      alternateTerminologyIds = new HashMap<>(2);
    }
    return alternateTerminologyIds;
  }

  /* see superclass */
  @Override
  public void setAlternateTerminologyIds(
    Map<String, String> alternateTerminologyIds) {
    this.alternateTerminologyIds = alternateTerminologyIds;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  /* see superclass */
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

  /* see superclass */
  @Override
  public String toString() {
    return getClass().getSimpleName() + " [name=" + name + ", description="
        + description + "]";
  }

}
