/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.jpa.helpers.MapKeyValueToCsvBridge;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptSubset;
import com.wci.umls.server.model.content.ConceptSubsetMember;
import com.wci.umls.server.model.content.Subset;

/**
 * JPA and JAXB enabled implementation of an {@link Concept} {@link Subset}.
 */
@Entity
@Table(name = "concept_subsets", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "version", "id"
}))
@Audited
@XmlRootElement(name = "conceptSubset")
public class ConceptSubsetJpa extends AbstractSubset implements ConceptSubset {

  /** The disjoint subset. */
  @Column(nullable = false)
  private boolean disjointSubset = false;

  /** The label subset flag. */
  @Column(nullable = false)
  private boolean labelSubset = false;

  /** The members. */
  @OneToMany(mappedBy = "subset", targetEntity = ConceptSubsetMemberJpa.class)
  private List<ConceptSubsetMember> members = null;

  /** The alternate terminology ids. */
  @ElementCollection
  @MapKeyColumn(length = 100)
  @Column(nullable = true, length = 100)
  private Map<String, String> alternateTerminologyIds;

  /**
   * Instantiates an empty {@link ConceptSubsetJpa}.
   */
  public ConceptSubsetJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ConceptSubsetJpa} from the specified parameters.
   *
   * @param subset the subset
   * @param collectionCopy the deep copy
   */
  public ConceptSubsetJpa(ConceptSubset subset, boolean collectionCopy) {
    super(subset, collectionCopy);
    disjointSubset = subset.isDisjointSubset();
    labelSubset = subset.isLabelSubset();
    alternateTerminologyIds =
        new HashMap<>(subset.getAlternateTerminologyIds());

    if (collectionCopy) {
      members = new ArrayList<>(subset.getMembers());
    }

  }

  /* see superclass */
  @XmlElement(type = ConceptSubsetMemberJpa.class)
  @Override
  public List<ConceptSubsetMember> getMembers() {
    if (members == null) {
      members = new ArrayList<>();
    }
    return members;
  }

  /* see superclass */
  @Override
  public void setMembers(List<ConceptSubsetMember> members) {
    this.members = members;
  }

  /* see superclass */
  @Override
  public void clearMembers() {
    members = new ArrayList<>();
  }

  /* see superclass */
  @Override
  public boolean isDisjointSubset() {
    return disjointSubset;
  }

  /* see superclass */
  @Override
  public void setDisjointSubset(boolean disjointSubset) {
    this.disjointSubset = disjointSubset;
  }

  /* see superclass */
  @Override
  public boolean isLabelSubset() {
    return labelSubset;
  }

  /* see superclass */
  @Override
  public void setLabelSubset(boolean labelSubset) {
    this.labelSubset = labelSubset;
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
    result = prime * result + (disjointSubset ? 1231 : 1237);
    result = prime * result + (labelSubset ? 1231 : 1237);
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
    ConceptSubsetJpa other = (ConceptSubsetJpa) obj;
    if (disjointSubset != other.disjointSubset)
      return false;
    if (labelSubset != other.labelSubset)
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return getClass().getSimpleName() + " [name=" + getName() + ", description="
        + getDescription() + ", disjointSubset=" + disjointSubset + "]";
  }

}
