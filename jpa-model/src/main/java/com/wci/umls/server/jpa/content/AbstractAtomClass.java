/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlElement;

import org.apache.lucene.analysis.core.KeywordTokenizerFactory;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilterFactory;
import org.apache.lucene.analysis.ngram.EdgeNGramFilterFactory;
import org.apache.lucene.analysis.ngram.NGramFilterFactory;
import org.apache.lucene.analysis.pattern.PatternReplaceFilterFactory;
import org.apache.lucene.analysis.standard.StandardFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.AnalyzerDefs;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.search.annotations.SortableField;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;
import org.hibernate.search.bridge.builtin.EnumBridge;

import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomClass;
import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * Abstract JPA and JAXB enabled implementation of {@link AtomClass}.
 */
@AnalyzerDefs({
    @AnalyzerDef(name = "noStopWord", tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class), filters = {
        @TokenFilterDef(factory = StandardFilterFactory.class),
        @TokenFilterDef(factory = LowerCaseFilterFactory.class)
    }), @AnalyzerDef(name = "whitespace", tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class), filters = {
        @TokenFilterDef(factory = StandardFilterFactory.class),
        @TokenFilterDef(factory = LowerCaseFilterFactory.class)
    }), @AnalyzerDef(name = "autocompleteEdgeAnalyzer",
        // Split input into tokens according to tokenizer
        tokenizer = @TokenizerDef(factory = KeywordTokenizerFactory.class), filters = {
            // Normalize token text to lowercase, as the user is unlikely to
            // care about casing when searching for matches
            @TokenFilterDef(factory = PatternReplaceFilterFactory.class, params = {
                @Parameter(name = "pattern", value = "([^a-zA-Z0-9\\.])"),
                @Parameter(name = "replacement", value = " "),
                @Parameter(name = "replace", value = "all")
            }), @TokenFilterDef(factory = LowerCaseFilterFactory.class), @TokenFilterDef(factory = StopFilterFactory.class),
            // Index partial words starting at the front, so we can provide
            // Autocomplete functionality
            @TokenFilterDef(factory = EdgeNGramFilterFactory.class, params = {
                @Parameter(name = "minGramSize", value = "3"),
                @Parameter(name = "maxGramSize", value = "50")
            })
        }), @AnalyzerDef(name = "autocompleteNGramAnalyzer",
            // Split input into tokens according to tokenizer
            tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class), filters = {
                // Normalize token text to lowercase, as the user is unlikely to
                // care about casing when searching for matches
                @TokenFilterDef(factory = WordDelimiterFilterFactory.class),
                @TokenFilterDef(factory = LowerCaseFilterFactory.class),
                @TokenFilterDef(factory = NGramFilterFactory.class, params = {
                    @Parameter(name = "minGramSize", value = "3"),
                    @Parameter(name = "maxGramSize", value = "5")
                }), @TokenFilterDef(factory = PatternReplaceFilterFactory.class, params = {
                    @Parameter(name = "pattern", value = "([^a-zA-Z0-9\\.])"),
                    @Parameter(name = "replacement", value = " "),
                    @Parameter(name = "replace", value = "all")
                })
            })
})
@Audited
@MappedSuperclass
public abstract class AbstractAtomClass extends AbstractComponent
    implements AtomClass {

//  /** The descriptions. */
//  @ManyToMany(targetEntity = AtomJpa.class)
//  @CollectionTable(name = "concepts_atoms", joinColumns = @JoinColumn(name = "concepts_id"))
//  @IndexedEmbedded(targetElement = AtomJpa.class)
//  private List<Atom> atoms = null;

  /** The name. */
  @Column(nullable = false, length = 4000)
  private String name;

  /** branched to tracking. */
  @Column(nullable = true)
  private String branchedTo;

  /** The workflow status. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private WorkflowStatus workflowStatus;

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
   * @param collectionCopy the deep copy
   */
  public AbstractAtomClass(AtomClass atomClass, boolean collectionCopy) {
    super(atomClass);
    name = atomClass.getName();
    workflowStatus = atomClass.getWorkflowStatus();
    branchedTo = atomClass.getBranchedTo();
//    if (collectionCopy) {
//      atoms = new ArrayList<>(atomClass.getAtoms());
//    }
  }

//  /* see superclass */
//  @XmlElement(type = AtomJpa.class)
//  @Override
//  public List<Atom> getAtoms() {
//    if (atoms == null) {
//      atoms = new ArrayList<>();
//    }
//    return atoms;
//  }
//
//  /* see superclass */
//  @Override
//  public void setAtoms(List<Atom> atoms) {
//    this.atoms = atoms;
//  }

  /* see superclass */
  @Override
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO, analyzer = @Analyzer(definition = "noStopWord")),
      @Field(name = "nameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  @SortableField(forField = "nameSort")
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
  @FieldBridge(impl = EnumBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public WorkflowStatus getWorkflowStatus() {
    return workflowStatus;
  }

  /* see superclass */
  @Override
  public void setWorkflowStatus(WorkflowStatus workflowStatus) {
    this.workflowStatus = workflowStatus;

  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
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
    AbstractAtomClass other = (AbstractAtomClass) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  public void addBranchedTo(String newBranch) {
    if (newBranch.indexOf(Branch.SEPARATOR) != -1) {
      throw new IllegalArgumentException(
          "New branches may not have a comma in them.");
    }
    branchedTo += newBranch + Branch.SEPARATOR;
  }

  /* see superclass */
  @Override
  public void removeBranchedTo(String closedBranch) {
    if (closedBranch.indexOf(Branch.SEPARATOR) != -1) {
      throw new IllegalArgumentException(
          "New branches may not have a comma in them.");
    }
    final int index = branchedTo.indexOf(closedBranch);
    if (index != -1) {
      branchedTo = branchedTo.substring(0, index - 1)
          + branchedTo.substring(index + closedBranch.length() + 1);
    }

  }

  /* see superclass */
  @Override
  public String toString() {
    return getClass().getSimpleName() + " [" + super.toString() + ", name="
        + name + "]";
  }

}
