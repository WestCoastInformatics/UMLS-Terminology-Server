/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.algo.Classifier;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.services.HistoryServiceJpa;
import com.wci.umls.server.jpa.services.helper.TerminologyUtility;
import com.wci.umls.server.model.content.Component;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.GeneralConceptAxiom;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.HistoryService;
import com.wci.umls.server.services.RootService;

/**
 * Classifier based on OWL-API.
 */
public class OwlApiClassifier extends AbstractAlgorithm implements Classifier {
  /** The reasoner. */
  private OWLReasoner reasoner = null;

  /** The ontology. */
  private OWLOntology ontology = null;

  /** The id map. */
  private Map<String, Long> idMap = new HashMap<>();

  /** The anonymous expr map. */
  private Map<String, OWLClassExpression> exprMap = new HashMap<>();

  /** The parents visited. */
  private Set<String> parentsVisited = null;

  /** The project. */
  @SuppressWarnings("unused")
  private Project project;

  /** The pre classify run. */
  private boolean preClassifyRun = false;

  /**
   * Instantiates a {@link OwlApiClassifier} from the specified parameters.
   *
   * @param ontology the ontology
   * @throws Exception the exception
   */
  public OwlApiClassifier(OWLOntology ontology) throws Exception {
    reasoner = new Reasoner.ReasonerFactory().createReasoner(ontology);
    this.ontology = ontology;
  }

  /* see superclass */

  @Override
  public void reset() throws Exception {
    // clear any local data structures

  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
  }

  /* see superclass */
  @Override
  public void preClassify(String terminology, String version, Project project)
    throws Exception {
    if (preClassifyRun) {
      throw new Exception(
          "To re-run preClassify, you must instantiate a new classifier.");
    }
    preClassifyRun = true;
    setTerminology(terminology);
    setVersion(version);
    this.project = project;
  }

  /* see superclass */

  @Override
  public void addModifiedConcepts(Set<Concept> modifiedConcepts)
    throws Exception {
    throw new UnsupportedOperationException();
  }

  /* see superclass */

  @Override
  public boolean isConsistent() throws Exception {
    return reasoner.isConsistent();
  }

  /* see superclass */

  @Override
  public List<Concept> getUnsatisfiableConcepts() throws Exception {

    List<Concept> result = new ArrayList<>();

    // Check unsatisfiable classes
    Node<OWLClass> bottomNode = reasoner.getUnsatisfiableClasses();
    // This node contains owl:Nothing and all the classes that are
    // equivalent to owl:Nothing - i.e. the unsatisfiable classes. We just
    // want to print out the unsatisfiable classes excluding owl:Nothing,
    // and we can used a convenience method on the node to get these
    Set<OWLClass> unsatisfiable = bottomNode.getEntitiesMinusBottom();
    Logger.getLogger(getClass())
        .info("  List unsatisfiable classes - equal to owl:Nothing");
    if (!unsatisfiable.isEmpty()) {
      for (final OWLClass owlClass : unsatisfiable) {
        Logger.getLogger(getClass()).error("    class = " + owlClass);
        result.add(getConceptForOwlClass(owlClass, this));
      }
    }
    return result;
  }

  /* see superclass */
  @Override
  public Set<Set<Concept>> getEquivalentClasses() throws Exception {
    Set<Set<Concept>> equiv = new HashSet<>();
    final HistoryService service = new HistoryServiceJpa();
    try {
      for (final OWLClass owlClass : ontology.getClassesInSignature()) {
        // get equivalent
        final Set<Concept> group = new HashSet<>();
        for (final OWLClass chdClass : reasoner
            .getEquivalentClasses(owlClass)) {
          group.add(getConceptForOwlClass(chdClass, service));
        }
      }
    } catch (Exception e) {
      throw e;
    } finally {
      service.close();
    }
    return equiv;
  }

  /* see superclass */
  @Override
  public List<ConceptRelationship> getInferredHierarchicalRelationships()
    throws Exception {
    return null;
  }

