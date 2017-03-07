/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.release;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.persistence.Query;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
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
import com.wci.umls.server.jpa.content.MappingJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.jpa.content.StringClassJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.Mapping;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.model.workflow.WorkflowStatus;
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
    logInfo("Starting " + getName());
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
      final int indexOfPdqPt = projectPrecedences.getKeyValuePairs()
          .indexOf(new KeyValuePair("PDQ", "PT"));
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
    Map<String, String> params = getDefaultQueryParams(getProject());
    params.put("terminology", "PDQ");
    params.put("termType", "XM");
    String query = "SELECT DISTINCT a.id FROM AtomJpa a "
        + "WHERE a.terminology=:terminology "
        + "  AND a.termType=:termType AND publishable=true ";

    // Execute a query to get atom ids
    final List<Long> atomIds = executeSingleComponentIdQuery(query,
        QueryType.JPQL, params, AtomJpa.class, false);

    for (final Long id : atomIds) {

      // Need to update the concept as well otherwise the index does not get
      // updated. ASSUME there is a matching project concept
      final Concept concept = this
          .findConcepts(getProject().getTerminology(),
              getProject().getVersion(), Branch.ROOT, "atoms.id:" + id, null)
          .getObjects().get(0);
      for (final Atom atom : concept.getAtoms()) {
        if (atom.getId().equals(id)) {
          atom.setPublishable(false);
          updateAtom(atom);
        }
        // Turn the code off too
        final Code code = getCode(atom.getCodeId(), atom.getTerminology(),
            atom.getVersion(), Branch.ROOT);
        code.setPublishable(false);
        updateCode(code);
      }
      concept.setPublishable(false);
      updateConcept(concept);

      // make the project concept unpublishable - e.g. MatrixInitializer at end
      // of "pre production"
    }

    // 2b. Make any other PDQ map sets unpublishable

    query = "SELECT DISTINCT m FROM MapSetJpa m "
        + "WHERE m.terminology=:terminology and m.publishable=true";
    Query jpaQuery = getEntityManager().createQuery(query);
    jpaQuery.setParameter("terminology", "PDQ");

    @SuppressWarnings("unchecked")
    final List<MapSet> mapsets = jpaQuery.getResultList();
    for (final MapSet mapset : mapsets) {
      mapset.setPublishable(false);
      updateMapSet(mapset);
      // Q: should we cascade this publishable setting?? NO
    }

    //
    // 3. Create a map set for this map (see #7 for most of the fields).
    MapSet mapSet = new MapSetJpa();
    // * name = "PDQ_$version to NCI_$version Mappings"
    // * terminologyId = 100001
    // * not obsolete, not suppressible, READY_FOR_PUBLICATION
    // * publishable, not published.
    mapSet.setName("PDQ_" + pdq.getVersion() + " to NCI_" + nci.getVersion()
        + " Mappings");
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
    concept.setName("PDQ_" + pdq.getVersion() + " to NCI_" + nci.getVersion()
        + " Mappings");
    concept.setTerminology(getProject().getTerminology());
    concept.setVersion(getProject().getVersion());
    concept.setObsolete(false);
    concept.setSuppressible(false);
    concept.setPublishable(true);
    concept.setPublished(false);
    concept.setTerminologyId("");
    concept.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
    addConcept(concept);
    concept.setTerminologyId(concept.getId().toString());
    updateConcept(concept);
    logInfo("conceptId " + concept.getId());

    // 5. Create a PDQ/XM atom in the concept just created
    // * name = "PDQ_$version to NCI_$version Mappings"
    // * codeId = 100001
    // * not obsolete, not suppressible, READY_FOR_PUBLICATION
    // * publishable, not published.
    IdentifierAssignmentHandler handler =
        getIdentifierAssignmentHandler(getProject().getTerminology());
    Atom xmAtom = new AtomJpa();
    xmAtom.setName("PDQ_" + pdq.getVersion() + " to NCI_" + nci.getVersion()
        + " Mappings");
    xmAtom.setTerminology(pdq.getTerminology());
    xmAtom.setCodeId("100001");
    xmAtom.setVersion(pdq.getVersion());
    xmAtom.setObsolete(false);
    xmAtom.setSuppressible(false);
    xmAtom.setPublishable(true);
    xmAtom.setPublished(false);
    xmAtom.setConceptId("");
    xmAtom.setDescriptorId("");
    xmAtom.setLanguage("ENG");
    xmAtom.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
    final StringClass strClass = new StringClassJpa();
    strClass.setLanguage(xmAtom.getLanguage());
    strClass.setName(xmAtom.getName());
    xmAtom.setStringClassId(handler.getTerminologyId(strClass));
    final LexicalClass lexClass = new LexicalClassJpa();
    lexClass.setLanguage(xmAtom.getLanguage());
    lexClass.setNormalizedName(getNormalizedString(xmAtom.getName()));
    xmAtom.setLexicalClassId(handler.getTerminologyId(lexClass));
    xmAtom.setTermType("XM");
    xmAtom.setTerminologyId("");
    xmAtom.getAlternateTerminologyIds().put(getProject().getTerminology(),
        handler.getTerminologyId(xmAtom));

    addAtom(xmAtom);
    concept.getAtoms().add(xmAtom);
    updateConcept(concept);

    // 5b. Create a "code" for the PDQ/XM atom
    Code code = new CodeJpa();
    code.setName("name"); // TODO
    code.setTerminology(pdq.getTerminology());
    code.setVersion(pdq.getVersion());
    code.setTerminologyId("100001");
    code.setObsolete(false);
    code.setSuppressible(false);
    code.setPublishable(true);
    code.setPublished(false);
    code.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
    code.getAtoms().add(xmAtom);
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
    sty.setTerminologyId("");
    sty.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
    addSemanticTypeComponent(sty, concept);
    concept.getSemanticTypes().add(sty);
    updateConcept(concept);

    // 7. Add code attributes for those things shown below

    // MAPSETVERSION|PDQ|2016_07_31
    // FROMVSAB|PDQ|PDQ_2016_07_31 (appended)
    // TOVSAB|PDQ|NCI_2016_10E (appended)
    // MAPSETVSAB|PDQ|PDQ_2016_07_31 (appended)
    // MTH_MAPSETCOMPLEXITY|PDQ|N_TO_N
    // TORSAB|PDQ|NCI
    // MTH_MAPTOCOMPLEXITY|PDQ|SINGLE SCUI
    // MAPSETRSAB|PDQ|PDQ
    // MTH_MAPTOEXHAUSTIVE|PDQ|N
    // FROMRSAB|PDQ|PDQ
    // MTH_MAPFROMCOMPLEXITY|PDQ|SINGLE SDUI
    // MTH_MAPFROMEXHAUSTIVE|PDQ|N
    Map<String, String> codeAttributes = new HashMap<>();
    codeAttributes.put("MAPSETVERSION", pdq.getVersion());
    codeAttributes.put("FROMVSAB",
        pdq.getTerminology() + "_" + pdq.getVersion());
    codeAttributes.put("TOVSAB", nci.getTerminology() + "_" + nci.getVersion());
    codeAttributes.put("MAPSETVSAB",
        pdq.getTerminology() + "_" + pdq.getVersion());
    codeAttributes.put("MTH_MAPSETCOMPLEXITY", "N_TO_N");
    codeAttributes.put("TORSAB", nci.getTerminology());
    codeAttributes.put("MTH_MAPTOCOMPLEXITY", "SINGLE SCUI");
    codeAttributes.put("MAPSETRSAB", pdq.getTerminology());
    codeAttributes.put("MTH_MAPTOEXHAUSTIVE", "N");
    codeAttributes.put("FROMRSAB", pdq.getTerminology());
    codeAttributes.put("MTH_MAPFROMCOMPLEXITY", "SINGLE SDUI");
    codeAttributes.put("MTH_MAPFROMEXHAUSTIVE", "N");
    codeAttributes.put("MAPSETSID", code.getTerminologyId());

    for (String key : codeAttributes.keySet()) {
      Attribute attribute = new AttributeJpa();
      attribute.setName(key);
      attribute.setValue(codeAttributes.get(key));
      attribute.setTerminology(pdq.getTerminology());
      attribute.setVersion(pdq.getVersion());
      attribute.setObsolete(false);
      attribute.setSuppressible(false);
      attribute.setPublishable(true);
      attribute.setPublished(false);
      attribute.setTerminologyId("");
      attribute.getAlternateTerminologyIds().put(getProject().getTerminology(),
          handler.getTerminologyId(attribute, code));
      addAttribute(attribute, code);
      code.getAttributes().add(attribute);
      updateCode(code);
    }

    // 8. Create mappings
    // * query: join PDQ->NCI in the same project concept, both publishable
    query =
        "select distinct ca.descriptorId, cb.conceptId, ca.termType, cb.termType "
            + "from ConceptJpa a join a.atoms ca, ConceptJpa b join b.atoms cb "
            + "where a.terminology = :projectTerminology and b.terminology = :projectTerminology "
            + "and a.id = b.id "
            + "and ca.terminology = 'PDQ' and cb.terminology = 'NCI'";
    jpaQuery = getEntityManager().createQuery(query);
    jpaQuery.setParameter("projectTerminology", getProject().getTerminology());
    @SuppressWarnings("unchecked")
    List<Object[]> results = jpaQuery.getResultList();
    Set<String> descriptorIdConceptIdCache = new HashSet<>();
    // Iterate through each result
    int objectCt = 0;
    steps = results.size();
    for (Object[] resultArray : results) {
      // If the descriptorId/conceptId combination hasn't yet been seen, create
      // a mapping
      if (!descriptorIdConceptIdCache
          .contains(resultArray[0].toString() + resultArray[1].toString())) {
        Mapping m = new MappingJpa();
        String pdqTty = resultArray[2].toString();
        String nciTty = resultArray[3].toString();
        m.setAdditionalRelationshipType("");
        m.setAdvice("");
        m.setFromIdType(IdType.DESCRIPTOR);
        m.setFromName("");
        m.setFromTerminologyId(resultArray[0].toString());
        m.getAlternateTerminologyIds().put(
            getProject().getTerminology() + "-FROMID",
            resultArray[0].toString());
        m.setGroup("");
        m.setMapSet(mapSet);
        // * use mapRank=1 if term-types are PT->PT, PT->PSC, or PT->HT
        // * use mapRank=2 if term-types are different and from/to map doesn't
        if (pdqTty.equals("PT") && (nciTty.equals("PT") || nciTty.equals("PSC")
            || nciTty.equals("HT"))) {
          m.setRank("1");
        } else {
          m.setRank("2");
        }
        m.setRelationshipType("SY");
        m.setRule("");
        m.setObsolete(false);
        m.setSuppressible(false);
        m.setPublishable(true);
        m.setPublished(false);
        m.setTerminology(pdq.getTerminology());
        m.setVersion(pdq.getVersion());
        m.setToIdType(IdType.CONCEPT);
        m.setToName("");
        m.setToTerminologyId(resultArray[1].toString());
        m.getAlternateTerminologyIds().put(
            getProject().getTerminology() + "-TOID", resultArray[1].toString());

        final Attribute att = new AttributeJpa();
        att.setName("XMAP");
        att.setPublishable(true);
        att.setTerminology(m.getTerminology());
        att.setVersion(m.getVersion());
        att.setTerminologyId("");
        att.setValue(m.getGroup() + "~" + m.getRank() + "~"
            + m.getAlternateTerminologyIds()
                .get(getProject().getTerminology() + "-FROMID")
            + "~" + m.getRelationshipType() + "~"
            + m.getAdditionalRelationshipType() + "~"
            + m.getAlternateTerminologyIds()
                .get(getProject().getTerminology() + "-TOID")
            + "~" + m.getRule() + "~" + mapSet.getMapType() + "~" + "~" + "~"
            + m.getTerminologyId() + "~" + m.getAdvice());
        m.getAlternateTerminologyIds().put(getProject().getTerminology(),
            handler.getTerminologyId(att, xmAtom));
        m.setTerminologyId("");

        addMapping(m);
        mapSet.getMappings().add(m);

        // add to cache
        descriptorIdConceptIdCache
            .add(resultArray[0].toString() + resultArray[1].toString());
      }
      updateProgress();
    }
    updateMapSet(mapSet);

    fireProgressEvent(100, "Finished - 100%");
    logInfo("  mapping count = " + objectCt);
    logInfo("Finished " + getName());
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    logInfo("Starting RESET " + getName());
    // n/a
    logInfo("Finished RESET " + getName());
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
          "CREATE NCI PDQ map progress: " + currentProgress + "%");
      previousProgress = currentProgress;
    }
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return ConfigUtility.getNameFromClass(getClass());
  }
}
