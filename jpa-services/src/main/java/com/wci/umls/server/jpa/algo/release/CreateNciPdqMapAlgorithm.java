/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.release;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.LexicalClassJpa;
import com.wci.umls.server.jpa.content.MapSetJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.jpa.content.StringClassJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;

/**
 * Algorithm for creating NCI-PDQ map.
 */
public class CreateNciPdqMapAlgorithm extends AbstractAlgorithm {

  /** The previous progress. */
  private int previousProgress;

  /** The steps. */
  private int steps;

  /** The steps completed. */
  private int stepsCompleted;

  /**
   * Instantiates an empty {@link CreateNciPdqMapAlgorithm}.
   *
   * @throws Exception the exception
   */
  public CreateNciPdqMapAlgorithm() throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting create NCI-PDQ map algorithm");
    this.setMolecularActionFlag(false);
    
    fireProgressEvent(0, "Starting");

    Terminology pdq = this.getTerminologyLatestVersion("PDQ");
    Terminology nci = this.getTerminologyLatestVersion("NCI");
    
    //
    // 1. Add PDQ/XM termgroup to precedence list (just above PDQ/PT) - only if
    // it doesn't already exist in the precedence list
    //
    final PrecedenceList list = getPrecedenceList(getProject().getTerminology(),
        getProject().getVersion());
    final KeyValuePairList precedences = list.getPrecedence();
    final KeyValuePair kvp = new KeyValuePair("PDQ", "XM");
    if (!precedences.contains(kvp)) {
      final int indexOfPdqPt =
          precedences.getKeyValuePairs().indexOf(new KeyValuePair("PDQ", "PT"));
      if (indexOfPdqPt == -1) {
        throw new Exception(
            "ERROR - PDQ/PT termgroup required in precedence list in order to insert PDQ/XM.");
      }
      precedences.getKeyValuePairs().add(indexOfPdqPt, kvp);
      updatePrecedenceList(list);
    }
    // also do this for the project precedence list 
    final PrecedenceList projectList = getProject().getPrecedenceList();
    final KeyValuePairList projectPrecedences = list.getPrecedence();
    if (!projectPrecedences.contains(kvp)) {
      final int indexOfPdqPt =
          projectPrecedences.getKeyValuePairs().indexOf(new KeyValuePair("PDQ", "PT"));
      if (indexOfPdqPt == -1) {
        throw new Exception(
            "ERROR - PDQ/PT termgroup required in project precedence list in order to insert PDQ/XM.");
      }
      projectPrecedences.getKeyValuePairs().add(indexOfPdqPt, kvp);
      updatePrecedenceList(projectList);
    }

    //
    // 2. Remove old PDQ/XM concept, atoms, attribute, stys, and old version
    // MapSets
    //

    // 2a. Find any PDQ/XM atoms that are publishable
    Map<String, String> params = new HashMap<>();

    params.put("terminology", "PDQ");
    params.put("termType", "XM");
    String query = "SELECT DISTINCT a.id FROM AtomJpa a "
        + "WHERE a.terminology=:terminology "
        + "  AND a.termType=:termType AND publishable=true ";

    // Execute a query to get atom ids
    final List<Long> atomIds = executeSingleComponentIdQuery(query,
        QueryType.JQL, params, AtomJpa.class);

    
    for (final Long id : atomIds) {
      final Atom atom = this.getAtom(id);
      atom.setPublishable(false);
      updateAtom(atom);

      // make the project concept unpublishable - e.g. MatrixInitializer at end
      // of "pre production"
    }

    // 2b. Make any other PDQ map sets unpublishable

    logInfo("Starting 2b");
    query = "SELECT DISTINCT m FROM MapSetJpa m "
        + "WHERE m.terminology=:terminology and m.publishable=true";
    final javax.persistence.Query jpaQuery =
        getEntityManager().createQuery(query);
    jpaQuery.setParameter("terminology", "PDQ");

    @SuppressWarnings("unchecked")
    final List<MapSet> mapsets = jpaQuery.getResultList();
    for (final MapSet mapset : mapsets) {
      mapset.setPublishable(false);
      updateMapSet(mapset);
      // TODO: should we cascade this publishable setting?? TBD
    }