  /**
   * Adds the inferred hierarchical relationships.
   *
   * @param service the service
   * @throws Exception the exception
   */
  public void addInferredHierarchicalRelationships(HistoryService service)
    throws Exception {
    int objectCt = 0;
    for (final OWLClass owlClass : ontology.getClassesInSignature()) {
      // get sub-classes
      for (final OWLClass chdClass : reasoner.getSubClasses(owlClass, true)
          .getFlattened()) {
        // Break if bottom node encountered
        if (chdClass.isBottomEntity()) {
          break;
        }
        final Concept chd = getConceptForOwlClass(chdClass, service);
        final Concept par = getConceptForOwlClass(owlClass, service);
        final ConceptRelationship rel = getSubClassOfRelationship(chd, par);
        Logger.getLogger(getClass()).debug("  add relationship - " + rel);
        service.addRelationship(rel);
        chd.getRelationships().add(rel);
        service.logAndCommit(++objectCt, RootService.logCt,
            RootService.commitCt);
      }
    }
  }

  /* see superclass */
  @Override
  public List<ConceptRelationship> getNewInferredHierarchicalRelationships() {

    throw new UnsupportedOperationException();
  }

  /* see superclass */
  @Override
  public List<ConceptRelationship> getOldInferredHierarchicalRelationships() {
    throw new UnsupportedOperationException();
  }

  /* see superclass */
  @Override
  public List<ConceptRelationship> getInferredRelationships() throws Exception {
    parentsVisited = new HashSet<>();

    return null;
  }

  /**
   * Adds the inferred relationships.
   *
   * @param service the service
   * @throws Exception the exception
   */
  public void addInferredRelationships(HistoryService service)
    throws Exception {
    parentsVisited = new HashSet<>();
    if (reasoner.getTopClassNode().getEntities().size() != 1) {
      throw new Exception("Unexpected equivalent classes at top node");
    }
    OWLClass topClass =
        reasoner.getTopClassNode().getEntities().iterator().next();

    // Prep GCI relationships

    //
    // Find all of the descendants of the LHS and create
    // relationships to the RHS rels
    //
    // Use reasoner to find all sub-classes of the LHS.
    Map<String, Set<ConceptRelationship>> gcaRels = new HashMap<>();
    for (final GeneralConceptAxiom axiom : service
        .getGeneralConceptAxioms(getTerminology(), getVersion(), Branch.ROOT)
        .getObjects()) {

      // Get subclasses of the LHS
      for (final OWLClass owlClass : reasoner
          .getSubClasses(
              exprMap.get(axiom.getLeftHandSide().getTerminologyId()), true)
          .getFlattened()) {
        final Concept concept = getConceptForOwlClass(owlClass, service);
        if (!gcaRels.containsKey(concept.getTerminologyId())) {
          gcaRels.put(concept.getTerminologyId(),
              new HashSet<ConceptRelationship>());
        }
        gcaRels.get(concept.getTerminologyId())
            .addAll(axiom.getRightHandSide().getRelationships());
      }

    }

    // infer non isa relationships at parent level to sub-class level
    inferRelationships(topClass, gcaRels, reasoner, ontology, service, 0);
  }

  /**
   * Returns the all superclass relationships.
   *
   * @param owlClass the owl class
   * @param gcaRels the gca rels
   * @param reasoner the reasoner
   * @param ontology the ontology
   * @param service the service
   * @param ct the ct
   * @return the all superclass relationships
   * @throws Exception the exception
   */
  private int inferRelationships(OWLClass owlClass,
    Map<String, Set<ConceptRelationship>> gcaRels, OWLReasoner reasoner,
    OWLOntology ontology, HistoryService service, int ct) throws Exception {

    int localCt = 0;

    // Mark this concept has having been visited
    parentsVisited.add(getTerminologyId(owlClass.getIRI()));

    // Get the child concept
    Logger.getLogger(getClass()).debug(
        "  Infer relationships - " + getTerminologyId(owlClass.getIRI()));

    // Get direct sub classes
    for (final OWLClass chdClass : reasoner.getSubClasses(owlClass, true)
        .getFlattened()) {

      // Skip bottom node
      if (chdClass.isBottomEntity()) {
        continue;
      }

      // Get the child concept
      Concept chdConcept = getConceptForOwlClass(chdClass, service);

      // Bail if not all parents have been looked at yet
      final Set<Concept> parents = new HashSet<>();
      boolean allParentsVisited = true;
      for (final OWLClass parClass : reasoner
          .getSuperClasses(exprMap.get(chdConcept.getTerminologyId()), true)
          .getFlattened()) {
        // Skip the top
        if (parClass.isTopEntity()) {
          continue;
        }
        final Concept par = getConceptForOwlClass(parClass, service);
        parents.add(par);
        if (!parentsVisited.contains(par.getTerminologyId())) {
          allParentsVisited = false;
        }
      }
      // Go past here only on a future iteration
      if (!allParentsVisited) {
        continue;
      }

      // Get and add inferred role relationships
      for (final ConceptRelationship rel : getRelationshipsToInfer(chdConcept,
          parents, gcaRels, reasoner, service)) {
        final ConceptRelationship rel2 = new ConceptRelationshipJpa(rel, true);
        rel2.setFrom(chdConcept);
        rel2.setInferred(true);
        rel2.setStated(false);
        rel2.setId(null);
        Logger.getLogger(getClass()).debug("  add relationship - " + rel);
        service.addRelationship(rel2);
        chdConcept.getRelationships().add(rel2);
        service.logAndCommit(++localCt + ct, RootService.logCt,
            RootService.commitCt);
      }

      // Handle children as parents
      localCt += inferRelationships(chdClass, gcaRels, reasoner, ontology,
          service, localCt);
    }

    return localCt;
  }

