/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitor;

import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.jpa.ReleaseInfoJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.helpers.PrecedenceListJpa;
import com.wci.umls.server.jpa.meta.AdditionalRelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.AttributeNameJpa;
import com.wci.umls.server.jpa.meta.GeneralMetadataEntryJpa;
import com.wci.umls.server.jpa.meta.RelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.RootTerminologyJpa;
import com.wci.umls.server.jpa.meta.TermTypeJpa;
import com.wci.umls.server.jpa.meta.TerminologyJpa;
import com.wci.umls.server.jpa.services.HistoryServiceJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.CodeVariantType;
import com.wci.umls.server.model.meta.GeneralMetadataEntry;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.NameVariantType;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.model.meta.TermTypeStyle;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.model.meta.UsageType;
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;

/**
 * Implementation of an algorithm to import Owl data.
 * 
 * TODO: make sure this all uses imports included.. e.g.
 * ontology.getImportsClosure()
 */
public class OwlLoaderAlgorithm extends HistoryServiceJpa implements Algorithm {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The logging object ct threshold. */
  private final static int logCt = 2000;

  /** The commit count. */
  private final static int commitCt = 2000;

  /** The terminology. */
  String terminology;

  /** The terminology version. */
  String version;

  /** release version. */
  String releaseVersion;

  /** The release version date. */
  Date releaseVersionDate;

  /** The terminology language. */
  String terminologyLanguage;

  /** counter for objects created, reset in each load section. */
  int objectCt;

  /** The input file. */
  private String inputFile;

  /** The additional relationship types. */
  Map<String, AdditionalRelationshipType> relaMap = new HashMap<>();

  /** The atn map. */
  Map<String, AttributeName> atnMap = new HashMap<>();

  /** The term types. */
  private Set<String> termTypes = new HashSet<>();

  /** The languages. */
  private Set<String> languages = new HashSet<>();

  /** The attribute names. */
  @SuppressWarnings("unused")
  private Set<String> attributeNames = new HashSet<>();

  /** The concept attribute values. */
  private Set<String> generalEntryValues = new HashSet<>();

  /** The loader. */
  final String label = "rdfs:label";

  /** The comment. */
  final String comment = "rdfs:comment";

  /** The loader. */
  final String loader = "loader";

  /** The published. */
  final String published = "PUBLISHED";

  /** The init pref name. */
  final String initPrefName = "No default preferred name found";

  /** The current date. */
  final Date currentDate = new Date();

  /**
   * Instantiates an empty {@link OwlLoaderAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public OwlLoaderAlgorithm() throws Exception {
    super();
  }

  /**
   * Sets the terminology.
   *
   * @param terminology the terminology
   */
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  /**
   * Sets the terminology version.
   *
   * @param version the terminology version
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Returns the input file.
   *
   * @return the input file
   */
  public String getInputFile() {
    return inputFile;
  }

  /**
   * Sets the input file.
   *
   * @param inputFile the input file
   */
  public void setInputFile(String inputFile) {
    this.inputFile = inputFile;
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    Logger.getLogger(getClass()).info("Starting loading Owl terminology");
    Logger.getLogger(getClass()).info("  inputFile = inputFile");
    Logger.getLogger(getClass()).info("  terminology = " + terminology);
    Logger.getLogger(getClass()).info("  version = " + version);

    try {

      setTransactionPerOperation(false);
      beginTransaction();

      if (!new File(inputFile).exists()) {
        throw new Exception("Specified input file does not exist");
      }

      // open input file and get effective time and version and language
      // TODO:
      // findVersion(inputFile);
      // findLanguage(inputFile);
      releaseVersion = version;
      releaseVersionDate = currentDate;

      final FileInputStream in = new FileInputStream(new File(inputFile));
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology directOntology = manager.loadOntologyFromOntologyDocument(in);

      // Use import closure
      for (OWLOntology ontology : directOntology.getImportsClosure()) {
        loadOntology(ontology);
      }

      // Handle metadata
      loadMetadata(directOntology);

      //
      // Create ReleaseInfo for this release if it does not already exist
      // TODO:
      //
      ReleaseInfo info = getReleaseInfo(terminology, releaseVersion);
      if (info == null) {
        info = new ReleaseInfoJpa();
        info.setName(releaseVersion);
        info.setDescription(terminology + " " + releaseVersion + " release");
        info.setPlanned(false);
        info.setPublished(true);
        info.setReleaseBeginDate(releaseVersionDate);
        info.setReleaseFinishDate(releaseVersionDate);
        info.setTerminology(terminology);
        info.setVersion(releaseVersion);
        // TODO:
        // info.setLastModified(releaseVersionDate);
        info.setLastModified(new Date());
        info.setLastModifiedBy(loader);
        addReleaseInfo(info);
      }

      commit();
      clear();
      close();

      Logger.getLogger(getClass()).info("Done ...");

    } catch (Exception e) {
      e.printStackTrace();
      throw new Exception("Owl loader failed", e);
    } finally {
      // tbd
    }

  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // do nothing
  }

