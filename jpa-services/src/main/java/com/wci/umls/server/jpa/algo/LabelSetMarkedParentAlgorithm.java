/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.CancelException;
import com.wci.umls.server.helpers.content.SubsetMemberList;
import com.wci.umls.server.helpers.meta.LabelSetList;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.meta.LabelSetJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptSubset;
import com.wci.umls.server.model.content.SubsetMember;
import com.wci.umls.server.model.meta.LabelSet;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.RootService;

/**
 * Implementation of an algorithm to compute label set marked parents using the
 * {@link ContentService}. Currently only concept label sets are supported.
 */
public class LabelSetMarkedParentAlgorithm extends AbstractAlgorithm {

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
    logInfo("Compute labels and marked parent labels");
    logInfo("  subset = " + subset);
    fireProgressEvent(0, "Starting...");

    setLastModifiedBy("admin");
    setLastModifiedFlag(true);
    setMolecularActionFlag(false);
    setTransactionPerOperation(false);
    beginTransaction();

    if (subset == null) {
      fireProgressEvent(100, "Finished...");
      throw new Exception("Subset must not be null.");
    }

    // Create the label set and add it (unless it exists already)
    fireProgressEvent(1, "Find ancestor label");
    LabelSet ancestorLabelSet = null;
    LabelSet labelSet = null;
    final LabelSetList list =
        getLabelSets(subset.getTerminology(), subset.getVersion());
    for (final LabelSet set : list.getObjects()) {
      if (set.getAbbreviation().equals(subset.getTerminologyId())) {
        logInfo("  existing label set =" + ancestorLabelSet);
        ancestorLabelSet = set;
        break;
      }
    }

    // Add the label set if null
    // Also add a label set for the content itself
    fireProgressEvent(2, "Add label sets");
    if (ancestorLabelSet == null) {
      final Date startDate = new Date();
      ancestorLabelSet = new LabelSetJpa();
      ancestorLabelSet.setAbbreviation("LABELFOR:" + subset.getTerminologyId());
      ancestorLabelSet.setDescription("label parent for " + subset.getName());
      ancestorLabelSet.setExpandedForm(subset.getName());
      ancestorLabelSet.setTimestamp(startDate);
      ancestorLabelSet.setPublishable(false);
      ancestorLabelSet.setPublished(false);
      ancestorLabelSet.setTerminology(subset.getTerminology());
      ancestorLabelSet.setVersion(subset.getVersion());
      ancestorLabelSet.setDerived(true);
      addLabelSet(ancestorLabelSet);
      logInfo("  new label set = " + ancestorLabelSet);
      labelSet = new LabelSetJpa();
      labelSet.setAbbreviation(subset.getTerminologyId());
      labelSet.setDescription("Concept in " + subset.getName());
      labelSet.setExpandedForm(subset.getName());
      labelSet.setTimestamp(startDate);
      labelSet.setPublishable(false);
      labelSet.setPublished(false);
      labelSet.setTerminology(subset.getTerminology());
      labelSet.setVersion(subset.getVersion());
      labelSet.setDerived(false);
      addLabelSet(labelSet);
      logInfo("  new label set = " + labelSet);
    }

    // Check cancel flag
    if (isCancelled()) {
      rollback();
      throw new CancelException(
          "Label set marked parent computation cancelled");
    }

    // Get subset members
    fireProgressEvent(3, "Get subset members");
    final SubsetMemberList members = findConceptSubsetMembers(
        subset.getTerminologyId(), subset.getTerminology(), subset.getVersion(),
        Branch.ROOT, "", null);
    logInfo("  subset members = " + members.size());

    // Look up ancestors
    fireProgressEvent(5, "Look up ancestors");
    final String tableName = "ConceptRelationshipJpa";
    final String tableName2 = "ConceptJpa";
    @SuppressWarnings("unchecked")
    final List<Object[]> relationships = manager
        .createQuery("select r.from.id, r.to.id from " + tableName + " r where "
            + "version = :version and terminology = :terminology "
            + "and hierarchical = 1 and inferred = 1 and obsolete = 0 "
            + "and r.from in (select o from " + tableName2
            + " o where obsolete = 0)")
        .setParameter("terminology", getTerminology())
        .setParameter("version", getVersion()).getResultList();

    int ct = 0;
    final Map<Long, Set<Long>> chdPar = new HashMap<>();
    for (final Object[] r : relationships) {
      ct++;
      long fromId = Long.parseLong(r[0].toString());
      long toId = Long.parseLong(r[1].toString());

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
      commit();
      return;
    }

    else {
      logInfo("  concepts with ancestors = " + chdPar.size());
    }

    // Collect member ids and marked parent ids
    fireProgressEvent(15, "Collect member ids and marked parent ids");
    final Set<Long> ancestorConceptIds = new HashSet<>();
    final Set<Long> conceptIds = new HashSet<>();
    for (@SuppressWarnings("rawtypes")
    final SubsetMember member : members.getObjects()) {
      final Concept concept = (Concept) member.getMember();
      // Add the member
      conceptIds.add(concept.getId());

      // Add the ancestors
      if (chdPar.containsKey(concept.getId())) {
        ancestorConceptIds.addAll(chdPar.get(concept.getId()));
      }

    }
    logInfo("    concept count = " + ancestorConceptIds.size());
    logInfo("    ancestor count = " + conceptIds.size());

    // Check cancel flag
    if (isCancelled()) {
      rollback();
      throw new CancelException(
          "Label set marked parent computation cancelled");
    }

    fireProgressEvent(25, "Tag concepts with label set");
    int objectCt = 0;
    for (final Long id : ancestorConceptIds) {
      final Concept concept = getConcept(id);
      concept.getLabels().add(ancestorLabelSet.getAbbreviation());
      updateConcept(concept);
      logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
      // Check cancel flag
      if (ct % RootService.logCt == 0 && isCancelled()) {
        rollback();
        throw new CancelException(
            "Label set marked parent computation cancelled");
      }
    }
    commitClearBegin();
    logInfo("    count = " + objectCt);

    // concepts that are both in the set and ancestor of the set get both tags.
    fireProgressEvent(50, "Tag ancestor concepts with marked parent label set");
    objectCt = 0;
    for (final Long id : conceptIds) {
      final Concept concept = getConcept(id);
      concept.getLabels().add(labelSet.getAbbreviation());
      updateConcept(concept);
      logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
      // Check cancel flag
      if (ct % RootService.logCt == 0 && isCancelled()) {
        rollback();
        throw new CancelException(
            "Label set marked parent computation cancelled");
      }
    }

    logInfo("    count = " + objectCt);
    fireProgressEvent(100, "Finished...");
    commit();
    clear();
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
    this.getName();
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

}
