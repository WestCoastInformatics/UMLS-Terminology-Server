/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlElement;

import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.standard.StandardFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;

import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomClass;

/**
 * Abstract JPA-enabled implementation of {@link AtomClass}.
 */
@AnalyzerDef(name = "noStopWord", tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class), filters = {
    @TokenFilterDef(factory = StandardFilterFactory.class),
    @TokenFilterDef(factory = LowerCaseFilterFactory.class)
})
@Audited
@MappedSuperclass
public class AbstractAtomClass extends AbstractComponentHasAttributes implements
    AtomClass {

  /** The descriptions. */
  @ManyToMany(targetEntity = AtomJpa.class)
  @IndexedEmbedded(targetElement = AtomJpa.class)
  private List<Atom> atoms = null;

  /** The default preferred name. */
  @Column(nullable = false, length = 4000)
  private String defaultPreferredName;

  /** branched to tracking. */
  @Column(nullable = true)
  private String branchedTo;

  /** The workflow status. */
  @Column(nullable = true)
  private String workflowStatus;

  /**
   * Instantiates an empty {@link AbstractAtomClass}.
   */
  public AbstractAtomClass() {
    // do nothing
  }

  /**
   * Instantiates a {@link AbstractAtomClass} from the specified parameters.
   *
   * @param atomClass the atom
   * @param deepCopy the deep copy
   */
  public AbstractAtomClass(AtomClass atomClass, boolean deepCopy) {
    super(atomClass, deepCopy);
    defaultPreferredName = atomClass.getDefaultPreferredName();
    workflowStatus = atomClass.getWorkflowStatus();
    if (deepCopy) {
      for (Atom atom : atomClass.getAtoms()) {
        addAtom(new AtomJpa(atom, deepCopy));
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.AtomClass#getAtoms()
   */
  /**
   * Returns the atoms.
   *
   * @return the atoms
   */
  @XmlElement(type = AtomJpa.class, name = "atom")
  @Override
  public List<Atom> getAtoms() {
    if (atoms == null) {
      atoms = new ArrayList<>();
    }
    return atoms;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.AtomClass#setAtoms(java.util.List)
   */
  /**
   * Sets the atoms.
   *
   * @param atoms the atoms
   */
  @Override
  public void setAtoms(List<Atom> atoms) {
    this.atoms = atoms;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.AtomClass#addAtom(com.wci.umls.server
   * .model.content.Atom)
   */
  /**
   * Adds the atom.
   *
   * @param atom the atom
   */
  @Override
  public void addAtom(Atom atom) {
    if (atoms == null) {
      atoms = new ArrayList<>();
    }
    atoms.add(atom);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.AtomClass#removeAtom(com.wci.umls.server
   * .model.content.Atom)
   */
  /**
   * Removes the atom.
   *
   * @param atom the atom
   */
  @Override
  public void removeAtom(Atom atom) {
    if (atoms == null) {
      atoms = new ArrayList<>();
    }
    atoms.remove(atom);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.AtomClass#getDefaultPreferredName()
   */
  /**
   * Returns the default preferred name.
   *
   * @return the default preferred name
   */
  @Override
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "defaultPreferredNameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  @Analyzer(definition = "noStopWord")
  public String getDefaultPreferredName() {
    return defaultPreferredName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.AtomClass#setDefaultPreferredName(java
   * .lang.String)
   */
  /**
   * Sets the default preferred name.
   *
   * @param defaultPreferredName the default preferred name
   */
  @Override
  public void setDefaultPreferredName(String defaultPreferredName) {
    this.defaultPreferredName = defaultPreferredName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.AtomClass#getWorkflowStatus()
   */
  /**
   * Returns the workflow status.
   *
   * @return the workflow status
   */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getWorkflowStatus() {
    return workflowStatus;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.AtomClass#setWorkflowStatus(java.lang
   * .String)
   */
  /**
   * Sets the workflow status.
   *
   * @param workflowStatus the workflow status
   */
  @Override
  public void setWorkflowStatus(String workflowStatus) {
    this.workflowStatus = workflowStatus;

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractComponent#hashCode()
   */
  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime
            * result
            + ((defaultPreferredName == null) ? 0 : defaultPreferredName
                .hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.content.AbstractComponent#equals(java.lang.Object)
   */
  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    AbstractAtomClass other = (AbstractAtomClass) obj;
    if (defaultPreferredName == null) {
      if (other.defaultPreferredName != null)
        return false;
    } else if (!defaultPreferredName.equals(other.defaultPreferredName))
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
    return "AbstractAtomClass [atoms=" + atoms + ", defaultPreferredName="
        + defaultPreferredName + "]";
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.AtomClass#getBranchedTo()
   */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getBranchedTo() {
    return branchedTo;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.AtomClass#setBranchedTo(java.lang.String)
   */
  @Override
  public void setBranchedTo(String branchedTo) {
    this.branchedTo = branchedTo;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.AtomClass#addBranchedTo(java.lang.String)
   */
  @Override
  public void addBranchedTo(String newBranch) {
    if (newBranch.indexOf(Branch.SEPARATOR) != -1) {
      throw new IllegalArgumentException(
          "New branches may not have a comma in them.");
    }
    branchedTo += newBranch + Branch.SEPARATOR;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.AtomClass#removeBranchedTo(java.lang.
   * String)
   */
  @Override
  public void removeBranchedTo(String closedBranch) {
    if (closedBranch.indexOf(Branch.SEPARATOR) != -1) {
      throw new IllegalArgumentException(
          "New branches may not have a comma in them.");
    }
    final int index = branchedTo.indexOf(closedBranch);
    if (index != -1) {
      branchedTo =
          branchedTo.substring(0, index - 1)
              + branchedTo.substring(index + closedBranch.length() + 1);
    }

  }

}
