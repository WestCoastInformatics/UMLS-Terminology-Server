/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.content.SubsetMemberList;
import com.wci.umls.server.helpers.meta.MarkerSetList;
import com.wci.umls.server.jpa.meta.MarkerSetJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptSubset;
import com.wci.umls.server.model.content.SubsetMember;
import com.wci.umls.server.model.meta.MarkerSet;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;

/**
 * Implementation of an algorithm to compute transitive closure using the
 * {@link ContentService}.
 */
public class MarkerSetMarkedParentAlgorithm extends ContentServiceJpa implements
    Algorithm {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The request cancel flag. */
  boolean requestCancel = false;

  /** The concept to generate marker set data from. */
  private ConceptSubset subset;

  /** commit count. */
  private final static int commitCt = 2000;

  /** log count. */
  private final static int logCt = 2000;

  /**
   * Instantiates an empty {@link MarkerSetMarkedParentAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public MarkerSetMarkedParentAlgorithm() throws Exception {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.jpa.algo.Algorithm#compute()
   */
  @Override
  public void compute() throws Exception {
    Logger.getLogger(getClass()).info("Start computing marked set");
    Logger.getLogger(getClass()).info("  subset = " + subset);
    setTransactionPerOperation(false);
    beginTransaction();
    
    fireProgressEvent(0, "Starting...");

    if (subset == null) {
      throw new Exception("Subset must not be null.");
    }

    // Create the marker set and add it (unless it exists already)
    MarkerSet markerSet = null;
    MarkerSetList list =
        getMarkerSets(subset.getTerminology(), subset.getVersion());
    for (MarkerSet set : list.getObjects()) {
      if (set.getAbbreviation().equals(subset.getTerminologyId())) {
        Logger.getLogger(getClass()).info("  Use existing marker set =" + markerSet);
        markerSet = set;
        break;
      }
    }

    // Add the marker set if null
    if (markerSet == null) {
      Date startDate = new Date();
      markerSet = new MarkerSetJpa();
      markerSet.setAbbreviation(subset.getTerminologyId());
      markerSet.setDescription("Marked parent for " + subset.getName());
      markerSet.setExpandedForm(subset.getName());
      markerSet.setLastModified(startDate);
      markerSet.setTimestamp(startDate);
      markerSet.setLastModifiedBy("loader");
      markerSet.setPublishable(false);
      markerSet.setPublished(false);
      markerSet.setTerminology(subset.getTerminology());
      markerSet.setVersion(subset.getVersion());
      addMarkerSet(markerSet);
      Logger.getLogger(getClass()).info("  Create new marker set = " + markerSet);
    }

    SubsetMemberList members =
        findConceptSubsetMembers(subset.getTerminologyId(),
            subset.getTerminology(), subset.getVersion(), Branch.ROOT, "", null);

    // Go through each concept in the subset
    // Look up and save all of the ancestors
    // TODO: this can be more efficient with an HQL Query
    Logger.getLogger(getClass()).info("  Lookup all ancestors");
    Set<Long> conceptIds = new HashSet<>();
    for (@SuppressWarnings("rawtypes") final SubsetMember member : members.getObjects()) {
      final Concept concept = (Concept) member.getMember();
      for (Concept ancConcept : findAncestorConcepts(concept.getTerminologyId(), 
          concept.getTerminology(), concept.getVersion(), false, Branch.ROOT, null).getObjects()) {
        conceptIds.add(ancConcept.getId());
      }
    }
    Logger.getLogger(getClass()).info("    count = " + conceptIds.size());

    Logger.getLogger(getClass()).info("  Tag concepts with marker set");
    int objectCt = 0;
    for (Long id : conceptIds) {
      final Concept concept =  getConcept(id);
      concept.addMarkerSet(markerSet.getAbbreviation());
      updateConcept(concept);
      logAndCommit(++objectCt);
    }

    commitClearBegin();
    
    Logger.getLogger(getClass()).info("    count = " + objectCt);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.jpa.algo.Algorithm#reset()
   */
  @Override
  public void reset() throws Exception {
    // n/a
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

  /**
   * Returns the subset.
   *
   * @return the subset
   */
  public ConceptSubset getSubset() {
    return subset;
  }

  /**
   * Sets the subset.
   *
   * @param subset the subset
   */
  public void setSubset(ConceptSubset subset) {
    this.subset = subset;
  }

  /**
   * Commit clear begin transaction.
   *
   * @throws Exception the exception
   */
  private void commitClearBegin() throws Exception {
    commit();
    clear();
    beginTransaction();
  }

  /**
   * Log and commit.
   * 
   * @param objectCt the object ct
   * @throws Exception the exception
   */
  private void logAndCommit(int objectCt) throws Exception {
    // log at regular intervals
    if (objectCt % logCt == 0) {
      Logger.getLogger(getClass()).info("    count = " + objectCt);
    }
    if (objectCt % commitCt == 0) {
      commitClearBegin();
    }
  }

}