  /**
   * Returns the relationships to infer.
   *
   * @param chd the chd
   * @param parents the parents
   * @param gcaRels the gca rels
   * @param reasoner the reasoner
   * @param service the service
   * @return the relationships to infer
   * @throws Exception the exception
   */
  private Set<ConceptRelationship> getRelationshipsToInfer(Concept chd,
    Set<Concept> parents, Map<String, Set<ConceptRelationship>> gcaRels,
    OWLReasoner reasoner, HistoryService service) throws Exception {
    final Set<ConceptRelationship> inferredRels = new HashSet<>();

    //
    // Gather candidate relationships
    // based on parent inferred rels and child stated rels
    // Ignore obsolete or hierarchical relationships
    //
    Set<ConceptRelationship> candidateRels = new HashSet<>();
    for (final Concept par : parents) {
      for (final ConceptRelationship parRel : par.getRelationships()) {
        if (parRel.isInferred() && !parRel.isObsolete()
            && !parRel.isHierarchical()) {
          candidateRels.add(parRel);
        }
      }
    }
    for (final ConceptRelationship chdRel : chd.getRelationships()) {
      if (chdRel.isStated() && !chdRel.isObsolete()
          && !chdRel.isHierarchical()) {
        candidateRels.add(chdRel);
      }
    }

    // Add GCA rels
    if (gcaRels.containsKey(chd.getTerminologyId())) {
      candidateRels.addAll(gcaRels.get(chd.getTerminologyId()));
    }

    //
    // Iterate through candidate rels
    //
    for (final ConceptRelationship rel : candidateRels) {
      // Determine if this rel is a superclass rel of a more-specific one
      // if so, do not keep it
      boolean keep = true;
      for (final ConceptRelationship rel2 : candidateRels) {
        if (rel.equals(rel2)) {
          continue;
        }
        if (isSameOrSubType(rel.getAdditionalRelationshipType(),
            rel2.getAdditionalRelationshipType())) {
          Integer result =
              isEquivalentSubOrSuper(rel.getTo(), rel2.getTo(), reasoner);
          // Do not keep less specific rels (e.g. superclass)
          if (result != null && result > 0) {
            keep = false;
            break;
          }
          if (result != null && result == 0 && rel.getId() > rel2.getId()) {
            // If equivalent, keep the one with the lower id
            keep = false;
            break;
          }
        }
      }

      if (keep) {
        ConceptRelationship rel3 = new ConceptRelationshipJpa(rel, true);
        rel3.setInferred(true);
        rel3.setStated(false);
        rel3.setId(null);
        rel3.setFrom(chd);
        // If there are duplicates, the set should distinct them
        inferredRels.add(rel3);
      }

    }

    return inferredRels;
  }

