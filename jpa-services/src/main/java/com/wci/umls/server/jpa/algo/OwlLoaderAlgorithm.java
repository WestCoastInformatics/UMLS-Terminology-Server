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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
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
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.helpers.PrecedenceListJpa;
import com.wci.umls.server.jpa.meta.AdditionalRelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.AttributeNameJpa;
import com.wci.umls.server.jpa.meta.CitationJpa;
import com.wci.umls.server.jpa.meta.GeneralMetadataEntryJpa;
import com.wci.umls.server.jpa.meta.PropertyChainJpa;
import com.wci.umls.server.jpa.meta.RelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.RootTerminologyJpa;
import com.wci.umls.server.jpa.meta.TermTypeJpa;
import com.wci.umls.server.jpa.meta.TerminologyJpa;
import com.wci.umls.server.jpa.services.HistoryServiceJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.CodeVariantType;
import com.wci.umls.server.model.meta.GeneralMetadataEntry;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.NameVariantType;
import com.wci.umls.server.model.meta.PropertyChain;
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

  /** The id map. */
  Map<String, Long> idMap = new HashMap<>();

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
  final String label = "label";

  /** The comment. */
  final String comment = "comment";

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

      final FileInputStream in = new FileInputStream(new File(inputFile));
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology directOntology = manager.loadOntologyFromOntologyDocument(in);

      // open input file and get effective time and version and language
      // TODO:
      // findVersion(inputFile);
      // findLanguage(inputFile);

      releaseVersion = getVersion(directOntology);
      if (releaseVersion != null) {
        releaseVersionDate = ConfigUtility.DATE_FORMAT.parse(releaseVersion);
      } else {
        releaseVersion = version;
        releaseVersionDate = currentDate;
      }

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
      if (idMap.containsKey(tty)) {
        termType.setExpandedForm(getConcept(idMap.get(tty)).getName());
      }
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

    // additional relationship types
    // If the abbreviation is a terminologyId, look up the value for expanded
    // form
    for (AdditionalRelationshipType type : getAdditionalRelationshipTypes(
        terminology, version).getObjects()) {
      if (idMap.containsKey(type.getAbbreviation())) {
        type.setExpandedForm(getConcept(idMap.get(type)).getName());
        updateAdditionalRelationshipType(type);
      }
    }

    // Attribute names
    // If the abbreviation is a terminologyId, look up the value for expanded
    // form
    for (AttributeName atn : getAttributeNames(terminology, version)
        .getObjects()) {
      if (idMap.containsKey(atn.getAbbreviation())) {
        atn.setExpandedForm(getConcept(idMap.get(atn)).getName());
        updateAttributeName(atn);
      }
    }

    // Build precedence list
    final PrecedenceList list = new PrecedenceListJpa();
    list.setDefaultList(true);

    final List<KeyValuePair> lkvp = new ArrayList<>();
    // Start with label
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
    root.setHierarchicalName(getRootTerminologyPreferredName(ontology));
    // Unable to determine overall "language" from OWL (unless maybe in headers)
    root.setLanguage(null);
    root.setTimestamp(releaseVersionDate);
    root.setLastModified(releaseVersionDate);
    root.setLastModifiedBy(loader);
    root.setPolyhierarchy(true);
    root.setPreferredName(getRootTerminologyPreferredName(ontology));
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
    term.setPreferredName(getTerminologyPreferredName(ontology));
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
    for (OWLAnnotation annotation : ontology.getAnnotations()) {
      if (annotation.getProperty().isLabel()) {
        return getValue(annotation);
      }
    }
    // otherwise, just use the terminology name
    return terminology;
  }

  /**
   * Returns the terminology preferred name.
   *
   * @param ontology the ontology
   * @return the terminology preferred name
   * @throws Exception the exception
   */
  private String getTerminologyPreferredName(OWLOntology ontology)
    throws Exception {

    // If >1 owl:versionInfo, use the first one
    for (OWLAnnotation annotation : ontology.getAnnotations()) {
      if (annotation.getProperty().toString().equals("owl:versionInfo")) {
        return getValue(annotation);
      }
    }
    // otherwise try rdfs:label
    for (OWLAnnotation annotation : ontology.getAnnotations()) {
      if (annotation.getProperty().isLabel()) {
        return getValue(annotation);
      }
    }

    return terminology;
  }

  /**
   * Returns the comment.
   *
   * @param ontology the ontology
   * @return the comment
   * @throws Exception the exception
   */
  private String getComment(OWLOntology ontology) throws Exception {
    for (OWLAnnotation annotation : ontology.getAnnotations()) {
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
  private String getVersion(OWLOntology ontology) throws Exception {
    String version = ontology.getOntologyID().getVersionIRI().get().toString();
    Logger.getLogger(getClass()).info("  version = " + version);

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
    for (String pattern : patterns) {
      Pattern pattern2 = Pattern.compile(pattern);
      Matcher matcher = pattern2.matcher(version);
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
   * Helper method to extract annotation properties attached to a class.
   *
   * @param owlClass the owl class
   * @param ontology the ontology
   * @return the annotation types
   * @throws Exception the exception
   */
  Set<Atom> getAtoms(OWLClass owlClass, OWLOntology ontology) throws Exception {
    Set<Atom> atoms = new HashSet<>();
    for (OWLAnnotationAssertionAxiom axiom : ontology
        .getAnnotationAssertionAxioms(owlClass.getIRI())) {
      OWLAnnotation annotation = axiom.getAnnotation();
      if (!isAtomAnnotation(annotation)) {
        continue;
      }
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
      atom.setTermType(getName(annotation));
      generalEntryValues.add(atom.getTermType());
      termTypes.add(atom.getTermType());
      atom.setName(getValue(annotation));
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
   * Returns the attributes.
   *
   * @param owlClass the owl class
   * @param ontology the ontology
   * @return the attributes
   * @throws Exception the exception
   */
  Set<Attribute> getAttributes(OWLClass owlClass, OWLOntology ontology)
    throws Exception {
    Set<Attribute> attributes = new HashSet<>();
    for (OWLAnnotationAssertionAxiom axiom : ontology
        .getAnnotationAssertionAxioms(owlClass.getIRI())) {

      OWLAnnotation annotation = axiom.getAnnotation();
      if (isAtomAnnotation(annotation)) {
        continue;
      }
      final Attribute attribute = new AttributeJpa();
      attribute.setTerminologyId("");
      attribute.setTimestamp(currentDate);
      attribute.setLastModified(currentDate);
      attribute.setLastModifiedBy(loader);
      attribute.setObsolete(false);
      attribute.setSuppressible(false);
      attribute.setName(getName(annotation));
      attribute.setValue(getValue(annotation));
      generalEntryValues.add(attribute.getName());
      attribute.setTerminology(terminology);
      attribute.setVersion(version);
      attribute.setPublished(true);
      attribute.setPublishable(true);
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
    } else {
      throw new Exception("Unexpected annotation that is not OWLLiteral");
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
    } else {
      throw new Exception("Unexpected annotation that is not OWLLiteral");
    }
  }

  /**
   * Is preferred type.
   *
   * @param tty the tty
   * @return the string
   */
  private boolean isPreferredType(String tty) {

    // TODO: make this configurable
    if (tty.equals("Description.term.en-us.preferred")) {
      return true;
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
    // TODO: we probably need to save information about the parts of the URL we
    // are stripping
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
   * Indicates whether or not atom annotation is the case.
   *
   * @param annotation the annotation
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  private boolean isAtomAnnotation(OWLAnnotation annotation) {
    final String name = getName(annotation);
    if (name.equals(label)) {
      return true;
    }
    // TODO: this may be better as a definition
    if (name.equals(comment)) {
      return true;
    }

    // TODO: make this configurable
    if (name.equals("Description.term.en-us.preferred")) {
      return true;
    }
    if (name.equals("Description.term.en-us.synonym")) {
      return true;
    }
    if (name.equals("hasExactSynonym")) {
      return true;
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

    // TODO: consider ways of shortening this while preserving the
    // info from the overall URL structure (for round-trip)
    return getTerminologyId(annotation.getProperty().getIRI());

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

    // Load annotation properties (e.g. attribute names)
    loadAnnotationProperties(ontology);

    // Load object properties (e.g. additional relationship types)
    loadObjectProperties(ontology);

    // Load data properties (e.g. attribute names)
    loadDataProperties(ontology);

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
          loadOwlClass(owlClass, ontology);
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
  @SuppressWarnings("deprecation")
  public void loadObjectProperties(OWLOntology ontology) throws Exception {
    Map<String, String> inverses = new HashMap<>();
    Map<String, String> parChd = new HashMap<>();
    // Add object properties
    for (OWLObjectProperty prop : ontology.getObjectPropertiesInSignature()) {
      logObjectProperty(prop, ontology);

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

      rela.setExpandedForm(prop.getIRI().toString());
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
    
    // Add property chains
    // Only way I could find to access property chains
    for (OWLSubPropertyChainOfAxiom prop : ontology.getAxioms(
        AxiomType.SUB_PROPERTY_CHAIN_OF, false)) {
      logPropertyChain(prop, ontology);

      String superProp =
          getTerminologyId(prop.getSuperProperty().getNamedProperty().getIRI());
      List<String> links = new ArrayList<>();
      List<AdditionalRelationshipType> types = new ArrayList<>();
      for (OWLObjectPropertyExpression link : prop.getPropertyChain()) {
        String name = getTerminologyId(link.getNamedProperty().getIRI());
        links.add(name);
        types.add(relaMap.get(name));
      }

      PropertyChain chain = new PropertyChainJpa();
      StringBuilder abbreviation = new StringBuilder();
      for (String link : links) {
        abbreviation.append(link).append(" o ");
      }
      chain.setAbbreviation(abbreviation.toString().replaceAll(" o $", " => ")
          + superProp);
      chain.setChain(types);
      chain.setExpandedForm(chain.getAbbreviation());
      chain.setTimestamp(releaseVersionDate);
      chain.setLastModified(releaseVersionDate);
      chain.setLastModifiedBy(loader);
      chain.setPublishable(true);
      chain.setPublished(true);
      chain.setResult(relaMap.get(superProp));
      chain.setTerminology(terminology);
      chain.setVersion(version);

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

    for (OWLAnnotationProperty prop : ontology
        .getAnnotationPropertiesInSignature()) {
      logAnnotationProperty(prop, ontology);

      final AttributeName atn = new AttributeNameJpa();
      atn.setAbbreviation(getTerminologyId(prop.getIRI()));
      atn.setAnnotation(true);
      atn.setExistentialQuantification(true);
      atn.setUniversalQuantification(false);
      atn.setExpandedForm(prop.getIRI().toString());
      atn.setLastModified(releaseVersionDate);
      atn.setLastModifiedBy(loader);
      atn.setPublishable(true);
      atn.setPublished(true);
      Logger.getLogger(getClass()).info(
          "    terminologyId = " + getTerminologyId(prop.getIRI()));

      // Add rela
      Logger.getLogger(getClass()).debug("  add atn - " + atn);
      atn.setTerminology(terminology);
      atn.setVersion(version);
      addAttributeName(atn);
      atnMap.put(atn.getAbbreviation(), atn);
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
      logDataProperty(prop, ontology);

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

      atn.setExpandedForm(prop.getIRI().toString());

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
      Logger.getLogger(getClass()).debug("  add atns - " + atn);
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
    Logger.getLogger(getClass()).info("  ontology = " + ontology);
    Logger.getLogger(getClass()).info(
        "    IRI = " + ontology.getOntologyID().getOntologyIRI());
    Logger.getLogger(getClass()).info(
        "    vIRI = " + ontology.getOntologyID().getVersionIRI());

    Logger.getLogger(getClass()).debug("  Imports = " + ontology.getImports());
    Logger.getLogger(getClass()).debug(
        "    Direct imports = " + ontology.getDirectImports());
    Logger.getLogger(getClass()).debug(
        "    Imports closure = " + ontology.getImportsClosure());
    Logger.getLogger(getClass()).debug(
        "    Axiom count = " + ontology.getAxiomCount());
    Logger.getLogger(getClass()).debug(
        "    Logical axiom count = " + ontology.getLogicalAxiomCount());
    // Logger.getLogger(getClass()).debug(
    // "  AboxAxioms (imports excluded) = "
    // + ontology.getABoxAxioms(Imports.EXCLUDED));
    //
    Logger.getLogger(getClass()).debug(
        "    Annotation properties in signature = "
            + ontology.getAnnotationPropertiesInSignature());
    Logger.getLogger(getClass()).debug(
        "    Annotations = " + ontology.getAnnotations());
    Logger.getLogger(getClass()).debug(
        "    Anonymous individuals = " + ontology.getAnonymousIndividuals());
    Logger.getLogger(getClass()).debug(
        "    Classes in signature = " + ontology.getClassesInSignature());
    Logger.getLogger(getClass()).debug(
        "    Data properties in signature = "
            + ontology.getDataPropertiesInSignature());
    Logger.getLogger(getClass()).debug(
        "    Data types in signature = " + ontology.getDatatypesInSignature());
    Logger.getLogger(getClass()).debug(
        "    General class axioms = " + ontology.getGeneralClassAxioms());
    Logger.getLogger(getClass()).debug(
        "    Individuals in signature = "
            + ontology.getIndividualsInSignature());
    // Logger.getLogger(getClass()).debug(
    // "    Nested class expressions = "
    // + ontology.getNestedClassExpressions());
    Logger.getLogger(getClass()).debug(
        "    Object properties in signature = "
            + ontology.getObjectPropertiesInSignature());
    // Logger.getLogger(getClass()).debug(
    // "    Signature = " + ontology.getSignature());
  }

  /**
   * Log annotation property.
   *
   * @param prop the prop
   * @param ontology the ontology
   */
  void logAnnotationProperty(OWLAnnotationProperty prop, OWLOntology ontology) {
    Logger.getLogger(getClass()).info("  annotation property = " + prop);
    Logger.getLogger(getClass()).info("    IRI = " + prop.getIRI());
    Logger.getLogger(getClass()).info("    signature = " + prop.getSignature());
    Logger.getLogger(getClass()).info("    anonymous = " + prop.isAnonymous());
    Logger.getLogger(getClass()).info("    builtIn = " + prop.isBuiltIn());
    Logger.getLogger(getClass()).info(
        "    entity type = " + prop.getEntityType());

    // Things connected to the property

    Logger.getLogger(getClass()).info(
        "    annotation properties in signature = "
            + prop.getAnnotationPropertiesInSignature().size());
    for (OWLAnnotationProperty aprop : prop
        .getAnnotationPropertiesInSignature()) {
      Logger.getLogger(getClass()).info("      annotation = " + aprop);
    }

    Logger.getLogger(getClass()).info(
        "    classes in signature = " + prop.getClassesInSignature().size());
    for (OWLClass owlClass : prop.getClassesInSignature()) {
      Logger.getLogger(getClass()).info("      class = " + owlClass);
    }

    Logger.getLogger(getClass()).info(
        "    data properties in signature = "
            + prop.getDataPropertiesInSignature().size());
    for (OWLDataProperty dprop : prop.getDataPropertiesInSignature()) {
      Logger.getLogger(getClass()).info("      data property = " + dprop);
    }

    Logger.getLogger(getClass())
        .info(
            "    datatypes in signature = "
                + prop.getDatatypesInSignature().size());
    for (OWLDatatype dtype : prop.getDatatypesInSignature()) {
      Logger.getLogger(getClass()).info("      datatype = " + dtype);
    }

    Logger.getLogger(getClass()).info(
        "    nested class expressions = "
            + prop.getNestedClassExpressions().size());
    for (OWLClassExpression expr : prop.getNestedClassExpressions()) {
      Logger.getLogger(getClass()).info("      expr = " + expr);
    }

    Logger.getLogger(getClass()).info(
        "    object properties in signature = "
            + prop.getObjectPropertiesInSignature().size());
    for (OWLObjectProperty oprop : prop.getObjectPropertiesInSignature()) {
      Logger.getLogger(getClass()).info("      property = " + oprop);
    }

    // Things connected to ontology

    Logger.getLogger(getClass()).info(
        "    annotation assertion axioms = "
            + ontology.getAnnotationAssertionAxioms(prop.getIRI()));
    for (OWLAnnotationAssertionAxiom axiom : ontology
        .getAnnotationAssertionAxioms(prop.getIRI())) {
      Logger.getLogger(getClass()).info("      axiom = " + axiom);
    }

    Logger.getLogger(getClass()).info(
        "    annotation domain axioms = "
            + ontology.getAnnotationPropertyDomainAxioms(prop));
    for (OWLAnnotationPropertyDomainAxiom axiom : ontology
        .getAnnotationPropertyDomainAxioms(prop)) {
      Logger.getLogger(getClass()).info("      axiom = " + axiom);
    }

    Logger.getLogger(getClass()).info(
        "    annotation range axioms = "
            + ontology.getAnnotationPropertyRangeAxioms(prop));
    for (OWLAnnotationPropertyRangeAxiom axiom : ontology
        .getAnnotationPropertyRangeAxioms(prop)) {
      Logger.getLogger(getClass()).info("      axiom = " + axiom);
    }

  }

  /**
   * Log data property.
   *
   * @param prop the prop
   * @param ontology the ontology
   */
  void logDataProperty(OWLDataProperty prop, OWLOntology ontology) {
    Logger.getLogger(getClass()).info("  object property = " + prop);
    Logger.getLogger(getClass()).info("    IRI = " + prop.getIRI());
    Logger.getLogger(getClass()).info("    signature = " + prop.getSignature());

    Logger.getLogger(getClass()).info("    anonymous = " + prop.isAnonymous());
    Logger.getLogger(getClass()).info("    builtIn = " + prop.isBuiltIn());
    Logger.getLogger(getClass()).info(
        "    entity type = " + prop.getEntityType());

    Logger.getLogger(getClass()).info(
        "    annotation properties in signature = "
            + prop.getAnnotationPropertiesInSignature().size());
    for (OWLAnnotationProperty aprop : prop
        .getAnnotationPropertiesInSignature()) {
      Logger.getLogger(getClass()).info("      annotation = " + aprop);
    }

    Logger.getLogger(getClass()).info(
        "    classes in signature = " + prop.getClassesInSignature().size());
    for (OWLClass owlClass : prop.getClassesInSignature()) {
      Logger.getLogger(getClass()).info("      class = " + owlClass);
    }

    Logger.getLogger(getClass()).info(
        "    data properties in signature = "
            + prop.getDataPropertiesInSignature().size());
    for (OWLDataProperty dprop : prop.getDataPropertiesInSignature()) {
      Logger.getLogger(getClass()).info("      data property = " + dprop);
    }

    Logger.getLogger(getClass())
        .info(
            "    datatypes in signature = "
                + prop.getDatatypesInSignature().size());
    for (OWLDatatype dtype : prop.getDatatypesInSignature()) {
      Logger.getLogger(getClass()).info("      datatype = " + dtype);
    }

    Logger.getLogger(getClass()).info(
        "    nested class expressions = "
            + prop.getNestedClassExpressions().size());
    for (OWLClassExpression expr : prop.getNestedClassExpressions()) {
      Logger.getLogger(getClass()).info("      expr = " + expr);
    }

    Logger.getLogger(getClass()).info(
        "    object properties in signature = "
            + prop.getObjectPropertiesInSignature().size());
    for (OWLObjectProperty oprop : prop.getObjectPropertiesInSignature()) {
      Logger.getLogger(getClass()).info("      property = " + oprop);
    }

    // loaded from ontology

    Logger.getLogger(getClass()).info(
        "    annotation assertion axioms = "
            + ontology.getAnnotationAssertionAxioms(prop.getIRI()));
    for (OWLAnnotationAssertionAxiom axiom : ontology
        .getAnnotationAssertionAxioms(prop.getIRI())) {
      Logger.getLogger(getClass()).info("      axiom = " + axiom);
    }

  }

  /**
   * Log property chain.
   *
   * @param prop the prop
   * @param ontology the ontology
   */
  void logPropertyChain(OWLSubPropertyChainOfAxiom prop, OWLOntology ontology) {
    Logger.getLogger(getClass()).info("  property chain= " + prop);
    Logger.getLogger(getClass()).info("    chain = " + prop.getPropertyChain());
    Logger.getLogger(getClass()).info(
        "    super property = " + prop.getSuperProperty());
    Logger.getLogger(getClass()).info("    signature = " + prop.getSignature());
    Logger.getLogger(getClass()).info(
        "    entity type = " + prop.getAxiomType());

    Logger.getLogger(getClass()).info(
        "    annotation properties in signature = "
            + prop.getAnnotationPropertiesInSignature().size());
    for (OWLAnnotationProperty aprop : prop
        .getAnnotationPropertiesInSignature()) {
      Logger.getLogger(getClass()).info("      annotation = " + aprop);
    }

    Logger.getLogger(getClass()).info(
        "    classes in signature = " + prop.getClassesInSignature().size());
    for (OWLClass owlClass : prop.getClassesInSignature()) {
      Logger.getLogger(getClass()).info("      class = " + owlClass);
    }

    Logger.getLogger(getClass()).info(
        "    data properties in signature = "
            + prop.getDataPropertiesInSignature().size());
    for (OWLDataProperty dprop : prop.getDataPropertiesInSignature()) {
      Logger.getLogger(getClass()).info("      data property = " + dprop);
    }

    Logger.getLogger(getClass())
        .info(
            "    datatypes in signature = "
                + prop.getDatatypesInSignature().size());
    for (OWLDatatype dtype : prop.getDatatypesInSignature()) {
      Logger.getLogger(getClass()).info("      datatype = " + dtype);
    }

    Logger.getLogger(getClass()).info(
        "    nested class expressions = "
            + prop.getNestedClassExpressions().size());
    for (OWLClassExpression expr : prop.getNestedClassExpressions()) {
      Logger.getLogger(getClass()).info("      expr = " + expr);
    }

    Logger.getLogger(getClass()).info(
        "    object properties in signature = "
            + prop.getObjectPropertiesInSignature().size());
    for (OWLObjectProperty oprop : prop.getObjectPropertiesInSignature()) {
      Logger.getLogger(getClass()).info("      property = " + oprop);
    }

    // loaded from ontology - n/a

  }

  /**
   * Log object property.
   *
   * @param prop the prop
   * @param ontology the ontology
   */
  void logObjectProperty(OWLObjectProperty prop, OWLOntology ontology) {
    Logger.getLogger(getClass()).info("  object property = " + prop);
    Logger.getLogger(getClass()).info("    IRI = " + prop.getIRI());
    Logger.getLogger(getClass()).info(
        "    inverse property = " + prop.getInverseProperty());
    Logger.getLogger(getClass()).info(
        "    named property = " + prop.getNamedProperty());
    Logger.getLogger(getClass()).info("    signature = " + prop.getSignature());
    Logger.getLogger(getClass()).info(
        "    simplified = " + prop.getSimplified());
    Logger.getLogger(getClass()).info("    anonymous = " + prop.isAnonymous());
    Logger.getLogger(getClass()).info("    builtIn = " + prop.isBuiltIn());
    Logger.getLogger(getClass()).info(
        "    entity type = " + prop.getEntityType());

    Logger.getLogger(getClass()).info(
        "    annotation properties in signature = "
            + prop.getAnnotationPropertiesInSignature().size());
    for (OWLAnnotationProperty aprop : prop
        .getAnnotationPropertiesInSignature()) {
      Logger.getLogger(getClass()).info("      annotation = " + aprop);
    }

    Logger.getLogger(getClass()).info(
        "    classes in signature = " + prop.getClassesInSignature().size());
    for (OWLClass owlClass : prop.getClassesInSignature()) {
      Logger.getLogger(getClass()).info("      class = " + owlClass);
    }

    Logger.getLogger(getClass()).info(
        "    data properties in signature = "
            + prop.getDataPropertiesInSignature().size());
    for (OWLDataProperty dprop : prop.getDataPropertiesInSignature()) {
      Logger.getLogger(getClass()).info("      data property = " + dprop);
    }

    Logger.getLogger(getClass())
        .info(
            "    datatypes in signature = "
                + prop.getDatatypesInSignature().size());
    for (OWLDatatype dtype : prop.getDatatypesInSignature()) {
      Logger.getLogger(getClass()).info("      datatype = " + dtype);
    }

    Logger.getLogger(getClass()).info(
        "    nested class expressions = "
            + prop.getNestedClassExpressions().size());
    for (OWLClassExpression expr : prop.getNestedClassExpressions()) {
      Logger.getLogger(getClass()).info("      expr = " + expr);
    }

    Logger.getLogger(getClass()).info(
        "    object properties in signature = "
            + prop.getObjectPropertiesInSignature().size());
    for (OWLObjectProperty oprop : prop.getObjectPropertiesInSignature()) {
      Logger.getLogger(getClass()).info("      property = " + oprop);
    }

    // loaded from ontology

    Logger.getLogger(getClass()).info(
        "    annotation assertion axioms = "
            + ontology.getAnnotationAssertionAxioms(prop.getIRI()));
    for (OWLAnnotationAssertionAxiom axiom : ontology
        .getAnnotationAssertionAxioms(prop.getIRI())) {
      Logger.getLogger(getClass()).info("      axiom = " + axiom);
    }

    Logger.getLogger(getClass()).info(
        "    sub properties for sub property = "
            + ontology.getObjectSubPropertyAxiomsForSubProperty(prop).size());
    for (OWLSubObjectPropertyOfAxiom axiom : ontology
        .getObjectSubPropertyAxiomsForSubProperty(prop)) {
      Logger.getLogger(getClass()).info("      axiom = " + axiom);
    }

    Logger.getLogger(getClass()).info(
        "    sub properties for super property = "
            + ontology.getObjectSubPropertyAxiomsForSuperProperty(prop).size());
    for (OWLSubObjectPropertyOfAxiom axiom : ontology
        .getObjectSubPropertyAxiomsForSuperProperty(prop)) {
      Logger.getLogger(getClass()).info("      axiom = " + axiom);
    }

  }

  /**
   * Load owl class.
   *
   * @param owlClass the owl class
   * @param ontology the ontology
   * @return the concept
   * @throws Exception the exception
   */
  Concept loadOwlClass(OWLClass owlClass, OWLOntology ontology)
    throws Exception {
    logOwlClass(owlClass, ontology);

    final Concept concept = new ConceptJpa();
    concept.setTimestamp(currentDate);
    concept.setObsolete(false);
    concept.setSuppressible(false);
    concept.setFullyDefined(false);
    concept.setName(initPrefName);
    concept.setLastModified(currentDate);
    concept.setLastModifiedBy(loader);
    concept.setPublished(true);
    concept.setPublishable(true);
    concept.setTerminologyId(getTerminologyId(owlClass.getIRI()));
    concept.setTerminology(terminology);
    concept.setVersion(version);
    concept.setWorkflowStatus(published);

    // Currently only OWL EL 2 is supported - no union
    concept.setUsesRelationshipUnion(false);
    concept.setUsesRelationshipIntersection(true);

    // first determine if this is an "equivalent class" situation
    if (ontology.getEquivalentClassesAxioms(owlClass).size() > 0) {

      //

      concept.setFullyDefined(true);
      concept.setAnonymous(false);

    } else {

      // Create concept
      concept.setAnonymous(owlClass.isAnonymous());

      // Lookup and create atoms (from annotations)
      final Set<Atom> atoms = getAtoms(owlClass, ontology);
      boolean flag = true;
      for (Atom atom : atoms) {
        Logger.getLogger(getClass()).info("  add atom = " + atom);
        addAtom(atom);
        // Use first RDFS label as the preferred name
        if (flag && isPreferredType(atom.getTermType())) {
          concept.setName(atom.getName());
          flag = false;
        }
        concept.addAtom(atom);
      }

      // Lookup and create attributes (from annotations)
      final Set<Attribute> attributes = getAttributes(owlClass, ontology);
      for (Attribute attribute : attributes) {
        Logger.getLogger(getClass()).info("  add attribute = " + attribute);
        addAttribute(attribute, concept);
        concept.addAttribute(attribute);
      }

      // TODO: relationships (may call back to this method)

    }

    Logger.getLogger(getClass()).info("  add concept  = " + concept);
    addConcept(concept);
    idMap.put(concept.getTerminologyId(), concept.getId());
    return concept;
  }

  /**
   * Log owl class.
   *
   * @param owlClass the owl class
   * @param ontology the ontology
   * @throws Exception the exception
   */
  void logOwlClass(OWLClass owlClass, OWLOntology ontology) throws Exception {
    Logger.getLogger(getClass()).info("  class = " + owlClass);
    Logger.getLogger(getClass()).info("    IRI = " + owlClass.getIRI());

    Logger.getLogger(getClass()).debug(
        "    signature = " + owlClass.getSignature());

    Logger.getLogger(getClass()).debug(
        "    class expression type = " + owlClass.getClassExpressionType());
    Logger.getLogger(getClass()).debug(
        "    entity type = " + owlClass.getEntityType());
    Logger.getLogger(getClass()).debug(
        "    anonymous = " + owlClass.isAnonymous());
    Logger.getLogger(getClass()).debug(
        "    class expression type = " + owlClass.getClassExpressionType());

    // Things connected to the class

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

    // things connected to ontology

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
