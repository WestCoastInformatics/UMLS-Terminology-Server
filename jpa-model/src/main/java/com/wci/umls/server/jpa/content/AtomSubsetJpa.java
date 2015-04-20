/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
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
@Audited
@DiscriminatorValue("Atom")
@XmlRootElement(name = "subset")
public class AtomSubsetJpa extends AbstractSubset implements AtomSubset {

  /** The members. */
  @OneToMany(orphanRemoval = true, targetEntity = AtomSubsetMemberJpa.class)
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
      for (AtomSubsetMember member : members) {
        addMember(new AtomSubsetMemberJpa(member, deepCopy));
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.AtomSubset#getMembers()
   */
  @Override
  public List<AtomSubsetMember> getMembers() {
    if (members == null) {
      members = new ArrayList<>();
    }
    return members;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.AtomSubset#setMembers(java.util.List)
   */
  @Override
  public void setMembers(List<AtomSubsetMember> members) {
    this.members = members;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.AtomSubset#addMember(com.wci.umls.server
   * .model.content.AtomSubsetMember)
   */
  @Override
  public void addMember(AtomSubsetMember member) {
    if (members == null) {
      members = new ArrayList<>();
    }
    members.add(member);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.AtomSubset#removeMember(com.wci.umls.
   * server.model.content.AtomSubsetMember)
   */
  @Override
  public void removeMember(AtomSubsetMember member) {
    if (members == null) {
      members = new ArrayList<>();
    }
    members.remove(member);
  }

}
