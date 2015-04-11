/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.metamodel.EntityType;

import org.apache.log4j.Logger;

import com.wci.umls.server.helpers.ConceptList;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.SearchCriteriaList;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.jpa.content.AbstractComponentHasAttributes;
import com.wci.umls.server.jpa.meta.AbstractAbbreviation;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.TransitiveRelationship;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.handlers.ComputePreferredNameHandler;
import com.wci.umls.server.services.handlers.GraphResolutionHandler;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;

/**
 * JPA enabled implementation of {@link ContentService}.
 */
public class ContentServiceJpa extends MetadataServiceJpa implements ContentService {

  /** The assign identifiers flag. */
  protected boolean assignIdentifiersFlag = false;

  /**
   * Instantiates an empty {@link ContentServiceJpa}.
   *
   * @throws Exception the exception
   */
  public ContentServiceJpa() throws Exception {
    super();
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#getConcepts(java.lang.String, java.lang.String, com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public ConceptList getConcepts(String terminology, String version,
    PfsParameter pfs) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#getConcept(java.lang.Long)
   */
  @Override
  public Concept getConcept(Long id) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#getConcepts(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public ConceptList getConcepts(String terminologyId, String terminology,
    String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#getSingleConcept(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Concept getSingleConcept(String terminologyId, String terminology,
    String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#addConcept(com.wci.umls.server.model.content.Concept)
   */
  @Override
  public Concept addConcept(Concept concept) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#updateConcept(com.wci.umls.server.model.content.Concept)
   */
  @Override
  public void updateConcept(Concept concept) throws Exception {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#removeConcept(java.lang.Long)
   */
  @Override
  public void removeConcept(Long id) throws Exception {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#getDescendantConcepts(com.wci.umls.server.model.content.Concept, com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public ConceptList getDescendantConcepts(Concept concept,
    PfsParameter pfsParameter) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#getAncestorConcepts(com.wci.umls.server.model.content.Concept, com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public ConceptList getAncestorConcepts(Concept concept,
    PfsParameter pfsParameter) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#getChildConcepts(com.wci.umls.server.model.content.Concept, com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public ConceptList getChildConcepts(Concept concept, PfsParameter pfs)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#getAtom(java.lang.Long)
   */
  @Override
  public Atom getAtom(Long id) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#getAtom(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Atom getAtom(String terminologyId, String terminology, String version)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#addAtom(com.wci.umls.server.model.content.Atom)
   */
  @Override
  public Atom addAtom(Atom atom) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#updateAtom(com.wci.umls.server.model.content.Atom)
   */
  @Override
  public void updateAtom(Atom atom) throws Exception {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#removeAtom(java.lang.Long)
   */
  @Override
  public void removeAtom(Long id) throws Exception {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#getRelationship(java.lang.Long)
   */
  @Override
  public Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> getRelationship(
    Long id) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#getRelationship(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> getRelationship(
    String terminologyId, String terminology, String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#addRelationship(com.wci.umls.server.model.content.Relationship)
   */
  @Override
  public Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> addRelationship(
    Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> relationship)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#updateRelationship(com.wci.umls.server.model.content.Relationship)
   */
  @Override
  public void updateRelationship(
    Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> relationship)
    throws Exception {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#removeRelationship(java.lang.Long)
   */
  @Override
  public void removeRelationship(Long id) throws Exception {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#addTransitiveRelationship(com.wci.umls.server.model.content.TransitiveRelationship)
   */
  @Override
  public TransitiveRelationship<? extends ComponentHasAttributes> addTransitiveRelationship(
    TransitiveRelationship<? extends ComponentHasAttributes> transitiveRelationship)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#updateTransitiveRelationship(com.wci.umls.server.model.content.TransitiveRelationship)
   */
  @Override
  public void updateTransitiveRelationship(
    TransitiveRelationship<? extends ComponentHasAttributes> transitiveRelationship)
    throws Exception {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#removeTransitiveRelationship(java.lang.Long)
   */
  @Override
  public void removeTransitiveRelationship(Long id) throws Exception {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#findConceptsForQuery(java.lang.String, java.lang.String, java.lang.String, com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public SearchResultList findConceptsForQuery(String terminology,
    String version, String query, PfsParameter pfs) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#findConceptsForSearchCriteria(java.lang.String, java.lang.String, java.lang.String, com.wci.umls.server.helpers.SearchCriteriaList, com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public SearchResultList findConceptsForSearchCriteria(String terminology,
    String version, String query, SearchCriteriaList criteria, PfsParameter pfs)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#getAllConcepts(java.lang.String, java.lang.String)
   */
  @Override
  public ConceptList getAllConcepts(String terminology, String version) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#getAllRelationshipTerminologyIds(java.lang.String, java.lang.String)
   */
  @Override
  public StringList getAllRelationshipTerminologyIds(String terminology,
    String version) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#getAllAtomTerminologyIds(java.lang.String, java.lang.String)
   */
  @Override
  public StringList getAllAtomTerminologyIds(String terminology, String version) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#getAllLanguageRefSetMemberTerminologyIds(java.lang.String, java.lang.String)
   */
  @Override
  public StringList getAllLanguageRefSetMemberTerminologyIds(
    String terminology, String version) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#getAllSimpleRefSetMemberTerminologyIds(java.lang.String, java.lang.String)
   */
  @Override
  public StringList getAllSimpleRefSetMemberTerminologyIds(String terminology,
    String version) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#clearTransitiveClosure(java.lang.String, java.lang.String)
   */
  @Override
  public void clearTransitiveClosure(String terminology, String version)
    throws Exception {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#clearConcepts(java.lang.String, java.lang.String)
   */
  @Override
  public void clearConcepts(String terminology, String version) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#getGraphResolutionHandler()
   */
  @Override
  public GraphResolutionHandler getGraphResolutionHandler() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#getIdentifierAssignmentHandler(java.lang.String)
   */
  @Override
  public IdentifierAssignmentHandler getIdentifierAssignmentHandler(
    String terminology) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#getComputePreferredNameHandler(java.lang.String)
   */
  @Override
  public ComputePreferredNameHandler getComputePreferredNameHandler(
    String terminology) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ContentService#getComputedPreferredName(com.wci.umls.server.model.content.Concept)
   */
  @Override
  public String getComputedPreferredName(Concept concept) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.services.ContentService#isLastModifiedFlag()
   */
  @Override
  public boolean isLastModifiedFlag() {
    return lastModifiedFlag;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.services.ContentService#setLastModifiedFlag(boolean)
   */
  @Override
  public void setLastModifiedFlag(boolean lastModifiedFlag) {
    this.lastModifiedFlag = lastModifiedFlag;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.services.ContentService#setAssignIdentifiersFlag(boolean)
   */
  @Override
  public void setAssignIdentifiersFlag(boolean assignIdentifiersFlag) {
    this.assignIdentifiersFlag = assignIdentifiersFlag;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.services.ContentService#getContentStats(java.lang.String,
   * java.lang.String)
   */
  @Override
  public Map<String, Integer> getComponentStats(String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).info("Content Service - getComponentStats");
    Map<String, Integer> stats = new HashMap<>();
    for (EntityType<?> type : manager.getMetamodel().getEntities()) {
      String jpaTable = type.getName();
      // Skip audit trail tables
      if (jpaTable.endsWith("_AUD")) {
        continue;
      }
      if (!AbstractAbbreviation.class.isAssignableFrom(type
          .getBindableJavaType())
          && !AbstractComponentHasAttributes.class.isAssignableFrom(type
              .getBindableJavaType())) {
        continue;
      }
      Logger.getLogger(getClass()).info("  " + jpaTable);
      javax.persistence.Query query =
          manager
              .createQuery("select count(*) from "
                  + jpaTable
                  + " where terminology = :terminology and terminologyVersion = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      int ct = ((Long) query.getSingleResult()).intValue();
      stats.put("Total " + jpaTable, ct);
      
      // Only compute active counts for components
      if (AbstractComponentHasAttributes.class.isAssignableFrom(type.getBindableJavaType())) {
        query =
            manager
                .createQuery("select count(*) from "
                    + jpaTable
                    + " where obsolete = 0"
                    + " and terminology = :terminology and terminologyVersion = :version");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);
        ct = ((Long) query.getSingleResult()).intValue();
        stats.put("Non-obsolete " + jpaTable, ct);
      }
    }
    return stats;
  }

}
