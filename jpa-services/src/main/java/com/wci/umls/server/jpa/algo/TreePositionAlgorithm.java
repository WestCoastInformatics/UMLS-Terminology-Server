/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.CancelException;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.CodeTreePositionJpa;
import com.wci.umls.server.jpa.content.ConceptTreePositionJpa;
import com.wci.umls.server.jpa.content.DescriptorTreePositionJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.jpa.meta.SemanticTypeJpa;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.CodeTreePosition;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptTreePosition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.DescriptorTreePosition;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.content.TreePosition;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.RootService;

/**
 * Implementation of an algorithm to compute tree positions using the
 * {@link ContentService}. Semantic type computation is based on top-level tree
 * positions, so the algorithm for computing semantic types is also included
 * here.
 */
public class TreePositionAlgorithm extends AbstractTerminologyAlgorithm {

  /** The id type. */
  private IdType idType;

  /** The cycle tolerant. */
  private boolean cycleTolerant;

  /** The compute semantic types. */
  private boolean computeSemanticTypes;

  /** The object ct. */
  private static int objectCt = 0;

  /**
   * Instantiates an empty {@link TreePositionAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public TreePositionAlgorithm() throws Exception {
    super();
  }

  /**
   * Returns the id type.
   *
   * @return the id type
   */
  public IdType getIdType() {
    return idType;
  }

  /**
   * Sets the id type.
   *
   * @param idType the id type
   */
  public void setIdType(IdType idType) {
    if (idType != IdType.CONCEPT && idType != IdType.DESCRIPTOR
        && idType != IdType.CODE) {
      throw new IllegalArgumentException(
          "Only CONCEPT, DESCRIPTOR, and CODE types are allowed.");
    }
    this.idType = idType;
  }

