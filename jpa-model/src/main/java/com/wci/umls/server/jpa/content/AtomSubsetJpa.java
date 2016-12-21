/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
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
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomSubset;
import com.wci.umls.server.model.content.AtomSubsetMember;
import com.wci.umls.server.model.content.Subset;

/**
 * JPA and JAXB enabled implementation of an {@link Atom} {@link Subset}.
 */
@Entity
@Table(name = "atom_subsets", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "version", "id"
}))
@Audited
@XmlRootElement(name = "atomSubset")
public class AtomSubsetJpa extends AbstractSubset implements AtomSubset {

  /** The members. */
  @OneToMany(mappedBy = "subset", targetEntity = AtomSubsetMemberJpa.class)
  private List<AtomSubsetMember> members = null;

  /** The alternate terminology ids. */
  @ElementCollection
//  @CollectionTable(name = "atom_subset_altIds", joinColumns = @JoinColumn(name = "altIds", referencedColumnName = "terminologyId"))
//  @MapKeyColumn(name = "terminology", length = 100)
//  @Column(nullable = true, length = 100)
  private Map<String, String> alternateTerminologyIds;

  /**
   * Instantiates an empty {@link AtomSubsetJpa}.
   */
  public AtomSubsetJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link AtomSubsetJpa} from the specified parameters.
   *
   * @param subset the subset
   * @param collectionCopy the deep copy
   */
  public AtomSubsetJpa(AtomSubset subset, boolean collectionCopy) {
    super(subset, collectionCopy);
    alternateTerminologyIds =
        new HashMap<>(subset.getAlternateTerminologyIds());

    if (collectionCopy) {
      members = new ArrayList<>(subset.getMembers());
    }
  }

  /* see superclass */
  @Override
  @XmlElement(type = AtomSubsetMemberJpa.class)
  public List<AtomSubsetMember> getMembers() {
    if (members == null) {
      members = new ArrayList<>();
    }
    return members;
  }

  /* see superclass */
  @Override
  public void setMembers(List<AtomSubsetMember> members) {
    this.members = members;
  }

  /* see superclass */
  @Override
  public void clearMembers() {
    members = new ArrayList<>();
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

}
