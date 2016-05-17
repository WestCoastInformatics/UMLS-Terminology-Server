/**
 * Copyright 2016 West Coast Informatics, LLC
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

import com.wci.umls.server.helpers.CancelException;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.CodeTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.DescriptorTransitiveRelationshipJpa;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.CodeTransitiveRelationship;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptTransitiveRelationship;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.DescriptorTransitiveRelationship;
import com.wci.umls.server.model.content.TransitiveRelationship;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.RootService;

/**
 * Implementation of an algorithm to compute transitive closure using the
 * {@link ContentService}.
 */
public class TransitiveClosureAlgorithm extends AbstractTerminologyAlgorithm {

  /** The descendants map. */
  private Map<Long, Set<Long>> descendantsMap = new HashMap<>();

  /** The id type. */
  private IdType idType;

  /** The cycle tolerant. */
  private boolean cycleTolerant;

  /**
   * Instantiates an empty {@link TransitiveClosureAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public TransitiveClosureAlgorithm() throws Exception {
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

  /* see superclass */
  @Override
  public void compute() throws Exception {
    computeTransitiveClosure();
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    clearTransitiveClosure(getTerminology(), getVersion());
  }

  /**
   * Compute transitive closure.
   *
   * @throws Exception the exception
   */
  private void computeTransitiveClosure() throws Exception {
    logInfo("Compute transitive closure");
    logInfo("  terminology = " + getTerminology());
    logInfo("  version = " + getVersion());
    logInfo("  idType = " + idType);
    fireProgressEvent(0, "Starting...");

    final Date startDate = new Date();
    // Disable transaction per operation and start transaction
    setTransactionPerOperation(false);
    beginTransaction();

    // Initialize rels
    fireProgressEvent(1, "Initialize hierarchical relationships");
    String tableName = "ConceptRelationshipJpa";
    if (idType == IdType.DESCRIPTOR) {
      tableName = "DescriptorRelationshipJpa";
    }
    if (idType == IdType.CODE) {
      tableName = "CodeRelationshipJpa";
    }
    final javax.persistence.Query query =
        manager
            .createQuery(
                "select r.from.id, r.to.id from " + tableName
                    + " r where obsolete = 0 and inferred = 1 "
                    + "and terminology = :terminology "
                    + "and version = :version " + "and hierarchical = 1")
            .setParameter("terminology", getTerminology())
            .setParameter("version", getVersion());

    @SuppressWarnings("unchecked")
    final List<Object[]> rels = query.getResultList();
    final Map<Long, Set<Long>> parChd = new HashMap<>();
    final Set<Long> allNodes = new HashSet<>();
    int ct = 0;
    for (final Object[] rel : rels) {
      ct++;
      final Long chd = Long.parseLong(rel[0].toString());
      final Long par = Long.parseLong(rel[1].toString());
      allNodes.add(par);
      allNodes.add(chd);
      if (!parChd.containsKey(par)) {
        parChd.put(par, new HashSet<Long>());
      }
      final Set<Long> children = parChd.get(par);
      children.add(chd);
      // Check cancel flag
      if (ct % RootService.logCt == 0 && isCancelled()) {
        rollback();
        throw new CancelException("Transitive closure computation cancelled.");
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

    //
    // Create transitive closure rels
    //
    fireProgressEvent(8, "Create transitive closure relationships");

    // Create "self" entries
    ct = 0;
    for (final Long code : allNodes) {

      // Create a "self" transitive relationship
      TransitiveRelationship<? extends ComponentHasAttributes> tr = null;
      if (idType == IdType.CONCEPT) {
        final ConceptTransitiveRelationship ctr =
            new ConceptTransitiveRelationshipJpa();
        final Concept superType = new ConceptJpa();
        superType.setId(code);
        ctr.setSuperType(superType);
        ctr.setSubType(ctr.getSuperType());
        tr = ctr;
      } else if (idType == IdType.DESCRIPTOR) {
        final DescriptorTransitiveRelationship dtr =
            new DescriptorTransitiveRelationshipJpa();
        final Descriptor superType = new DescriptorJpa();
        superType.setId(code);
        dtr.setSuperType(superType);
        dtr.setSubType(dtr.getSuperType());
        tr = dtr;
      } else if (idType == IdType.CODE) {
        final CodeTransitiveRelationship ctr =
            new CodeTransitiveRelationshipJpa();
        final Code superType = new CodeJpa();
        superType.setId(code);
        ctr.setSuperType(superType);
        ctr.setSubType(ctr.getSuperType());
        tr = ctr;
      } else {
        throw new Exception("Unexpected id type " + idType);
      }

      tr.setObsolete(false);
      tr.setTimestamp(startDate);
      tr.setLastModified(startDate);
      tr.setLastModifiedBy("admin");
      tr.setPublishable(true);
      tr.setPublished(false);
      tr.setTerminologyId("");
      tr.setTerminology(getTerminology());
      tr.setVersion(getVersion());
      tr.setDepth(0);
      addTransitiveRelationship(tr);

    }
    // to free up memory
    allNodes.clear();

    // initialize descendant map
    descendantsMap = new HashMap<>();
    int progressMax = parChd.keySet().size();
    int progress = 0;
    for (final Long code : parChd.keySet()) {
      // Check cancel flag
      if (isCancelled()) {
        rollback();
        throw new CancelException("Transitive closure computation cancelled.");
      }

      // Scale the progress monitor from 8%-100%
      int ctProgress = (int) ((((ct * 100) / progressMax) * .92) + 8);
      if (ctProgress > progress) {
        progress = ctProgress;
        fireProgressEvent((int) ((progress * .92) + 8),
            "creating transitive closure relationships");
      }

      final List<Long> ancPath = new ArrayList<>();
      ancPath.add(code);
      final Set<Long> descs = getDescendants(code, parChd, ancPath);
      final Set<Long> children = parChd.get(code);
      for (final Long desc : descs) {
        TransitiveRelationship<? extends ComponentHasAttributes> tr = null;
        if (idType == IdType.CONCEPT) {
          final ConceptTransitiveRelationship ctr =
              new ConceptTransitiveRelationshipJpa();
          final Concept superType = new ConceptJpa();
          superType.setId(code);
          final Concept subType = new ConceptJpa();
          subType.setId(desc);
          ctr.setSuperType(superType);
          ctr.setSubType(subType);
          tr = ctr;
        } else if (idType == IdType.DESCRIPTOR) {
          final DescriptorTransitiveRelationship dtr =
              new DescriptorTransitiveRelationshipJpa();
          final Descriptor superType = new DescriptorJpa();
          superType.setId(code);
          final Descriptor subType = new DescriptorJpa();
          subType.setId(desc);
          dtr.setSuperType(superType);
          dtr.setSubType(subType);
          tr = dtr;
        } else if (idType == IdType.CODE) {
          final CodeTransitiveRelationship ctr =
              new CodeTransitiveRelationshipJpa();
          final Code superType = new CodeJpa();
          superType.setId(code);
          final Code subType = new CodeJpa();
          subType.setId(desc);
          ctr.setSuperType(superType);
          ctr.setSubType(subType);
          tr = ctr;
        } else {
          throw new Exception("Illegal id type: " + idType);
        }

        if (children.contains(desc)) {
          tr.setDepth(1);
        } else {
          tr.setDepth(2);
        }
        tr.setObsolete(false);
        tr.setTimestamp(startDate);
        tr.setLastModified(startDate);
        tr.setLastModifiedBy("admin");
        tr.setPublishable(true);
        tr.setPublished(false);
        tr.setTerminologyId("");
        tr.setTerminology(getTerminology());
        tr.setVersion(getVersion());
        addTransitiveRelationship(tr);
      }

      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
      // Check cancel flag
      if (ct % RootService.logCt == 0 && isCancelled()) {
        rollback();
        throw new CancelException("Transitive closure computation cancelled.");
      }

    }

    fireProgressEvent(100, "Finished...");

    // release memory
    descendantsMap = new HashMap<>();
    commit();
    clear();

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

    if (isCancelled()) {
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
          if (cycleTolerant) {
            return new HashSet<>(0);
          } else {
            throw new Exception("Cycle detected: " + chd + ", " + ancPath);
          }
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

}