  /**
   * Indicates whether or not cycle tolerant is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isCycleTolerant() {
    return cycleTolerant;
  }

  /**
   * Sets the cycle tolerant.
   *
   * @param cycleTolerant the cycle tolerant
   */
  public void setCycleTolerant(boolean cycleTolerant) {
    this.cycleTolerant = cycleTolerant;
  }

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  @Override
  public void compute() throws Exception {

    // Get hierarchcial rels
    logInfo("Compute tree positions");
    logInfo("  terminology = " + getTerminology());
    logInfo("  version = " + getVersion());
    logInfo("  idType = " + idType);
    fireProgressEvent(0, "Starting...");

    // Get all relationships
    fireProgressEvent(1, "Initialize relationships");
    String tableName = "ConceptRelationshipJpa";
    String tableName2 = "ConceptJpa";
    if (idType == IdType.DESCRIPTOR) {
      tableName = "DescriptorRelationshipJpa";
      tableName2 = "DescriptorJpa";
    }
    if (idType == IdType.CODE) {
      tableName = "CodeRelationshipJpa";
      tableName2 = "CodeJpa";
    }
    @SuppressWarnings("unchecked")
    final List<Object[]> relationships =
        manager
            .createQuery(
                "select r.from.id, r.to.id from " + tableName + " r where "
                    + "version = :version and terminology = :terminology "
                    + "and hierarchical = 1 and inferred = 1 and obsolete = 0 "
                    + "and r.from in (select o from " + tableName2
                    + " o where obsolete = 0)")
            .setParameter("terminology", getTerminology())
            .setParameter("version", getVersion()).getResultList();

    int ct = 0;
    final Map<Long, Set<Long>> parChd = new HashMap<>();
    Map<Long, Set<Long>> chdPar = new HashMap<>();
    for (final Object[] r : relationships) {
      ct++;
      long fromId = Long.parseLong(r[0].toString());
      long toId = Long.parseLong(r[1].toString());

      if (!parChd.containsKey(toId)) {
        parChd.put(toId, new HashSet<Long>());
      }
      final Set<Long> children = parChd.get(toId);
      children.add(fromId);

      if (!chdPar.containsKey(fromId)) {
        chdPar.put(fromId, new HashSet<Long>());
      }
      final Set<Long> parents = chdPar.get(fromId);
      parents.add(toId);

      // Check cancel flag
      if (ct % RootService.logCt == 0 && isCancelled()) {
        rollback();
        throw new CancelException(
            "Label set marked parent computation cancelled");
      }
    }

    if (ct == 0) {
      fireProgressEvent(100, "Finished.");
      logInfo("    NO HIERARCHICAL RELATIONSHIPS");
      return;
    }

    else {
      logInfo("  concepts with descendants = " + parChd.size());
    }

    // Find roots
    fireProgressEvent(5, "Find roots");
    final Set<Long> rootIds = new HashSet<>();
    for (final Long par : parChd.keySet()) {
      // things with no children
      if (!chdPar.containsKey(par)) {
        rootIds.add(par);
      }
    }
    logInfo("  count = " + rootIds.size());
    chdPar = null;

    // Keep this after the read query above, in case there are no rels
    setTransactionPerOperation(false);
    beginTransaction();

    objectCt = 0;
    fireProgressEvent(10, "Compute tree positions for roots");
    int i = 0;
    final Map<Long, String> idValueMap = new HashMap<>();
    final Date startDate = new Date();
    for (final Long rootId : rootIds) {
      i++;

      // Check cancel flag
      if (isCancelled()) {
        rollback();
        throw new CancelException("Tree position computation cancelled.");
      }

      fireProgressEvent((int) (10 + (i * 85.0 / rootIds.size())),
          "Compute tree positions and semantic types for root " + rootId);

      final ValidationResult result = new ValidationResultJpa();
      final Map<Long, Set<Long>> semanticTypeMap =
          computeSemanticTypes ? new HashMap<>() : null;
      computeTreePositions(rootId, "", parChd, result, startDate,
          semanticTypeMap, rootIds.size() > 1);
      if (!result.isValid()) {
        logError("  validation result = " + result);
        throw new Exception("Validation failed");
      }
      // Commit
      commitClearBegin();

      // Check cancel flag
      if (isCancelled()) {
        rollback();
        throw new CancelException("Tree position computation cancelled.");
      }

      // Handle "semantic types"
      if (computeSemanticTypes) {
        objectCt = 0;
        for (final Long conceptId : semanticTypeMap.keySet()) {
          final Concept concept = getConcept(conceptId);
          for (Long styId : semanticTypeMap.get(conceptId)) {
            if (!idValueMap.containsKey(styId)) {
              final Concept styConcept = getConcept(styId);
              idValueMap.put(styConcept.getId(), styConcept.getName());
            }
            final SemanticTypeComponent sty = new SemanticTypeComponentJpa();
            sty.setTerminologyId("");
            sty.setLastModifiedBy("admin");
            sty.setObsolete(false);
            sty.setPublishable(false);
            sty.setPublished(false);
            sty.setSemanticType(idValueMap.get(styId));
            sty.setTerminology(getTerminology());
            sty.setVersion(getVersion());
            sty.setTimestamp(startDate);
            sty.setLastModified(startDate);
            addSemanticTypeComponent(sty, concept);
            concept.getSemanticTypes().add(sty);
          }
          updateConcept(concept);
          logAndCommit(++objectCt, logCt, commitCt);
          // Check cancel flag
          if (objectCt % RootService.logCt == 0 && isCancelled()) {
            rollback();
            throw new CancelException("Tree position computation cancelled.");
          }
        }

      }
    }

    commitClearBegin();

    fireProgressEvent(95, "Insert semantic type metadata");
    // Get all semantic type values from idValueMap
    // Add metadata and general metadata entries
    final StringBuilder sb = new StringBuilder();
    // For single root, add the extra layer
    String root = "";
    if (rootIds.size() == 1) {
      root = rootIds.iterator().next().toString() + "~";
    }
    // needed for dev UMLS because SNOMED has "multiple roots" that contain dup
    // strings

    final Set<String> seen = new HashSet<>();
    // Add STYs already existing
    for (final SemanticType sty : getSemanticTypes(getTerminology(),
        getVersion()).getObjects()) {
      seen.add(sty.getValue());
    }
    for (final Map.Entry<Long, String> entry : idValueMap.entrySet()) {
      final String semanticType = entry.getValue();
      sb.append((sb.length() == 0 ? "" : ",")).append(semanticType);
      if (seen.contains(semanticType)) {
        continue;
      }
      seen.add(semanticType);
      final SemanticType sty = new SemanticTypeJpa();
      sty.setAbbreviation(root + entry.getKey().toString());
      sty.setDefinition(semanticType);
      sty.setExample("");
      sty.setExpandedForm(semanticType);
      sty.setNonHuman(false);
      sty.setTerminology(getTerminology());
      sty.setVersion(getVersion());
      sty.setTreeNumber("");
      sty.setTypeId("");
      sty.setUsageNote("");
      sty.setValue(semanticType);
      sty.setTimestamp(startDate);
      sty.setLastModified(startDate);
      sty.setLastModifiedBy("admin");
      sty.setPublished(false);
      sty.setPublishable(false);
      logInfo("    add semantic type - " + sty);
      addSemanticType(sty);
    }

    fireProgressEvent(100, "Finished.");
    commit();
    clear();
  }

