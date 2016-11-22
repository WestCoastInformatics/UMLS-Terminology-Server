/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.rel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.persistence.NoResultException;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.meta.AdditionalRelationshipTypeList;
import com.wci.umls.server.helpers.meta.RelationshipTypeList;
import com.wci.umls.server.jpa.AlgorithmParameterJpa;
import com.wci.umls.server.jpa.ComponentInfoJpa;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
import com.wci.umls.server.jpa.content.AtomRelationshipJpa;
import com.wci.umls.server.jpa.content.CodeRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.DescriptorRelationshipJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;

/**
 * Algorithm to compute context types.
 */
public class ComputeContextTypeAlgorithm extends AbstractAlgorithm {

  private int siblingsThreshold = 100; // TODO 1000

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
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    boolean includeSiblings = false;
    boolean polyhierarchy = false; // TODO; what is this for?

    logInfo("Starting Create new release");
    fireProgressEvent(0, "Starting progress: " + 0 + "%");

    IdentifierAssignmentHandler handler =
        getIdentifierAssignmentHandler(getProject().getTerminology());
    
    //  First create map of rel and rela inverses
    RelationshipTypeList relTypeList = getRelationshipTypes(getProject().getTerminology(), getProject().getVersion());
    AdditionalRelationshipTypeList addRelTypeList = getAdditionalRelationshipTypes(getProject().getTerminology(), getProject().getVersion());
    Map<String, String> relToInverseMap = new HashMap<>();
    for (RelationshipType relType : relTypeList.getObjects()) {
      relToInverseMap.put(relType.getAbbreviation(), relType.getInverse().getAbbreviation());        
    }
    for (AdditionalRelationshipType relType : addRelTypeList.getObjects()) {
      relToInverseMap.put(relType.getAbbreviation(), relType.getInverse().getAbbreviation());        
    }

