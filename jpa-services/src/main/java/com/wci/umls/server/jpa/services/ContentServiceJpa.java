package com.wci.umls.server.jpa.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.metamodel.EntityType;

import com.wci.umls.server.helpers.ConceptList;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.SearchCriteriaList;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.jpa.content.AbstractComponent;
import com.wci.umls.server.jpa.meta.AbstractAbbreviation;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Component;
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
public class ContentServiceJpa extends RootServiceJpa implements ContentService {

  /** The listeners enabled. */
  private boolean listenersEnabled = true;

  /** The config properties. */
  private static Properties config = null;

  /** The last modified flag. */
  protected boolean lastModifiedFlag = false;

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

  @Override
  public void enableListeners() {
    // TODO Auto-generated method stub

  }

  @Override
  public void disableListeners() {
    // TODO Auto-generated method stub

  }

  @Override
  public ConceptList getConcepts(String terminology, String version,
    PfsParameter pfs) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Concept getConcept(Long id) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ConceptList getConcepts(String terminologyId, String terminology,
    String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Concept getSingleConcept(String terminologyId, String terminology,
    String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Concept addConcept(Concept concept) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateConcept(Concept concept) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeConcept(Long id) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public ConceptList getDescendantConcepts(Concept concept,
    PfsParameter pfsParameter) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ConceptList getAncestorConcepts(Concept concept,
    PfsParameter pfsParameter) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ConceptList getChildConcepts(Concept concept, PfsParameter pfs)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Atom getAtom(Long id) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Atom getAtom(String terminologyId, String terminology, String version)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Atom addAtom(Atom atom) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateAtom(Atom atom) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeAtom(Long id) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public Relationship<? extends Component, ? extends Component> getRelationship(
    Long id) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Relationship<? extends Component, ? extends Component> getRelationship(
    String terminologyId, String terminology, String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Relationship<? extends Component, ? extends Component> addRelationship(
    Relationship<? extends Component, ? extends Component> relationship)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateRelationship(
    Relationship<? extends Component, ? extends Component> relationship)
    throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeRelationship(Long id) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public TransitiveRelationship<? extends Component> addTransitiveRelationship(
    TransitiveRelationship<? extends Component> transitiveRelationship)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateTransitiveRelationship(
    TransitiveRelationship<? extends Component> transitiveRelationship)
    throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeTransitiveRelationship(Long id) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public SearchResultList findConceptsForQuery(String terminology,
    String version, String query, PfsParameter pfs) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SearchResultList findConceptsForSearchCriteria(String terminology,
    String version, String query, SearchCriteriaList criteria, PfsParameter pfs)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ConceptList getAllConcepts(String terminology, String version) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public StringList getAllRelationshipTerminologyIds(String terminology,
    String version) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public StringList getAllAtomTerminologyIds(String terminology, String version) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public StringList getAllLanguageRefSetMemberTerminologyIds(
    String terminology, String version) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public StringList getAllSimpleRefSetMemberTerminologyIds(String terminology,
    String version) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void clearTransitiveClosure(String terminology, String version)
    throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void clearConcepts(String terminology, String version) {
    // TODO Auto-generated method stub

  }

  @Override
  public GraphResolutionHandler getGraphResolutionHandler() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IdentifierAssignmentHandler getIdentifierAssignmentHandler(
    String terminology) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ComputePreferredNameHandler getComputePreferredNameHandler(
    String terminology) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

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
    Map<String, Integer> stats = new HashMap<>();
    for (EntityType<?> type : manager.getMetamodel().getEntities()) {
      String jpaTable = type.getName();
      // Skip audit trail tables
      if (jpaTable.endsWith("_AUD")) {
        continue;
      }
      if (!AbstractAbbreviation.class.isAssignableFrom(type
          .getBindableJavaType())
          && !AbstractComponent.class.isAssignableFrom(type
              .getBindableJavaType())) {
        continue;
      }
      javax.persistence.Query query =
          manager
              .createQuery("select count(*) from "
                  + jpaTable
                  + " where terminology = :terminology and terminologyVersion = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      int ct = ((Long) query.getSingleResult()).intValue();
      stats.put("Total " + jpaTable, ct);
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
    return stats;
  }

}