  /**
   * Indicates whether or not the first concept is an ancestor of the second.
   *
   * @param anc the anc
   * @param desc the desc
   * @param reasoner the reasoner
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  private boolean isAncestor(OWLClass anc, OWLClass desc,
    OWLReasoner reasoner) {

    Set<OWLClass> parents = reasoner.getSuperClasses(desc, true).getFlattened();
    if (parents.contains(anc)) {
      return true;
    }
    boolean result = false;
    for (final OWLClass parClass : parents) {
      if (isAncestor(anc, parClass, reasoner)) {
        result = true;
        break;
      }
    }
    return result;
  }

  /**
   * Indicates whether or not equivalent sub or super is the case. Returns -1
   * for child is subclass of parent Returns 0 for equivalent Returns +1 for
   * child is superclass of parent Returns null if none of these conditions are
   * the case
   *
   * @param concept1 the chd
   * @param concept2 the par
   * @param reasoner the reasoner
   * @return <code>true</code> if so, <code>false</code> otherwise
   * @throws Exception the exception
   */
  private Integer isEquivalentSubOrSuper(Concept concept1, Concept concept2,
    OWLReasoner reasoner) throws Exception {

    // Equivalent if the terminology ids (which represent the expressions) are
    // equal
    if (concept1.getTerminologyId().equals(concept2.getTerminologyId())) {
      return 0;
    }

    // ASSUMPTION: concepts cannot be equivalent if one is anonymous and the
    // other is not. This may not be true, but generally anonymous concepts
    // do not include "isa" rels, so it would be necessarily true
    if (concept1.isAnonymous() != concept2.isAnonymous()) {
      return null;
    }

    // test assumption
    if (concept1.isAnonymous()) {
      for (final ConceptRelationship rel : concept1.getRelationships()) {
        if (rel.isHierarchical()) {
          throw new Exception(
              "Unexpected hierarchical relationship in anonymous concept");
        }
      }
    }
    if (concept2.isAnonymous()) {
      for (final ConceptRelationship rel : concept1.getRelationships()) {
        if (rel.isHierarchical()) {
          throw new Exception(
              "Unexpected hierarchical relationship in anonymous concept");
        }
      }
    }

    // If neither class is anonymous, see if they are either
    // equivalent or sub/super classes of each other
    if (!concept1.isAnonymous() && !concept2.isAnonymous()) {
      if (concept1.getTerminologyId().equals(concept2.getTerminologyId())) {
        // equivalent
        return 0;
      }

      if (isAncestor((OWLClass) exprMap.get(concept2.getTerminologyId()),
          (OWLClass) exprMap.get(concept1.getTerminologyId()), reasoner)) {
        // concept 1 is a subclass of concept 2
        return -1;
      }
      if (isAncestor((OWLClass) exprMap.get(concept1.getTerminologyId()),
          (OWLClass) exprMap.get(concept2.getTerminologyId()), reasoner)) {
        // concept 2 is a subclass of concept 1
        return 1;
      }
      return null;
    }

    // ASSUMPTION: all concepts are anonymous
    if (!concept2.isAnonymous()) {
      throw new Exception("Unexpected non anonymous concept: " + concept2);
    }
    if (!concept1.isAnonymous()) {
      throw new Exception("Unexpected non anonymous concept: " + concept2);
    }

    // Identify 8 things
    // a* whether concept1 has rels with targets that are equiv to concept2
    // b* whether concept2 has rels with targets that are equiv to concept1
    // c* whether concept1 has rels with targets that are sub to concept2
    // d* whether concept2 has rels with targets that are sub to concept1
    // e* whether concept1 has rels with targets that are super to concept2
    // f* whether concept2 has rels with targets that are super to concept1
    // g* whether concept1 has rels with types not matching concept 2
    // h* whether concept2 has rels with types not matching concept 1
    boolean a = false;
    boolean b = false;
    boolean c = false;
    boolean d = false;
    boolean e = false;
    boolean f = false;
    boolean g = false;
    boolean h = false;
    for (final ConceptRelationship rel : concept1.getRelationships()) {
      // ASSUMPTION: all rels are stated
      if (!rel.isStated()) {
        throw new Exception(
            "Unexpected non stated rel: " + rel + ", " + concept2);
      }
      boolean typeNotFound = true;
      for (final ConceptRelationship rel2 : concept2.getRelationships()) {
        // ASSUMPTION: all rels are stated
        if (!rel.isStated()) {
          throw new Exception(
              "Unexpected non stated rel: " + rel + ", " + concept2);
        }
        // compare targets
        Integer result =
            isEquivalentSubOrSuper(rel.getTo(), rel2.getTo(), reasoner);
        if (isSameOrSubType(rel.getAdditionalRelationshipType(),
            rel2.getAdditionalRelationshipType())) {
          typeNotFound = false;
          if (result != null && result == 0) {
            a = true;
            b = true;
            break;
          }
          if (result != null && result == -1) {
            c = true;
            f = true;
            break;
          }
          if (result != null && result == 1) {
            d = true;
            e = true;
            break;
          }
        }
      }
      if (typeNotFound) {
        g = true;
      }
    }
    // Compute h
    for (final ConceptRelationship rel : concept2.getRelationships()) {
      boolean typeNotFound = true;
      for (final ConceptRelationship rel2 : concept1.getRelationships()) {
        if (isSameOrSubType(rel.getAdditionalRelationshipType(),
            rel2.getAdditionalRelationshipType())) {
          typeNotFound = false;
          break;
        }
      }
      if (typeNotFound) {
        h = true;
        break;
      }
    }

    // concept1 is more specific than concept2 if
    if ((a || c) && !d && !e && !h) {
      return -1;
    }

    // concept 2 is more specific than concept1 if
    if ((b || d) && !c && !f && !g) {
      return 1;
    }

    // concept 1 is equiv to concept 2 if
    if (a && b && !c && !d && !e && !f && !g && !h) {
      return 0;
    }

    // otherwise null
    return null;

  }

