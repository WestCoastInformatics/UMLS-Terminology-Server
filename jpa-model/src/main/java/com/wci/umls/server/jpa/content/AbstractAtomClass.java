/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;

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
public class AbstractAtomClass extends AbstractComponentHasAttributes implements AtomClass {

  /** The descriptions. */
  @ManyToMany(targetEntity = AtomJpa.class)
  @IndexedEmbedded(targetElement = AtomJpa.class)
  private List<Atom> atoms = null;

  /** The default preferred name. */
  @Column(nullable = false, length = 4000)
  private String defaultPreferredName;

  /**
   * Instantiates an empty {@link AbstractAtomClass}.
   */
  protected AbstractAtomClass() {
    // do nothing
  }

  /**
   * Instantiates a {@link AbstractAtomClass} from the specified parameters.
   *
   * @param atomClass the atom
   * @param deepCopy the deep copy
   */
  protected AbstractAtomClass(AtomClass atomClass, boolean deepCopy) {
    super(atomClass, deepCopy);
    defaultPreferredName = atomClass.getDefaultPreferredName();
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
  @Override
  public void setDefaultPreferredName(String defaultPreferredName) {
    this.defaultPreferredName = defaultPreferredName;
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

}
