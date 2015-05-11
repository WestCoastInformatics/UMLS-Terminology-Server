/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.CodeTreePositionJpa;
import com.wci.umls.server.jpa.content.ConceptTreePositionJpa;
import com.wci.umls.server.jpa.content.DescriptorTreePositionJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.MetadataServiceJpa;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Relationship;
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
  private String terminologyVersion;

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
   * @param terminologyVersion the terminology version
   */
  public void setTerminologyVersion(String terminologyVersion) {
    this.terminologyVersion = terminologyVersion;
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

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.jpa.algo.Algorithm#compute()
   */
  @Override
  public void compute() throws Exception {

    // Get hierarchcial rels
    Logger.getLogger(getClass())
        .info(
            "  Get hierarchical rel for " + terminology + ", "
                + terminologyVersion);
    MetadataService service = new MetadataServiceJpa();
    if (service
        .getHierarchicalRelationshipTypes(terminology, terminologyVersion)
        .getObjects().size() == 0) {
      fireProgressEvent(100, "NO hierarchical rels, exiting...");
      Logger.getLogger(getClass()).info("  NO hierarchical rels, exiting...");
      return;
    }
    String chdRel =
        service
            .getHierarchicalRelationshipTypes(terminology, terminologyVersion)
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
    List<Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>> relationships =
        manager
            .createQuery(
                "select r from "
                    + tableName
                    + " r where "
                    + "terminologyVersion = :terminologyVersion and terminology = :terminology "
                    + "and relationshipType = :relationshipType and obsolete = 0 "
                    + "and r.from in (select o from " + tableName2
                    + " o where obsolete = 0)").setParameter("relationshipType", chdRel)
            .setParameter("terminology", terminology)
            .setParameter("terminologyVersion", terminologyVersion)
            .getResultList();

    int ct = 0;
    Map<Long, Set<Long>> parChd = new HashMap<>();
    Map<Long, Set<Long>> chdPar = new HashMap<>();
    for (Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> r : relationships) {
      ct++;
      final ComponentHasAttributes from = r.getFrom();
      final ComponentHasAttributes to = r.getTo();

      if (!parChd.containsKey(to.getId())) {
        parChd.put(to.getId(), new HashSet<Long>());
      }
      Set<Long> children = parChd.get(to.getId());
      children.add(from.getId());

      if (!chdPar.containsKey(from.getId())) {
        chdPar.put(from.getId(), new HashSet<Long>());
      }
      Set<Long> parents = chdPar.get(from.getId());
      parents.add(to.getId());
    }
    Logger.getLogger(this.getClass()).info("    count = " + ct);

    // Find roots
    Set<Long> rootIds = new HashSet<>();
    for (Long par : parChd.keySet()) {
      // things with no children
      if (!chdPar.containsKey(par)) {
        Logger.getLogger(this.getClass()).info("    rootId = " + par);
        rootIds.add(par);
      }
    }
    chdPar = null;

    Map<Long, String> names = new HashMap<>();
    for (Concept concept : getAllConcepts(terminology, terminologyVersion,
        Branch.ROOT).getObjects()) {
      names.put(concept.getId(),
          concept.getName());
    }

    setTransactionPerOperation(false);
    objectCt = 0;

    for (Long rootId : rootIds) {
      Logger.getLogger(getClass()).info(
          "  Compute tree positions for root " + rootId);
      ValidationResult result = new ValidationResultJpa();
      computeTreePositions(rootId, "", parChd, result, names);
      if (!result.isValid()) {
        Logger.getLogger(getClass()).error("  validation result = " + result);
        throw new Exception("Validation failed");
      }
    }
  }

  /**
   * Compute tree positions.
   *
   * @param id the id
   * @param ancestorPath the ancestor path
   * @param parChd the par chd
   * @param validationResult the validation result
   * @param names the names
   * @return the sets the
   * @throws Exception
   */
  public Set<Long> computeTreePositions(Long id, String ancestorPath,
    Map<Long, Set<Long>> parChd, ValidationResult validationResult,
    Map<Long, String> names) throws Exception {

    final Set<Long> descConceptIds = new HashSet<>();

    // extract the ancestor terminology ids
    Set<String> ancestors = new HashSet<>();
    for (String ancestor : ancestorPath.split("~")) {
      ancestors.add(ancestor);
    }

    // if ancestor path contains this terminology id, a child/ancestor cycle
    // exists
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

    // instantiate the tree position
    TreePosition tp = null;
    if (idType == IdType.CONCEPT) {
      tp = new ConceptTreePositionJpa();
    } else if (idType == IdType.DESCRIPTOR) {
      tp = new DescriptorTreePositionJpa();
    } else if (idType == IdType.CODE) {
      tp = new CodeTreePositionJpa();
    }

    tp.setAncestorPath(ancestorPath);
    tp.setTerminology(terminology);
    tp.setTerminologyVersion(terminologyVersion);
    // No ids if computing - only if loading
    tp.setTerminologyId("");
    tp.setName(names.get(id));

    // persist the tree position
    manager.persist(tp);

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
                validationResult, names);
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

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.jpa.algo.Algorithm#reset()
   */
  @Override
  public void reset() throws Exception {
    clearTreePositions(terminology, terminologyVersion);
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.jpa.services.helper.ProgressReporter#addProgressListener
   * (org.ihtsdo.otf.ts.jpa.services.helper.ProgressListener)
   */
  @Override
  public void addProgressListener(ProgressListener l) {
    listeners.add(l);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.jpa.services.helper.ProgressReporter#removeProgressListener
   * (org.ihtsdo.otf.ts.jpa.services.helper.ProgressListener)
   */
  @Override
  public void removeProgressListener(ProgressListener l) {
    listeners.remove(l);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.jpa.algo.Algorithm#cancel()
   */
  @Override
  public void cancel() {
    requestCancel = true;
  }

}
