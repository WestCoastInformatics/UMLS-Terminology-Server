package com.wci.umls.server.jpa.algo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.CancelException;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;

/**
 * Implementation of an algorithm to compute transitive closure using the
 * {@link ContentService}.
 */
public class TransitiveClosureAlgorithm extends ContentServiceJpa implements
    Algorithm {

  /** Listeners */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The request cancel flag. */
  boolean requestCancel = false;

  /** The terminology. */
  private String terminology;

  /** The terminology version. */
  private String terminologyVersion;

  /** The descendants map. */
  private Map<Long, Set<Long>> descendantsMap = new HashMap<>();

  /** The Constant commitCt. */
  private final static int commitCt = 2000;

  /**
   * Instantiates an empty {@link TransitiveClosureAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public TransitiveClosureAlgorithm() throws Exception {
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

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.jpa.algo.Algorithm#compute()
   */
  @Override
  public void compute() throws Exception {
    computeTransitiveClosure(terminology, terminologyVersion);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.jpa.algo.Algorithm#reset()
   */
  @Override
  public void reset() throws Exception {
    clearTransitiveClosure(terminology, terminologyVersion);
  }

  /**
   * Compute transitive closure.
   *
   * @param terminology the terminology
   * @param version the terminology version
   * @throws Exception the exception
   */
  private void computeTransitiveClosure(String terminology, String version)
    throws Exception {
    //
    // Check assumptions/prerequisites
    //
    Logger.getLogger(getClass()).info(
        "Start computing transitive closure ... " + new Date());
    fireProgressEvent(0, "Starting...");
// TODO:
    // // Disable transaction per operation
    // boolean currentTransactionStrategy = getTransactionPerOperation();
    // if (getTransactionPerOperation()) {
    // this.setTransactionPerOperation(false);
    // }
    //
    // //
    // // Initialize rels
    // // id effectiveTime active moduleId sourceId destinationId
    // relationshipGroup
    // // typeId characteristicTypeId modifierId
    // //
    // Logger.getLogger(getClass()).info(
    // "  Initialize relationships ... " + new Date());
    //
    // String inferredCharType =
    // TerminologyUtility.getInferredType(terminology, version);
    // Logger.getLogger(getClass()).info("    inferredType = " +
    // inferredCharType);
    //
    // String isaRel =
    // TerminologyUtility
    // .getHierarchicalIsaRels(terminology, version).iterator()
    // .next();
    // Logger.getLogger(getClass()).info("    isaRel = " + isaRel);
    //
    // // Skip non isa
    // // Skip non-inferred
    // fireProgressEvent(1, "Initialize relationships");
    // javax.persistence.Query query =
    // manager
    // .createQuery(
    // "select r from RelationshipJpa r where active = 1 "
    // + "and terminology = :terminology "
    // + "and terminologyVersion = :version "
    // +
    // "and typeId = :typeId and characteristicTypeId = :characteristicTypeId")
    // .setParameter("terminology", terminology)
    // .setParameter("version", version)
    // .setParameter("typeId", isaRel)
    // .setParameter("characteristicTypeId", inferredCharType);
    //
    // @SuppressWarnings("unchecked")
    // List<Relationship> rels = query.getResultList();
    // Map<Long, Set<Long>> parChd = new HashMap<>();
    // int ct = 0;
    // for (Relationship rel : rels) {
    // final Long chd = rel.getSourceConcept().getId();
    // final Long par = rel.getDestinationConcept().getId();
    // if (!parChd.containsKey(par)) {
    // parChd.put(par, new HashSet<Long>());
    // }
    // final Set<Long> children = parChd.get(par);
    // children.add(chd);
    // ct++;
    // if (requestCancel) {
    // rollback();
    // throw new CancelException("Transitive closure computation cancelled.");
    // }
    // }
    // Logger.getLogger(getClass()).info("    ct = " + ct);
    //
    // // Initialize concepts
    // fireProgressEvent(5, "Initialize concepts");
    // Logger.getLogger(getClass()).info(
    // "  Initialize concepts ... " + new Date());
    // Map<Long, Concept> conceptMap = new HashMap<>();
    // for (Concept concept : getAllConcepts(terminology, version)
    // .getObjects()) {
    // conceptMap.put(concept.getId(), concept);
    // }
    // // detatch concepts
    // manager.clear();
    // fireProgressEvent(8, "Start creating transitive closure relationships");
    //
    // //
    // // Create transitive closure rels
    // //
    // Logger.getLogger(getClass()).info(
    // "  Create transitive closure rels... " + new Date());
    // ct = 0;
    // // initialize descendant map
    // descendantsMap = new HashMap<>();
    // beginTransaction();
    // int progressMax = parChd.keySet().size();
    // int progress = 0;
    // for (Long code : parChd.keySet()) {
    // if (requestCancel) {
    // rollback();
    // throw new CancelException("Transitive closure computation cancelled.");
    // }
    //
    // // Scale the progress monitor from 8%-100%
    // ct++;
    // int ctProgress = (int) ((((ct * 100) / progressMax) * .92) + 8);
    // if (ctProgress > progress) {
    // progress = ctProgress;
    // fireProgressEvent((int) ((progress * .92) + 8),
    // "Creating transitive closure relationships");
    // }
    // List<Long> ancPath = new ArrayList<>();
    // ancPath.add(code);
    // final Set<Long> descs = getDescendants(code, parChd, ancPath);
    // for (final Long desc : descs) {
    // final TransitiveRelationship tr = new TransitiveRelationshipJpa();
    // tr.setSuperTypeConcept(conceptMap.get(code));
    // tr.setSubTypeConcept(conceptMap.get(desc));
    // tr.setActive(true);
    // tr.setLastModified(new Date());
    // tr.setLastModifiedBy("admin");
    // tr.setEffectiveTime(new Date());
    // tr.setLabel("");
    // tr.setModuleId("");
    // tr.setTerminologyId("");
    // tr.setTerminology(terminology);
    // tr.setTerminologyVersion(version);
    // addTransitiveRelationship(tr);
    // }
    // if (ct % commitCt == 0) {
    // Logger.getLogger(getClass()).info(
    // "      " + ct + " codes processed ..." + new Date());
    // commit();
    // clear();
    // beginTransaction();
    // }
    // }
    // // release memory
    // descendantsMap = new HashMap<>();
    // commit();
    // clear();

    Logger.getLogger(getClass()).info(
        "Finished computing transitive closure ... " + new Date());
    // set the transaction strategy based on status starting this routine
    // setTransactionPerOperation(currentTransactionStrategy);
    fireProgressEvent(100, "Finished...");
  }

  /**
   * Returns the descendants.
   *
   * @param par the par
   * @param parChd the par chd
   * @param ancPath the anc path
   * @return the descendants
   * @throws Exception the exception
   */
  private Set<Long> getDescendants(Long par, Map<Long, Set<Long>> parChd,
    List<Long> ancPath) throws Exception {
    Logger.getLogger(getClass()).debug(
        "  Get descendants for " + par + ", " + ancPath);

    if (requestCancel) {
      rollback();
      throw new CancelException("Transitive closure computation cancelled.");
    }

    Set<Long> descendants = new HashSet<>();
    // If cached, return them
    if (descendantsMap.containsKey(par)) {
      descendants = descendantsMap.get(par);
    }
    // Otherwise, compute them
    else {

      // Get Children of this node
      final Set<Long> children = parChd.get(par);

      // If this is a leaf node, bail
      if (children == null || children.isEmpty()) {
        return new HashSet<>(0);
      }
      // Iterate through children, mark as descendant and recursively call
      for (Long chd : children) {
        if (ancPath.contains(chd)) {
          throw new Exception("Cycle detected: " + chd + ", " + ancPath);
        }
        descendants.add(chd);
        ancPath.add(chd);
        descendants.addAll(getDescendants(chd, parChd, ancPath));
        ancPath.remove(chd);
      }
      Logger.getLogger(getClass()).debug("    descCt = " + descendants.size());

      descendantsMap.put(par, descendants);
    }

    return descendants;
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