  /**
   * Compute tree positions.
   *
   * @param id the id
   * @param ancestorPath the ancestor path
   * @param parChd the par chd
   * @param validationResult the validation result
   * @param startDate the start date
   * @param semanticTypeMap the semantic type map
   * @param multipleRoots the multiple roots
   * @return the sets the
   * @throws Exception the exception
   */
  public Set<Long> computeTreePositions(Long id, String ancestorPath,
    Map<Long, Set<Long>> parChd, ValidationResult validationResult,
    Date startDate, Map<Long, Set<Long>> semanticTypeMap, boolean multipleRoots)
    throws Exception {

    final Set<Long> descConceptIds = new HashSet<>();

    // Check for cycles
    final Set<String> ancestors = new HashSet<>();
    for (String ancestor : ancestorPath.split("~")) {
      ancestors.add(ancestor);
    }
    if (ancestors.contains(id.toString())) {

      if (cycleTolerant) {
        return descConceptIds;
      } else {
        // add error to validation result
        validationResult.getErrors().add("Cycle detected for concept " + id
            + ", ancestor path is " + ancestorPath);
      }

      // return empty set of descendants to truncate calculation on this path
      return descConceptIds;
    }

    // Instantiate the tree position
    TreePosition<? extends ComponentHasAttributesAndName> tp = null;
    if (idType == IdType.CONCEPT) {
      final ConceptTreePosition ctp = new ConceptTreePositionJpa();
      final Concept concept = getConcept(id);
      ctp.setNode(concept);
      tp = ctp;
    } else if (idType == IdType.DESCRIPTOR) {
      final DescriptorTreePosition dtp = new DescriptorTreePositionJpa();
      final Descriptor descriptor = getDescriptor(id);
      dtp.setNode(descriptor);
      tp = dtp;
    } else if (idType == IdType.CODE) {
      final CodeTreePosition ctp = new CodeTreePositionJpa();
      final Code code = getCode(id);
      ctp.setNode(code);
      tp = ctp;
    } else {
      throw new Exception("Unsupported id type: " + idType);
    }
    tp.setTimestamp(startDate);
    tp.setLastModified(startDate);
    tp.setLastModifiedBy("admin");
    tp.setObsolete(false);
    tp.setSuppressible(false);
    tp.setPublishable(true);
    tp.setPublished(false);
    tp.setAncestorPath(ancestorPath);
    tp.setTerminology(getTerminology());
    tp.setVersion(getVersion());
    // No ids if computing - only if loading
    tp.setTerminologyId("");

    // persist the tree position
    addTreePosition(tp);

    // If semantic tags are to be computed, determine the "type id" and the node
    // id, only do this for CONCEPT
    if (computeSemanticTypes && idType == IdType.CONCEPT
        && !ancestorPath.isEmpty()) {
      final String[] tokens = FieldedStringTokenizer.split(ancestorPath, "~");
      // if single root, only process where ancestorPath has a ~
      if (multipleRoots || tokens.length > 1) {
        if (!semanticTypeMap.containsKey(tp.getNode().getId())) {
          semanticTypeMap.put(tp.getNode().getId(), new HashSet<Long>());
        }
        final Set<Long> types = semanticTypeMap.get(tp.getNode().getId());
        types.add(Long.valueOf(tokens[(multipleRoots ? 0 : 1)]));
      }
    }

    // construct the ancestor path terminating at this concept
    final String conceptPath =
        (ancestorPath.equals("") ? id.toString() : ancestorPath + "~" + id);

    // Gather descendants if this is not a leaf node
    if (parChd.containsKey(id)) {

      descConceptIds.addAll(parChd.get(id));

      // iterate over the child terminology ids
      // this iteration is entirely local and depends on no managed
      // objects
      for (Long childConceptId : parChd.get(id)) {

        // call helper function on child concept
        // add the results to the local descendant set
        final Set<Long> desc =
            computeTreePositions(childConceptId, conceptPath, parChd,
                validationResult, startDate, semanticTypeMap, multipleRoots);
        descConceptIds.addAll(desc);
      }
    }

    // set the children count
    tp.setChildCt(parChd.containsKey(id) ? parChd.get(id).size() : 0);

    // set the descendant count
    tp.setDescendantCt(descConceptIds.size());

    // In case manager was cleared here, get it back onto changed list
    manager.merge(tp);

    // check for cancel request
    if (isCancelled()) {
      rollback();
      throw new CancelException("Tree Position computation cancelled");
    }

    // Log and commit
    logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);

    // Check that this concept does not reference itself as a child
    if (descConceptIds.contains(id)) {

      // add error to validation result
      validationResult.getErrors().add("Concept " + id + " claims itself as a child");

      // remove this terminology id to prevent infinite loop
      descConceptIds.remove(id);
    }

    // return the descendant concept set
    // note that the local child and descendant set will be garbage
    // collected
    return descConceptIds;

  }

  /**
   * Reset.
   *
   * @throws Exception the exception
   */
  @Override
  public void reset() throws Exception {
    clearTreePositions(getTerminology(), getVersion());
  }

  /**
   * Indicates whether or not semantic type flag is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isComputeSemanticTypes() {
    return computeSemanticTypes;
  }

  /**
   * Sets the semantic type flag.
   *
   * @param flag the compute semantic type
   */
  public void setComputeSemanticType(boolean flag) {
    this.computeSemanticTypes = flag;
  }

}
