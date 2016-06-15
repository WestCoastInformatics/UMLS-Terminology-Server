/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.util.SimpleRootClassChecker;

import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.helpers.CancelException;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.jpa.ReleaseInfoJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetMemberJpa;
import com.wci.umls.server.jpa.content.DefinitionJpa;
import com.wci.umls.server.jpa.content.GeneralConceptAxiomJpa;
import com.wci.umls.server.jpa.helpers.PrecedenceListJpa;
import com.wci.umls.server.jpa.meta.AdditionalRelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.AttributeNameJpa;
import com.wci.umls.server.jpa.meta.CitationJpa;
import com.wci.umls.server.jpa.meta.GeneralMetadataEntryJpa;
import com.wci.umls.server.jpa.meta.LanguageJpa;
import com.wci.umls.server.jpa.meta.PropertyChainJpa;
import com.wci.umls.server.jpa.meta.RelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.RootTerminologyJpa;
import com.wci.umls.server.jpa.meta.TermTypeJpa;
import com.wci.umls.server.jpa.meta.TerminologyJpa;
import com.wci.umls.server.jpa.services.helper.OwlUtility;
import com.wci.umls.server.jpa.services.helper.TerminologyUtility;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Component;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.ConceptSubset;
import com.wci.umls.server.model.content.ConceptSubsetMember;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.GeneralConceptAxiom;
import com.wci.umls.server.model.meta.Abbreviation;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.CodeVariantType;
import com.wci.umls.server.model.meta.GeneralMetadataEntry;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.Language;
import com.wci.umls.server.model.meta.NameVariantType;
import com.wci.umls.server.model.meta.PropertyChain;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.model.meta.TermTypeStyle;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.model.meta.UsageType;
import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * Implementation of an algorithm to import Owl data.
 */
public class OwlLoaderAlgorithm extends AbstractTerminologyLoaderAlgorithm {

  /** The logging object ct threshold. */
  private final static int logCt = 2000;

  /** The commit count. */
  private final static int commitCt = 2000;

  /** The terminology. */
  private String terminology;

  /** The version. */
  private String version;

  /** release version. */
  private String releaseVersion;

  /** The release version date. */
  private Date releaseVersionDate;

  /** counter for objects created, reset in each load section. */
  int objectCt;

  /** The input file. */
  private String inputFile;

  /** The additional relationship types. */
  private Map<String, AdditionalRelationshipType> relaMap = new HashMap<>();

  /** The atn map. */
  private Map<String, AttributeName> atnMap = new HashMap<>();

  /** The id map. */
  private Map<String, Long> idMap = new HashMap<>();

  /** The disjoint map. */
  private Map<String, Set<String>> disjointMap = new HashMap<>();

  /** The anonymous expr map. */
  private Map<String, OWLClassExpression> exprMap = new HashMap<>();

  /** The term types. */
  private Set<String> termTypes = new HashSet<>();

  /** The languages. */
  private Set<String> languages = new HashSet<>();

  /** The concept attribute values. */
  private Set<String> generalEntryValues = new HashSet<>();

  /** The root class checker. */
  private SimpleRootClassChecker rootClassChecker = null;

  /** The top concept. */
  private Concept topConcept = null;

  /** The load as inferred. */
  private boolean loadInferred = false;

  /** The loader. */
  private final String label = "label";

  /** The comment. */
  private final String comment = "comment";

  /** The loader. */
  private final String loader = "loader";

  /** The current date. */
  private final Date currentDate = new Date();

  /** The el2 profile. */
  private final String el2Profile =
      "http://www.w3.org/TR/owl2-profiles/#OWL_2_EL";

  /** The dl2 profile. */
  private final String dl2Profile =
      "http://www.w3.org/TR/owl2-profiles/#OWL_2_DL";

  /** The tree pos algorithm. */
  final TreePositionAlgorithm treePosAlgorithm = new TreePositionAlgorithm();

  /** The trans closure algorithm. */
  final TransitiveClosureAlgorithm transClosureAlgorithm =
      new TransitiveClosureAlgorithm();

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
  @Override
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  /**
   * Sets the version.
   *
   * @param version the version
   */
  @Override
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

  @Override
  public String getFileVersion() throws Exception {
    final FileInputStream in = new FileInputStream(new File(inputFile));
    final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    final OWLOntology directOntology =
        manager.loadOntologyFromOntologyDocument(in);
    // Determine version
    return getReleaseVersion(directOntology);
  }

  /* see superclass */
  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  @Override
  public void compute() throws Exception {
    logInfo("Starting loading Owl terminology");
    logInfo("  inputFile = inputFile");
    logInfo("  terminology = " + terminology);
    logInfo("  version = " + version);

    long startTimeOrig = System.nanoTime();

    if (!new File(inputFile).exists()) {
      throw new Exception("Specified input file does not exist");
    }

    setAssignIdentifiersFlag(false);
    setLastModifiedFlag(false);
    setTransactionPerOperation(false);
    beginTransaction();

    //
    // Load ontology into memory
    final FileInputStream in = new FileInputStream(new File(inputFile));
    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    OWLOntology directOntology = manager.loadOntologyFromOntologyDocument(in);

    //
    // Check compliance
    //
    logInfo("Testing compliance ");
    logInfo("  profile = " + getConfigurableValue(terminology, "profile"));
    if ("EL".equals(getConfigurableValue(terminology, "profile"))) {
      OwlUtility.checkOWL2ELProfile(directOntology);
    } else if ("DL".equals(getConfigurableValue(terminology, "profile"))) {
      OwlUtility.checkOWL2DLProfile(directOntology);
    } else {
      // no profile checking - other assumptions will be tested
    }

    //
    // Determine version
    //
    releaseVersion = getReleaseVersion(directOntology);
    if (releaseVersion != null) {
      try {
        releaseVersionDate = ConfigUtility.DATE_FORMAT.parse(releaseVersion);
      } catch (Exception e) {
        releaseVersionDate = new Date();
      }
    } else {
      releaseVersion = version;
      releaseVersionDate = currentDate;
    }
    logInfo("  release version = " + releaseVersion);

    //
    // Set "load as inferred" flag
    //
    loadInferred =
        "true".equals(getConfigurableValue(terminology, "loadInferred"));

    //
    // Add the root concept, if configured to do so
    //
    if ("true".equals(getConfigurableValue(terminology, "top"))) {
      loadTopConcept(directOntology);
    }

    //
    // Initialize root class checker
    //
    rootClassChecker =
        new SimpleRootClassChecker(directOntology.getImportsClosure());

    //
    // Load ontology import closure
    //
    for (final OWLOntology ontology : directOntology.getImportsClosure()) {
      logInfo("Processing ontology - " + ontology);
      loadOntology(ontology);
    }

    //
    // Handle metadata (after all ontology processing is done)
    //
    loadMetadata(directOntology);

    //
    // Handle reasoner and inferences
    //
    if ("true".equals(getConfigurableValue(terminology, "computeInferred"))) {
      for (final OWLOntology ontology : directOntology.getImportsClosure()) {
        logInfo("Processing inferred ontology - " + ontology);
        loadInferred(ontology);
      }
    }

    //
    // Create ReleaseInfo for this release if it does not already exist
    //
    loadReleaseInfo();

    logInfo("      elapsed time = " + getTotalElapsedTimeStr(startTimeOrig));
    logInfo("Done ...");

    commit();

  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // do nothing
  }

  /* see superclass */
  @Override
  public void computeTreePositions() throws Exception {

    try {
      Logger.getLogger(getClass()).info("Computing tree positions");
      treePosAlgorithm.setCycleTolerant(false);
      treePosAlgorithm.setIdType(IdType.CONCEPT);
      // some terminologies may have cycles, allow these for now.
      treePosAlgorithm.setCycleTolerant(true);
      treePosAlgorithm.setComputeSemanticType(true);
      treePosAlgorithm.setTerminology(terminology);
      treePosAlgorithm.setVersion(version);
      treePosAlgorithm.reset();
      treePosAlgorithm.compute();
      treePosAlgorithm.close();
    } catch (CancelException e) {
      Logger.getLogger(getClass()).info("Cancel request detected");
      throw new CancelException("Tree position computation cancelled");
    }

  }

  /* see superclass */
  @Override
  public void computeTransitiveClosures() throws Exception {
    Logger.getLogger(getClass()).info(
        "  Compute transitive closure from  " + terminology + "/" + version);
    try {
      transClosureAlgorithm.setCycleTolerant(false);
      transClosureAlgorithm.setIdType(IdType.CONCEPT);
      transClosureAlgorithm.setTerminology(terminology);
      transClosureAlgorithm.setVersion(version);
      transClosureAlgorithm.reset();
      transClosureAlgorithm.compute();
      transClosureAlgorithm.close();

    } catch (CancelException e) {
      Logger.getLogger(getClass()).info("Cancel request detected");
      throw new CancelException("Tree position computation cancelled");
    }
  }

  /* see superclass */
  @Override
  public void cancel() throws Exception {
    // cancel any currently running local algorithms
    treePosAlgorithm.cancel();
    transClosureAlgorithm.cancel();

    // invoke superclass cancel
    super.cancel();
  }

