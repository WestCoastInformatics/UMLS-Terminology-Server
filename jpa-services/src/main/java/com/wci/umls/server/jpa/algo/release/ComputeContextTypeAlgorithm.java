/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.release;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.meta.AdditionalRelationshipTypeList;
import com.wci.umls.server.helpers.meta.RelationshipTypeList;
import com.wci.umls.server.jpa.AlgorithmParameterJpa;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
import com.wci.umls.server.jpa.content.AtomRelationshipJpa;
import com.wci.umls.server.jpa.content.CodeRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.DescriptorRelationshipJpa;
import com.wci.umls.server.model.content.Component;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.RootService;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;

/**
 * Algorithm to compute context types.
 */
public class ComputeContextTypeAlgorithm extends AbstractAlgorithm {

  /** The siblings threshold. */
  private int siblingsThreshold;

  /**
   * Instantiates an empty {@link ComputeContextTypeAlgorithm}.
   *
   * @throws Exception the exception
   */
  public ComputeContextTypeAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("CONTEXTTYPE");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    // no preconditions
    return new ValidationResultJpa();
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public void compute() throws Exception {
    logInfo("Starting " + getName());
    fireProgressEvent(0, "Starting");

    // Get id handler
    final IdentifierAssignmentHandler handler =
        getIdentifierAssignmentHandler(getProject().getTerminology());

    // Collect metadata
    logInfo("  Collect metadata");
    final RelationshipTypeList relTypeList = getRelationshipTypes(
        getProject().getTerminology(), getProject().getVersion());
    final AdditionalRelationshipTypeList addRelTypeList =
        getAdditionalRelationshipTypes(getProject().getTerminology(),
            getProject().getVersion());
    final Map<String, String> relToInverseMap = new HashMap<>();
    for (final RelationshipType relType : relTypeList.getObjects()) {
      relToInverseMap.put(relType.getAbbreviation(),
          relType.getInverse().getAbbreviation());
    }
    for (final AdditionalRelationshipType relType : addRelTypeList
        .getObjects()) {
      relToInverseMap.put(relType.getAbbreviation(),
          relType.getInverse().getAbbreviation());
    }

    // Build a map of tree position poly-hierarchies
    final String[] types = new String[] {
        "Atom", "Code", "Concept", "Descriptor"
    };
    logAndCommit(0, RootService.logCt, RootService.commitCt);

    logInfo("  Compute polyhierarchy flags");
    fireProgressEvent(1, "Compute polyhierarchy flags");
    final Set<String> polyHierarchyTerminology = new HashSet<>();
    for (final String type : types) {
      final javax.persistence.Query query = manager
          .createQuery("select distinct terminology, version from " + type
              + "TreePositionJpa " + "group by terminology, version, node_id "
              + "having count(*)>1");
      final List<Object[]> results = query.getResultList();
      checkCancel();
      for (final Object[] result : results) {
        logInfo("    poly = " + result[0]);
        polyHierarchyTerminology.add(result[0].toString());
        logAndCommit(0, RootService.logCt, RootService.commitCt);
      }
    }

    logInfo("  Compute sibling flags");
    fireProgressEvent(10, "Compute include sibling flags");
    final Map<String, String> siblingTypeMap = new HashMap<>();
    for (final String type : types) {
      final javax.persistence.Query query =
          manager.createQuery("select distinct terminology, version from "
              + type + "TreePositionJpa "
              + "group by terminology, version, ancestorPath "
              + "having count(*) < :threshold");
      query.setParameter("threshold", Long.valueOf(siblingsThreshold));
      final List<Object[]> results = query.getResultList();
      checkCancel();

      final javax.persistence.Query query2 =
          manager.createQuery("select distinct terminology, version from "
              + type + "TreePositionJpa "
              + "group by terminology, version, ancestorPath "
              + "having count(*) > :threshold");
      query2.setParameter("threshold", Long.valueOf(siblingsThreshold));
      final List<Object[]> results2 = query2.getResultList();
      checkCancel();

      // Add things with < :threshold siblings
      for (final Object[] result : results) {
        siblingTypeMap.put(result[0].toString(), type);
      }
      // Remove things with > :threshold siblings
      for (final Object[] result : results2) {
        siblingTypeMap.remove(result[0].toString());
      }

      for (final String key : siblingTypeMap.keySet()) {
        logInfo("    siblings = " + key + ", " + siblingTypeMap.get(key));
      }
    }

    // Iterate through terminologies to determine context type
    logInfo("  Compute siblings");
    fireProgressEvent(20, "Compute siblings");

    int prevProgress = 0;
    int startProgress = 20;
    int totalCt = getCurrentTerminologies().size();
    int objectCt = 0;
    int termCt = 0;
    // Sort the terminologies
    Collections.sort(getCurrentTerminologies().getObjects(),
        (t1, t2) -> t1.getTerminology().compareTo(t2.getTerminology()));
    for (final Terminology term : getCurrentTerminologies().getObjects()) {
      logInfo("    terminology = " + term.getTerminology());
      checkCancel();

      // Set polyhierarchy and include siblings flags
      if (polyHierarchyTerminology.contains(term.getTerminology())) {
        // set flag on rootTerminology and update
        term.getRootTerminology().setPolyhierarchy(true);
      } else {
        term.getRootTerminology().setPolyhierarchy(false);
      }
      updateRootTerminology(term.getRootTerminology());

      final boolean includeSiblings =
          siblingTypeMap.containsKey(term.getTerminology());
      if (includeSiblings) {
        // set flag on rootTerminology and update
        term.setIncludeSiblings(true);
      } else {
        term.setIncludeSiblings(false);
      }
      updateTerminology(term);

      // Compute RUIs for SIB relationships
      if (includeSiblings) {
        setMolecularActionFlag(false);
        final String type = siblingTypeMap.get(term.getTerminology());
        final IdType idType = IdType.valueOf(type.toUpperCase());

        final javax.persistence.Query query = manager.createQuery(
            "select a.node.id, b.node.id, a.additionalRelationshipType from "
                + type + "TreePositionJpa a, " + type + "TreePositionJpa b "
                + "where a.ancestorPath = b.ancestorPath "
                + "  and a.additionalRelationshipType = b.additionalRelationshipType "
                + "  and a.node.id < b.node.id"
                + "  and a.terminology = :terminology and b.terminology = :terminology "
                + "  and a.version = :version and b.version= :version ");
        query.setParameter("terminology", term.getTerminology());
        query.setParameter("version", term.getVersion());
        final List<Object[]> results = query.getResultList();
        checkCancel();

        for (final Object[] result : results) {
          final Long fromId = Long.valueOf(result[0].toString());
          final Long toId = Long.valueOf(result[1].toString());
          final String addRelType = result[2].toString();

          Component from = null;
          Component to = null;
          @SuppressWarnings("rawtypes")
          Relationship newRel = null;
          if (idType == IdType.ATOM) {
            newRel = new AtomRelationshipJpa();
            from = getAtom(fromId);
            to = getAtom(toId);
          } else if (idType == IdType.CONCEPT) {
            newRel = new ConceptRelationshipJpa();
            from = getConcept(fromId);
            to = getConcept(toId);
          } else if (idType == IdType.CODE) {
            newRel = new CodeRelationshipJpa();
            from = getCode(fromId);
            to = getCode(toId);
          } else if (idType == IdType.DESCRIPTOR) {
            newRel = new DescriptorRelationshipJpa();
            from = getDescriptor(fromId);
            to = getDescriptor(toId);
          }

          newRel.setFrom(from);
          newRel.setTo(to);
          newRel.setAdditionalRelationshipType("sib_in_" + addRelType);
          newRel.setTerminology(term.getTerminology());
          newRel.setVersion(term.getVersion());
          newRel.setRelationshipType("SIB");
          newRel.setPublishable(true);
          newRel.setObsolete(false);
          newRel.setSuppressible(false);
          newRel.setGroup(null);
          newRel.setPublished(true);
          newRel.setWorkflowStatus(WorkflowStatus.PUBLISHED);
          newRel.setHierarchical(false);
          newRel.setAssertedDirection(false);
          newRel.setInferred(true);
          newRel.setStated(true);
          newRel.setTerminologyId("");

          // This is just to assign identifiers
          final String rui = handler.getTerminologyId(newRel, "SIB",
              relToInverseMap.get(addRelType));
          newRel.setTerminologyId(rui);

          // check cancel
          if (objectCt % RootService.logCt == 0) {
            checkCancel();
          }

          logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);

        }
        commitClearBegin();

        // update progress
        int progress = (int) ((100.0 - startProgress) * ++termCt / totalCt)
            + startProgress;
        if (progress > prevProgress) {
          fireProgressEvent(progress, "Assigning SIB RUIs");
          prevProgress = progress;
        }

      }
    }
    commitClearBegin();

    fireProgressEvent(100, "Finished");
    logInfo("Finished " + getName());
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    logInfo("Starting RESET " + getName());
    // n/a
    logInfo("Finished RESET " + getName());
  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        ""
    }, p);
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {

    if (p.getProperty("siblingsThreshold") != null) {
      siblingsThreshold = Integer.parseInt(p.getProperty("siblingsThreshold"));
    }
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() throws Exception {
    final List<AlgorithmParameter> params = super.getParameters();

    AlgorithmParameter param = new AlgorithmParameterJpa("Siblings threshold",
        "siblingsThreshold", "Indicates maximum number of siblings.",
        "e.g. 100", 0, AlgorithmParameter.Type.INTEGER, "100");
    params.add(param);

    return params;
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return ConfigUtility.getNameFromClass(getClass());
  }

}
