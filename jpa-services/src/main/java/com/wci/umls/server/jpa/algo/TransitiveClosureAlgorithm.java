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

import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.CancelException;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.CodeTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.DescriptorTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.MetadataServiceJpa;
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
import com.wci.umls.server.services.MetadataService;
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;

/**
 * Implementation of an algorithm to compute transitive closure using the
 * {@link ContentService}.
 */
public class TransitiveClosureAlgorithm extends ContentServiceJpa implements
    Algorithm {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The request cancel flag. */
  boolean requestCancel = false;

  /** The terminology. */
  private String terminology;

  /** The terminology version. */
  private String version;

  /** The descendants map. */
  private Map<Long, Set<Long>> descendantsMap = new HashMap<>();

  /** The id type. */
  private IdType idType;

  /** The cycle tolerant. */
  private boolean cycleTolerant;

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

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.jpa.algo.Algorithm#compute()
   */
  @Override
  public void compute() throws Exception {
    computeTransitiveClosure(terminology, version, idType);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.jpa.algo.Algorithm#reset()
   */
  @Override
  public void reset() throws Exception {
    clearTransitiveClosure(terminology, version);
  }

  /**
   * Compute transitive closure.
   *
   * @param terminology the terminology
   * @param version the terminology version
   * @param idType the id type
   * @throws Exception the exception
   */
  private void computeTransitiveClosure(String terminology, String version,
    IdType idType) throws Exception {
    final Date startDate = new Date();
    // Check assumptions/prerequisites
    Logger.getLogger(getClass()).info(
        "Start computing transitive closure - " + terminology);
    fireProgressEvent(0, "Starting...");

    // Disable transaction per operation
    setTransactionPerOperation(false);

    // Initialize rels
    Logger.getLogger(getClass()).info(
        "  Initialize relationships ... " + new Date());

    // Get hierarchcial rels
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

    fireProgressEvent(1, "Initialize relationships");
    String tableName = "ConceptRelationshipJpa";
    if (idType == IdType.DESCRIPTOR) {
      tableName = "DescriptorRelationshipJpa";
    }
    if (idType == IdType.CODE) {
      tableName = "CodeRelationshipJpa";
    }
    javax.persistence.Query query =
        manager
            .createQuery(
                "select r.from.id, r.to.id from " + tableName
                    + " r where obsolete = 0 "
                    + "and terminology = :terminology "
                    + "and version = :version "
                    + "and relationshipType = :relationshipType")
            .setParameter("terminology", terminology)
            .setParameter("version", version)
            .setParameter("relationshipType", chdRel);

    @SuppressWarnings("unchecked")
    List<Object[]> rels = query.getResultList();
    Map<Long, Set<Long>> parChd = new HashMap<>();
    Set<Long> allNodes = new HashSet<>();
    int ct = 0;
    for (final Object[] rel : rels) {
      final Long chd = Long.parseLong(rel[0].toString());
      final Long par = Long.parseLong(rel[1].toString());
      allNodes.add(par);
      allNodes.add(chd);
      if (!parChd.containsKey(par)) {
        parChd.put(par, new HashSet<Long>());
      }
      final Set<Long> children = parChd.get(par);
      children.add(chd);
      ct++;
      if (requestCancel) {
        rollback();
        throw new CancelException("Transitive closure computation cancelled.");
      }
    }
    Logger.getLogger(getClass()).info("    ct = " + ct);
    fireProgressEvent(8, "Start creating transitive closure relationships");

    //
    // Create transitive closure rels
    //
    Logger.getLogger(getClass()).info(
        "  Create transitive closure rels... " + new Date());

    // Create "self" entries
    ct = 0;
    for (Long code : allNodes) {

      // Create a "self" transitive relationship
      TransitiveRelationship<? extends ComponentHasAttributes> tr = null;
      if (idType == IdType.CONCEPT) {
        final ConceptTransitiveRelationship ctr =
            new ConceptTransitiveRelationshipJpa();
        Concept superType = new ConceptJpa();
        superType.setId(code);
        ctr.setSuperType(superType);
        ctr.setSubType(ctr.getSuperType());
        tr = ctr;
      } else if (idType == IdType.DESCRIPTOR) {
        final DescriptorTransitiveRelationship dtr =
            new DescriptorTransitiveRelationshipJpa();
        Descriptor superType = new DescriptorJpa();
        superType.setId(code);
        dtr.setSuperType(superType);
        dtr.setSubType(dtr.getSuperType());
        tr = dtr;
      } else if (idType == IdType.CODE) {
        final CodeTransitiveRelationship ctr =
            new CodeTransitiveRelationshipJpa();
        Code superType = new CodeJpa();
        superType.setId(code);
        ctr.setSuperType(superType);
        ctr.setSubType(ctr.getSuperType());
        tr = ctr;
      }

      tr.setObsolete(false);
      tr.setTimestamp(startDate);
      tr.setLastModified(startDate);
      tr.setLastModifiedBy("admin");
      tr.setPublishable(true);
      tr.setPublished(false);
      tr.setTerminologyId("");
      tr.setTerminology(terminology);
      tr.setVersion(version);
      tr.setDepth(0);
      addTransitiveRelationship(tr);
    }
    allNodes = new HashSet<>();
    
    // initialize descendant map
    descendantsMap = new HashMap<>();
    beginTransaction();
    int progressMax = parChd.keySet().size();
    int progress = 0;
    for (Long code : parChd.keySet()) {
      if (requestCancel) {
        rollback();
        throw new CancelException("Transitive closure computation cancelled.");
      }

      // Scale the progress monitor from 8%-100%
      ct++;
      int ctProgress = (int) ((((ct * 100) / progressMax) * .92) + 8);
      if (ctProgress > progress) {
        progress = ctProgress;
        fireProgressEvent((int) ((progress * .92) + 8),
            "Creating transitive closure relationships");
      }


      List<Long> ancPath = new ArrayList<>();
      ancPath.add(code);
      final Set<Long> descs = getDescendants(code, parChd, ancPath);
      final Set<Long> children = parChd.get(code);
      for (final Long desc : descs) {
        TransitiveRelationship<? extends ComponentHasAttributes> tr = null;        
        if (idType == IdType.CONCEPT) {
          final ConceptTransitiveRelationship ctr =
              new ConceptTransitiveRelationshipJpa();
          Concept superType = new ConceptJpa();
          superType.setId(code);
          Concept subType = new ConceptJpa();
          subType.setId(desc);
          ctr.setSuperType(superType);
          ctr.setSubType(subType);
          tr = ctr;
        } else if (idType == IdType.DESCRIPTOR) {
          final DescriptorTransitiveRelationship dtr =
              new DescriptorTransitiveRelationshipJpa();
          Descriptor superType = new DescriptorJpa();
          superType.setId(code);
          Descriptor subType = new DescriptorJpa();
          subType.setId(desc);
          dtr.setSuperType(superType);
          dtr.setSubType(subType);
          tr = dtr;
        } else if (idType == IdType.CODE) {
          final CodeTransitiveRelationship ctr =
              new CodeTransitiveRelationshipJpa();
          Code superType = new CodeJpa();
          superType.setId(code);
          Code subType = new CodeJpa();
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
        tr.setTerminology(terminology);
        tr.setVersion(version);
        addTransitiveRelationship(tr);
      }
      if (ct % commitCt == 0) {
        Logger.getLogger(getClass()).info(
            "      " + ct + " codes processed ..." + new Date());
        commit();
        clear();
        beginTransaction();
      }
    }
    // release memory
    descendantsMap = new HashMap<>();
    commit();
    clear();

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
