/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.rel;

import java.util.Properties;
import java.util.UUID;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.services.RootService;
import com.wci.umls.server.services.handlers.ComputePreferredNameHandler;

/**
 * Algorithm for computing concept preferred names.
 */
public class ComputeConceptPreferredNamesAlgorithm extends AbstractAlgorithm {

  /**
   * Instantiates an empty {@link ComputeConceptPreferredNamesAlgorithm}.
   *
   * @throws Exception the exception
   */
  public ComputeConceptPreferredNamesAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("COMPUTECONCEPTPREFERREDNAMES");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {

    fireProgressEvent(0, "Progress: " + 0 + "%");
    logInfo("Starting Compute concept preferred names");
    
    int objectCt = 0;
    final Session session = manager.unwrap(Session.class);
    org.hibernate.Query hQuery = session
        .createQuery("select a from ConceptJpa a WHERE a.publishable = true and terminology = :terminology");

    hQuery.setParameter("terminology", getProject().getTerminology());
    hQuery.setReadOnly(true).setFetchSize(2000).setCacheable(false);
    ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    ComputePreferredNameHandler handler = getComputePreferredNameHandler(getProject().getTerminology());
    setMolecularActionFlag(false);  
    while (results.next()) {
      final Concept c = (Concept) results.get()[0];
      String computedName = handler.computePreferredName(c.getAtoms(), getProject().getPrecedenceList());
      if (!computedName.equals(c.getName())) {
        c.setName(computedName);
        updateConcept(c);
      }
      logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
    }  

    logInfo("Finished Compute concept preferred names");
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
    // n/a
  }

}
