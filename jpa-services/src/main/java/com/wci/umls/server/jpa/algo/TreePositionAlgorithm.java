/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.CodeTreePositionJpa;
import com.wci.umls.server.jpa.content.ConceptTreePositionJpa;
import com.wci.umls.server.jpa.content.DescriptorTreePositionJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.jpa.meta.SemanticTypeJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
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
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;

/**
 * Implementation of an algorithm to compute transitive closure using the
 * {@link ContentService}.
 */
public class TreePositionAlgorithm extends ContentServiceJpa implements
    Algorithm {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The request cancel flag. */
  boolean requestCancel = false;

  /** The terminology. */
  private String terminology;

  /** The version. */
  private String version;

  /** The id type. */
  private IdType idType;

  /** The cycle tolerant. */
  private boolean cycleTolerant;

  /** The compute semantic types. */
  private boolean computeSemanticTypes;

  /** The Constant commitCt. */
  private final static int commitCt = 2000;

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
   * Sets the terminology.
   *
   * @param terminology the terminology
   */
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  /**
   * Sets the version.
   *
   * @param version the version
   */
  public void setVersion(String version) {
    this.version = version;
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
    Logger.getLogger(getClass()).info(
        "  Get hierarchical rel for " + terminology + ", " + version);
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
    List<Object[]> relationships =
        manager
            .createQuery(
                "select r.from.id, r.to.id from " + tableName + " r where "
                    + "version = :version and terminology = :terminology "
                    + "and hierarchical = 1 and inferred = 1 and obsolete = 0 "
                    + "and r.from in (select o from " + tableName2
                    + " o where obsolete = 0)")
            .setParameter("terminology", terminology)
            .setParameter("version", version).getResultList();

    int ct = 0;
    Map<Long, Set<Long>> parChd = new HashMap<>();
    Map<Long, Set<Long>> chdPar = new HashMap<>();
    for (Object[] r : relationships) {
      ct++;
      long fromId = Long.parseLong(r[0].toString());
      long toId = Long.parseLong(r[1].toString());

      if (!parChd.containsKey(toId)) {
        parChd.put(toId, new HashSet<Long>());
      }
      Set<Long> children = parChd.get(toId);
      children.add(fromId);

      if (!chdPar.containsKey(fromId)) {
        chdPar.put(fromId, new HashSet<Long>());
      }
      Set<Long> parents = chdPar.get(fromId);
      parents.add(toId);
    }
    Logger.getLogger(this.getClass()).info("    count = " + ct);

    if (ct == 0) {
      Logger.getLogger(this.getClass()).info("    NO TREE POSITIONS");
      fireProgressEvent(100, "Finished.");
      return;
    }
    // Find roots
    fireProgressEvent(5, "Find roots");
    Set<Long> rootIds = new HashSet<>();
    for (Long par : parChd.keySet()) {
      // things with no children
      if (!chdPar.containsKey(par)) {
        rootIds.add(par);
      }
    }
    chdPar = null;

    setTransactionPerOperation(false);
    beginTransaction();

    objectCt = 0;
    fireProgressEvent(10, "Compute tree positions for roots");
    int i = 0;
    final Map<Long, String> idValueMap = new HashMap<>();
    Date startDate = new Date();
    for (Long rootId : rootIds) {
      i++;
      Logger.getLogger(getClass()).debug(
          "  Compute tree positions for root " + rootId);
      fireProgressEvent((int) (10 + (i * 90.0 / rootIds.size())),
          "Compute tree positions for root " + rootId);
      ValidationResult result = new ValidationResultJpa();
      Map<Long, Set<Long>> semanticTypeMap = null;
      if (computeSemanticTypes) {
        semanticTypeMap = new HashMap<>();
      }
      computeTreePositions(rootId, "", parChd, result, startDate,
          semanticTypeMap, rootIds.size() > 1);
      if (!result.isValid()) {
        Logger.getLogger(getClass()).error("  validation result = " + result);
        throw new Exception("Validation failed");
      }
      // Commit
      commit();
      clear();
      beginTransaction();

      // Handle "semantic types"
      if (computeSemanticTypes) {
        objectCt = 0;
        Logger.getLogger(getClass()).info(
            "Compute semantic types based on tree");
        for (Long conceptId : semanticTypeMap.keySet()) {
          Concept concept = getConcept(conceptId);
          for (Long styId : semanticTypeMap.get(conceptId)) {
            if (!idValueMap.containsKey(styId)) {
              Concept styConcept = getConcept(styId);
              idValueMap.put(styConcept.getId(), styConcept.getName());
            }
            final SemanticTypeComponent sty = new SemanticTypeComponentJpa();
            sty.setTerminologyId("");
            sty.setLastModifiedBy("admin");
            sty.setObsolete(false);
            sty.setPublishable(false);
            sty.setPublished(false);
            sty.setSemanticType(idValueMap.get(styId));
            sty.setTerminology(terminology);
            sty.setVersion(version);
            sty.setTimestamp(startDate);
            sty.setLastModified(startDate);
            addSemanticTypeComponent(sty, concept);
            concept.addSemanticType(sty);
          }
          updateConcept(concept);
          logAndCommit(++objectCt, commitCt, commitCt);
        }

      }
    }

    commitClearBegin();

    // Get all semantic type values from idValueMap
    // Add metadata and general metadata entries
    StringBuilder sb = new StringBuilder();
    // For single root, add the extra layer
    String root = "";
    if (rootIds.size() == 1) {
      root = rootIds.iterator().next().toString() + "~";
    }
    // needed for dev UMLS because SNOMED has "multiple roots" that contain dup
    // strings
    
    Set<String> seen = new HashSet<>();
    // Add STYs already existing
    for (final SemanticType sty : getSemanticTypes(terminology,version).getObjects()){
      seen.add(sty.getValue());
    }
    for (Map.Entry<Long, String> entry : idValueMap.entrySet()) {
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
      sty.setTerminology(terminology);
      sty.setVersion(version);
      sty.setTreeNumber("");
      sty.setTypeId("");
      sty.setUsageNote("");
      sty.setValue(semanticType);
      sty.setTimestamp(startDate);
      sty.setLastModified(startDate);
      sty.setLastModifiedBy("admin");
      sty.setPublished(false);
      sty.setPublishable(false);
      Logger.getLogger(getClass()).info("    add semantic type - " + sty);
      addSemanticType(sty);
    }

    commitClearBegin();
    fireProgressEvent(100, "Finished.");
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

    Logger.getLogger(getClass()).debug(
        "    compute for " + id + ", " + ancestorPath);
    final Set<Long> descConceptIds = new HashSet<>();

    // Check for cycles
    Set<String> ancestors = new HashSet<>();
    for (String ancestor : ancestorPath.split("~")) {
      ancestors.add(ancestor);
    }
    if (ancestors.contains(id.toString())) {

      if (cycleTolerant) {
        return descConceptIds;
      } else {
        // add error to validation result
        validationResult.addError("Cycle detected for concept " + id
            + ", ancestor path is " + ancestorPath);
      }

      // return empty set of descendants to truncate calculation on this path
      return descConceptIds;
    }

    // Instantiate the tree position
    TreePosition<? extends ComponentHasAttributesAndName> tp = null;
    if (idType == IdType.CONCEPT) {
      ConceptTreePosition ctp = new ConceptTreePositionJpa();
      Concept concept = getConcept(id);
      ctp.setNode(concept);
      tp = ctp;
    } else if (idType == IdType.DESCRIPTOR) {
      DescriptorTreePosition dtp = new DescriptorTreePositionJpa();
      Descriptor descriptor = getDescriptor(id);
      dtp.setNode(descriptor);
      tp = dtp;
    } else if (idType == IdType.CODE) {
      CodeTreePosition ctp = new CodeTreePositionJpa();
      Code code = getCode(id);
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
    tp.setTerminology(terminology);
    tp.setVersion(version);
    // No ids if computing - only if loading
    tp.setTerminologyId("");

    // persist the tree position
    addTreePosition(tp);

    // If semantic tags are to be computed, determine the "type id" and the node
    // id, only do this for CONCEPT
    if (computeSemanticTypes && idType == IdType.CONCEPT
        && !ancestorPath.isEmpty()) {
      String[] tokens = FieldedStringTokenizer.split(ancestorPath, "~");
      // if single root, only process where ancestorPath has a ~
      if (multipleRoots || tokens.length > 1) {
        if (!semanticTypeMap.containsKey(tp.getNode().getId())) {
          semanticTypeMap.put(tp.getNode().getId(), new HashSet<Long>());
        }
        Set<Long> types = semanticTypeMap.get(tp.getNode().getId());
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

    // routinely commit and force clear the manager
    // any existing recursive threads are entirely dependent on local
    // variables
    if (++objectCt % commitCt == 0) {
      Logger.getLogger(getClass()).debug("    count = " + objectCt);
      commit();
      clear();
      beginTransaction();
    }

    // Check that this concept does not reference itself as a child
    if (descConceptIds.contains(id)) {

      // add error to validation result
      validationResult.addError("Concept " + id + " claims itself as a child");

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
    clearTreePositions(terminology, version);
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

  /**
   * Adds the progress listener.
   *
   * @param l the l
   */
  @Override
  public void addProgressListener(ProgressListener l) {
    listeners.add(l);
  }

  /**
   * Removes the progress listener.
   *
   * @param l the l
   */
  @Override
  public void removeProgressListener(ProgressListener l) {
    listeners.remove(l);
  }

  /**
   * Cancel.
   */
  @Override
  public void cancel() {
    requestCancel = true;
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
   * @param computeSemanticTypes the compute semantic type
   */
  public void setComputeSemanticType(boolean computeSemanticTypes) {
    this.computeSemanticTypes = computeSemanticTypes;
  }

}