    //
    // 3. Create a map set for this map (see #7 for most of the fields).
    MapSet mapSet = new MapSetJpa();
    // * name = "PDQ_$version to NCI_$version Mappings"
    // * terminologyId = 100001
    // * not obsolete, not suppressible, READY_FOR_PUBLICATION
    // * publishable, not published.
    mapSet.setName("PDQ_" + pdq.getVersion() + " to NCI_$version Mappings");
    mapSet.setTerminologyId("100001");
    mapSet.setObsolete(false);
    mapSet.setSuppressible(false);
    mapSet.setPublishable(true);
    mapSet.setPublished(false);
    mapSet.setFromComplexity("SINGLE SDUI");
    mapSet.setToComplexity("SINGLE SCUI");
    mapSet.setFromExhaustive("N");
    mapSet.setToExhaustive("N");
    mapSet.setFromTerminology(pdq.getTerminology());
    mapSet.setToTerminology(nci.getTerminology());
    mapSet.setFromVersion(pdq.getVersion());
    mapSet.setToVersion(nci.getVersion());
    mapSet.setTerminology(pdq.getTerminology());
    mapSet.setVersion(pdq.getVersion());
    mapSet.setComplexity("N_TO_N");
    // MAPSETVERSION|PDQ|2016_07_31
    // FROMVSAB|PDQ|PDQ_2016_07_31
    // TOVSAB|PDQ|NCI_2016_10E
    // MAPSETVSAB|PDQ|PDQ_2016_07_31
    // TORSAB|PDQ|NCI
    // MAPSETRSAB|PDQ|PDQ
    // FROMRSAB|PDQ|PDQ
    addMapSet(mapSet);

    // Algorithm (use molecular actions for id assignment).

    // 4. Create a new concept
    // * terminology=project.getTerminology(),version=project.getVersion
    // * name = "PDQ_$version to NCI_$version Mappings"
    // * not obsolete, not suppressible, READY_FOR_PUBLICATION
    // * publishable, not published.
    Concept concept = new ConceptJpa();
    concept.setName("PDQ_$version to NCI_$version Mappings");
    concept.setTerminology(getProject().getTerminology());
    concept.setVersion(getProject().getVersion());
    concept.setObsolete(false);
    concept.setSuppressible(false);
    concept.setPublishable(true);
    concept.setPublished(false);
    concept.setTerminologyId("");
    addConcept(concept);
    concept.setTerminologyId(concept.getId().toString());
    updateConcept(concept);

    // 5. Create a PDQ/XM atom in the concept just created
    // * name = "PDQ_$version to NCI_$version Mappings"
    // * codeId = 100001
    // * not obsolete, not suppressible, READY_FOR_PUBLICATION
    // * publishable, not published.
    IdentifierAssignmentHandler handler =
      getIdentifierAssignmentHandler(getProject().getTerminology());
    Atom atom = new AtomJpa();
    atom.setName("PDQ_$version to NCI_$version Mappings");
    atom.setTerminology(pdq.getTerminology());
    atom.setCodeId("100001");
    atom.setVersion(pdq.getVersion());
    atom.setObsolete(false);
    atom.setSuppressible(false);
    atom.setPublishable(true);
    atom.setPublished(false);
    atom.setConceptId("");
    atom.setDescriptorId(""); 
    final StringClass strClass = new StringClassJpa();
    strClass.setLanguage(atom.getLanguage());
    strClass.setName(atom.getName());
    atom.setStringClassId(handler.getTerminologyId(strClass));
    final LexicalClass lexClass = new LexicalClassJpa();
    lexClass.setLanguage(atom.getLanguage());
    lexClass.setNormalizedName(getNormalizedString(atom.getName()));
    atom.setLexicalClassId(handler.getTerminologyId(lexClass));
    atom.setTermType("XM"); 
    atom.setTerminologyId("");
    atom.getAlternateTerminologyIds().put(getProject().getTerminology(), handler.getTerminologyId(atom));
       
    addAtom(atom);
    concept.getAtoms().add(atom);
    updateConcept(concept);

