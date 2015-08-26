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
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.CodeTreePositionJpa;
import com.wci.umls.server.jpa.content.ConceptTreePositionJpa;
import com.wci.umls.server.jpa.content.DescriptorTreePositionJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.MetadataServiceJpa;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.CodeTreePosition;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptTreePosition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.DescriptorTreePosition;
import com.wci.umls.server.model.content.TreePosition;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.MetadataService;
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

  /** The terminology version. */
  private String version;

  /** The id type. */
  private IdType idType;

  /** The cycle tolerant. */
  private boolean cycleTolerant;

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
   * Sets the terminology version.
   *
   * @param version the terminology version
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
    MetadataService service = new MetadataServiceJpa();
    if (service.getHierarchicalRelationshipTypes(terminology, version)
        .getObjects().size() == 0) {
      fireProgressEvent(100, "NO hierarchical rels, exiting...");
      Logger.getLogger(getClass()).info("  NO hierarchical rels, exiting...");
      return;
    }
    String chdRel =
        service.getHierarchicalRelationshipTypes(terminology, version)
            .getObjects().iterator().next().getAbbreviation();
    service.close();
    Logger.getLogger(getClass()).info("    hierarchical rel = " + chdRel);

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
                "select r.from.id, r.to.id from "
                    + tableName
                    + " r where "
                    + "version = :version and terminology = :terminology "
                    + "and relationshipType = :relationshipType and inferred = 1 and obsolete = 0 "
                    + "and r.from in (select o from " + tableName2
                    + " o where obsolete = 0)")
            .setParameter("relationshipType", chdRel)
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
    fireProgressEvent(10, "Find roots");
    int i = 0;
    for (Long rootId : rootIds) {
      i++;
      Logger.getLogger(getClass()).debug(
          "  Compute tree positions for root " + rootId);
      fireProgressEvent((int) (10 + (i * 90.0 / rootIds.size())),
          "Compute tree positions for root " + rootId);
      ValidationResult result = new ValidationResultJpa();
      computeTreePositions(rootId, "", parChd, result, new Date());
      if (!result.isValid()) {
        Logger.getLogger(getClass()).error("  validation result = " + result);
        throw new Exception("Validation failed");
      }
      // Commit
      commit();
      clear();
      beginTransaction();
    }
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
   * @return the sets the
   * @throws Exception the exception
   */
  public Set<Long> computeTreePositions(Long id, String ancestorPath,
    Map<Long, Set<Long>> parChd, ValidationResult validationResult,
    Date startDate) throws Exception {

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
    this.addTreePosition(tp);

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
                validationResult, startDate);
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

}
