/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.meta.AdditionalRelationshipTypeList;
import com.wci.umls.server.helpers.meta.RelationshipTypeList;
import com.wci.umls.server.jpa.content.AtomRelationshipJpa;
import com.wci.umls.server.jpa.content.AtomSubsetJpa;
import com.wci.umls.server.jpa.content.AtomSubsetMemberJpa;
import com.wci.umls.server.jpa.content.CodeRelationshipJpa;
import com.wci.umls.server.jpa.content.CodeTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.content.CodeTreePositionJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetMemberJpa;
import com.wci.umls.server.jpa.content.ConceptTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptTreePositionJpa;
import com.wci.umls.server.jpa.content.DescriptorRelationshipJpa;
import com.wci.umls.server.jpa.content.DescriptorTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.content.DescriptorTreePositionJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;

/**
 * Implementation of an algorithm to compute transitive closure using the
 * {@link ContentService}.
 */
public class RemoveTerminologyAlgorithm extends ContentServiceJpa implements
    Algorithm {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The request cancel flag. */
  boolean requestCancel = false;

  /** The terminology. */
  private String terminology;

  /** The terminology version. */
  private String version;

  /** The id type. */
  private IdType idType;

  /** The cycle tolerant. */
  private boolean cycleTolerant;

  /** The Constant commitCt. */
  private final static int commitCt = 2000;

  /** The Constant logCt. */
  private final static int logCt = 2000;

  /**
   * Instantiates an empty {@link RemoveTerminologyAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public RemoveTerminologyAlgorithm() throws Exception {
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
   * @param version the terminology version
   */
  public void setVersion(String version) {
    this.version = version;
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
    removeTerminology(terminology, version, idType);
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
   * Remove a terminology of a given version.
   *
   * @param terminology the terminology
   * @param version the terminology version
   * @param idType the id type
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private void removeTerminology(String terminology, String version,
    IdType idType) throws Exception {
    
    // Check assumptions/prerequisites
    Logger.getLogger(getClass()).info(
        "Start removing terminology - " + terminology + " " + version);
    fireProgressEvent(0, "Starting...");

    // Disable transaction per operation
    setTransactionPerOperation(false);

    beginTransaction();

    // remove terminology
    javax.persistence.Query query = manager.createQuery("SELECT a.id FROM TerminologyJpa a WHERE terminology = :terminology "
              + " AND version = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
    List<Long> tIds = query.getResultList();
    for (int ct=0; ct<tIds.size(); ct++) {
      removeTerminology(tIds.get(ct));
      logAndCommit(ct, "remove terminology");
    }
    commitClearBegin();
    
    // remove root terminology if all versions removed
    query =
        manager
            .createQuery("SELECT a.id FROM TerminologyJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    tIds = query.getResultList();
    if (tIds.size() == 0) {
      query =
          manager
              .createQuery("SELECT a.id FROM RootTerminologyJpa a WHERE terminology = :terminology ");
      query.setParameter("terminology", terminology);
      List<Long> rtIds = query.getResultList();
      for (int ct = 0; ct < rtIds.size(); ct++) {
        removeRootTerminology(rtIds.get(ct));
        logAndCommit(ct, "remove root terminology");
      }
      commitClearBegin();
    }
    
    // remove property chain
    query = manager.createQuery("SELECT a.id FROM PropertyChainJpa a WHERE terminology = :terminology "
              + " AND version = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
    List<Long> pcIds = query.getResultList();
    for (int ct=0; ct<pcIds.size(); ct++) {
      removePropertyChain(pcIds.get(ct));
      logAndCommit(ct, "remove property chain");
    }
    commitClearBegin();    
    
    // remove attribute names
    query =
        manager
            .createQuery("SELECT a.id FROM AttributeNameJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> atnIds = query.getResultList();
    for (int ct = 0; ct < atnIds.size(); ct++) {
      removeAttributeName(atnIds.get(ct));
      logAndCommit(ct, "remove attribute names");
    }
    commitClearBegin();
    
    // remove additional relationship type
    // load all and set inverse_id to null and then update
    // then go through all and remove each
    AdditionalRelationshipTypeList list = 
        getAdditionalRelationshipTypes(terminology, version);
    for (AdditionalRelationshipType relType : list.getObjects()) {
      relType.setInverseType(null);
      updateAdditionalRelationshipType(relType);
    }
    commitClearBegin();
    
    query =
        manager
            .createQuery("SELECT a.id FROM AdditionalRelationshipTypeJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> artIds = query.getResultList();
    for (int ct = 0; ct < artIds.size(); ct++) {
      removeAdditionalRelationshipType(artIds.get(ct));
      logAndCommit(ct, "remove additional relationship types");
    }
    commitClearBegin();
    
    // remove general metadata entry
    query =
        manager
            .createQuery("SELECT a.id FROM GeneralMetadataEntryJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> gmeIds = query.getResultList();
    for (int ct = 0; ct < gmeIds.size(); ct++) {
      removeGeneralMetadataEntry(gmeIds.get(ct));
      logAndCommit(ct, "remove general metadata entry");
    }
    commitClearBegin();
    
    // remove semantic types
    query =
        manager
            .createQuery("SELECT a.id FROM SemanticTypeJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> stIds = query.getResultList();
    for (int ct = 0; ct < stIds.size(); ct++) {
      removeSemanticType(stIds.get(ct));
      logAndCommit(ct, "remove semantic types");
    }
    commitClearBegin();
    
    // remove term types
    query =
        manager
            .createQuery("SELECT a.id FROM TermTypeJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> ttIds = query.getResultList();
    for (int ct = 0; ct < ttIds.size(); ct++) {
      removeTermType(ttIds.get(ct));
      logAndCommit(ct, "remove term types");
    }
    commitClearBegin();
    
    // remove  relationship type
    // load all and set inverse_id to null and then update
    // then go through all and remove each
    RelationshipTypeList list2 = 
        getRelationshipTypes(terminology, version);
    for (RelationshipType relType : list2.getObjects()) {
      relType.setInverse(null);
      updateRelationshipType(relType);
    }
    commitClearBegin();
    
    query =
        manager
            .createQuery("SELECT a.id FROM RelationshipTypeJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> rtIds = query.getResultList();
    for (int ct = 0; ct < rtIds.size(); ct++) {
      removeRelationshipType(rtIds.get(ct));
      logAndCommit(ct, "remove relationship types");
    }
    commitClearBegin();
    
    // remove languages
    query =
        manager
            .createQuery("SELECT a.id FROM LanguageJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> lIds = query.getResultList();
    for (int ct = 0; ct < lIds.size(); ct++) {
      removeLanguage(lIds.get(ct));
      logAndCommit(ct, "remove languages");
    }
    commitClearBegin();
    
    // remove concept subset members
    query = manager.createQuery("SELECT a.id FROM ConceptSubsetMemberJpa a WHERE terminology = :terminology "
              + " AND version = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
    List<Long> csmIds = query.getResultList();
    for (int ct = 0; ct < csmIds.size(); ct++) {
      removeSubsetMember(csmIds.get(ct), ConceptSubsetMemberJpa.class);
      logAndCommit(ct, "remove subset members");
    }
    commitClearBegin();

    // remove concept subsets
    query =
        manager
            .createQuery("SELECT a.id FROM ConceptSubsetJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> csIds = query.getResultList();
    for (int ct = 0; ct < csIds.size(); ct++) {
      removeSubset(csIds.get(ct), ConceptSubsetJpa.class);
      logAndCommit(ct, "remove concept subsets");
    }
    commitClearBegin();

    // remove atom subset members
    query =
        manager
            .createQuery("SELECT a.id FROM AtomSubsetMemberJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> asmIds = query.getResultList();
    for (int ct = 0; ct < asmIds.size(); ct++) {
      removeSubsetMember(asmIds.get(ct), AtomSubsetMemberJpa.class);
      logAndCommit(ct, "remove atom subset members");
    }
    commitClearBegin();

    // remove atom subsets
    query =
        manager
            .createQuery("SELECT a.id FROM AtomSubsetJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> amIds = query.getResultList();
    for (int ct = 0; ct < amIds.size(); ct++) {
      removeSubset(amIds.get(ct), AtomSubsetJpa.class);
      logAndCommit(ct, "remove atom subsets");
    }
    commitClearBegin();

    // remove concept relationships
    query =
        manager
            .createQuery("SELECT a.id FROM ConceptRelationshipJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> crIds = query.getResultList();
    for (int ct = 0; ct < crIds.size(); ct++) {
      removeRelationship(crIds.get(ct), ConceptRelationshipJpa.class);
      logAndCommit(ct, "remove concept relationships");
    }
    commitClearBegin();

    // remove definitions from the concepts; definitions cannot be removed yet,
    // because they are used by atoms
    query =
        manager
            .createQuery("SELECT a.id FROM ConceptJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> cIds = query.getResultList();
    for (int ct = 0; ct < cIds.size(); ct++) {
      Concept c = getConcept(cIds.get(ct));
      c.setDefinitions(new ArrayList<Definition>());
      updateConcept(c);
      logAndCommit(ct, "remove definitions from concepts");
    }
    commitClearBegin();

    // remove the concept transitive relationships
    query =
        manager
            .createQuery("SELECT a.id FROM ConceptTransitiveRelationshipJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> ctrIds = query.getResultList();
    for (int ct = 0; ct < ctrIds.size(); ct++) {
      removeTransitiveRelationship(ctrIds.get(ct),
          ConceptTransitiveRelationshipJpa.class);
      logAndCommit(ct, "remove concept transitive relationships");
    }
    commitClearBegin();

    // remove the concept tree positions
    query =
        manager
            .createQuery("SELECT a.id FROM ConceptTreePositionJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> ctpIds = query.getResultList();
    for (int ct = 0; ct < ctpIds.size(); ct++) {
      removeTreePosition(ctpIds.get(ct), ConceptTreePositionJpa.class);
      logAndCommit(ct, "remove concept tree positions");
    }
    commitClearBegin();

    // remove the concepts
    for (int ct = 0; ct < cIds.size(); ct++) {
      removeConcept(cIds.get(ct));
      logAndCommit(ct, "remove concepts");
    }
    commitClearBegin();

    // go through all remaining concepts and remove atoms with matching
    // terminology and version
    // concepts may have UMLS terminology and matching atoms wouldn't otherwise
    // be removed
    // not using this code bc of invalid field names
    /*
     * SearchResultList results = findConceptsForQuery(null, null, Branch.ROOT,
     * "atoms.terminology:" + terminology + " atoms.version:" + version, new
     * PfscParameterJpa()); for (SearchResult result : results.getObjects()) {
     * Concept concept = getConcept(result.getId());
     */
    query = manager.createQuery("SELECT a.id FROM ConceptJpa a");
    List<Long> allConceptIds = query.getResultList();
    for (int ct = 0; ct < allConceptIds.size(); ct++) {
      Concept concept = getConcept(allConceptIds.get(ct));
      List<Atom> keepAtoms = new ArrayList<Atom>();
      for (Atom atom : concept.getAtoms()) {
        if (!atom.getTerminology().equals(terminology)
            || !atom.getVersion().equals(version)) {
          keepAtoms.add(atom);
        }
      }
      concept.setAtoms(keepAtoms);
      updateConcept(concept);
      logAndCommit(ct, "remove atoms from remaining concepts");
    }
    commitClearBegin();

    // remove definitions from the atoms
    query =
        manager
            .createQuery("SELECT a.id FROM AtomJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> aIds = query.getResultList();
    for (int ct = 0; ct < aIds.size(); ct++) {
      Atom a = getAtom(aIds.get(ct));
      a.setDefinitions(new ArrayList<Definition>());
      updateAtom(a);
      logAndCommit(ct, "remove definitions from atoms");
    }
    commitClearBegin();

    // remove atom relationships
    query =
        manager
            .createQuery("SELECT a.id FROM AtomRelationshipJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> arIds = query.getResultList();
    for (int ct = 0; ct < arIds.size(); ct++) {
      removeRelationship(arIds.get(ct), AtomRelationshipJpa.class);
      logAndCommit(ct, "remove atom relationships");
    }
    commitClearBegin();

    // remove descriptor relationships
    query =
        manager
            .createQuery("SELECT a.id FROM DescriptorRelationshipJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> drIds = query.getResultList();
    for (int ct = 0; ct < drIds.size(); ct++) {
      removeRelationship(drIds.get(ct), DescriptorRelationshipJpa.class);
      logAndCommit(ct, "remove descriptor relationships");
    }
    commitClearBegin();

    // remove the descriptor transitive relationships
    query =
        manager
            .createQuery("SELECT a.id FROM DescriptorTransitiveRelationshipJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> dtrIds = query.getResultList();
    for (int ct = 0; ct < dtrIds.size(); ct++) {
      removeTransitiveRelationship(dtrIds.get(ct),
          DescriptorTransitiveRelationshipJpa.class);
      logAndCommit(ct, "remove descriptor transitive relationships");
    }
    commitClearBegin();

    // remove the descriptor tree positions
    query =
        manager
            .createQuery("SELECT a.id FROM DescriptorTreePositionJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> dtpIds = query.getResultList();
    for (int ct = 0; ct < dtpIds.size(); ct++) {
      removeTreePosition(dtpIds.get(ct), DescriptorTreePositionJpa.class);
      logAndCommit(ct, "remove descriptor tree positions");
    }
    commitClearBegin();

    // remove descriptors
    query =
        manager
            .createQuery("SELECT a.id FROM DescriptorJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> dscIds = query.getResultList();
    for (int ct = 0; ct < dscIds.size(); ct++) {
      removeDescriptor(dscIds.get(ct));
      logAndCommit(ct, "remove descriptors");
    }
    commitClearBegin();

    // remove code relationships
    query =
        manager
            .createQuery("SELECT a.id FROM CodeRelationshipJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> cdrIds = query.getResultList();
    for (int ct = 0; ct < cdrIds.size(); ct++) {
      removeRelationship(cdrIds.get(ct), CodeRelationshipJpa.class);
      logAndCommit(ct, "remove code relationships");
    }
    commitClearBegin();

    // remove the code transitive relationships
    query =
        manager
            .createQuery("SELECT a.id FROM CodeTransitiveRelationshipJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> cdtrIds = query.getResultList();
    for (int ct = 0; ct < cdtrIds.size(); ct++) {
      removeTransitiveRelationship(cdtrIds.get(ct),
          CodeTransitiveRelationshipJpa.class);
      logAndCommit(ct, "remove code transitive relationships");
    }
    commitClearBegin();

    // remove the code tree positions
    query =
        manager
            .createQuery("SELECT a.id FROM CodeTreePositionJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> cdtpIds = query.getResultList();
    for (int ct = 0; ct < cdtpIds.size(); ct++) {
      removeTreePosition(cdtpIds.get(ct), CodeTreePositionJpa.class);
      logAndCommit(ct, "remove tree positions");
    }
    commitClearBegin();

    // remove codes
    query =
        manager
            .createQuery("SELECT a.id FROM CodeJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> c = query.getResultList();
    for (int ct = 0; ct < c.size(); ct++) {
      removeCode(c.get(ct));
      logAndCommit(ct, "remove codes");
    }
    commitClearBegin();

    // remove atoms - don't do this until after removing codes
    for (int ct = 0; ct < aIds.size(); ct++) {
      removeAtom(aIds.get(ct));
      logAndCommit(ct, "remove atoms");
    }
    commitClearBegin();

    // remove semantic type components, definitions and attributes last
    query =
        manager
            .createQuery("SELECT a.id FROM SemanticTypeComponentJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> stcIds = query.getResultList();
    for (int ct=0; ct<stcIds.size(); ct++) {
      removeSemanticTypeComponent(stcIds.get(ct));
      logAndCommit(ct, "remove semantic type components");
    }
    commitClearBegin();

    // remove the definitions
    query =
        manager
            .createQuery("SELECT a.id FROM DefinitionJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> dIds = query.getResultList();
    for (int ct = 0; ct < dIds.size(); ct++) {
      removeDefinition(dIds.get(ct));
      logAndCommit(ct, "remove definitions");
    }
    commitClearBegin();

    // remove the attributes
    query =
        manager
            .createQuery("SELECT a.id FROM AttributeJpa a WHERE terminology = :terminology "
                + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    List<Long> attIds = query.getResultList();
    for (int ct = 0; ct < attIds.size(); ct++) {
      removeAttribute(attIds.get(ct));
      logAndCommit(ct, "remove attributes");
    }
    commitClearBegin();

    commit();
    clear();

    Logger.getLogger(getClass()).info(
        "Finished computing transitive closure ... " + new Date());
    // set the transaction strategy based on status starting this routine
    // setTransactionPerOperation(currentTransactionStrategy);
    fireProgressEvent(100, "Finished...");
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
   * @param message the message
   * @throws Exception the exception
   */
  private void logAndCommit(int objectCt, String message) throws Exception {
    // log at regular intervals
    if (objectCt % logCt == 0) {
      Logger.getLogger(getClass()).info(
          "    count = " + objectCt + " " + message);
    }
    if (objectCt % commitCt == 0) {
      commitClearBegin();
    }
  }

}