  /**
   * Load metadata.
   *
   * @param ontology the ontology
   * @throws Exception the exception
   */
  private void loadMetadata(OWLOntology ontology) throws Exception {
    logInfo("Load metadata");
    // relationship types - CHD, PAR, and RO
    String[] relTypes = new String[] {
        "other", "subClassOf", "superClassOf", "unionOf", "hasUnion"
    };
    RelationshipType chd = null;
    RelationshipType par = null;
    RelationshipType ro = null;
    RelationshipType unionOf = null;
    RelationshipType hasUnion = null;
    for (final String rel : relTypes) {
      final RelationshipType type = new RelationshipTypeJpa();
      setCommonFields(type);
      type.setAbbreviation(rel);
      if (rel.equals("subClassOf")) {
        chd = type;
        type.setExpandedForm("Sub class of");
      } else if (rel.equals("superClassOf")) {
        par = type;
        type.setExpandedForm("Super class of");
      } else if (rel.equals("unionOf")) {
        unionOf = type;
        type.setExpandedForm("Union of");
      } else if (rel.equals("hasUnion")) {
        hasUnion = type;
        type.setExpandedForm("Has union");
      } else if (rel.equals("other")) {
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
    unionOf.setInverse(hasUnion);
    hasUnion.setInverse(unionOf);
    updateRelationshipType(chd);
    updateRelationshipType(par);
    updateRelationshipType(ro);

    // Term types
    for (final String tty : termTypes) {

      final TermType termType = new TermTypeJpa();
      setCommonFields(termType);
      termType.setAbbreviation(tty);
      termType.setCodeVariantType(CodeVariantType.UNDEFINED);
      termType.setExpandedForm(tty);
      if (idMap.containsKey(tty)) {
        termType.setExpandedForm(getConcept(idMap.get(tty)).getName());
      }
      termType.setHierarchicalType(false);
      termType.setNameVariantType(NameVariantType.UNDEFINED);
      termType.setStyle(TermTypeStyle.SEMANTIC);
      termType.setUsageType(UsageType.UNDEFINED);
      addTermType(termType);
    }

    // Build precedence list
    final PrecedenceList list = new PrecedenceListJpa();
    list.setDefaultList(true);

    final List<KeyValuePair> lkvp = new ArrayList<>();
    // Start with "preferred"
    for (final String tty : termTypes) {
      if (isPreferredType(tty)) {
        final KeyValuePair pair = new KeyValuePair();
        pair.setKey(terminology);
        pair.setValue(tty);
        lkvp.add(pair);
      }
    }
    // Next, do label (unless already done)
    if (!isPreferredType(label)) {
      KeyValuePair pr = new KeyValuePair();
      pr.setKey(terminology);
      pr.setValue(label);
      lkvp.add(pr);
    }
    // then comment
    KeyValuePair pr = new KeyValuePair();
    pr.setKey(terminology);
    pr.setValue(comment);
    lkvp.add(pr);
    // next do anything else that is not the preferred type or label or comment
    for (final String tty : termTypes) {
      if (!isPreferredType(tty) && !tty.equals(label) && !tty.equals(comment)) {
        final KeyValuePair pair = new KeyValuePair();
        pair.setKey(terminology);
        pair.setValue(tty);
        lkvp.add(pair);
      }
    }

    final KeyValuePairList kvpl = new KeyValuePairList();
    kvpl.setKeyValuePairs(lkvp);
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
    root.setHierarchicalName(topConcept.getName());
    // Unable to determine overall "language" from OWL (unless maybe in headers)
    root.setLanguage(null);
    root.setTimestamp(releaseVersionDate);
    root.setLastModified(releaseVersionDate);
    root.setLastModifiedBy(loader);
    root.setPolyhierarchy(true);
    root.setPreferredName(topConcept.getName());
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
    if ("EL".equals(getConfigurableValue(terminology, "profile"))) {
      term.setDescriptionLogicProfile(el2Profile);
    } else if ("DL".equals(getConfigurableValue(terminology, "profile"))) {
      term.setDescriptionLogicProfile(dl2Profile);
    }
    term.setOrganizingClassType(IdType.CONCEPT);
    term.setPreferredName(topConcept.getName());
    term.setRootTerminology(root);
    // package comment as a citation
    String comment = getComment(ontology);
    if (comment != null) {
      term.setCitation(new CitationJpa(comment));
    }
    addTerminology(term);

    String[] labels = new String[] {
        "Tree_Sort_Field", "Atoms_Label", "Attributes_Label"
    };
    String[] labelValues = new String[] {
        "nodeName", "Labels", "Properties"
    };
    int i = 0;
    for (final String label : labels) {
      final GeneralMetadataEntry entry = new GeneralMetadataEntryJpa();
      setCommonFields(entry);
      entry.setAbbreviation(label);
      entry.setExpandedForm(labelValues[i++]);
      entry.setKey("label_metadata");
      entry.setType("label_values");
      addGeneralMetadataEntry(entry);
    }

    // Add "en" language
    final Language lat = new LanguageJpa();
    lat.setAbbreviation("en");
    lat.setExpandedForm("English");
    lat.setTimestamp(releaseVersionDate);
    lat.setLastModified(releaseVersionDate);
    lat.setLastModifiedBy(loader);
    lat.setTerminology(terminology);
    lat.setVersion(version);
    lat.setPublished(true);
    lat.setPublishable(true);
    lat.setISO3Code("ENG");
    lat.setISOCode("en");
    Logger.getLogger(getClass()).debug("    add language - " + lat);
    addLanguage(lat);

    // Commit
    commitClearBegin();
  }

  /**
   * Returns the terminology preferred name.
   *
   * @param ontology the ontology
   * @return the terminology preferred name
   * @throws Exception the exception
   */
  private String getRootTerminologyPreferredName(OWLOntology ontology)
    throws Exception {
    // Get the rdfs:label property of the ontology itself
    for (final OWLAnnotation annotation : ontology.getAnnotations()) {
      if (annotation.getProperty().isLabel()) {
        return getValue(annotation);
      }
    }
    // otherwise, just use the terminology name
    return terminology;
  }

  /**
   * Load metadata.
   *
   * @param ontology the ontology
   * @throws Exception the exception
   */
  private void loadInferred(OWLOntology ontology) throws Exception {
    logInfo("Load inferred axioms");

    // Create a classifier for checking things
    OwlApiClassifier classifier = new OwlApiClassifier(ontology);
    classifier.setIdMap(idMap);
    classifier.setExprMap(exprMap);
    // perform classification (or precompute inferences)
    classifier.preClassify(terminology, version, null);
    classifier.compute();

    // verify consistent
    if (!classifier.isConsistent()) {
      throw new Exception("Unexpected inconsistent ontology");
    }

    // Unsatisfiable classes
    if (classifier.getUnsatisfiableConcepts().size() > 0) {
      throw new Exception("Unexpected unsatisfiable classes.");
    }

    if (classifier.getEquivalentClasses().size() > 0) {
      // throw new Exception("Unexpected equivalent classes.");
      for (final Set<Concept> concepts : classifier.getEquivalentClasses()) {
        Logger.getLogger(getClass())
            .error("  EQUIVALENCE detected " + concepts);
      }
    }

    // Add all sub/super-class relationships and add them as inferred
    // Also add any restrictions at this point
    Logger.getLogger(getClass())
        .info("  Add inferred subClassOf relationships");
    classifier.addInferredHierarchicalRelationships(this);
    commitClearBegin();

    // For each class, follow superclass path and gather all non "subClassOf"
    // relationships. Verify there are no duplicates on
    // additionalRelationshipType
    // add inferred rels to the same concepts.
    logInfo("  Add inferred restriction relationships");
    classifier.addInferredRelationships(this);
    commitClearBegin();

  }

  /**
   * Load release info.
   *
   * @throws Exception the exception
   */
  private void loadReleaseInfo() throws Exception {
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
      info.setLastModified(releaseVersionDate);
      info.setLastModified(new Date());
      info.setLastModifiedBy(loader);
      addReleaseInfo(info);
    }
    commitClearBegin();

  }

  /**
   * Returns the comment.
   *
   * @param ontology the ontology
   * @return the comment
   * @throws Exception the exception
   */
  private String getComment(OWLOntology ontology) throws Exception {
    for (final OWLAnnotation annotation : ontology.getAnnotations()) {
      if (annotation.getProperty().isComment()) {
        return getValue(annotation);
      }
    }

    return null;
  }

  /**
   * Returns the version.
   *
   * @param ontology the ontology
   * @return the version
   * @throws Exception the exception
   */
  private String getReleaseVersion(OWLOntology ontology) throws Exception {

    String version = null;
    if (ontology.getOntologyID().getVersionIRI() != null) {
      version = ontology.getOntologyID().getVersionIRI().toString();
    } else {
      // check versionInfo
      for (final OWLAnnotation annotation : ontology.getAnnotations()) {
        if (getTerminologyId(annotation.getProperty().getIRI()).equals(
            "versionInfo")) {
          version = getValue(annotation);
        }
      }
    }
    logInfo("  release version = " + version);

    // This is the list of available patterns for extracting a date.
    // Try each one
    String[] patterns =
        new String[] {
            // e.g.
            // http://snomed.info/sct/900000000000207008/version/20150131
            ".*\\/(\\d{8})$",
            // e.g.
            // http://purl.obolibrary.org/obo/go/releases/2015-07-28/go.owl
            ".*\\/(\\d\\d\\d\\d-\\d\\d-\\d\\d)$",
            ".*\\/(\\d\\d\\d\\d-\\d\\d-\\d\\d\\/)$"
        };

    // Iterate through patterns
    for (final String pattern : patterns) {
      final Pattern pattern2 = Pattern.compile(pattern);
      final Matcher matcher = pattern2.matcher(version);
      // Assume if it matches, the pattern has a group 1, extract and
      // prepare it.
      if (matcher.matches()) {
        String parsedVersion = matcher.group(1);
        // Remove dashes
        parsedVersion = parsedVersion.replaceAll("-", "");
        Logger.getLogger(getClass())
            .info("  parsed version = " + parsedVersion);
        return parsedVersion;
      }
    }

    // else return null
    return null;
  }

  /**
   * Returns the relationships.
   *
   * @param concept the concept
   * @param owlClass the owl class
   * @param ontology the ontology
   * @return the relationships
   * @throws Exception the exception
   */
  private Set<ConceptRelationship> getRelationships(Concept concept,
    OWLClass owlClass, OWLOntology ontology) throws Exception {
    Set<ConceptRelationship> rels = new HashSet<>();

    OwlUtility.logOwlClass(owlClass, ontology, 0);

    // Handle top-level "equivalent class"
    if (ontology.getEquivalentClassesAxioms(owlClass).size() > 0) {
      Logger.getLogger(getClass()).debug("  EQUIVALENT class detected");

      final OWLEquivalentClassesAxiom axiom =
          ontology.getEquivalentClassesAxioms(owlClass).iterator().next();
      for (final OWLClassExpression expr : axiom.getClassExpressions()) {

        // Skip this class
        if (expr.equals(owlClass)) {
          continue;
        }

        // Any OWLClass encountered here is simply subClassOf relationship
        if (expr instanceof OWLClass) {
          OwlUtility.logOwlClass((OWLClass) expr, ontology, 1);
          Concept toConcept =
              getConcept(idMap
                  .get(getTerminologyId(((OWLClass) expr).getIRI())));
          rels.add(getSubClassOfRelationship(concept, toConcept));
        }

        // Otherwise it is an embedded class expression from which
        // we will borrow relationships
        else {
          final Concept concept2 =
              getConceptForOwlClassExpression(expr, ontology, 1);
          for (final ConceptRelationship rel : concept2.getRelationships()) {
            rel.setFrom(concept);
            rels.add(rel);
          }

          // ASSUMPTION: there is at least one rel here
          if (rels.size() == 0) {
            throw new Exception(
                "Unexpected absence of relationships from embedded class expression "
                    + expr);
          }
        }

      }

      // Presence of equivalent class signals fully defined
      concept.setFullyDefined(true);

    }

    // Handle top-level SubClassAxioms
    if (ontology.getSubClassAxiomsForSubClass(owlClass).size() > 0) {

      // Iterate through, add super classes
      for (final OWLSubClassOfAxiom axiom : ontology
          .getSubClassAxiomsForSubClass(owlClass)) {

        Logger.getLogger(getClass()).debug("  subClassOfAxiom = " + axiom);

        // Handle axioms that point to an OWLClass
        if (axiom.getSuperClass() instanceof OWLClass) {
          final Concept toConcept =
              getConcept(idMap.get(getTerminologyId(((OWLClass) axiom
                  .getSuperClass()).getIRI())));
          rels.add(getSubClassOfRelationship(concept, toConcept));

        }

        // Handle intersections
        else if (axiom.getSuperClass() instanceof OWLObjectIntersectionOf) {
          Concept concept2 =
              getConceptForOwlClassExpression(axiom.getSuperClass(), ontology,
                  1);
          // Wire relationships to this concept and save
          for (final ConceptRelationship rel : concept2.getRelationships()) {

            rel.setFrom(concept);
            rels.add(rel);
          }

        }

        // Handle intersections
        else if (axiom.getSuperClass() instanceof OWLObjectUnionOf) {
          final Concept concept2 =
              getConceptForOwlClassExpression(axiom.getSuperClass(), ontology,
                  1);
          // Wire relationships to this concept and save
          for (final ConceptRelationship rel : concept2.getRelationships()) {

            rel.setFrom(concept);
            rels.add(rel);
          }

        }

        // Handle someValuesFrom
        else if (axiom.getSuperClass() instanceof OWLObjectSomeValuesFrom) {
          final Concept concept2 =
              getConceptForOwlClassExpression(axiom.getSuperClass(), ontology,
                  1);
          // Wire relationships to this concept and save
          for (final ConceptRelationship rel : concept2.getRelationships()) {
            rel.setFrom(concept);
            rels.add(rel);
          }
        }

        // Otherwise error
        else {
          throw new Exception(
              "Unexpected subClassOfAxiom expression type for super class - "
                  + axiom.getSuperClass());
        }
      }
    }

    // ASSUMPTION: no duplicate relationships
    for (final ConceptRelationship rel : rels) {
      for (final ConceptRelationship rel2 : rels) {
        // Avoid comparing to itself
        if (rel == rel2) {
          continue;
        }
        // look for matching source/type/destination
        if (rel.getFrom().getId().equals(rel2.getFrom().getId())
            && rel.getTo().getId().equals(rel2.getTo().getId())
            && rel.getRelationshipType().equals(rel2.getRelationshipType())) {
          logInfo("  rel = " + rel);
          logInfo("  rel2 = " + rel2);
          logInfo("  rel hashcode = " + rel.hashCode());
          logInfo("  rel2 hashcode = " + rel2.hashCode());
          logInfo("  eq = " + rel.equals(rel2));
          logInfo("  rel identity = " + System.identityHashCode(rel));
          logInfo("  rel2 identity = " + System.identityHashCode(rel2));
          throw new Exception("Unexpected duplicate rels");
        }
      }
    }
    return rels;
  }

  /**
   * Returns the sub class of relationship.
   *
   * @param fromConcept the from concept
   * @param toConcept the to concept
   * @return the sub class of relationship
   * @throws Exception the exception
   */
  private ConceptRelationship getSubClassOfRelationship(Concept fromConcept,
    Concept toConcept) throws Exception {

    // Standard "isa" relationship
    ConceptRelationship rel = new ConceptRelationshipJpa();
    setCommonFields(rel);
    rel.setWorkflowStatus(WorkflowStatus.PUBLISHED);
    // blank terminology id
    rel.setFrom(fromConcept);
    rel.setTo(toConcept);
    // This is an "isa" rel.
    rel.setRelationshipType("subClassOf");
    rel.setHierarchical(true);
    String subClassOfRel = getConfigurableValue(terminology, "subClassOf");
    if (subClassOfRel == null) {
      rel.setAdditionalRelationshipType("");
    } else if (relaMap.containsKey(subClassOfRel)) {
      rel.setAdditionalRelationshipType(subClassOfRel);
    } else {
      throw new Exception(
          "configurable subClassOf rel does not exist as an additionalRelationshipType: "
              + subClassOfRel);
    }
    return rel;
  }

  /**
   * Returns the preferred name.
   *
   * @param iri the iri
   * @param ontology the ontology
   * @return the preferred name
   * @throws Exception the exception
   */
  private String getPreferredName(IRI iri, OWLOntology ontology)
    throws Exception {
    for (final OWLAnnotationAssertionAxiom axiom : ontology
        .getAnnotationAssertionAxioms(iri)) {
      final OWLAnnotation annotation = axiom.getAnnotation();
      if (!isAtomAnnotation(annotation)) {
        continue;
      }
      if (isPreferredType(getName(annotation))) {
        return getValue(annotation);
      }
    }
    return getTerminologyId(iri);
  }

  /**
   * Helper method to extract annotation properties attached to a class.
   *
   * @param owlClass the owl class
   * @param ontology the ontology
   * @return the annotation types
   * @throws Exception the exception
   */
  private Set<Atom> getAtoms(OWLClass owlClass, OWLOntology ontology)
    throws Exception {
    Set<Atom> atoms = new HashSet<>();
    for (final OWLAnnotationAssertionAxiom axiom : ontology
        .getAnnotationAssertionAxioms(owlClass.getIRI())) {
      final OWLAnnotation annotation = axiom.getAnnotation();
      if (!isAtomAnnotation(annotation)) {
        continue;
      }
      final Atom atom = new AtomJpa();
      setCommonFields(atom);
      atom.setWorkflowStatus(WorkflowStatus.PUBLISHED);
      // everything after the #
      atom.setConceptId(getTerminologyId(owlClass.getIRI()));
      atom.setDescriptorId("");
      atom.setCodeId("");
      atom.setLexicalClassId("");
      atom.setStringClassId("");
      // this is based on xml-lang attribute on the annotation
      atom.setLanguage(getLanguage(annotation));
      languages.add(atom.getLanguage());
      atom.setTermType(atnMap.get(getName(annotation)).getAbbreviation());
      generalEntryValues.add(atom.getTermType());
      termTypes.add(atom.getTermType());
      atom.setName(getValue(annotation));
      atom.setWorkflowStatus(WorkflowStatus.PUBLISHED);
      atoms.add(atom);

    }
    return atoms;
  }

  /**
   * Returns the definitions.
   *
   * @param owlClass the owl class
   * @param ontology the ontology
   * @return the definitions
   * @throws Exception the exception
   */
  private Set<Definition> getDefinitions(OWLClass owlClass, OWLOntology ontology)
    throws Exception {
    Set<Definition> defs = new HashSet<>();
    for (final OWLAnnotationAssertionAxiom axiom : ontology
        .getAnnotationAssertionAxioms(owlClass.getIRI())) {
      final OWLAnnotation annotation = axiom.getAnnotation();
      if (!isDefinitionAnnotation(annotation)) {
        continue;
      }
      final Definition def = new DefinitionJpa();
      setCommonFields(def);
      // this is based on xml-lang attribute on the annotation
      def.setValue(getValue(annotation));
      defs.add(def);
    }
    return defs;
  }

  /**
   * Returns the attributes.
   *
   * @param owlClass the owl class
   * @param ontology the ontology
   * @return the attributes
   * @throws Exception the exception
   */
  private Set<Attribute> getAttributes(OWLClass owlClass, OWLOntology ontology)
    throws Exception {
    Set<Attribute> attributes = new HashSet<>();
    for (final OWLAnnotationAssertionAxiom axiom : ontology
        .getAnnotationAssertionAxioms(owlClass.getIRI())) {

      final OWLAnnotation annotation = axiom.getAnnotation();
      if (isAtomAnnotation(annotation)) {
        continue;
      }
      final Attribute attribute = new AttributeJpa();
      setCommonFields(attribute);
      attribute.setName(atnMap.get(getName(annotation)).getAbbreviation());
      attribute.setValue(getValue(annotation));
      generalEntryValues.add(attribute.getName());
      attributes.add(attribute);
    }
    return attributes;
  }

  /**
   * Returns the language.
   *
   * @param annotation the annotation
   * @return the language
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private String getLanguage(OWLAnnotation annotation) throws Exception {
    if (annotation.getValue() instanceof OWLLiteral) {
      return ((OWLLiteral) annotation.getValue()).getLang();
    }
    // ASSUMPTION: annotation is an OWLLiteral
    else {
      // throw new Exception("Unexpected annotation that is not OWLLiteral - " +
      // annotation);
      return "";
    }
  }

  /**
   * Returns the name.
   *
   * @param annotation the annotation
   * @return the name
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private String getValue(OWLAnnotation annotation) throws Exception {
    if (annotation.getValue() instanceof OWLLiteral) {
      return ((OWLLiteral) annotation.getValue()).getLiteral();
    }
    // ASSUMPTION: annotation is an OWLLiteral
    else {
      // throw new Exception("Unexpected annotation that is not OWLLiteral - " +
      // annotation);
      return annotation.getValue().toString();
    }
  }

  /**
   * Load top concept.
   *
   * @param ontology the ontology
   * @throws Exception the exception
   */
  private void loadTopConcept(OWLOntology ontology) throws Exception {
    topConcept = new ConceptJpa();

    setCommonFields(topConcept);
    topConcept.setWorkflowStatus(WorkflowStatus.PUBLISHED);
    topConcept.setTerminologyId("Thing");
    topConcept.setAnonymous(false);
    topConcept.setFullyDefined(false);
    topConcept.setUsesRelationshipIntersection(true);
    topConcept.setName(getRootTerminologyPreferredName(ontology));
    topConcept.setWorkflowStatus(WorkflowStatus.PUBLISHED);
    Atom atom = new AtomJpa();
    setCommonFields(atom);
    atom.setWorkflowStatus(WorkflowStatus.PUBLISHED);
    atom.setName(getRootTerminologyPreferredName(ontology));
    atom.setDescriptorId("");
    atom.setCodeId("");
    atom.setLexicalClassId("");
    atom.setStringClassId("");
    atom.setConceptId("Thing");
    atom.setTerminologyId("");
    atom.setLanguage("");
    atom.setTermType(label);
    atom.setWorkflowStatus(WorkflowStatus.PUBLISHED);
    addAtom(atom);
    topConcept.getAtoms().add(atom);
    addConcept(topConcept);
  }

  /**
   * Log ontology.
   *
   * @param ontology the ontology
   * @throws Exception the exception
   */
  private void loadOntology(final OWLOntology ontology) throws Exception {
    logInfo("Load ontology - " + ontology);

    OwlUtility.logOntology(ontology);

    // Load annotation properties (e.g. attribute names)
    loadAnnotationProperties(ontology);

    // Load object properties (e.g. additional relationship types)
    loadObjectProperties(ontology);

    // Load data properties (e.g. attribute names)
    loadDataProperties(ontology);

    // Load general class axioms
    loadGeneralClassAxioms(ontology);

    //
    // Iterate through all owl classes
    // Load concepts, atoms, definitions, and attributes
    //
    logInfo("  Load Concepts, atoms, and attributes");
    for (final OWLClass owlClass : ontology.getClassesInSignature()) {

      // If we've already encountered this class, just skip it
      if (idMap.containsKey(getTerminologyId(owlClass.getIRI()))) {
        continue;
      }

      // Skip if the owl class
      if (isObsolete(owlClass, ontology)) {
        continue;
      }

      // Get the concept object
      final Concept concept =
          getConceptForOwlClassExpression(owlClass, ontology, 0);

      // Persist the concept object
      for (final Atom atom : concept.getAtoms()) {
        Logger.getLogger(getClass()).debug("  add atom = " + atom);
        addAtom(atom);
      }
      for (final Definition def : concept.getDefinitions()) {
        Logger.getLogger(getClass()).debug("  add definition = " + def);
        addDefinition(def, concept);
      }
      for (final Attribute attribute : concept.getAttributes()) {
        Logger.getLogger(getClass()).debug("  add attribute = " + attribute);
        addAttribute(attribute, concept);
      }
      Logger.getLogger(getClass()).debug("  add concept = " + concept);
      addConcept(concept);
      exprMap.put(concept.getTerminologyId(), owlClass);
      idMap.put(concept.getTerminologyId(), concept.getId());

      // Check whether to add a link to "top concept"
      if (rootClassChecker.isRootClass(owlClass)) {
        if ("true".equals(getConfigurableValue(terminology, "top"))) {
          ConceptRelationship rel =
              getSubClassOfRelationship(concept, topConcept);
          Logger.getLogger(getClass()).debug("  add top relationship = " + rel);
          addRelationship(rel);
          concept.getRelationships().add(rel);
        } else {
          topConcept = getConceptForOwlClass(owlClass, ontology, 0);
        }
      }
      logAndCommit(++objectCt, logCt, commitCt);

    }
    commitClearBegin();

    //
    // Iterate through classes again and connect relationships
    //
    logInfo("  Load relationships");
    objectCt = 0;
    Set<String> visited = new HashSet<>();
    for (final OWLClass owlClass : ontology.getClassesInSignature()) {
      final String terminologyId = getTerminologyId(owlClass.getIRI());
      if (visited.contains(terminologyId)) {
        continue;
      }
      visited.add(terminologyId);

      // Skip if the owl class
      if (isObsolete(owlClass, ontology)) {
        continue;
      }

      final Concept concept = getConcept(idMap.get(terminologyId));
      // ASSUMPTION: concept exists
      if (concept == null) {
        throw new Exception("Unexpected missing concept for " + terminologyId);
      }
      for (final ConceptRelationship rel : getRelationships(concept, owlClass,
          ontology)) {
        // ASSUMPTION: embedded anonymous concepts have been added
        Logger.getLogger(getClass()).debug("  add relationship = " + rel);
        addRelationship(rel);
        concept.getRelationships().add(rel);
      }

      // Update the concept a
      Logger.getLogger(getClass()).debug("  update concept = " + concept);
      updateConcept(concept);
      logAndCommit(++objectCt, logCt, commitCt);

    }

    loadDisjointSets();
    commitClearBegin();

  }

  /**
   * Indicates whether or not obsolete is the case.
   *
   * @param owlClass the owl class
   * @param ontology the ontology
   * @return <code>true</code> if so, <code>false</code> otherwise
   * @throws Exception the exception
   */
  private boolean isObsolete(OWLClass owlClass, OWLOntology ontology)
    throws Exception {
    String obsoletePattern =
        getConfigurableValue(terminology, "obsoletePattern");
    String obsoleteAnnotation =
        getConfigurableValue(terminology, "obsoleteAnnotation");
    if (obsoletePattern == null || obsoleteAnnotation == null) {
      return false;
    }

    for (final OWLAnnotationAssertionAxiom axiom : ontology
        .getAnnotationAssertionAxioms(owlClass.getIRI())) {
      OWLAnnotation annotation = axiom.getAnnotation();
      if (!isAtomAnnotation(annotation)) {
        continue;
      }
      // Look for a label matching the pattern
      if (getName(annotation).equals(label)
          && getValue(annotation).matches(obsoletePattern)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Load object properties.
   *
   * @param ontology the ontology
   * @throws Exception the exception
   */
  public void loadObjectProperties(OWLOntology ontology) throws Exception {
    logInfo("  Loading object properties");
    Map<String, String> inverses = new HashMap<>();
    Map<String, String> equiv = new HashMap<>();
    // Assume at most one "super" property
    Map<String, String> chdPar = new HashMap<>();
    // Add object properties
    for (final OWLObjectProperty prop : ontology
        .getObjectPropertiesInSignature()) {
      OwlUtility.logObjectProperty(prop, ontology);

      final AdditionalRelationshipType rela =
          new AdditionalRelationshipTypeJpa();
      setCommonFields(rela);

      rela.setAbbreviation(getPreferredName(prop.getIRI(), ontology));
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
        }
        // ASSUMPTION: object property domain is an OWLClass
        else {
          throw new Exception("Unexpected domain type, not class");
        }
      }
      // ASSUMPTION: object property has only one domain axiom
      else if (ontology.getObjectPropertyDomainAxioms(prop).size() > 1) {
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
        }
        // ASSUMPTION: object property rangeis an OWLClass
        else {
          throw new Exception("Unexpected range type, not class");
        }
      }
      // ASSUMPTION: object property has only one range axiom
      else if (ontology.getObjectPropertyRangeAxioms(prop).size() > 1) {
        throw new Exception("Unexpectedly more than one range axiom");
      }

      rela.setEquivalentClasses(false);

      // e.g. "someValuesFrom"
      rela.setExistentialQuantification(true);

      // This applies to relationship group style
      rela.setGroupingType(false);

      rela.setExpandedForm(prop.getIRI().toString());

      rela.setReflexive(ontology.getReflexiveObjectPropertyAxioms(prop).size() != 0);

      Logger.getLogger(getClass()).debug(
          "    terminologyId = " + getTerminologyId(prop.getIRI()));

      // ASSUMPTION: object property has no annotations
      // Only works in OWLAPI 4
      // if (prop.getAnnotationPropertiesInSignature().size() > 0) {
      // throw new Exception(
      // "Unexpected annotation properties on OWLObjectProperty.");
      // }
      // ASSUMPTION: object property has no classes in signature
      if (prop.getClassesInSignature().size() > 0) {
        throw new Exception("Unexpected classes on OWLObjectProperty.");
      }
      // ASSUMPTION: object property has no data properties
      if (prop.getDataPropertiesInSignature().size() > 0) {
        throw new Exception("Unexpected data properties on OWLObjectProperty.");
      }
      // ASSUMPTION: object property has no nested class expressions
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

      }
      // equivalent
      if (ontology.getEquivalentObjectPropertiesAxioms(prop).size() == 1) {
        OWLEquivalentObjectPropertiesAxiom axiom =
            ontology.getEquivalentObjectPropertiesAxioms(prop).iterator()
                .next();
        for (final OWLObjectPropertyExpression prop2 : axiom.getProperties()) {
          final String abbr =
              getTerminologyId(prop2.getNamedProperty().getIRI());
          // Skip this property
          if (abbr.equals(rela.getAbbreviation())) {
            continue;
          }
          // ASSUMPTION - only one equivalent property
          // If need be, support equivalences as a set
          if (equiv.containsKey(rela.getAbbreviation())) {
            throw new Exception(
                "Unexpected multiple equivalent properties for " + rela);
          }
          equiv.put(rela.getAbbreviation(), abbr);
        }
      }
      // ASSUMPTION: object property has at most one inverse
      else if (ontology.getInverseObjectPropertyAxioms(prop).size() > 1) {
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
        chdPar
            .put(rela.getAbbreviation(), getTerminologyId(superProp.getIRI()));
      }
      // ASSUMPTION: object property has at most one super property
      else if (ontology.getObjectSubPropertyAxiomsForSubProperty(prop).size() > 1) {
        throw new Exception("Unexpected more than one super property");
      }

      // Add rela
      Logger.getLogger(getClass()).debug("  add rela - " + rela);
      addAdditionalRelationshipType(rela);
      relaMap.put(rela.getAbbreviation(), rela);
      relaMap.put(getTerminologyId(prop.getIRI()), rela);
    }

    commitClearBegin();

    // Iterate through inverses, set and update
    for (final String key : inverses.keySet()) {
      final AdditionalRelationshipType type1 = relaMap.get(key);
      final AdditionalRelationshipType type2 = relaMap.get(inverses.get(key));
      type1.setInverse(type2);
      type2.setInverse(type1);
      updateAdditionalRelationshipType(type1);
      updateAdditionalRelationshipType(type2);
    }
    commitClearBegin();

    // Iterate through additional rel types and create fake inverses
    // for those without inverses
    for (final AdditionalRelationshipType type : relaMap.values()) {
      if (type.getInverse() == null) {
        final AdditionalRelationshipType inv =
            new AdditionalRelationshipTypeJpa(type);
        inv.setId(null);
        inv.setAbbreviation("Inverse " + type.getAbbreviation());
        inv.setAbbreviation("Inverse " + type.getExpandedForm());
        inv.setInverse(type);
        addAdditionalRelationshipType(inv);
        type.setInverse(inv);
        updateAdditionalRelationshipType(type);
      }
    }
    commitClearBegin();

    // Iterate through parChd properties, set and update
    for (final String key : chdPar.keySet()) {
      final AdditionalRelationshipType par = relaMap.get(chdPar.get(key));
      final AdditionalRelationshipType chd = relaMap.get(key);
      chd.setSuperType(par);
      updateAdditionalRelationshipType(chd);
    }

    // Iterate through equiv properties, set and update
    for (final String key : equiv.keySet()) {
      final AdditionalRelationshipType rela1 = relaMap.get(key);
      final AdditionalRelationshipType rela2 = relaMap.get(equiv.get(key));
      rela1.setEquivalentType(rela2);
      rela2.setEquivalentType(rela1);
      updateAdditionalRelationshipType(rela1);
      updateAdditionalRelationshipType(rela2);
    }

    // Add property chains
    // Only way I could find to access property chains
    for (final OWLSubPropertyChainOfAxiom prop : ontology.getAxioms(
        AxiomType.SUB_PROPERTY_CHAIN_OF, false)) {
      OwlUtility.logPropertyChain(prop, ontology);

      final String superProp =
          getTerminologyId(prop.getSuperProperty().getNamedProperty().getIRI());
      final List<String> links = new ArrayList<>();
      final List<AdditionalRelationshipType> types = new ArrayList<>();
      for (final OWLObjectPropertyExpression link : prop.getPropertyChain()) {
        String name = getTerminologyId(link.getNamedProperty().getIRI());
        links.add(name);
        types.add(relaMap.get(name));
      }

      PropertyChain chain = new PropertyChainJpa();
      setCommonFields(chain);
      StringBuilder abbreviation = new StringBuilder();
      for (final String link : links) {
        abbreviation.append(link).append(" o ");
      }
      chain.setAbbreviation(abbreviation.toString().replaceAll(" o $", " => ")
          + superProp);
      chain.setChain(types);
      chain.setExpandedForm(chain.getAbbreviation());
      chain.setResult(relaMap.get(superProp));

      Logger.getLogger(getClass()).debug("  add property chain - " + chain);
      addPropertyChain(chain);

    }
    commitClearBegin();
  }