    for (Terminology term : getCurrentTerminologies().getObjects()) {
      IdType organizingClassType = term.getOrganizingClassType();

      // compute the "includeSiblings" flag for the terminology
      String queryStr = "";
      if (organizingClassType == IdType.ATOM) {
        queryStr = "select count(*) from AtomTreePositionJpa where "
            + "terminology = :terminology and version = :version group by ancestorPath order by 1 desc ";
      } else if (organizingClassType == IdType.CONCEPT) {
        queryStr = "select count(*) from ConceptTreePositionJpa where "
            + "terminology = :terminology and version = :version group by ancestorPath order by 1 desc ";
      } else if (organizingClassType == IdType.CODE) {
        queryStr = "select count(*) from CodeTreePositionJpa where "
            + "terminology = :terminology and version = :version group by ancestorPath order by 1 desc ";
      } else if (organizingClassType == IdType.DESCRIPTOR) {
        queryStr = "select count(*) from DescriptorTreePositionJpa where "
            + "terminology = :terminology and version = :version group by ancestorPath order by 1 desc ";
      }
      javax.persistence.Query query = manager.createQuery(queryStr);
      query.setParameter("terminology", term.getTerminology());
      query.setParameter("version", term.getVersion());
      query.setMaxResults(1);
      String result = null;
      try {
        result = query.getSingleResult().toString();
      } catch (NoResultException nre) {
        // ignore
      }
      if (result != null) {
        int ct = new Integer(result).intValue();
        if (ct > siblingsThreshold) {
          includeSiblings = true;
        } else {
          includeSiblings = false;
        }
        // update field in terminology
        term.setIncludeSiblings(includeSiblings);
        updateTerminology(term);
      }

      if (organizingClassType == IdType.ATOM) {
        queryStr = "select count(*) from AtomTreePositionJpa" + " where "
            + "terminology = :terminology and version = :version group by node having count(*)>1 order by 1 desc ";
      } else if (organizingClassType == IdType.CONCEPT) {
        queryStr = "select count(*) from ConceptTreePositionJpa" + " where "
            + "terminology = :terminology and version = :version group by node having count(*)>1 order by 1 desc ";
      } else if (organizingClassType == IdType.CODE) {
        queryStr = "select count(*) from CodeTreePositionJpa" + " where "
            + "terminology = :terminology and version = :version group by node having count(*)>1 order by 1 desc ";
      } else if (organizingClassType == IdType.DESCRIPTOR) {
        queryStr = "select count(*) from DescriptorTreePositionJpa" + " where "
            + "terminology = :terminology and version = :version group by node having count(*)>1 order by 1 desc ";
      }
      query = manager.createQuery(queryStr);
      query.setParameter("terminology", term.getTerminology());
      query.setParameter("version", term.getVersion());
      query.setMaxResults(1);
      result = null;
      try {
        result = query.getSingleResult().toString();
      } catch (NoResultException nre) {
        // ignore
      }
      if (result != null) {
        int ct = new Integer(result).intValue();
        if (ct > 0) {
          polyhierarchy = true;
        } else {
          polyhierarchy = false;
        }
        // TODO set flag on rootTerminology and update
        
      }

      // Compute RUIs for SIB relationships
      if (includeSiblings) {
        setMolecularActionFlag(false);

        // compute sibling pairs
        final Session session = manager.unwrap(Session.class);
        org.hibernate.Query hQuery = null;

        if (organizingClassType == IdType.ATOM) {
          hQuery = session.createQuery(
              "select a.node, b.node, a.additionalRelationshipType from AtomTreePositionJpa a, AtomTreePositionJpa b "
                  + " where a.ancestorPath = b.ancestorPath " // TODO and a.additionalRelationshipType = b.additionalRelationshipType "
                  + " and a.node.id < b.node.id");
        } else if (organizingClassType == IdType.CONCEPT) {
          hQuery = session.createQuery(
              "select a.node, b.node, a.additionalRelationshipType from ConceptTreePositionJpa a, ConceptTreePositionJpa b "
                  + " where a.ancestorPath = b.ancestorPath " // TODO and a.additionalRelationshipType = b.additionalRelationshipType "
                  + " and a.node.id < b.node.id");
        } else if (organizingClassType == IdType.CODE) {
          hQuery = session.createQuery(
              "select a.node, b.node, a.additionalRelationshipType from CodeTreePositionJpa a, CodeTreePositionJpa b "
                  + " where a.ancestorPath = b.ancestorPath " // TODO and a.additionalRelationshipType = b.additionalRelationshipType "
                  + " and a.node.id < b.node.id");
        } else if (organizingClassType == IdType.DESCRIPTOR) {
          hQuery = session.createQuery(
              "select a.node, b.node, a.additionalRelationshipType from DescriptorTreePositionJpa a, DescriptorTreePositionJpa b "
                  + " where a.ancestorPath = b.ancestorPath " // TODO and a.additionalRelationshipType = b.additionalRelationshipType "
                  + " and a.node.id < b.node.id");
        }

        hQuery.setReadOnly(true).setFetchSize(1000);
        ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
        while (results.next()) {
          final Concept from = (Concept) results.get()[0];
          final Concept to = (Concept) results.get()[1];
          final AdditionalRelationshipType addRelType = (AdditionalRelationshipType) results.get()[2];
          Relationship newRel = null;
          if (organizingClassType == IdType.ATOM) {
            newRel = new AtomRelationshipJpa();
          } else if (organizingClassType == IdType.CONCEPT) {
            newRel = new ConceptRelationshipJpa();
          } else if (organizingClassType == IdType.CODE) {
            newRel = new CodeRelationshipJpa();
          } else if (organizingClassType == IdType.DESCRIPTOR) {
            newRel = new DescriptorRelationshipJpa();
          }
          newRel.setFrom(from);
          newRel.setTo(to);
          // TODO remove faked row
          newRel.setAdditionalRelationshipType(relToInverseMap.get("analyzed_by"));
          //newRel.setAdditionalRelationshipType(addRelType.getAbbreviation());
          newRel.setTerminology(term.getTerminology());
          newRel.setVersion(term.getVersion());
          newRel.setRelationshipType("SIB");
          newRel.setPublishable(true);
          newRel.setObsolete(false);
          newRel.setSuppressible(false);
          newRel.setGroup(null);
          
          // TODO check these values
          /*relationship.setTimestamp(releaseVersionDate);
          relationship.setLastModified(releaseVersionDate);
          relationship.setLastModifiedBy(loader);*/
          newRel.setPublished(true);
          newRel.setWorkflowStatus(WorkflowStatus.PUBLISHED);
          newRel.setHierarchical(false);          
          newRel.setAssertedDirection(false);
          newRel.setInferred(true);
          newRel.setStated(true);
          newRel.setTerminologyId("");

/*        2016-11-16_15:18:33.380 ERROR - Column 'terminologyId' cannot be null
          2016-11-16_15:18:33.388 ERROR - HHH000315: Exception executing batch [could not execute batch]
          javax.persistence.RollbackException: Error while committing the transaction
              at org.hibernate.jpa.internal.TransactionImpl.commit(TransactionImpl.java:94)
              at com.wci.umls.server.jpa.services.RootServiceJpa.addObject(RootServiceJpa.java:912)
              at com.wci.umls.server.jpa.services.UmlsIdentityServiceJpa.addRelationshipIdentity(UmlsIdentityServiceJpa.java:750)
              at com.wci.umls.server.jpa.services.handlers.UmlsIdentifierAssignmentHandler.getTerminologyId(UmlsIdentifierAssignmentHandler.java:438)*/
          
          // TODO remove faked row
          //String rui = handler.getTerminologyId(newRel, "SIB", relToInverseMap.get(addRelType.getAbbreviation())); 
          String rui = handler.getTerminologyId(newRel, "SIB", "analyzed"); 
          newRel.setTerminologyId(rui);
           
          addRelationship(newRel);
        }
        commit();
      }
    }

  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        ""
    }, p);

    if (p.getProperty("siblingsThreshold") != null) {
      siblingsThreshold = Integer.parseInt(p.getProperty("siblingsThreshold"));
    }
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() {
    final List<AlgorithmParameter> params = super.getParameters();

    // TODO activityId, last parameter
    AlgorithmParameter param = new AlgorithmParameterJpa("Siblings threshold",
        "siblingsThreshold", "Indicates maximum number of siblings.",
        "e.g. 1000", 0, AlgorithmParameter.Type.INTEGER, "");
    params.add(param);

    return params;
  }

}