  /**
   * Fires a {@link ProgressEvent}.
   * @param pct percent done
   * @param note progress note
   */
  public void fireProgressEvent(int pct, String note) {
    ProgressEvent pe = new ProgressEvent(this, pct, pct, note);
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).updateProgress(pe);
    }
    Logger.getLogger(getClass()).info("    " + pct + "% " + note);
  }

  /* see superclass */
  @Override
  public void addProgressListener(ProgressListener l) {
    listeners.add(l);
  }

  /* see superclass */
  @Override
  public void removeProgressListener(ProgressListener l) {
    listeners.remove(l);
  }

  /* see superclass */
  @Override
  public void cancel() {
    // n/a
  }

  /**
   * Find version.
   *
   * @param inputFile the input file
   * @throws Exception the exception
   */
  public void findVersion(String inputFile) throws Exception {
    BufferedReader br = new BufferedReader(new FileReader(inputFile));
    String line = null;
    while ((line = br.readLine()) != null) {
      if (line.contains("<Title")) {
        int versionIndex = line.indexOf("version=");
        if (line.contains("></Title>"))
          releaseVersion =
              line.substring(versionIndex + 9, line.indexOf("></Title>") - 1);
        else
          releaseVersion = line.substring(versionIndex + 9, versionIndex + 13);
        break;
      }
    }
    br.close();
    // Override terminology version with parameter
    releaseVersionDate = ConfigUtility.DATE_FORMAT3.parse(releaseVersion);
    Logger.getLogger(getClass()).info("terminologyVersion: " + releaseVersion);
  }

  /**
   * Find language.
   *
   * @param inputFile the input file
   * @throws Exception the exception
   */
  public void findLanguage(String inputFile) throws Exception {
    BufferedReader br = new BufferedReader(new FileReader(inputFile));
    String line = null;
    while ((line = br.readLine()) != null) {
      // <Meta name="lang" value="en"/>
      if (line.contains("<Meta") && line.contains("lang")) {
        int versionIndex = line.indexOf("value=");
        terminologyLanguage =
            line.substring(versionIndex + 7, line.indexOf("/>") - 1);
        break;
      }
    }
    br.close();
    Logger.getLogger(getClass()).info(
        "terminologyLanguage: " + terminologyLanguage);
  }

  /**
   * Load metadata.
   *
   * @param ontology the ontology
   * @throws Exception the exception
   */
  private void loadMetadata(OWLOntology ontology) throws Exception {

    // relationship types - CHD, PAR, and RO
    String[] relTypes = new String[] {
        "RO", "CHD", "PAR"
    };
    RelationshipType chd = null;
    RelationshipType par = null;
    RelationshipType ro = null;
    for (String rel : relTypes) {
      final RelationshipType type = new RelationshipTypeJpa();
      type.setTerminology(terminology);
      type.setVersion(version);
      type.setLastModified(releaseVersionDate);
      type.setLastModifiedBy(loader);
      type.setPublishable(true);
      type.setPublished(true);
      type.setAbbreviation(rel);
      if (rel.equals("CHD")) {
        chd = type;
        type.setExpandedForm("Child of");
      } else if (rel.equals("PAR")) {
        par = type;
        type.setExpandedForm("Parent of");
      } else if (rel.equals("RO")) {
        ro = type;
        type.setExpandedForm("Other");
      } else {
        throw new Exception("Unhandled type");
      }
      addRelationshipType(type);
    }
    chd.setInverse(par);
    par.setInverse(chd);
    ro.setInverse(ro);
    updateRelationshipType(chd);
    updateRelationshipType(par);
    updateRelationshipType(ro);

    // Term types
    for (String tty : termTypes) {

      final TermType termType = new TermTypeJpa();
      termType.setAbbreviation(tty);
      termType.setCodeVariantType(CodeVariantType.UNDEFINED);
      termType.setExpandedForm(tty);
      termType.setHierarchicalType(false);
      termType.setLastModified(releaseVersionDate);
      termType.setLastModifiedBy(loader);
      termType.setNameVariantType(NameVariantType.UNDEFINED);
      termType.setObsolete(false);
      termType.setPublishable(true);
      termType.setPublished(true);
      termType.setStyle(TermTypeStyle.SEMANTIC);
      termType.setSuppressible(false);
      termType.setTerminology(terminology);
      termType.setTimestamp(releaseVersionDate);
      termType.setUsageType(UsageType.UNDEFINED);
      termType.setVersion(version);
      addTermType(termType);
    }

    // Build precedence list
    final PrecedenceList list = new PrecedenceListJpa();
    list.setDefaultList(true);

    final List<KeyValuePair> lkvp = new ArrayList<>();
    // Start with rdfs:label
    KeyValuePair pr = new KeyValuePair();
    pr.setKey(terminology);
    pr.setValue(label);
    lkvp.add(pr);
    // then comment
    pr = new KeyValuePair();
    pr.setKey(terminology);
    pr.setValue(comment);
    lkvp.add(pr);
    // next do anything else starting with "preferred"
    for (String tty : termTypes) {
      if (!tty.equals(label) && !tty.equals(comment)) {
        final KeyValuePair pair = new KeyValuePair();
        pair.setKey(terminology);
        pair.setValue(tty);
        lkvp.add(pair);
      }
    }

    final KeyValuePairList kvpl = new KeyValuePairList();
    kvpl.setKeyValuePairList(lkvp);
    list.setPrecedence(kvpl);
    list.setTimestamp(releaseVersionDate);
    list.setLastModified(releaseVersionDate);
    list.setLastModifiedBy(loader);
    list.setName("DEFAULT");
    list.setTerminology(terminology);
    list.setVersion(version);
    addPrecedenceList(list);

    // Root Terminology
    RootTerminology root = new RootTerminologyJpa();
    root.setFamily(terminology);
    root.setHierarchicalName(terminology);
    // Unable to determine overall "language" from OWL (unless maybe in headers)
    root.setLanguage(null);
    root.setTimestamp(releaseVersionDate);
    root.setLastModified(releaseVersionDate);
    root.setLastModifiedBy(loader);
    root.setPolyhierarchy(true);
    root.setPreferredName(terminology);
    root.setRestrictionLevel(-1);
    root.setTerminology(terminology);
    addRootTerminology(root);

    // Terminology
    Terminology term = new TerminologyJpa();
    term.setTerminology(terminology);
    term.setVersion(version);
    term.setTimestamp(releaseVersionDate);
    term.setLastModified(releaseVersionDate);
    term.setLastModifiedBy(loader);
    term.setAssertsRelDirection(true);
    term.setCurrent(true);
    term.setDescriptionLogicTerminology(true);
    term.setOrganizingClassType(IdType.CONCEPT);
    term.setPreferredName(root.getPreferredName());
    term.setRootTerminology(root);
    addTerminology(term);

    String[] labels = new String[] {
        "Tree_Sort_Field", "Atoms_Label", "Attributes_Label"
    };
    String[] labelValues = new String[] {
        "nodeTerminologyName", "Labels", "Properties"
    };
    int i = 0;
    for (String label : labels) {
      GeneralMetadataEntry entry = new GeneralMetadataEntryJpa();
      entry.setTerminology(terminology);
      entry.setVersion(version);
      entry.setLastModified(releaseVersionDate);
      entry.setLastModifiedBy(loader);
      entry.setPublishable(true);
      entry.setPublished(true);
      entry.setAbbreviation(label);
      entry.setExpandedForm(labelValues[i++]);
      entry.setKey("label_metadata");
      entry.setType("label_values");
      addGeneralMetadataEntry(entry);
    }
  }

  /**
   * Returns the concept.
   *
   * @param owlClass the owl class
   * @param ontology the ontology
   * @return the concept
   */
  Concept getConcept(OWLClass owlClass, OWLOntology ontology) {
    final Concept concept = new ConceptJpa();
    concept.setTerminologyId(getTerminologyId(owlClass.getIRI()));
    concept.setTimestamp(currentDate);
    concept.setObsolete(false);
    concept.setSuppressible(false);
    // TODO:
    concept.setFullyDefined(false);
    concept.setTerminology(terminology);
    concept.setVersion(version);
    concept.setName(initPrefName);
    concept.setLastModified(currentDate);
    concept.setLastModifiedBy(loader);
    concept.setPublished(true);
    concept.setPublishable(true);
    concept.setUsesRelationshipUnion(true);
    concept.setWorkflowStatus(published);
    return concept;
  }

  /**
   * Helper method to extract annotation properties attached to a class.
   *
   * @param owlClass the owl class
   * @param ontology the ontology
   * @return the annotation types
   * @throws Exception
   */
  Set<Atom> getAtoms(OWLClass owlClass, OWLOntology ontology) throws Exception {
    Set<Atom> atoms = new HashSet<>();
    for (OWLAnnotationAssertionAxiom axiom : ontology
        .getAnnotationAssertionAxioms(owlClass.getIRI())) {
      OWLAnnotation annotation = axiom.getAnnotation();
      final Atom atom = new AtomJpa();
      atom.setTerminologyId("");
      atom.setTimestamp(currentDate);
      atom.setLastModified(currentDate);
      atom.setLastModifiedBy(loader);
      atom.setObsolete(false);
      atom.setSuppressible(false);
      // everything after the #
      atom.setConceptId(getTerminologyId(owlClass.getIRI()));
      atom.setDescriptorId("");
      atom.setCodeId("");
      atom.setLexicalClassId("");
      atom.setStringClassId("");
      // this is based on xml-lang attribute on the annotation
      atom.setLanguage(getLanguage(annotation));
      languages.add(atom.getLanguage());
      atom.setTermType(getTermType(annotation));
      generalEntryValues.add(atom.getTermType());
      termTypes.add(atom.getTermType());
      atom.setName(getName(annotation));
      atom.setTerminology(terminology);
      atom.setVersion(version);
      atom.setPublished(true);
      atom.setPublishable(true);
      atom.setWorkflowStatus(published);
      atoms.add(atom);
    }
    return atoms;
  }

  /**
   * Returns the language.
   *
   * @param annotation the annotation
   * @return the language
   * @throws Exception
   */
  @SuppressWarnings("static-method")
  private String getLanguage(OWLAnnotation annotation) throws Exception {
    if (annotation.getValue() instanceof OWLLiteral) {
      return ((OWLLiteral) annotation.getValue()).getLang();
    } else {
      throw new Exception("Unexpected annotation that is not OWLLiteral");
    }
  }

  /**
   * Returns the name.
   *
   * @param annotation the annotation
   * @return the name
   * @throws Exception
   */
  @SuppressWarnings("static-method")
  private String getName(OWLAnnotation annotation) throws Exception {
    if (annotation.getValue() instanceof OWLLiteral) {
      return ((OWLLiteral) annotation.getValue()).getLiteral();
    } else {
      throw new Exception("Unexpected annotation that is not OWLLiteral");
    }
  }

  /**
   * Returns the terminology id.
   *
   * @param iri the iri
   * @return the terminology id
   */
  @SuppressWarnings("static-method")
  private String getTerminologyId(IRI iri) {
    return iri.toString().substring(iri.toString().indexOf("#") + 1);
  }

  /**
   * Returns the term type.
   *
   * @param annotation the annotation
   * @return the term type
   */
  private String getTermType(OWLAnnotation annotation) {
    if (annotation.getProperty().isLabel()) {
      return label;
    }
    if (annotation.getProperty().isComment()) {
      return comment;
    } else
      return annotation.getProperty().toString();

  }

  /**
   * Commit clear begin transaction.
   *
   * @throws Exception the exception
   */
  private void commitClearBegin() throws Exception {
    commit();
    clear();
    beginTransaction();
  }

  /**
   * Log and commit.
   * 
   * @param objectCt the object ct
   * @throws Exception the exception
   */
  void logAndCommit(int objectCt) throws Exception {
    // log at regular intervals
    if (objectCt % logCt == 0) {
      Logger.getLogger(getClass()).info("    count = " + objectCt);
    }
    if (objectCt % commitCt == 0) {
      commitClearBegin();
    }
  }

  /**
   * Log ontology.
   *
   * @param ontology the ontology
   * @throws Exception the exception
   */
  private void loadOntology(final OWLOntology ontology) throws Exception {

    logOntology(ontology);

    // Load object properties (e.g. additional relationship types)
    loadObjectProperties(ontology);

    // Load data type properties (e.g. attribute names)

    //
    // Load concepts and atoms
    //
    Logger.getLogger(getClass()).info("  Load Concepts and Atoms");
    OWLOntologyWalker walker =
        new OWLOntologyWalker(Collections.singleton(ontology));
    // Now ask our walker to walk over the ontology
    objectCt = 0;

    OWLOntologyWalkerVisitor visitor = new OWLOntologyWalkerVisitor(walker) {
      @Override
      public void visit(OWLClass owlClass) {
        Logger.getLogger(getClass()).info("  class = " + owlClass);

        try {
          logOwlClass(owlClass, ontology);

          final Concept concept = getConcept(owlClass, ontology);
          final Set<Atom> atoms = getAtoms(owlClass, ontology);
          for (Atom atom : atoms) {
            Logger.getLogger(getClass()).info("  add atom = " + atom);
            addAtom(atom);
            // Use first RDFS label as the preferred name
            if (atom.getTermType().equals(label)) {
              concept.setName(atom.getName());
            }
            concept.addAtom(atom);
          }

          // TODO
          // concept.setName(getComputePreferredNameHandler(terminology).computePreferredName(
          // atoms));
          Logger.getLogger(getClass()).info("  add concept  = " + concept);
          addConcept(concept);

          logAndCommit(++objectCt);

        } catch (Exception e) {
          throw new RuntimeException("Unexpected error.", e);
        }

      }
    };
    walker.walkStructure(visitor);
    commitClearBegin();

    //
    // Relationships/Attributes/Restrictions, etc
    //

  }

  /**
   * Load object properties.
   *
   * @param ontology the ontology
   * @throws Exception the exception
   */
  public void loadObjectProperties(OWLOntology ontology) throws Exception {
    Map<String, String> inverses = new HashMap<>();
    Map<String, String> parChd = new HashMap<>();

    for (OWLObjectProperty prop : ontology.getObjectPropertiesInSignature()) {
      Logger.getLogger(getClass()).info("  object property = " + prop);
      Logger.getLogger(getClass()).info("    IRI = " + prop.getIRI());

      final AdditionalRelationshipType rela =
          new AdditionalRelationshipTypeJpa();

      rela.setAbbreviation(getTerminologyId(prop.getIRI()));
      rela.setAsymmetric(ontology.getAsymmetricObjectPropertyAxioms(prop)
          .size() != 0);
      // domain
      if (ontology.getObjectPropertyDomainAxioms(prop).size() == 1) {
        OWLObjectPropertyDomainAxiom axiom =
            ontology.getObjectPropertyDomainAxioms(prop).iterator().next();
        // Expect a class with an IRI
        if (axiom.getDomain() instanceof OWLClass) {
          rela.setDomainId(getTerminologyId(((OWLClass) axiom.getDomain())
              .getIRI()));
        } else {
          throw new Exception("Unexpected domain type, not class");
        }
      } else if (ontology.getObjectPropertyDomainAxioms(prop).size() > 1) {
        throw new Exception("Unexpectedly more than one domain axiom");
      }

      // range
      if (ontology.getObjectPropertyRangeAxioms(prop).size() == 1) {
        OWLObjectPropertyRangeAxiom axiom =
            ontology.getObjectPropertyRangeAxioms(prop).iterator().next();
        // Expect a class with an IRI
        if (axiom.getRange() instanceof OWLClass) {
          rela.setRangeId(getTerminologyId(((OWLClass) axiom.getRange())
              .getIRI()));
        } else {
          throw new Exception("Unexpected range type, not class");
        }
      } else if (ontology.getObjectPropertyRangeAxioms(prop).size() > 1) {
        throw new Exception("Unexpectedly more than one range axiom");
      }

      rela.setEquivalentClasses(false);

      // TODO: handle equivalent types, like inverse
      // rela.setEquivalentType(...)

      // e.g. "someValuesFrom"
      rela.setExistentialQuantification(true);

      // This applies to relationship group style
      rela.setGroupingType(false);

      rela.setExpandedForm(rela.getAbbreviation());
      rela.setFunctional(ontology.getFunctionalObjectPropertyAxioms(prop)
          .size() != 0);
      rela.setInverseFunctional(ontology
          .getInverseFunctionalObjectPropertyAxioms(prop).size() != 0);

      rela.setIrreflexive(ontology.getIrreflexiveObjectPropertyAxioms(prop)
          .size() != 0);
      rela.setReflexive(ontology.getReflexiveObjectPropertyAxioms(prop).size() != 0);

      rela.setLastModified(releaseVersionDate);
      rela.setLastModifiedBy(loader);
      rela.setPublishable(true);
      rela.setPublished(true);

      Logger.getLogger(getClass()).info(
          "    terminologyId = " + getTerminologyId(prop.getIRI()));

      // Check assumptions
      if (prop.getAnnotationPropertiesInSignature().size() > 0) {
        throw new Exception(
            "Unexpected annotation properties on OWLObjectProperty.");
      }
      if (prop.getClassesInSignature().size() > 0) {
        throw new Exception("Unexpected classes on OWLObjectProperty.");
      }
      if (prop.getDataPropertiesInSignature().size() > 0) {
        throw new Exception("Unexpected data properties on OWLObjectProperty.");
      }
      if (prop.getNestedClassExpressions().size() > 0) {
        throw new Exception(
            "Unexpected nested class expressions on OWLObjectProperty.");
      }

      // inverse
      if (ontology.getInverseObjectPropertyAxioms(prop).size() == 1) {
        OWLInverseObjectPropertiesAxiom axiom =
            ontology.getInverseObjectPropertyAxioms(prop).iterator().next();
        OWLObjectProperty iprop = axiom.getSecondProperty().getNamedProperty();
        inverses.put(rela.getAbbreviation(), getTerminologyId(iprop.getIRI()));

      } else if (ontology.getInverseObjectPropertyAxioms(prop).size() > 1) {
        throw new Exception(
            "Unexpected more than one inverse object property axiom");
      }

      // par/chd
      if (ontology.getObjectSubPropertyAxiomsForSubProperty(prop).size() == 1) {
        OWLSubObjectPropertyOfAxiom axiom =
            ontology.getObjectSubPropertyAxiomsForSubProperty(prop).iterator()
                .next();
        OWLObjectProperty superProp =
            axiom.getSuperProperty().getNamedProperty();
        parChd
            .put(getTerminologyId(superProp.getIRI()), rela.getAbbreviation());
      } else if (ontology.getObjectSubPropertyAxiomsForSubProperty(prop).size() > 1) {
        throw new Exception("Unexpected more than one super property");
      }

      // Add rela
      Logger.getLogger(getClass()).debug("  add rela - " + rela);
      rela.setTerminology(terminology);
      rela.setVersion(version);
      addAdditionalRelationshipType(rela);
      relaMap.put(rela.getAbbreviation(), rela);
    }

    commitClearBegin();

    // Iterate through inverses, set and update
    for (String key : inverses.keySet()) {
      AdditionalRelationshipType type1 = relaMap.get(key);
      AdditionalRelationshipType type2 = relaMap.get(inverses.get(key));
      type1.setInverseType(type2);
      type2.setInverseType(type1);
      updateAdditionalRelationshipType(type1);
      updateAdditionalRelationshipType(type2);
    }
    commitClearBegin();

    // Iterate through parChd properties, set and update
    for (String key : parChd.keySet()) {
      AdditionalRelationshipType par = relaMap.get(key);
      AdditionalRelationshipType chd = relaMap.get(parChd.get(key));
      chd.setSuperType(par);
      updateAdditionalRelationshipType(chd);
    }
    commitClearBegin();

  }

  /**
   * Load data properties.
   *
   * @param ontology the ontology
   * @throws Exception the exception
   */
  public void loadDataProperties(OWLOntology ontology) throws Exception {
    Map<String, String> inverses = new HashMap<>();
    Map<String, String> parChd = new HashMap<>();

    for (OWLDataProperty prop : ontology.getDataPropertiesInSignature()) {
      Logger.getLogger(getClass()).info("  data property = " + prop);
      Logger.getLogger(getClass()).info("    IRI = " + prop.getIRI());

      final AttributeName atn = new AttributeNameJpa();

      atn.setAbbreviation(getTerminologyId(prop.getIRI()));

      // domain
      if (ontology.getDataPropertyDomainAxioms(prop).size() == 1) {
        OWLDataPropertyDomainAxiom axiom =
            ontology.getDataPropertyDomainAxioms(prop).iterator().next();
        // Expect a class with an IRI
        if (axiom.getDomain() instanceof OWLClass) {
          atn.setDomainId(getTerminologyId(((OWLClass) axiom.getDomain())
              .getIRI()));
        } else {
          throw new Exception("Unexpected domain type, not class");
        }
      } else if (ontology.getDataPropertyDomainAxioms(prop).size() > 1) {
        throw new Exception("Unexpectedly more than one domain axiom");
      }

      // TODO
      // atn.setEquivalentName(equivalentName);

      atn.setExistentialQuantification(true);
      atn.setUniversalQuantification(false);

      atn.setExpandedForm(atn.getAbbreviation());

      atn.setFunctional(ontology.getFunctionalDataPropertyAxioms(prop).size() != 0);
      atn.setLastModified(releaseVersionDate);
      atn.setLastModifiedBy(loader);
      atn.setPublishable(true);
      atn.setPublished(true);

      Logger.getLogger(getClass()).info(
          "    terminologyId = " + getTerminologyId(prop.getIRI()));

      // par/chd
      if (ontology.getDataSubPropertyAxiomsForSubProperty(prop).size() == 1) {
        OWLSubDataPropertyOfAxiom axiom =
            ontology.getDataSubPropertyAxiomsForSubProperty(prop).iterator()
                .next();
        OWLDataProperty superProp =
            axiom.getSuperProperty().asOWLDataProperty();
        parChd.put(getTerminologyId(superProp.getIRI()), atn.getAbbreviation());
      } else if (ontology.getDataSubPropertyAxiomsForSubProperty(prop).size() > 1) {
        throw new Exception("Unexpected more than one super property");
      }

      // Add rela
      Logger.getLogger(getClass()).debug("  add rela - " + atn);
      atn.setTerminology(terminology);
      atn.setVersion(version);
      addAttributeName(atn);
      atnMap.put(atn.getAbbreviation(), atn);
    }

    commitClearBegin();

    // PAR/CHD
    for (String key : inverses.keySet()) {
      AttributeName par = atnMap.get(key);
      AttributeName chd = atnMap.get(parChd.get(key));
      chd.setSuperName(par);
      updateAttributeName(chd);
    }
    commitClearBegin();

  }

  /**
   * Log ontology for debugging.
   *
   * @param ontology the ontology
   */
  private void logOntology(OWLOntology ontology) {
    Logger.getLogger(getClass()).debug(
        "  Axiom count = " + ontology.getAxiomCount());
    Logger.getLogger(getClass()).debug(
        "  Logical axiom count = " + ontology.getLogicalAxiomCount());
    // Logger.getLogger(getClass()).debug(
    // "  AboxAxioms (imports excluded) = "
    // + ontology.getABoxAxioms(Imports.EXCLUDED));
    //
    Logger.getLogger(getClass()).debug(
        "  Annotation properties in signature = "
            + ontology.getAnnotationPropertiesInSignature());
    Logger.getLogger(getClass()).debug(
        "  Annotations = " + ontology.getAnnotations());
    Logger.getLogger(getClass()).debug(
        "  Anonymous individuals = " + ontology.getAnonymousIndividuals());
    Logger.getLogger(getClass()).debug("  Axioms = " + ontology.getAxioms());
    Logger.getLogger(getClass()).debug(
        "  Classes in signature = " + ontology.getClassesInSignature());
    Logger.getLogger(getClass()).debug(
        "  Data properties in signature = "
            + ontology.getDataPropertiesInSignature());
    Logger.getLogger(getClass()).debug(
        "  Data types in signature = " + ontology.getDatatypesInSignature());
    Logger.getLogger(getClass()).debug(
        "  General class axioms = " + ontology.getGeneralClassAxioms());
    Logger.getLogger(getClass()).debug(
        "  Individuals in signature = " + ontology.getIndividualsInSignature());
    Logger.getLogger(getClass()).debug(
        "  Logical Axioms = " + ontology.getLogicalAxioms());
    Logger.getLogger(getClass()).debug(
        "  Nested class expressions = " + ontology.getNestedClassExpressions());
    Logger.getLogger(getClass()).debug(
        "  Object properties in signature = "
            + ontology.getObjectPropertiesInSignature());
    Logger.getLogger(getClass()).debug(
        "  Ontology ID = " + ontology.getOntologyID());
    Logger.getLogger(getClass()).debug(
        "  Signature = " + ontology.getSignature());

  }

  /**
   * Log owl class.
   *
   * @param owlClass the owl class
   * @param ontology the ontology
   * @throws Exception
   */
  void logOwlClass(OWLClass owlClass, OWLOntology ontology) throws Exception {
    Logger.getLogger(getClass()).debug(
        "    signature = " + owlClass.getSignature());

    Logger.getLogger(getClass()).debug(
        "    class expression type = " + owlClass.getClassExpressionType());
    Logger.getLogger(getClass()).debug(
        "    entity type = " + owlClass.getEntityType());
    Logger.getLogger(getClass()).debug("    IRI = " + owlClass.getIRI());
    Logger.getLogger(getClass()).debug(
        "    annotation properties in signature = "
            + owlClass.getAnnotationPropertiesInSignature().size());
    for (OWLAnnotationProperty prop : owlClass
        .getAnnotationPropertiesInSignature()) {
      Logger.getLogger(getClass()).debug("      prop = " + prop);
    }

    Logger.getLogger(getClass()).debug(
        "    data type properties in signature = "
            + owlClass.getDataPropertiesInSignature().size());
    for (OWLDataProperty prop : owlClass.getDataPropertiesInSignature()) {
      Logger.getLogger(getClass()).debug("      prop = " + prop);
    }

    Logger.getLogger(getClass()).debug(
        "    object properties in signature = "
            + owlClass.getObjectPropertiesInSignature());
    for (OWLObjectProperty prop : owlClass.getObjectPropertiesInSignature()) {
      Logger.getLogger(getClass()).debug("      prop = " + prop);
    }

    Logger.getLogger(getClass()).debug(
        "    nested class expressions = "
            + owlClass.getNestedClassExpressions().size());
    for (OWLClassExpression expr : owlClass.getNestedClassExpressions()) {
      Logger.getLogger(getClass()).debug("      class expr = " + expr);
    }

    Logger.getLogger(getClass()).debug(
        "    class assertion axioms = "
            + ontology.getClassAssertionAxioms(owlClass).size());
    for (OWLClassAssertionAxiom axiom : ontology
        .getClassAssertionAxioms(owlClass)) {
      Logger.getLogger(getClass()).debug("      axiom = " + axiom);
    }

    Logger.getLogger(getClass()).debug(
        "    disoint classes axioms = "
            + ontology.getDisjointClassesAxioms(owlClass).size());
    for (OWLDisjointClassesAxiom axiom : ontology
        .getDisjointClassesAxioms(owlClass)) {
      Logger.getLogger(getClass()).debug("      axiom = " + axiom);
    }

    Logger.getLogger(getClass()).debug(
        "    disoint union axioms = "
            + ontology.getDisjointUnionAxioms(owlClass).size());
    for (OWLDisjointUnionAxiom axiom : ontology
        .getDisjointUnionAxioms(owlClass)) {
      Logger.getLogger(getClass()).debug("      axiom = " + axiom);
    }

    Logger.getLogger(getClass()).debug(
        "    equivalent classes axioms = "
            + ontology.getEquivalentClassesAxioms(owlClass).size());
    for (OWLEquivalentClassesAxiom axiom : ontology
        .getEquivalentClassesAxioms(owlClass)) {
      Logger.getLogger(getClass()).debug("      axiom = " + axiom);
    }

    Logger.getLogger(getClass()).debug(
        "    sub class axioms for subclass  = "
            + ontology.getSubClassAxiomsForSubClass(owlClass).size());
    for (OWLSubClassOfAxiom axiom : ontology
        .getSubClassAxiomsForSubClass(owlClass)) {
      Logger.getLogger(getClass()).debug("      axiom = " + axiom);
    }

    Logger.getLogger(getClass()).debug(
        "    sub class axioms for superclass  = "
            + ontology.getSubClassAxiomsForSuperClass(owlClass).size());
    for (OWLSubClassOfAxiom axiom : ontology
        .getSubClassAxiomsForSuperClass(owlClass)) {
      Logger.getLogger(getClass()).debug("      axiom = " + axiom);
    }

    Logger.getLogger(getClass()).debug(
        "    annotation assertion axioms = "
            + ontology.getAnnotationAssertionAxioms(owlClass.getIRI()));
    for (OWLAnnotationAssertionAxiom axiom : ontology
        .getAnnotationAssertionAxioms(owlClass.getIRI())) {
      OWLAnnotation annotation = axiom.getAnnotation();
      Logger.getLogger(getClass()).debug("      axiom = " + axiom);
      Logger.getLogger(getClass()).debug(
          "        property = " + annotation.getProperty());
      Logger.getLogger(getClass()).debug(
          "        value = " + annotation.getValue());
      if (annotation.getValue() instanceof OWLLiteral) {
        Logger.getLogger(getClass()).debug(
            "          literal = "
                + ((OWLLiteral) annotation.getValue()).getLiteral());
        Logger.getLogger(getClass()).debug(
            "          lang = "
                + ((OWLLiteral) annotation.getValue()).getLang());
      } else {
        throw new Exception("Unexpected annotation that is not OWLLiteral");
      }
    }

    Logger.getLogger(getClass()).debug(
        "    all axioms = " + ontology.getAxioms(owlClass, Imports.EXCLUDED));
    for (OWLClassAxiom axiom : ontology.getAxioms(owlClass, Imports.EXCLUDED)) {
      Logger.getLogger(getClass()).debug("      axiom = " + axiom);
    }

  }

}
