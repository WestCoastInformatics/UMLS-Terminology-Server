/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomSubset;
import com.wci.umls.server.model.content.AtomSubsetMember;
import com.wci.umls.server.model.content.Subset;

/**
 * JPA-enabled implementation of an {@link Atom} {@link Subset}.
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
   * @param deepCopy the deep copy
   */
  public AtomSubsetJpa(AtomSubset subset, boolean deepCopy) {
    super(subset, deepCopy);

    if (deepCopy) {
      for (AtomSubsetMember member : subset.getMembers()) {
        addMember(new AtomSubsetMemberJpa(member, deepCopy));
      }
    }
  }

  /* see superclass */
  @Override
  @XmlElement(type = AtomSubsetMemberJpa.class, name = "member")
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
  public void addMember(AtomSubsetMember member) {
    if (members == null) {
      members = new ArrayList<>();
    }
    members.add(member);
  }

  /* see superclass */
  @Override
  public void removeMember(AtomSubsetMember member) {
    if (members == null) {
      members = new ArrayList<>();
    }
    members.remove(member);
  }

  /* see superclass */
  @Override
  public void clearMembers() {
    members = new ArrayList<>();
  }

}