  /**
   * Indicates whether or not the two types are the same or sub types of each
   * other.
   *
   * @param type1 the type1
   * @param type2 the type2
   * @return <code>true</code> if so, <code>false</code> otherwise
   * @throws Exception the exception
   */
  public boolean isSameOrSubType(String type1, String type2) throws Exception {
    if (type1.equals(type2)) {
      return true;
    }
    return TerminologyUtility
        .getDescendantTypes(type2, getTerminology(), getVersion())
        .contains(type1)
        || TerminologyUtility
            .getDescendantTypes(type1, getTerminology(), getVersion())
            .contains(type2);
  }

  /* see superclass */
  /**
   * Returns the new inferred relationships.
   *
   * @return the new inferred relationships
   */
  @Override
  public List<ConceptRelationship> getNewInferredRelationships() {
    throw new UnsupportedOperationException();
  }

  /* see superclass */
  /**
   * Returns the old inferred relationships.
   *
   * @return the old inferred relationships
   */
  @Override
  public List<ConceptRelationship> getOldInferredRelationships() {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the id map.
   *
   * @return the id map
   */
  public Map<String, Long> getIdMap() {
    return idMap;
  }

  /**
   * Sets the id map.
   *
   * @param idMap the id map
   */
  public void setIdMap(Map<String, Long> idMap) {
    this.idMap = idMap;
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
    rel.setTerminologyId("");
    rel.setFrom(fromConcept);
    rel.setTo(toConcept);
    rel.setAssertedDirection(true);
    rel.setGroup(null);
    rel.setInferred(true);
    rel.setStated(false);
    // This is an "isa" rel.
    rel.setRelationshipType("subClassOf");
    rel.setAdditionalRelationshipType("");
    rel.setHierarchical(true);

    return rel;
  }

  /**
   * Sets the common fields.
   *
   * @param component the common fields
   */
  private void setCommonFields(Component component) {
    component.setTerminology(getTerminology());
    component.setVersion(getVersion());
    component.setPublishable(true);
    component.setPublished(true);
    component.setObsolete(false);
    component.setSuppressible(false);
    component.setLastModified(new Date());
    component.setTimestamp(new Date());
    component.setLastModifiedBy("loader");
  }

  /**
   * Returns the concept for owl class.
   *
   * @param owlClass the owl class
   * @param service the service
   * @return the concept for owl class
   * @throws Exception the exception
   */
  private Concept getConceptForOwlClass(OWLClass owlClass,
    HistoryService service) throws Exception {
    return service.getConcept(idMap.get(getTerminologyId(owlClass.getIRI())));
  }

  /**
   * Returns the expr map.
   *
   * @return the expr map
   */
  public Map<String, OWLClassExpression> getExprMap() {
    return exprMap;
  }

  /**
   * Sets the expr map.
   *
   * @param exprMap the expr map
   */
  public void setExprMap(Map<String, OWLClassExpression> exprMap) {
    this.exprMap = exprMap;
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    return new ValidationResultJpa();
  }

  @Override
  public void setProperties(Properties p) throws Exception {
    // Unable to set via properteis
    throw new UnsupportedOperationException();

  }

}