  /**
   * Load annotation properties.
   *
   * @param ontology the ontology
   * @throws Exception the exception
   */
  public void loadAnnotationProperties(OWLOntology ontology) throws Exception {
    logInfo("  Loading annotation properties");
    for (final OWLAnnotationProperty prop : ontology
        .getAnnotationPropertiesInSignature()) {

      OwlUtility.logAnnotationProperty(prop, ontology);

      final AttributeName atn = new AttributeNameJpa();
      setCommonFields(atn);
      atn.setAbbreviation(getPreferredName(prop.getIRI(), ontology));
      atn.setAnnotation(true);
      atn.setExistentialQuantification(true);
      // NOT in OWL EL 2
      atn.setUniversalQuantification(false);
      atn.setExpandedForm(prop.getIRI().toString());
      Logger.getLogger(getClass()).debug(
          "    terminologyId = " + getTerminologyId(prop.getIRI()));

      // Add rela
      Logger.getLogger(getClass()).debug("  add atn - " + atn);
      addAttributeName(atn);
      atnMap.put(atn.getAbbreviation(), atn);
      atnMap.put(getTerminologyId(prop.getIRI()), atn);
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
    logInfo("  Loading data properties");
    Map<String, String> equiv = new HashMap<>();
    Map<String, String> parChd = new HashMap<>();

    for (final OWLDataProperty prop : ontology.getDataPropertiesInSignature()) {
      OwlUtility.logDataProperty(prop, ontology);

      final AttributeName atn = new AttributeNameJpa();
      setCommonFields(atn);
      atn.setAbbreviation(getPreferredName(prop.getIRI(), ontology));

      // domain
      if (ontology.getDataPropertyDomainAxioms(prop).size() == 1) {
        OWLDataPropertyDomainAxiom axiom =
            ontology.getDataPropertyDomainAxioms(prop).iterator().next();
        // Expect a class with an IRI
        if (axiom.getDomain() instanceof OWLClass) {
          atn.setDomainId(getTerminologyId(((OWLClass) axiom.getDomain())
              .getIRI()));
        }
        // ASSUMPTION: data property domain is not an OWLClass
        else {
          throw new Exception("Unexpected domain type, not class");
        }
      }
      // ASSUMPTION: data property has at most one domains
      else if (ontology.getDataPropertyDomainAxioms(prop).size() > 1) {
        throw new Exception("Unexpectedly more than one domain axiom");
      }

      // equivalent
      if (ontology.getEquivalentDataPropertiesAxioms(prop).size() == 1) {
        OWLEquivalentDataPropertiesAxiom axiom =
            ontology.getEquivalentDataPropertiesAxioms(prop).iterator().next();
        for (final OWLDataPropertyExpression prop2 : axiom.getProperties()) {
          String abbr = getTerminologyId(prop2.asOWLDataProperty().getIRI());
          // Skip this property
          if (abbr.equals(atn.getAbbreviation())) {
            continue;
          }
          // ASSUMPTION - only one equivalent property
          // If need be, support equivalences as a set
          if (equiv.containsKey(atn.getAbbreviation())) {
            throw new Exception(
                "Unexpected multiple equivalent properties for " + atn);
          }
          equiv.put(atn.getAbbreviation(), abbr);
        }
      }

      atn.setExistentialQuantification(true);
      // NOT in OWL EL 2
      atn.setUniversalQuantification(false);
      atn.setExpandedForm(prop.getIRI().toString());
      atn.setFunctional(ontology.getFunctionalDataPropertyAxioms(prop).size() != 0);

      Logger.getLogger(getClass()).debug(
          "    terminologyId = " + getTerminologyId(prop.getIRI()));

      // par/chd
      if (ontology.getDataSubPropertyAxiomsForSubProperty(prop).size() == 1) {
        OWLSubDataPropertyOfAxiom axiom =
            ontology.getDataSubPropertyAxiomsForSubProperty(prop).iterator()
                .next();
        OWLDataProperty superProp =
            axiom.getSuperProperty().asOWLDataProperty();
        parChd.put(getTerminologyId(superProp.getIRI()), atn.getAbbreviation());
      }
      // ASSUMPTION: data property has at most one super property
      else if (ontology.getDataSubPropertyAxiomsForSubProperty(prop).size() > 1) {
        throw new Exception("Unexpected more than one super property");
      }

      // Add rela
      Logger.getLogger(getClass()).debug("  add atns - " + atn);
      addAttributeName(atn);
      atnMap.put(atn.getAbbreviation(), atn);
      atnMap.put(getTerminologyId(prop.getIRI()), atn);
    }

    commitClearBegin();

    // PAR/CHD
    for (final String key : parChd.keySet()) {
      final AttributeName par = atnMap.get(key);
      final AttributeName chd = atnMap.get(parChd.get(key));
      chd.setSuperName(par);
      updateAttributeName(chd);
    }
    // equiv
    for (final String key : equiv.keySet()) {
      final AttributeName atn1 = atnMap.get(key);
      final AttributeName atn2 = atnMap.get(equiv.get(key));
      atn1.setEquivalentName(atn2);
      atn2.setEquivalentName(atn1);
      updateAttributeName(atn2);
      updateAttributeName(atn1);
    }
    commitClearBegin();

  }

  /**
   * Load general class axioms.
   *
   * @param ontology the ontology
   * @throws Exception the exception
   */
  public void loadGeneralClassAxioms(OWLOntology ontology) throws Exception {
    logInfo("  Loading general class axioms");

    for (final OWLClassAxiom axiom : ontology.getGeneralClassAxioms()) {

      if (axiom instanceof OWLDisjointClassesAxiom) {
        // Create disjointness
        logInfo("  DISJOINT CLASSES AXIOM: " + axiom);
        throw new Exception("Not handled yet, needs impl");

      } else if (axiom instanceof OWLSubClassOfAxiom) {

        logInfo("  SUB CLASS AXIOM: " + axiom);
        OWLSubClassOfAxiom axiom2 = (OWLSubClassOfAxiom) axiom;
        if (axiom2.getSuperClass() instanceof OWLClass) {
          throw new Exception(
              "Unexpectedly encountered a simple OWLClass in a general subclass axiom - "
                  + axiom2.getSuperClass());
        }
        if (axiom2.getSubClass() instanceof OWLClass) {
          throw new Exception(
              "Unexpectedly encountered a simple OWLClass in a general subclass axiom - "
                  + axiom2.getSuperClass());
        }

        Concept concept1 =
            getConceptForOwlClassExpression(axiom2.getSuperClass(), ontology, 1);
        Concept concept2 =
            getConceptForOwlClassExpression(axiom2.getSubClass(), ontology, 1);
        // Reuse if they exist already, otherwise add
        if (idMap.containsKey(concept1.getTerminologyId())) {
          concept1 = getConcept(idMap.get(concept1.getTerminologyId()));
        } else {
          addAnonymousConcept(concept1);
          exprMap.put(concept1.getTerminologyId(), axiom2.getSuperClass());
        }
        if (idMap.containsKey(concept2.getTerminologyId())) {
          concept2 = getConcept(idMap.get(concept2.getTerminologyId()));
        } else {
          addAnonymousConcept(concept2);
          exprMap.put(concept2.getTerminologyId(), axiom2.getSubClass());
        }
        GeneralConceptAxiom gca = new GeneralConceptAxiomJpa();
        setCommonFields(gca);
        gca.setEquivalent(false);
        gca.setSubClass(true);
        gca.setLeftHandSide(concept1);
        gca.setRightHandSide(concept2);
        logInfo("  add general class axiom - " + gca);
        addGeneralConceptAxiom(gca);
        throw new Exception(
            "General class axioms - need to implement inference based on these.");

      } else if (axiom instanceof OWLEquivalentClassesAxiom) {
        logInfo("  EQUIVALENT CLASSES AXIOM: " + axiom);
        OWLEquivalentClassesAxiom axiom2 = (OWLEquivalentClassesAxiom) axiom;
        // Each of the class expressions is equivalent,
        // create pairwise "general class axioms" from them
        for (final OWLClassExpression expr : axiom2.getClassExpressions()) {
          for (final OWLClassExpression expr2 : axiom2.getClassExpressions()) {
            // Get concepts
            Concept concept1 =
                getConceptForOwlClassExpression(expr, ontology, 1);
            Concept concept2 =
                getConceptForOwlClassExpression(expr2, ontology, 1);
            // Only do in one direction
            if (concept1.getTerminologyId().compareTo(
                concept2.getTerminologyId()) >= 0) {
              continue;
            }
            // Reuse if they exist already, otherwise add
            if (idMap.containsKey(concept1.getTerminologyId())) {
              concept1 = getConcept(idMap.get(concept1.getTerminologyId()));
            } else {
              addAnonymousConcept(concept1);
              exprMap.put(concept1.getTerminologyId(), expr);
            }
            if (idMap.containsKey(concept2.getTerminologyId())) {
              concept2 = getConcept(idMap.get(concept2.getTerminologyId()));
            } else {
              addAnonymousConcept(concept2);
              exprMap.put(concept2.getTerminologyId(), expr2);
            }
            GeneralConceptAxiom gca = new GeneralConceptAxiomJpa();
            setCommonFields(gca);
            gca.setEquivalent(true);
            gca.setSubClass(false);
            gca.setLeftHandSide(concept1);
            gca.setRightHandSide(concept2);
            logInfo("  add general class axiom - " + gca);
            addGeneralConceptAxiom(gca);
            throw new Exception(
                "General class axioms - need to implement inference based on these.");
          }
        }

      } else {
        throw new Exception("Unexpected general class axiom type: " + axiom);
      }
    }
    commitClearBegin();

  }

  /**
   * Load disjoint sets.
   *
   * @throws Exception the exception
   */
  private void loadDisjointSets() throws Exception {
    Logger.getLogger(getClass()).debug("  Load disjoint subsets");

    // Iterate through disjoint Map
    // Create a subset, wire all subset members, etc.
    int ct = 1;
    for (final String key : disjointMap.keySet()) {
      final ConceptSubset subset = new ConceptSubsetJpa();
      setCommonFields(subset);
      subset.setTerminologyId("");
      subset.setDisjointSubset(true);
      subset.setLabelSubset(false);
      subset.setName(terminology + " disjoint subset " + ct++);
      subset.setDescription("Collection of disjoint concepts from "
          + terminology);
      Logger.getLogger(getClass()).debug("    subset = " + subset);
      addSubset(subset);
      commitClearBegin();

      for (final String id : disjointMap.get(key)) {
        final ConceptSubsetMember member = new ConceptSubsetMemberJpa();
        setCommonFields(member);
        member.setTerminologyId("");
        member.setMember(getConcept(idMap.get(id)));
        member.setSubset(subset);
        Logger.getLogger(getClass()).debug("  add member = " + member);
        addSubsetMember(member);
        member.getMember().getMembers().add(member);
        updateConcept(member.getMember());
        subset.getMembers().add(member);
      }
      // Update the subset
      updateSubset(subset);
      commitClearBegin();
      Logger.getLogger(getClass()).debug(
          "      count = " + subset.getMembers().size());
    }
  }

  /**
   * Load owl class.
   *
   * @param expr the owl class
   * @param ontology the ontology
   * @param level the level
   * @return the concept
   * @throws Exception the exception
   */
  private Concept getConceptForOwlClassExpression(OWLClassExpression expr,
    OWLOntology ontology, int level) throws Exception {

    // Log it
    if (expr instanceof OWLClass) {
      OwlUtility.logOwlClass((OWLClass) expr, ontology, level);
    } else {
      OwlUtility.logOwlClassExpression(expr, ontology, level);
    }

    // Handle direct OWLClass
    if (expr instanceof OWLClass) {
      return getConceptForOwlClass((OWLClass) expr, ontology, level);
    }

    // Handle ObjectIntersectionOf
    else if (expr instanceof OWLObjectIntersectionOf) {
      return getConceptForIntersectionOf((OWLObjectIntersectionOf) expr,
          ontology, level);
    }

    // Handle ObjectUnionOf
    else if (expr instanceof OWLObjectUnionOf) {
      return getConceptForUnionOf((OWLObjectUnionOf) expr, ontology, level);
    }

    // Handle ObjectSomeValuesFrom
    else if (expr instanceof OWLObjectSomeValuesFrom) {
      return getConceptForSomeValuesFrom((OWLObjectSomeValuesFrom) expr,
          ontology, level);

    }

    else {
      throw new Exception("Unexpected class expression type - "
          + expr.getClassExpressionType());
    }

  }

  /**
   * Returns the concept for owl class.
   *
   * @param owlClass the owl class
   * @param ontology the ontology
   * @param level the level
   * @return the concept for owl class
   * @throws Exception the exception
   */
  private Concept getConceptForOwlClass(OWLClass owlClass,
    OWLOntology ontology, int level) throws Exception {

    // If class already exists, simply return it.
    if (idMap.containsKey(getTerminologyId(owlClass.getIRI()))) {
      return getConcept(idMap.get(getTerminologyId(owlClass.getIRI())));
    }

    Concept concept = new ConceptJpa();

    // Standard settings
    setCommonFields(concept);
    concept.setWorkflowStatus(WorkflowStatus.PUBLISHED);

    // owl classes always use conjunction
    concept.setUsesRelationshipUnion(false);
    concept.setUsesRelationshipIntersection(true);

    // Set fully defined
    if (ontology.getEquivalentClassesAxioms(owlClass).size() > 0) {
      // ASSUMPTION: only one equivalent class statement
      if (ontology.getEquivalentClassesAxioms(owlClass).size() > 1) {
        throw new Exception(
            "Unexpected more than one equivalent class axiom for " + owlClass);
      }
      concept.setFullyDefined(true);
    } else {
      concept.setFullyDefined(false);
    }

    // Set anonymous flag and identifier
    if (owlClass.isAnonymous()) {
      concept.setAnonymous(true);
      String uuid = TerminologyUtility.getUuid(owlClass.toString()).toString();
      // Check for an already existing, matching anonymous class
      if (idMap.containsKey(uuid)) {
        concept = getConcept(idMap.get(uuid));
      } else {
        concept.setTerminologyId(uuid);
      }
      Logger.getLogger(getClass()).debug(
          "  anonymous class = " + uuid + ", " + concept);

    } else {
      concept.setAnonymous(owlClass.isAnonymous());
      concept.setFullyDefined(false);
      concept.setTerminologyId(getTerminologyId(owlClass.getIRI()));
    }

    //
    // Lookup and create atoms (from annotations)
    //
    final Set<Atom> atoms = getAtoms(owlClass, ontology);
    boolean flag = true;
    for (final Atom atom : atoms) {
      // Use first RDFS label as the preferred name
      if (flag && isPreferredType(atom.getTermType())) {
        concept.setName(atom.getName());
        flag = false;
      }
      concept.getAtoms().add(atom);
    }

    //
    // Lookup and create definitions (from annotations)
    //
    final Set<Definition> defs = getDefinitions(owlClass, ontology);
    for (final Definition def : defs) {
      concept.getDefinitions().add(def);
    }

    //
    // Lookup and create attributes (from annotations)
    //
    final Set<Attribute> attributes = getAttributes(owlClass, ontology);
    for (final Attribute attribute : attributes) {
      concept.getAttributes().add(attribute);

    }

    //
    // Handle disjoint classes
    //
    for (final OWLDisjointClassesAxiom axiom : ontology
        .getDisjointClassesAxioms(owlClass)) {

      final Set<String> disjointSet = new HashSet<>();
      for (final OWLClassExpression expr : axiom.getClassExpressions()) {
        if (expr instanceof OWLClass) {
          final String disjointId =
              getTerminologyId(((OWLClass) expr).getIRI());
          if (!concept.getTerminologyId().equals(disjointId)) {
            disjointSet.add(disjointId);
          }
        } else {
          throw new Exception(
              "Unexpected disjoint classes axiom that is not an OWLClass - "
                  + expr);
        }
      }
      // If this disjoint set overlaps with another one, add all
      // this way at the end we have fully computed sets
      boolean disjointFlag = false;
      for (final Set<String> set : disjointMap.values()) {
        for (final String id : disjointSet) {
          if (set.contains(id)) {
            disjointFlag = true;
            break;
          }
        }
        if (disjointFlag) {
          set.addAll(disjointSet);
          break;
        }
      }
      if (!disjointFlag) {
        disjointMap.put(concept.getTerminologyId(), disjointSet);
      }
    }

    return concept;
  }

  /**
   * Returns the concept for an ObjectIntersectionOf. This is just used to
   * borrow relationships and never actually creates an anonymous concept.
   *
   * @param expr the expr
   * @param ontology the ontology
   * @param level the level
   * @return the concept for intersection
   * @throws Exception the exception
   */
  private Concept getConceptForIntersectionOf(OWLObjectIntersectionOf expr,
    OWLOntology ontology, int level) throws Exception {
    String uuid = TerminologyUtility.getUuid(expr.toString()).toString();

    if (idMap.containsKey(uuid)) {
      return getConcept(idMap.get(uuid));
    }

    Concept concept = new ConceptJpa();
    setCommonFields(concept);
    concept.setWorkflowStatus(WorkflowStatus.PUBLISHED);
    concept.setAnonymous(true);
    concept.setTerminologyId(uuid);
    concept.setName(expr.toString());
    concept.setUsesRelationshipIntersection(true);
    concept.setUsesRelationshipUnion(false);

    // Handle nested class expressions
    if (expr.getOperands().size() > 1) {

      // Iterate through expressions and either add a parent relationship
      // or add relationships from the concept itself. No new anonymous
      // concepts are directly created here.
      for (final OWLClassExpression expr2 : expr.getOperands()) {
        final Concept concept2 =
            getConceptForOwlClassExpression(expr2, ontology, level + 1);
        // If it's a restriction, borrow its relationships
        if (expr2 instanceof OWLObjectSomeValuesFrom) {
          for (final ConceptRelationship rel : concept2.getRelationships()) {
            // In case this is from a reused inserted anonymous concept, copy it
            // first
            ConceptRelationship rel2 = new ConceptRelationshipJpa(rel, true);
            rel2.setId(null);
            rel2.setFrom(concept);
            concept.getRelationships().add(rel2);
          }
        }
        // next, handle unionOf
        else if (expr2 instanceof OWLObjectUnionOf) {
          // Here, the concept will have a series of "other" relationships
          // to the members of the collection. We need to add it
          if (concept2.isAnonymous()
              && !idMap.containsKey(concept2.getTerminologyId())) {
            addAnonymousConcept(concept2);
            exprMap.put(concept2.getTerminologyId(), expr);
          }
          ConceptRelationship rel =
              getSubClassOfRelationship(concept, concept2);
          rel.setRelationshipType("unionOf");
          rel.setAdditionalRelationshipType("");
          concept.getRelationships().add(rel);

        }

        // otherwise, simply add this concept as a parent
        else if (expr2 instanceof OWLClass) {
          ConceptRelationship rel =
              getSubClassOfRelationship(concept, concept2);
          concept.getRelationships().add(rel);
        }
        // otherwise, unknown type
        else {
          throw new Exception("Unexpected operand expression type - " + expr2);
        }
      }

      return concept;

    }

    // ASSUMPTION: intersection has at least two sub-expressions
    else {
      throw new Exception(
          "Unexpected number of intersection nested class expressions - "
              + expr);
    }

  }

  /**
   * Returns the concept for an ObjectUnionOf. This is just used to borrow
   * relationships and never actually creates an anonymous concept.
   *
   * @param expr the expr
   * @param ontology the ontology
   * @param level the level
   * @return the concept for union
   * @throws Exception the exception
   */
  private Concept getConceptForUnionOf(OWLObjectUnionOf expr,
    OWLOntology ontology, int level) throws Exception {
    String uuid = TerminologyUtility.getUuid(expr.toString()).toString();

    if (idMap.containsKey(uuid)) {
      return getConcept(idMap.get(uuid));
    }

    Concept concept = new ConceptJpa();
    setCommonFields(concept);
    concept.setWorkflowStatus(WorkflowStatus.PUBLISHED);
    concept.setAnonymous(true);
    concept.setTerminologyId(uuid);
    concept.setName(expr.toString());
    concept.setUsesRelationshipIntersection(false);
    concept.setUsesRelationshipUnion(true);

    // Handle nested class expressions
    if (expr.getOperands().size() > 1) {

      // Iterate through expressions and either add a parent relationship
      // or add relationships from the concept itself. No new anonymous
      // concepts are directly created here.
      for (final OWLClassExpression expr2 : expr.getOperands()) {
        final Concept concept2 =
            getConceptForOwlClassExpression(expr2, ontology, level + 1);
        // If it's a restriction, borrow its relationships
        if (expr2 instanceof OWLObjectIntersectionOf) {
          // make an anonymous concept out of this and a relationship to it.
          // Add if anonymous and doesn't exist yet
          if (concept2.isAnonymous()
              && !idMap.containsKey(concept2.getTerminologyId())) {
            addAnonymousConcept(concept2);
            exprMap.put(concept2.getTerminologyId(), expr);
          }
          ConceptRelationship rel =
              getSubClassOfRelationship(concept, concept2);
          rel.setRelationshipType("other");
          rel.setAdditionalRelationshipType("");
          concept.getRelationships().add(rel);
        }

        // otherwise, unknown type
        else {
          throw new Exception("Unexpected operand expression type - " + expr2);
        }
      }

      return concept;

    }

    // ASSUMPTION: union has at least two sub-expressions
    else {
      throw new Exception(
          "Unexpected number of union nested class expressions - " + expr);
    }

  }

  /**
   * Adds the anonymous concept.
   *
   * @param concept the concept
   * @throws Exception the exception
   */
  private void addAnonymousConcept(Concept concept) throws Exception {
    if (concept.isAnonymous() && !idMap.containsKey(concept.getTerminologyId())) {
      Atom atom = new AtomJpa();
      setCommonFields(atom);
      atom.setWorkflowStatus(WorkflowStatus.PUBLISHED);
      atom.setTerminologyId("");
      atom.setTermType(label);
      atom.setConceptId(concept.getTerminologyId());
      atom.setCodeId("");
      atom.setDescriptorId("");
      atom.setLexicalClassId("");
      atom.setStringClassId("");
      atom.setLanguage("en");
      atom.setPublishable(false);
      atom.setWorkflowStatus(WorkflowStatus.PUBLISHED);
      atom.setPublished(false);
      atom.setName(concept.getName());
      Logger.getLogger(getClass()).debug("  add atom - " + atom);
      addAtom(atom);
      concept.getAtoms().add(atom);

      Logger.getLogger(getClass()).debug("  add concept - " + concept);
      addConcept(concept);
      idMap.put(concept.getTerminologyId(), concept.getId());

      // ASSUMPTION - the concept has no atoms or attributes
      if (concept.getAtoms().size() > 1 || concept.getAttributes().size() > 0) {
        logError("  atoms = " + concept.getAtoms());
        logError("  attributes = " + concept.getAttributes());
        throw new Exception(
            "Unexpected anonymous concept with atoms or attributes ");
      }

      List<ConceptRelationship> relsToAdd = concept.getRelationships();
      concept.setRelationships(new ArrayList<ConceptRelationship>());
      for (final ConceptRelationship rel : relsToAdd) {
        addRelationship(rel);
        concept.getRelationships().add(rel);
        Logger.getLogger(getClass()).debug("  add relationship - " + rel);
      }
    }
  }

  /**
   * Returns the concept for some values from.
   *
   * @param expr the expr
   * @param ontology the ontology
   * @param level the level
   * @return the concept for some values from
   * @throws Exception the exception
   */
  private Concept getConceptForSomeValuesFrom(OWLObjectSomeValuesFrom expr,
    OWLOntology ontology, int level) throws Exception {
    String uuid = TerminologyUtility.getUuid(expr.toString()).toString();

    if (idMap.containsKey(uuid)) {
      return getConcept(idMap.get(uuid));
    }

    Concept concept = new ConceptJpa();
    setCommonFields(concept);
    concept.setWorkflowStatus(WorkflowStatus.PUBLISHED);
    concept.setAnonymous(true);
    concept.setTerminologyId(uuid);
    concept.setName(expr.toString());
    concept.setUsesRelationshipIntersection(true);
    concept.setUsesRelationshipUnion(false);

    // This is a restriction on a property with existential quantification.
    // It is a relationship to either a class or an anonymous class

    // Get the target concept
    Concept concept2 =
        getConceptForOwlClassExpression(expr.getFiller(), ontology, level + 1);
    // Add if anonymous and doesn't exist yet
    if (concept2.isAnonymous()
        && !idMap.containsKey(concept2.getTerminologyId())) {
      addAnonymousConcept(concept2);
      exprMap.put(concept2.getTerminologyId(), expr);
    }

    // Get the property and create a relationship
    OWLObjectProperty property = (OWLObjectProperty) expr.getProperty();
    ConceptRelationship rel = new ConceptRelationshipJpa();
    setCommonFields(rel);
    rel.setWorkflowStatus(WorkflowStatus.PUBLISHED);
    rel.setRelationshipType("other");
    rel.setAdditionalRelationshipType(relaMap.get(
        getTerminologyId(property.getIRI())).getAbbreviation());
    rel.setFrom(concept);
    rel.setTo(concept2);
    concept.getRelationships().add(rel);

    return concept;
  }

  /**
   * Indicates whether or not atom annotation is the case.
   *
   * @param annotation the annotation
   * @return <code>true</code> if so, <code>false</code> otherwise
   * @throws Exception the exception
   */
  private boolean isAtomAnnotation(OWLAnnotation annotation) throws Exception {
    String name = getName(annotation);
    if (name.equals(label)) {
      return true;
    }

    String atomAnnotations =
        getConfigurableValue(terminology, "atomAnnotations");
    if (atomAnnotations != null) {
      for (final String field : FieldedStringTokenizer.split(atomAnnotations,
          ",")) {
        if (name.equals(field)) {
          return true;
        }
      }
    } else {
      logWarn("  NO atom annotations are specifically declared, "
          + "just using rdfs:label and rdfs:comment");
    }

    return false;
  }

  /**
   * Indicates whether or not definition annotation is the case.
   *
   * @param annotation the annotation
   * @return <code>true</code> if so, <code>false</code> otherwise
   * @throws Exception the exception
   */
  private boolean isDefinitionAnnotation(OWLAnnotation annotation)
    throws Exception {
    String name = getName(annotation);
    String atomAnnotations =
        getConfigurableValue(terminology, "definitionAnnotations");
    if (atomAnnotations != null) {
      for (final String field : FieldedStringTokenizer.split(atomAnnotations,
          ",")) {
        if (name.equals(field)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns the term type.
   *
   * @param annotation the annotation
   * @return the term type
   */
  private String getName(OWLAnnotation annotation) {
    return getTerminologyId(annotation.getProperty().getIRI());
  }

  /**
   * Is preferred type.
   *
   * @param tty the tty
   * @return the string
   * @throws Exception the exception
   */
  private boolean isPreferredType(String tty) throws Exception {

    if (tty.equals(getConfigurableValue(terminology, "preferredType"))) {
      return true;
    }

    // Don't look further if the configurable type is set
    if (getConfigurableValue(terminology, "preferredType") != null) {
      return false;
    }

    if (tty.equals(label)) {
      return true;
    }

    return false;
  }

  /**
   * Returns the terminology id.
   *
   * @param iri the iri
   * @return the terminology id
   */
  @SuppressWarnings("static-method")
  private String getTerminologyId(IRI iri) {

    if (iri.toString().contains("#")) {
      // everything after the last #
      return iri.toString().substring(iri.toString().lastIndexOf("#") + 1);
    } else if (iri.toString().contains("/")) {
      // everything after the last slash
      return iri.toString().substring(iri.toString().lastIndexOf("/") + 1);
    }
    // otherwise, just return the iri
    return iri.toString();
  }

  /**
   * Sets the common fields.
   *
   * @param component the common fields
   */
  private void setCommonFields(Component component) {
    component.setTerminologyId("");
    component.setTerminology(terminology);
    component.setVersion(version);
    component.setTimestamp(releaseVersionDate);
    component.setLastModified(releaseVersionDate);
    component.setLastModifiedBy(loader);
    component.setPublishable(true);
    component.setPublished(true);
    component.setObsolete(false);
    component.setSuppressible(false);

    if (component instanceof ConceptRelationship) {
      ((ConceptRelationship) component).setAssertedDirection(true);
      ((ConceptRelationship) component).setGroup(null);
      ((ConceptRelationship) component).setHierarchical(false);
      ((ConceptRelationship) component).setInferred(loadInferred);
      ((ConceptRelationship) component).setStated(!loadInferred);
      // So the field is {} instead of null to match when copied
      ((ConceptRelationship) component).getAlternateTerminologyIds();
    }
  }

  /**
   * Sets the common fields.
   *
   * @param abbreviation the common fields
   */
  private void setCommonFields(Abbreviation abbreviation) {
    abbreviation.setTerminology(terminology);
    abbreviation.setVersion(version);
    abbreviation.setTimestamp(releaseVersionDate);
    abbreviation.setLastModified(releaseVersionDate);
    abbreviation.setLastModifiedBy(loader);
    abbreviation.setPublishable(true);
    abbreviation.setPublished(true);

  }

  /**
   * Returns the configurable value.
   *
   * @param terminology the terminology
   * @param key the key
   * @return the configurable value
   * @throws Exception the exception
   */
  private String getConfigurableValue(String terminology, String key)
    throws Exception {
    Properties p = ConfigUtility.getConfigProperties();
    String fullKey = getClass().getName() + "." + terminology + "." + key;
    if (p.containsKey(fullKey)) {
      return p.getProperty(fullKey);
    }
    return null;
  }

  /* see superclass */
  @Override
  public String getTerminology() {
    return terminology;
  }

  /* see superclass */
  @Override
  public String getVersion() {
    return version;
  }

  @Override
  public void computeExpressionIndexes() throws Exception {
    final EclConceptIndexingAlgorithm algo = new EclConceptIndexingAlgorithm();
    algo.setTerminology(getTerminology());
    algo.setVersion(getVersion());
    algo.compute();
  }

}
