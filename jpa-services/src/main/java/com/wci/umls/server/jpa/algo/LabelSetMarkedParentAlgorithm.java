/**
 * Copyright 2016 West Coast Informatics, LLC
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
import com.wci.umls.server.helpers.meta.LabelSetList;
import com.wci.umls.server.jpa.meta.LabelSetJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptSubset;
import com.wci.umls.server.model.content.SubsetMember;
import com.wci.umls.server.model.meta.LabelSet;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.RootService;
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;

/**
 * Implementation of an algorithm to compute transitive closure using the
 * {@link ContentService}.
 */
public class LabelSetMarkedParentAlgorithm extends ContentServiceJpa implements
    Algorithm {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The request cancel flag. */
  boolean requestCancel = false;

  /** The concept to generate label set data from. */
  private ConceptSubset subset;

  /**
   * Instantiates an empty {@link LabelSetMarkedParentAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public LabelSetMarkedParentAlgorithm() throws Exception {
    super();
  }

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  @Override
  public void compute() throws Exception {
    Logger.getLogger(getClass())
        .info("Start computing marked parent label set");
    Logger.getLogger(getClass()).info("  subset = " + subset);
    setTransactionPerOperation(false);
    beginTransaction();

    if (subset == null) {
      throw new Exception("Subset must not be null.");
    }

    // Create the label set and add it (unless it exists already)
    LabelSet ancestorLabelSet = null;
    LabelSet labelSet = null;

    LabelSetList list =
        getLabelSets(subset.getTerminology(), subset.getVersion());
    for (LabelSet set : list.getObjects()) {
      if (set.getAbbreviation().equals(subset.getTerminologyId())) {
        Logger.getLogger(getClass()).info(
            "  Use existing label set =" + ancestorLabelSet);
        ancestorLabelSet = set;
        break;
      }
    }

    // Add the label set if null
    // Also add a label set for the content itself
    if (ancestorLabelSet == null) {
      Date startDate = new Date();
      ancestorLabelSet = new LabelSetJpa();
      ancestorLabelSet.setAbbreviation("LABELFOR:" + subset.getTerminologyId());
      ancestorLabelSet.setDescription("label parent for " + subset.getName());
      ancestorLabelSet.setExpandedForm(subset.getName());
      ancestorLabelSet.setLastModified(startDate);
      ancestorLabelSet.setTimestamp(startDate);
      ancestorLabelSet.setLastModifiedBy("loader");
      ancestorLabelSet.setPublishable(false);
      ancestorLabelSet.setPublished(false);
      ancestorLabelSet.setTerminology(subset.getTerminology());
      ancestorLabelSet.setVersion(subset.getVersion());
      ancestorLabelSet.setDerived(true);
      addLabelSet(ancestorLabelSet);
      Logger.getLogger(getClass()).info(
          "  Create new label set = " + ancestorLabelSet);
      labelSet = new LabelSetJpa();
      labelSet.setAbbreviation(subset.getTerminologyId());
      labelSet.setDescription("Concept in " + subset.getName());
      labelSet.setExpandedForm(subset.getName());
      labelSet.setLastModified(startDate);
      labelSet.setTimestamp(startDate);
      labelSet.setLastModifiedBy("loader");
      labelSet.setPublishable(false);
      labelSet.setPublished(false);
      labelSet.setTerminology(subset.getTerminology());
      labelSet.setVersion(subset.getVersion());
      labelSet.setDerived(false);
      addLabelSet(labelSet);
      Logger.getLogger(getClass()).info("  Create new label set = " + labelSet);
    }

    SubsetMemberList members =
        findConceptSubsetMembers(subset.getTerminologyId(),
            subset.getTerminology(), subset.getVersion(), Branch.ROOT, "", null);

    // Go through each concept in the subset
    // Look up and save all of the ancestors
    
    Logger.getLogger(getClass()).info("  Lookup all ancestors");
    Set<Long> ancestorConceptIds = new HashSet<>();
    Set<Long> conceptIds = new HashSet<>();
    for (@SuppressWarnings("rawtypes")
    final SubsetMember member : members.getObjects()) {
      final Concept concept = (Concept) member.getMember();
      // Save this to mark it later
      conceptIds.add(concept.getId());
      // If the concept is already label as an ancestor, its ancestors
      // have been computed and we can move on
      if (ancestorConceptIds.contains(concept.getId())) {
        continue;
      }
      // Get all ancestor concepts
      for (Concept ancConcept : findAncestorConcepts(
          concept.getTerminologyId(), concept.getTerminology(),
          concept.getVersion(), false, Branch.ROOT, null).getObjects()) {
        ancestorConceptIds.add(ancConcept.getId());
      }
    }
    Logger.getLogger(getClass()).info(
        "    count = " + ancestorConceptIds.size());

    Logger.getLogger(getClass()).info("  Tag ancestor concepts with label set");
    int objectCt = 0;
    for (Long id : ancestorConceptIds) {
      final Concept concept = getConcept(id);
      concept.addLabel(ancestorLabelSet.getAbbreviation());
      updateConcept(concept);
      logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
    }

    commitClearBegin();

    // concepts that are both in the set and ancestor of the set get both tags.
    Logger.getLogger(getClass()).info("  Tag concepts with label set");
    objectCt = 0;
    for (Long id : conceptIds) {
      final Concept concept = getConcept(id);
      concept.addLabel(labelSet.getAbbreviation());
      updateConcept(concept);
      logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
    }

    commitClearBegin();

    Logger.getLogger(getClass()).info("    count = " + objectCt);
  }

  /**
   * Reset.
   *
   * @throws Exception the exception
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


}
