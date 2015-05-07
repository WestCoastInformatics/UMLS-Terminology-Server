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
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.CancelException;
import com.wci.umls.server.jpa.content.CodeTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptTransitiveRelationshipJpa;
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
import com.wci.umls.server.model.content.Relationship;
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

  /** The descendants map. */
  private Map<Long, Set<Long>> descendantsMap = new HashMap<>();

  /** The id type. */
  private IdType idType;

  /** The cycle tolerant. */
  private boolean cycleTolerant;

  /** The Constant commitCt. */
  private final static int commitCt = 2000;

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
    Map<Long, Set<Long>> parChd = new HashMap<>();
    fireProgressEvent(1, "Initialize relationships");
    String tableName = "ConceptRelationshipJpa";
    if (idType == IdType.DESCRIPTOR) {
      tableName = "DescriptorRelationshipJpa";
    }
    if (idType == IdType.CODE) {
      tableName = "CodeRelationshipJpa";
    }
//    @SuppressWarnings("unchecked")
//    List<Relationship> relationships =
//        manager
//            .createQuery(
//                "select r from "
//                    + tableName
//                    + " r where "
//                    + "terminologyVersion = :terminologyVersion and terminology = :terminology "
//                    + "and typeId = :typeId and obsolete = 1 "
//                    + "and sourceConcept in (select sc from ConceptJpa sc where active = 1)")
//            .setParameter("typeId", chdRel)
//            .setParameter("terminology", terminology)
//            .setParameter("terminologyVersion", terminologyVersion)
//            .getResultList();
//    int ct = 0;
//    for (Relationship r : relationships) {
//      ct++;
//      final Concept sourceConcept = r.getSourceConcept();
//      final Concept destinationConcept = r.getDestinationConcept();
//      if (!localParChd.containsKey(destinationConcept.getId())) {
//        localParChd.put(destinationConcept.getId(), new HashSet<Long>());
//      }
//      Set<Long> children = localParChd.get(destinationConcept.getId());
//      children.add(sourceConcept.getId());
//    }
//    Logger.getLogger(this.getClass()).info("    count = " + ct);
//
//    Set<String> rootIds = new HashSet<>();
//    // TODO: get root concepts (things that have no parents)
//
//    for (String rootId : rootIds) {
//      Logger.getLogger(getClass()).info(
//          "  Compute tree positions for root " + rootId);
//      computeTreePositions(rootId, "");
//    }
//  }
//
//  /**
//   * Compute tree positions.
//   *
//   * @param rootId the root id
//   * @param ancPath the anc path
//   */
//  public void computeTreePositions(String rootId, String ancPath,
//    Map<Long, Set<Long>> parChd) {
//
//    final Set<Long> descConceptIds = new HashSet<>();
//
//    // if concept is active
//    if (concept.isActive()) {
//
//      // extract the ancestor terminology ids
//      Set<String> ancestors = new HashSet<>();
//      for (String ancestor : ancestorPath.split("~"))
//        ancestors.add(ancestor);
//
//      // if ancestor path contains this terminology id, a child/ancestor cycle
//      // exists
//      if (ancestors.contains(concept.getTerminologyId())) {
//
//        // add error to validation result
//        computeTreePositionValidationResult
//            .addError("Cycle detected for concept "
//                + concept.getTerminologyId() + ", ancestor path is "
//                + ancestorPath);
//
//        // return empty set of descendants to truncate calculation on this path
//        return descConceptIds;
//      }
//
//      // instantiate the tree position
//      final TreePosition tp = new TreePositionJpa();
//
//      if (!cycleCheckOnly) {
//
//        // logging information
//        int ancestorCount =
//            ancestorPath.length() - ancestorPath.replaceAll("~", "").length();
//        String loggerPrefix = "";
//        for (int i = 0; i < ancestorCount; i++)
//          loggerPrefix += "  ";
//
//        // Logger.get// Logger(ContentServiceJpa.class).info(loggerPrefix +
//        // "Computing position for concept " + concept.getTerminologyId() +
//        // ", " + concept.getDefaultPreferredName());;
//
//        tp.setAncestorPath(ancestorPath);
//        tp.setTerminology(concept.getTerminology());
//        tp.setTerminologyVersion(concept.getTerminologyVersion());
//        tp.setTerminologyId(concept.getTerminologyId());
//        tp.setDefaultPreferredName(concept.getDefaultPreferredName());
//
//        // persist the tree position
//        manager.persist(tp);
//      }
//      // construct the ancestor path terminating at this concept
//      final String conceptPath =
//          (ancestorPath.equals("") ? concept.getTerminologyId() : ancestorPath
//              + "~" + concept.getTerminologyId());
//
//      // Gather descendants if this is not a leaf node
//      if (localParChd.containsKey(concept.getId())) {
//
//        descConceptIds.addAll(localParChd.get(concept.getId()));
//
//        // iterate over the child terminology ids
//        // this iteration is entirely local and depends on no managed
//        // objects
//        for (Long childConceptId : localParChd.get(concept.getId())) {
//
//          // call helper function on child concept
//          // add the results to the local descendant set
//          final Set<Long> desc =
//              computeTreePositionsHelper(localParChd,
//                  getConcept(childConceptId), typeId, conceptPath,
//                  computeTreePositionCommitCt, computeTreePositionTransaction,
//                  cycleCheckOnly);
//          if (!cycleCheckOnly) {
//            descConceptIds.addAll(desc);
//          }
//
//        }
//
//      }
//
//      if (!cycleCheckOnly) {
//
//        // set the children count
//        tp.setChildrenCount(localParChd.containsKey(concept.getId())
//            ? localParChd.get(concept.getId()).size() : 0);
//
//        // set the descendant count
//        tp.setDescendantCount(descConceptIds.size());
//
//        // In case manager was cleared here, get it back onto changed list
//        manager.merge(tp);
//
//        // routinely commit and force clear the manager
//        // any existing recursive threads are entirely dependent on local
//        // variables
//        if (++computeTreePositionTotalCount % computeTreePositionCommitCt == 0) {
//
//          // commit the transaction
//          computeTreePositionTransaction.commit();
//
//          // Clear manager for memory management
//          manager.clear();
//
//          // begin a new transaction
//          computeTreePositionTransaction.begin();
//
//          // report progress and memory usage
//          Runtime runtime = Runtime.getRuntime();
//          float elapsedTime =
//              System.currentTimeMillis() - computeTreePositionLastTime;
//          elapsedTime = elapsedTime / 1000;
//          computeTreePositionLastTime = System.currentTimeMillis();
//
//          if (runtime.totalMemory() > computeTreePositionMaxMemoryUsage)
//            computeTreePositionMaxMemoryUsage = runtime.totalMemory();
//
//          Logger.getLogger(ContentServiceJpa.class).info(
//              "\t" + System.currentTimeMillis() / 1000 + "\t"
//                  + computeTreePositionTotalCount + "\t"
//                  + Math.floor(runtime.totalMemory() / 1024 / 1024) + "\t"
//                  + Double.toString(computeTreePositionCommitCt / elapsedTime));
//
//        }
//      }
//    }
//
//    // Check that this concept does not reference itself as a child
//    if (descConceptIds.contains(concept.getTerminologyId())) {
//
//      // add error to validation result
//      computeTreePositionValidationResult.addError("Concept "
//          + concept.getTerminologyId() + " claims itself as a child");
//
//      // remove this terminology id to prevent infinite loop
//      descConceptIds.remove(concept.getTerminologyId());
//    }
//
//    // return the descendant concept set
//    // note that the local child and descendant set will be garbage
//    // collected
//    return descConceptIds;

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
   * Compute transitive closure.
   *
   * @param terminology the terminology
   * @param version the terminology version
   * @param idType the id type
   * @throws Exception the exception
   */
  private void computeTreePositions(String rootId, String terminology,
    String version, IdType idType) throws Exception {

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
                "select r from " + tableName + " r where obsolete = 0 "
                    + "and terminology = :terminology "
                    + "and terminologyVersion = :version "
                    + "and relationshipType = :relationshipType")
            .setParameter("terminology", terminology)
            .setParameter("version", version)
            .setParameter("relationshipType", chdRel);

    @SuppressWarnings("unchecked")
    List<Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>> rels =
        query.getResultList();
    Map<Long, Set<Long>> parChd = new HashMap<>();
    int ct = 0;
    for (Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> rel : rels) {
      final Long chd = rel.getFrom().getId();
      final Long par = rel.getTo().getId();
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

    // Initialize concepts
    fireProgressEvent(5, "Initialize concepts");
    Logger.getLogger(getClass())
        .info("  Initialize concepts ... " + new Date());
    Map<Long, ComponentHasAttributes> componentMap = new HashMap<>();
    if (idType == IdType.CONCEPT) {
      for (Concept concept : getAllConcepts(terminology, version, Branch.ROOT)
          .getObjects()) {
        getGraphResolutionHandler(terminology).resolveEmpty(concept);
        componentMap.put(concept.getId(), concept);
      }
    } else if (idType == IdType.DESCRIPTOR) {
      for (Descriptor descriptor : getAllDescriptors(terminology, version,
          Branch.ROOT).getObjects()) {
        componentMap.put(descriptor.getId(), descriptor);
      }
    } else if (idType == IdType.CODE) {
      for (Code code : getAllCodes(terminology, version, Branch.ROOT)
          .getObjects()) {
        componentMap.put(code.getId(), code);
      }
    }

    // detatch concepts
    manager.clear();
    fireProgressEvent(8, "Start creating transitive closure relationships");

    //
    // Create transitive closure rels
    //
    Logger.getLogger(getClass()).info(
        "  Create transitive closure rels... " + new Date());
    ct = 0;
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
      for (final Long desc : descs) {
        TransitiveRelationship<? extends ComponentHasAttributes> tr = null;
        if (idType == IdType.CONCEPT) {
          final ConceptTransitiveRelationship ctr =
              new ConceptTransitiveRelationshipJpa();
          ctr.setSuperType((Concept) componentMap.get(code));
          ctr.setSubType((Concept) componentMap.get(desc));
          tr = ctr;
        } else if (idType == IdType.DESCRIPTOR) {
          final DescriptorTransitiveRelationship ctr =
              new DescriptorTransitiveRelationshipJpa();
          ctr.setSuperType((Descriptor) componentMap.get(code));
          ctr.setSubType((Descriptor) componentMap.get(desc));
          tr = ctr;
        } else if (idType == IdType.CODE) {
          final CodeTransitiveRelationship ctr =
              new CodeTransitiveRelationshipJpa();
          ctr.setSuperType((Code) componentMap.get(code));
          ctr.setSubType((Code) componentMap.get(desc));
          tr = ctr;
        } else {
          throw new Exception("Illegal id type: " + idType);
        }

        tr.setObsolete(false);
        tr.setLastModified(new Date());
        tr.setLastModifiedBy("admin");
        tr.setPublishable(true);
        tr.setPublished(false);
        tr.setTerminologyId("");
        tr.setTerminology(terminology);
        tr.setTerminologyVersion(version);
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
