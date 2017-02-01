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
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.MapSet;

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
    fireProgressEvent(0, "Starting");

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
    // TODO: also do this for the project precedence list (e.g.
    // getProject().getPrecedenceList()).

    //
    // 2. Remove old PDQ/XM concept, atoms, attribute, stys, and old version
    // MapSets
    //

    // 2a. Find any PDQ/XM atoms that are publishable
    final Map<String, String> params = new HashMap<>();
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
      // TODO: add a matrix initializer algorithm execution at the end of pre
      // production
    }

    // 2b. Make any other PDQ map sets unpublishable
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
    // * name = "PDQ_$version to NCI_$version Mappings"
    // * terminologyId = 100001
    // * not obsolete, not suppressible, READY_FOR_PUBLICATION
    // * publishable, not published.
    // service.addMapSet(...)

    // Algorithm (use molecular actions for id assignment).

    // 4. Create a new concept
    // * terminology=project.getTerminology(),version=project.getVersion
    // * name = "PDQ_$version to NCI_$version Mappings"
    // * not obsolete, not suppressible, READY_FOR_PUBLICATION
    // * publishable, not published.
    // service.addConcept(concept);
    // concept.setTerminolgyId(concept.getId().toString());
    // service.updateConcept(concept);

    // 5. Create a PDQ/XM atom in the concept just created
    // * name = "PDQ_$version to NCI_$version Mappings"
    // * codeId = 100001
    // * not obsolete, not suppressible, READY_FOR_PUBLICATION
    // * publishable, not published.
    // IdentifierAssignmentHandler handler =
    // getIdentifierAssignmentHandler(getProject().getTerminology());
    // Atom atom = new AtomJpa();
    // ..
    // atom.setTerminologyId(handler.getTerminologyId(atom));
    // ..
    // service.addAtom(atom);
    // concept.getAtoms().add(atom);
    // service.updateConcept(concept);

    // 5b. Create a "code" for the PDQ/XM atom
    // Code code = new CodeJpa();
    // terminology=PDQ, version = current version of PDQ
    // terminologyId=10001
    // ....
    // code.getAtoms().add(atom):
    // service.addCode(code);
    
    
    // 6. Add a "Intellectual Product" to the concept
    // * terminology=project.getTerminology(),version=project.getVersion
    // * not obsolete, not suppressible, READY_FOR_PUBLICATION
    // * publishable, not published.
    // service.addSemanticTypeComponent(sty);
    // concept.getSemanticTypes().add(sty);
    // service.updateConcept(concept);

    // 7. Add code attributes for those things shown belo
    // Attribute attribute = new AttributeJpa();
    // * see above for obsolete, suppressible, etc
    // * terminology=PDQ, version = current version of PDQ
    // service.addAttribute(attribute);
    // code.getAttributes.add(attribute);
    // ..
    // service.updateCode(code);
    
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
    // select distinct ca.descriptorId, cb.conceptId, ca.termType, cb.termType
    // from ConceptJpa a join a.atoms ca, ConceptJpa b join b.atoms cb    
    // where a.terminology = :projectTerminology and b.terminology = :projectTerminology
    //   and a.id == b.id
    //   and ca.terminology = 'PDQ' and cb.terminology = 'NCI';

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
