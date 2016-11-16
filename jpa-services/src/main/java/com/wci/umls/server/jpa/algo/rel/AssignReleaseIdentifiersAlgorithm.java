/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.rel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.meta.AdditionalRelationshipTypeList;
import com.wci.umls.server.helpers.meta.RelationshipTypeList;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.services.RootService;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;

/**
 * Algorithm for assigning release identifiers.
 */
public class AssignReleaseIdentifiersAlgorithm extends AbstractAlgorithm {

  /**
   * Instantiates an empty {@link AssignReleaseIdentifiersAlgorithm}.
   *
   * @throws Exception the exception
   */
  public AssignReleaseIdentifiersAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("ASSIGNRELEASEIDS");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {

    logInfo("Starting Assign release identifiers");
    fireProgressEvent(0, "Starting progress: " + 0 + "%");

    // Assign CUIs:
    //•   TODO: we need to come back and do a better job here.
    //atom.getConceptTerminologyIds().get(project.getTerminology()) -> “last release cui”
    //Consider writing out the preferred atom of each data structure (CUI, SCUI, SDUI, CODE, RUI) -> for later use
    IdentifierAssignmentHandler handler = getIdentifierAssignmentHandler(getProject().getTerminology());
    
    javax.persistence.Query query =
        manager.createQuery("select c from ConceptJpa c "
            + "where c.terminologyId = c.id and c.publishable = true and terminology = :terminology");

    query.setParameter("terminology", getProject().getTerminology());
    List<Concept> newConcepts = query.getResultList();

    setMolecularActionFlag(false);
    for (final Concept concept : newConcepts) {
      concept.setTerminologyId(handler.getTerminologyId(concept));
      updateConcept(concept);
    }
    fireProgressEvent(0, "Starting progress: " + 33 + "%");
       
    //Assign RUIs to concept relationships
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
    //  Iterate through all concept relationships
    int objectCt = 0;
    // NOTE: Hibernate-specific to support iterating
    final Session session = manager.unwrap(Session.class);
    org.hibernate.Query hQuery = session
        .createQuery("select a from ConceptRelationshipJpa a WHERE a.publishable = true and terminology = :terminology");
    
    hQuery.setParameter("terminology", getProject().getTerminology());
    hQuery.setReadOnly(true).setFetchSize(1000);
    ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {
      final ConceptRelationship rel = (ConceptRelationship) results.get()[0];

      String origRui = rel.getTerminologyId();
      rel.setTerminologyId("");
      // TODO (below) Returned object 1: 156209448
      /*2016-11-11_14:08:47.061 INFO - Returned object 2: 156209450
      java.lang.Exception: Error: query returned more than one id: RelationshipIdentityJpa, terminology:UMLS AND NOT terminologyId:[* TO *] AND relationshipType:RN AND NOT additionalRelationshipType:[* TO *] AND fromId:C4048332 AND fromType:CONCEPT AND fromTerminology:UMLS AND toId:C0040558 AND toType:CONCEPT AND toTerminology:UMLS
          at com.wci.umls.server.jpa.services.UmlsIdentityServiceJpa.getIdentityId(UmlsIdentityServiceJpa.java:822)
          at com.wci.umls.server.jpa.services.UmlsIdentityServiceJpa.getRelationshipIdentity(UmlsIdentityServiceJpa.java:731)
          at com.wci.umls.server.jpa.services.handlers.UmlsIdentifierAssignmentHandler.getTerminologyId(UmlsIdentifierAssignmentHandler.java:421)
          at com.wci.umls.server.jpa.algo.rel.AssignReleaseIdentifiersAlgorithm.compute(AssignReleaseIdentifiersAlgorithm.java:106)*/
      String rui = handler.getTerminologyId(rel, relToInverseMap.get(rel.getRelationshipType()), 
        relToInverseMap.get(rel.getAdditionalRelationshipType())); 
      if (!origRui.equals(rui)) {
        rel.setTerminologyId(rui);
        updateRelationship(rel);
      }
      logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();
    fireProgressEvent(0, "Starting progress: " + 66 + "%");
    
    // Assign ATUIs for semantic types
    objectCt = 0;
    hQuery = session
        .createQuery("select a from ConceptJpa a WHERE a.publishable = true and terminology = :terminology");

    hQuery.setParameter("terminology", getProject().getTerminology());
    hQuery.setReadOnly(true).setFetchSize(1000);
    results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {
      final Concept c = (Concept) results.get()[0];
    
      //For each semantic type component (e.g. concept.getSemanticTypes())
      for (SemanticTypeComponent sty : c.getSemanticTypes()) {
        String origAtui = sty.getTerminologyId();
        sty.setTerminologyId("");
        // TODO (below) java.lang.NullPointerException
        //at com.wci.umls.server.jpa.services.handlers.UmlsIdentifierAssignmentHandler.getTerminologyId(UmlsIdentifierAssignmentHandler.java:588)
        //at com.wci.umls.server.jpa.algo.rel.AssignReleaseIdentifiersAlgorithm.compute(AssignReleaseIdentifiersAlgorithm.java:132)
        
        String atui = handler.getTerminologyId(sty, c);
        if (!origAtui.equals(atui)) {
          sty.setTerminologyId(atui);
          updateSemanticTypeComponent(sty, c);
        }
        logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
      }
    }  

    logInfo("Finished Assign release identifiers");
    fireProgressEvent(100, "Progress: " + 100 + "%");
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
  }

}