    // 5b. Create a "code" for the PDQ/XM atom
    Code code = new CodeJpa();
    code.setTerminology(pdq.getTerminology());
    code.setVersion(pdq.getVersion());
    code.setTerminologyId("100001");
    code.setObsolete(false);
    code.setSuppressible(false);
    code.setPublishable(true);
    code.setPublished(false);
    code.getAtoms().add(atom);
    addCode(code);
    
    
    // 6. Add a "Intellectual Product" to the concept
    // * terminology=project.getTerminology(),version=project.getVersion
    // * not obsolete, not suppressible, READY_FOR_PUBLICATION
    // * publishable, not published.
    SemanticTypeComponent sty = new SemanticTypeComponentJpa();
    sty.setName("Intellectual Product");
    sty.setSemanticType("Intellectual Product");
    sty.setTerminology(getProject().getTerminology());
    sty.setVersion(getProject().getVersion());
    sty.setObsolete(false);
    sty.setSuppressible(false);
    sty.setPublishable(true);
    sty.setPublished(false);
    addSemanticTypeComponent(sty, concept);
    concept.getSemanticTypes().add(sty);
    updateConcept(concept);

    // 7. Add code attributes for those things shown below
    Attribute attribute = new AttributeJpa();
    attribute.setName("MAPSETVERSION");
    attribute.setValue(pdq.getVersion());
    attribute.setTerminology(pdq.getTerminology());
    attribute.setVersion(pdq.getVersion());
    attribute.setObsolete(false);
    attribute.setSuppressible(false);
    attribute.setPublishable(true);
    attribute.setPublished(false);
    addAttribute(attribute, code);
    code.getAttributes().add(attribute);
    
    attribute = new AttributeJpa();
    attribute.setName("MTH_MAPFROMEXHAUSTIVE");
    attribute.setValue("N");
    attribute.setTerminology(pdq.getTerminology());
    attribute.setVersion(pdq.getVersion());
    attribute.setObsolete(false);
    attribute.setSuppressible(false);
    attribute.setPublishable(true);
    attribute.setPublished(false);
    addAttribute(attribute, code);
    code.getAttributes().add(attribute);
    
    updateCode(code);
    
    // MAPSETVERSION|PDQ|2016_07_31
    // FROMVSAB|PDQ|PDQ_2016_07_31
    // TOVSAB|PDQ|NCI_2016_10E
    // MAPSETVSAB|PDQ|PDQ_2016_07_31
    // MTH_MAPSETCOMPLEXITY|PDQ|N_TO_N
    // TORSAB|PDQ|NCI
    // MTH_MAPTOCOMPLEXITY|PDQ|SINGLE SCUI
    // MAPSETRSAB|PDQ|PDQ
    // MTH_MAPTOEXHAUSTIVE|PDQ|N
    // FROMRSAB|PDQ|PDQ
    // MTH_MAPFROMCOMPLEXITY|PDQ|SINGLE SDUI
    // MTH_MAPFROMEXHAUSTIVE|PDQ|N       
    
    // 8. Create mappings
    // * query: join PDQ->NCI in the same project concept, both publishable
    
    query = "select distinct ca.descriptorId, cb.conceptId, ca.termType, cb.termType " 
      + "from ConceptJpa a join a.atoms ca, ConceptJpa b join b.atoms cb "
      + "where a.terminology = :projectTerminology and b.terminology = :projectTerminology " 
      + "and a.id == b.id "
      + "and ca.terminology = 'PDQ' and cb.terminology = 'NCI'";
    final javax.persistence.Query jpaQuery2 =
        getEntityManager().createQuery(query);
    jpaQuery2.setParameter("projectTerminology", getProject().getTerminology());
    /*final List<MapSet> mapsets = jpaQuery.getResultList();
    for (final MapSet mapset : mapsets) {*/
    // iterate over results
    // If the descriptorId/conceptId combination hasn't yet been seen, create a mapping
    // Mapping mapping = new MappingJpa();
    // mapping.set XXX
    // * use mapRank=1 if term-types are PT->PT, PT->PSC, or PT->HT
    // * use mapRank=2 if term-types are different and from/to map doesn't
    // servicde.addMapping(mapping);
    // mapset.getMappings().addMapping(mapping);
    //...
    // service.updateMapSet(mapset);

  }
 
  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        ""
    }, p);
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /**
   * Update progress.
   *
   * @throws Exception the exception
   */
  public void updateProgress() throws Exception {
    stepsCompleted++;
    int currentProgress = (int) ((100.0 * stepsCompleted / steps));
    if (currentProgress > previousProgress) {
      checkCancel();
      fireProgressEvent(currentProgress,
          "ASSIGN RELEASE IDS progress: " + currentProgress + "%");
      previousProgress = currentProgress;
    }
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return ConfigUtility.getNameFromClass(getClass());
  }
}
