/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.AtomRelationshipJpa;
import com.wci.umls.server.jpa.content.AtomSubsetJpa;
import com.wci.umls.server.jpa.content.AtomSubsetMemberJpa;
import com.wci.umls.server.jpa.content.AtomTreePositionJpa;
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
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.GeneralMetadataEntry;
import com.wci.umls.server.model.meta.Language;
import com.wci.umls.server.model.meta.PropertyChain;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.RootService;

/**
 * Implementation of an algorithm to compute transitive closure using the
 * {@link ContentService}.
 */
public class RemoveTerminologyAlgorithm extends AbstractAlgorithm {

  /**
   * Standalone means that it is not also represented as part of a
   * metathesaurus. Default is true.
   */
  private boolean standalone = true;

  /**
   * There are some situations when all of a terminology's contents want to be
   * removed, but the actual terminology itself wants to be kept. Default is
   * false.
   */
  private boolean keepTerminology = false;

  /**
   * Instantiates an empty {@link RemoveTerminologyAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public RemoveTerminologyAlgorithm() throws Exception {
    super();
  }

  /**
   * Sets the standalone flag. For deleting a UMLS terminology, this would be
   * set to false so that the atoms would be removed also from UMLS concepts.
   *
   * @param standalone the standalone
   */
  public void setStandalone(boolean standalone) {
    this.standalone = standalone;
  }

  /**
   * Sets the keep terminology flag. For deleting a terminology's contents,
   * without removing the terminology itself.
   *
   * @param keepTerminology the keepTerminology
   */
  public void setKeepTerminology(boolean keepTerminology) {
    this.keepTerminology = keepTerminology;
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    removeTerminology(getTerminology(), getVersion());
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a
  }

  /**
   * Remove a terminology of a given version.
   *
   * @param terminology the terminology
   * @param version the version
   * @throws Exception the exception
   */
  @SuppressWarnings({
      "unchecked", "rawtypes"
  })
  private void removeTerminology(String terminology, String version) throws Exception {

    // NOTE: do not change the order of calls, they are tuned
    // to properly handle foreign key dependencies.

    // Check assumptions/prerequisites
    Logger.getLogger(getClass())
        .info("Start removing terminology - " + terminology + " " + version);
    fireProgressEvent(0, "Starting...");

    setLastModifiedFlag(false);
    setMolecularActionFlag(false);
    setTransactionPerOperation(false);
    beginTransaction();

    // remove terminology
    if (!keepTerminology) {
      logInfo("  Remove terminology");
      Terminology t = getTerminology(terminology, version);
      if (t != null) {
        logInfo("  remove terminology = " + t.getTerminology() + ", " + version);
        removeTerminology(t.getId());
        commitClearBegin();
        commitClearBegin();
      }
    }

    // remove root terminology if all versions removed
    if (!keepTerminology) {
      logInfo("  Remove root terminology");
      for (final RootTerminology root : getRootTerminologies().getObjects()) {
        if (root.getTerminology().equals(terminology)) {
          Logger.getLogger(getClass()).info("  remove root terminology = " + root.getTerminology());
          removeRootTerminology(root.getId());
          break;
        }
      }
      commitClearBegin();
    }

    // remove property chain
    // Need to use query for metadata to override what the handler may be doing
    // this specifically means to remove things with this terminology/version
    // tuple
    logInfo("  Remove property chains");
    Query query =
        manager.createQuery("SELECT a FROM PropertyChainJpa a WHERE terminology = :terminology "
            + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    for (final PropertyChain chain : (List<PropertyChain>) query.getResultList()) {
      logInfo("  remove property chain = " + chain);
      removePropertyChain(chain.getId());

    }
    commitClearBegin();

    // remove attribute names
    logInfo("  Remove attribute names");
    query = manager.createQuery("SELECT a FROM AttributeNameJpa a WHERE terminology = :terminology "
        + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    for (final AttributeName name : (List<AttributeName>) query.getResultList()) {
      logInfo("  remove attribute name = " + name);
      removeAttributeName(name.getId());
    }
    commitClearBegin();

    // remove additional relationship type
    logInfo("  Remove additional relationship types");
    query = manager.createQuery(
        "SELECT a FROM AdditionalRelationshipTypeJpa a WHERE terminology = :terminology "
            + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    for (final AdditionalRelationshipType rela : (List<AdditionalRelationshipType>) query
        .getResultList()) {
      logInfo("  set inverse to null = " + rela);
      rela.setInverse(null);
      updateAdditionalRelationshipType(rela);
    }
    commitClearBegin();
    query = manager.createQuery(
        "SELECT a FROM AdditionalRelationshipTypeJpa a WHERE terminology = :terminology "
            + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    for (final AdditionalRelationshipType rela : (List<AdditionalRelationshipType>) query
        .getResultList()) {
      Logger.getLogger(getClass()).info("  remove additional relationship type = " + rela);
      removeAdditionalRelationshipType(rela.getId());
    }
    commitClearBegin();

    // remove general metadata entry
    logInfo("  Remove general metadata");
    query = manager
        .createQuery("SELECT a FROM GeneralMetadataEntryJpa a WHERE terminology = :terminology "
            + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    for (final GeneralMetadataEntry entry : (List<GeneralMetadataEntry>) query.getResultList()) {
      Logger.getLogger(getClass()).info("  remove general metadata entry = " + entry);
      removeGeneralMetadataEntry(entry.getId());
    }
    commitClearBegin();

    // remove semantic types
    logInfo("  Remove semantic types");
    query = manager.createQuery("SELECT a FROM SemanticTypeJpa a WHERE terminology = :terminology "
        + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    for (final SemanticType sty : (List<SemanticType>) query.getResultList()) {
      removeSemanticType(sty.getId());
    }
    commitClearBegin();

    // remove term types
    logInfo("  Remove term types");
    query = manager.createQuery("SELECT a FROM TermTypeJpa a WHERE terminology = :terminology "
        + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    for (final TermType tty : (List<TermType>) query.getResultList()) {
      logInfo("  remove term types = " + tty);
      removeTermType(tty.getId());
    }
    commitClearBegin();

    // remove relationship type
    logInfo("  Remove relationship types");
    query =
        manager.createQuery("SELECT a FROM RelationshipTypeJpa a WHERE terminology = :terminology "
            + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    for (final RelationshipType rel : (List<RelationshipType>) query.getResultList()) {
      logInfo("  set inverse to null = " + rel);
      rel.setInverse(null);
      updateRelationshipType(rel);
    }
    commitClearBegin();
    query =
        manager.createQuery("SELECT a FROM RelationshipTypeJpa a WHERE terminology = :terminology "
            + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    for (final RelationshipType rel : (List<RelationshipType>) query.getResultList()) {
      logInfo("  remove relationship type = " + rel);
      removeRelationshipType(rel.getId());
    }
    commitClearBegin();

    // remove languages
    logInfo("  Remove languages");
    query = manager.createQuery("SELECT a FROM LanguageJpa a WHERE terminology = :terminology "
        + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    for (final Language lat : (List<Language>) query.getResultList()) {
      logInfo("  remove languages = " + lat);
      removeLanguage(lat.getId());
    }
    commitClearBegin();

    // remove concept subset members
    logInfo("  Remove concept subset members");
    query = manager
        .createQuery("SELECT a.id FROM ConceptSubsetMemberJpa a WHERE terminology = :terminology "
            + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    int ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      removeSubsetMember(id, ConceptSubsetMemberJpa.class);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove concept subsets
    logInfo("  Remove concept subsets");
    query =
        manager.createQuery("SELECT a.id FROM ConceptSubsetJpa a WHERE terminology = :terminology "
            + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      removeSubset(id, ConceptSubsetJpa.class);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove atom subset members
    logInfo("  Remove atom subset members");
    query = manager
        .createQuery("SELECT a.id FROM AtomSubsetMemberJpa a WHERE terminology = :terminology "
            + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      removeSubsetMember(id, AtomSubsetMemberJpa.class);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove atom subsets
    logInfo("  Remove atom subsets");
    query = manager.createQuery("SELECT a.id FROM AtomSubsetJpa a WHERE terminology = :terminology "
        + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      removeSubset(id, AtomSubsetJpa.class);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove concept relationships
    logInfo("  Remove " + terminology + "/" + version + " concept relationships");
    query = manager
        .createQuery("SELECT a.id FROM ConceptRelationshipJpa a WHERE terminology = :terminology "
            + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      removeRelationship(id, ConceptRelationshipJpa.class);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove concept relationships from terminology atoms
    // This will catch any other terminology's relationships that may be on
    // the concept (such as MED-RT)
    logInfo("  Remove concept relationships from " + terminology + "/" + version + " concepts");
    query = manager.createQuery("SELECT a.id FROM ConceptJpa a WHERE terminology = :terminology "
        + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      Concept concept = getConcept(id);
      Concept relatedConcept = null;
      for (final ConceptRelationship conceptRelationship : new ArrayList<>(
          concept.getRelationships())) {

        ConceptRelationship inverseRelationship =
            (ConceptRelationship) getInverseRelationship(getProject().getTerminology(),
                getProject().getVersion(), conceptRelationship);
        relatedConcept = conceptRelationship.getTo();

        concept.getRelationships().remove(conceptRelationship);
        relatedConcept.getRelationships().remove(inverseRelationship);

        updateConcept(concept);
        updateConcept(relatedConcept);

        removeRelationship(conceptRelationship.getId(), ConceptRelationshipJpa.class);
        removeRelationship(inverseRelationship.getId(), ConceptRelationshipJpa.class);
      }
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove definitions from the concepts; definitions cannot be removed yet,
    // because they are used by atoms
    logInfo("  Remove definitions");
    query = manager.createQuery("SELECT a.id FROM ConceptJpa a WHERE terminology = :terminology "
        + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      Concept c = getConcept(id);
      c.setDefinitions(new ArrayList<Definition>());
      updateConcept(c);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove the concept transitive relationships
    Logger.getLogger(getClass()).info("  Remove concept transitive relationships");
    query = manager.createQuery(
        "SELECT a.id FROM ConceptTransitiveRelationshipJpa a WHERE terminology = :terminology "
            + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      removeTransitiveRelationship(id, ConceptTransitiveRelationshipJpa.class);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove the concept tree positions
    logInfo("  Remove concept tree positions");
    query = manager
        .createQuery("SELECT a.id FROM ConceptTreePositionJpa a WHERE terminology = :terminology "
            + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      removeTreePosition(id, ConceptTreePositionJpa.class);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove the concepts
    logInfo("  Remove concepts");
    query = manager.createQuery("SELECT a.id FROM ConceptJpa a WHERE terminology = :terminology "
        + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      removeConcept(id);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    if (!standalone) {
      // go through all remaining concepts and remove atoms with matching
      // terminology and version
      // concepts may have UMLS terminology and matching atoms wouldn't
      // otherwise
      // be removed
      // not using this code bc of invalid field names
      /*
       * SearchResultList results = findConcepts(null, null, Branch.ROOT,
       * "atoms.terminology:" + terminology + " atoms.version:" + version, new
       * PfsParameterJpa()); for (SearchResult result : results.getObjects()) {
       * Concept concept = getConcept(result.getId());
       */
      query = manager.createQuery("SELECT a.id FROM ConceptJpa a");
      ct = 0;
      for (final Long id : (List<Long>) query.getResultList()) {
        Concept concept = getConcept(id);
        List<Atom> keepAtoms = new ArrayList<Atom>();
        for (final Atom atom : concept.getAtoms()) {
          if (!atom.getTerminology().equals(terminology) || !atom.getVersion().equals(version)) {
            keepAtoms.add(atom);
          }
        }
        concept.setAtoms(keepAtoms);
        updateConcept(concept);
        logAndCommit(++ct, RootService.logCt, RootService.commitCt);
      }
      commitClearBegin();
    }

    // remove definitions from the atoms
    logInfo("  Remove definitions from atoms");
    query = manager.createQuery(
        "SELECT a.id FROM AtomJpa a WHERE terminology = :terminology " + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      Atom a = getAtom(id);
      a.setDefinitions(new ArrayList<Definition>());
      updateAtom(a);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove terminology atom relationships
    logInfo("  Remove " + terminology + "/" + version + " atom relationships");
    query = manager
        .createQuery("SELECT a.id FROM AtomRelationshipJpa a WHERE terminology = :terminology "
            + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      AtomRelationship atomRelationship =
          (AtomRelationshipJpa) getRelationship(id, AtomRelationshipJpa.class);
      Atom fromAtom = getAtom(atomRelationship.getFrom().getId());

      fromAtom.getRelationships().remove(atomRelationship);
      updateAtom(fromAtom);

      removeRelationship(atomRelationship.getId(), AtomRelationshipJpa.class);

      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove atom relationships from terminology atoms
    // This will catch any non-source-terminology relationships that may be on
    // the atom (such as demotions)
    logInfo("  Remove atom relationships from " + terminology + "/" + version + " atoms");
    query = manager.createQuery(
        "SELECT a.id FROM AtomJpa a join a.relationships r WHERE a.terminology = :terminology "
            + " AND a.version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    logInfo("  " + query.getResultList().size() + " atoms with atom relationships identified");

    for (final Long id : (List<Long>) query.getResultList()) {
      Atom atom = getAtom(id);
      Atom relatedAtom = null;
      for (final AtomRelationship atomRelationship : new ArrayList<>(atom.getRelationships())) {
        logInfo("  starting to remove " + atom.getRelationships().size()
            + " relationships from atom with terminologyId: " + atom.getTerminologyId());

        AtomRelationship inverseRelationship =
            (AtomRelationship) getInverseRelationship(getProject().getTerminology(),
                getProject().getVersion(), atomRelationship);
        relatedAtom = getAtom(atomRelationship.getTo().getId());

        atom.getRelationships().remove(atomRelationship);
        relatedAtom.getRelationships().remove(inverseRelationship);

        removeRelationship(atomRelationship.getId(), AtomRelationshipJpa.class);
        removeRelationship(inverseRelationship.getId(), AtomRelationshipJpa.class);
      }

      if (atom.getRelationships().size() > 0) {
        updateAtom(atom);
        updateAtom(relatedAtom);
      }
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }

    commitClearBegin();

    // Remove last release CUIs from atoms
    logInfo("  Remove last release CUIs");
    query = manager.createQuery(
        "SELECT a.id FROM AtomJpa a where KEY(a.conceptTerminologyIds) = :terminologyVersion");
    query.setParameter("terminologyVersion", terminology + version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      Atom atom = getAtom(id);
      atom.getConceptTerminologyIds().remove(terminology + version);
      updateAtom(atom);

      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    //
    // query = manager.createQuery(
    // "SELECT a.id FROM AtomRelationshipJpa a WHERE terminology = :terminology
    // "
    // + " AND version = :version");
    // query.setParameter("terminology", terminology);
    // query.setParameter("version", version);
    // ct = 0;
    // for (final Long id : (List<Long>) query.getResultList()) {
    // removeRelationship(id, AtomRelationshipJpa.class);
    // logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    // }
    // commitClearBegin();

    // TODO - query a.id, ar.id to to_id
    // TODO - query a.id, ar.id to from_id

    // remove descriptor relationships
    logInfo("  Remove descriptor relationships");
    query = manager.createQuery(
        "SELECT a.id FROM DescriptorRelationshipJpa a WHERE terminology = :terminology "
            + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      removeRelationship(id, DescriptorRelationshipJpa.class);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove the descriptor transitive relationships
    Logger.getLogger(getClass()).info("  Remove descriptor transitive relationships");
    query = manager.createQuery(
        "SELECT a.id FROM DescriptorTransitiveRelationshipJpa a WHERE terminology = :terminology "
            + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      removeTransitiveRelationship(id, DescriptorTransitiveRelationshipJpa.class);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove the descriptor tree positions
    logInfo("  Remove descriptor tree positions");
    query = manager.createQuery(
        "SELECT a.id FROM DescriptorTreePositionJpa a WHERE terminology = :terminology "
            + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      removeTreePosition(id, DescriptorTreePositionJpa.class);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove descriptors
    logInfo("  Remove descriptors");
    query = manager.createQuery("SELECT a.id FROM DescriptorJpa a WHERE terminology = :terminology "
        + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      removeDescriptor(id);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove code relationships
    logInfo("  Remove code relationships");
    query = manager
        .createQuery("SELECT a.id FROM CodeRelationshipJpa a WHERE terminology = :terminology "
            + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      removeRelationship(id, CodeRelationshipJpa.class);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove the code transitive relationships
    logInfo("  Remove code transitive relationships");
    query = manager.createQuery(
        "SELECT a.id FROM CodeTransitiveRelationshipJpa a WHERE terminology = :terminology "
            + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      removeTransitiveRelationship(id, CodeTransitiveRelationshipJpa.class);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove the code tree positions
    logInfo("  Remove code tree positions");
    query = manager
        .createQuery("SELECT a.id FROM CodeTreePositionJpa a WHERE terminology = :terminology "
            + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      removeTreePosition(id, CodeTreePositionJpa.class);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove codes
    logInfo("  Remove codes");
    query = manager.createQuery(
        "SELECT a.id FROM CodeJpa a WHERE terminology = :terminology " + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      removeCode(id);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove the atom tree positions
    logInfo("  Remove atom tree positions");
    query = manager
        .createQuery("SELECT a.id FROM AtomTreePositionJpa a WHERE terminology = :terminology "
            + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      removeTreePosition(id, AtomTreePositionJpa.class);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove atoms from any concept/codes/descriptors (they can be added to
    // non-project/terminology ones during an insertion)
    logInfo("  Remove atoms from concepts");
    query = manager.createQuery(
        "SELECT c.id, a.id FROM ConceptJpa c join c.atoms a WHERE a.terminology = :terminology "
            + " AND a.version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Object[] entry : (List<Object[]>) query.getResultList()) {
      final Concept concept = getConcept((Long) entry[0]);
      final Atom atom = getAtom((Long) entry[1]);
      concept.getAtoms().remove(atom);
      updateConcept(concept);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    logInfo("  Remove atoms from codes");
    query = manager.createQuery(
        "SELECT c.id, a.id FROM CodeJpa c join c.atoms a WHERE a.terminology = :terminology "
            + " AND a.version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Object[] entry : (List<Object[]>) query.getResultList()) {
      final Code code = getCode((Long) entry[0]);
      final Atom atom = getAtom((Long) entry[1]);
      code.getAtoms().remove(atom);
      updateCode(code);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    logInfo("  Remove atoms from descriptors");
    query = manager.createQuery(
        "SELECT d.id, a.id FROM DescriptorJpa d join d.atoms a WHERE a.terminology = :terminology "
            + " AND a.version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Object[] entry : (List<Object[]>) query.getResultList()) {
      final Descriptor descriptor = getDescriptor((Long) entry[0]);
      final Atom atom = getAtom((Long) entry[1]);
      descriptor.getAtoms().remove(atom);
      updateDescriptor(descriptor);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove atoms - don't do this until after removing codes
    logInfo("  Remove atoms");
    query = manager.createQuery(
        "SELECT a.id FROM AtomJpa a WHERE terminology = :terminology " + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      removeAtom(id);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove semantic type components, definitions and attributes last
    logInfo("  Remove semantic type components");
    query = manager
        .createQuery("SELECT a.id FROM SemanticTypeComponentJpa a WHERE terminology = :terminology "
            + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      removeSemanticTypeComponent(id);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove the definitions
    // query = manager.createQuery(
    // "SELECT a.id FROM DefinitionJpa a WHERE terminology = :terminology "
    // + " AND version = :version");
    // query.setParameter("terminology", terminology);
    // query.setParameter("version", version);
    // ct = 0;
    // for (final Long id : (List<Long>) query.getResultList()) {
    // removeDefinition(id);
    // logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    // }
    // commitClearBegin();

    logInfo("  Remove definitions from atoms");
    query = manager.createQuery(
        "SELECT a.id, def.id FROM AtomJpa a join a.definitions def WHERE def.terminology = :terminology "
            + " AND def.version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Object[] entry : (List<Object[]>) query.getResultList()) {
      final Atom atom = getAtom((Long) entry[0]);
      final Definition definition = getDefinition((Long) entry[1]);
      atom.getDefinitions().remove(definition);
      updateAtom(atom);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    logInfo("  Remove definitions from concepts");
    query = manager.createQuery(
        "SELECT c.id, def.id FROM ConceptJpa c join c.definitions def WHERE def.terminology = :terminology "
            + " AND def.version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Object[] entry : (List<Object[]>) query.getResultList()) {
      final Concept concept = getConcept((Long) entry[0]);
      final Definition definition = getDefinition((Long) entry[1]);
      concept.getDefinitions().remove(definition);
      updateConcept(concept);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove the mappings
    logInfo("  Remove mappings ");
    query = manager.createQuery("SELECT a.id FROM MappingJpa a WHERE terminology = :terminology "
        + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      removeMapping(id);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove the map Sets
    logInfo("  Remove mapsets ");
    query = manager.createQuery("SELECT a.id FROM MapSetJpa a WHERE terminology = :terminology "
        + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      removeMapSet(id);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove old attributes from updated components (can happen during an
    // insertion)
    logInfo("  Remove attributes from components");
    query = manager.createQuery(
        "SELECT a.id, att.id FROM AtomJpa a join a.attributes att WHERE att.terminology = :terminology "
            + " AND att.version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Object[] entry : (List<Object[]>) query.getResultList()) {
      final Atom atom = getAtom((Long) entry[0]);
      final Attribute attribute = getAttribute((Long) entry[1]);
      atom.getAttributes().remove(attribute);
      updateAtom(atom);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    query = manager.createQuery(
        "SELECT c.id, att.id FROM ConceptJpa c join c.attributes att WHERE att.terminology = :terminology "
            + " AND att.version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Object[] entry : (List<Object[]>) query.getResultList()) {
      final Concept concept = getConcept((Long) entry[0]);
      final Attribute attribute = getAttribute((Long) entry[1]);
      concept.getAttributes().remove(attribute);
      updateConcept(concept);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    query = manager.createQuery(
        "SELECT c.id, att.id FROM CodeJpa c join c.attributes att WHERE att.terminology = :terminology "
            + " AND att.version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Object[] entry : (List<Object[]>) query.getResultList()) {
      final Code code = getCode((Long) entry[0]);
      final Attribute attribute = getAttribute((Long) entry[1]);
      code.getAttributes().remove(attribute);
      updateCode(code);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    query = manager.createQuery(
        "SELECT d.id, att.id FROM DescriptorJpa d join d.attributes att WHERE att.terminology = :terminology "
            + " AND att.version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Object[] entry : (List<Object[]>) query.getResultList()) {
      final Descriptor descriptor = getDescriptor((Long) entry[0]);
      final Attribute attribute = getAttribute((Long) entry[1]);
      descriptor.getAttributes().remove(attribute);
      updateDescriptor(descriptor);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    query = manager.createQuery(
        "SELECT a.id, att.id FROM AtomRelationshipJpa a join a.attributes att WHERE att.terminology = :terminology "
            + " AND att.version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Object[] entry : (List<Object[]>) query.getResultList()) {
      final Relationship relationship = getRelationship((Long) entry[0], AtomRelationshipJpa.class);
      final Attribute attribute = getAttribute((Long) entry[1]);
      relationship.getAttributes().remove(attribute);
      updateRelationship(relationship);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    query = manager.createQuery(
        "SELECT a.id, att.id FROM CodeRelationshipJpa a join a.attributes att WHERE att.terminology = :terminology "
            + " AND att.version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Object[] entry : (List<Object[]>) query.getResultList()) {
      final Relationship relationship = getRelationship((Long) entry[0], CodeRelationshipJpa.class);
      final Attribute attribute = getAttribute((Long) entry[1]);
      relationship.getAttributes().remove(attribute);
      updateRelationship(relationship);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    query = manager.createQuery(
        "SELECT a.id, att.id FROM ConceptRelationshipJpa a join a.attributes att WHERE att.terminology = :terminology "
            + " AND att.version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Object[] entry : (List<Object[]>) query.getResultList()) {
      final Relationship relationship =
          getRelationship((Long) entry[0], ConceptRelationshipJpa.class);
      final Attribute attribute = getAttribute((Long) entry[1]);
      relationship.getAttributes().remove(attribute);
      updateRelationship(relationship);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    query = manager.createQuery(
        "SELECT a.id, att.id FROM DescriptorRelationshipJpa a join a.attributes att WHERE att.terminology = :terminology "
            + " AND att.version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Object[] entry : (List<Object[]>) query.getResultList()) {
      final Relationship relationship =
          getRelationship((Long) entry[0], DescriptorRelationshipJpa.class);
      final Attribute attribute = getAttribute((Long) entry[1]);
      relationship.getAttributes().remove(attribute);
      updateRelationship(relationship);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // remove the attributes
    logInfo("  Remove attributes");
    query = manager.createQuery("SELECT a.id FROM AttributeJpa a WHERE terminology = :terminology "
        + " AND version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    ct = 0;
    for (final Long id : (List<Long>) query.getResultList()) {
      removeAttribute(id);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    Logger.getLogger(getClass()).info("Finished removing attributes ... " + new Date());
    // set the transaction strategy based on status starting this routine
    // setTransactionPerOperation(currentTransactionStrategy);

    logInfo("  Remove expression indexes...");

    ConfigUtility.removeExpressionIndexDirectory(getTerminology(), getVersion());

    fireProgressEvent(100, "Finished...");

    commit();
    clear();
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() throws Exception {
    return super.getParameters();
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

  /* see superclass */
  @Override
  public String getDescription() {
    return ConfigUtility.getNameFromClass(getClass());
  }
}
