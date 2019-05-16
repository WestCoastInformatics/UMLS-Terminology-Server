/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.release;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractInsertMaintReleaseAlgorithm;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.services.helper.ReportsAtomComparator;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.AtomSubsetMember;
import com.wci.umls.server.model.content.AtomTreePosition;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.CodeRelationship;
import com.wci.umls.server.model.content.CodeTreePosition;
import com.wci.umls.server.model.content.Component;
import com.wci.umls.server.model.content.ComponentInfoRelationship;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.ConceptSubsetMember;
import com.wci.umls.server.model.content.ConceptTreePosition;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.DescriptorRelationship;
import com.wci.umls.server.model.content.DescriptorTreePosition;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.Mapping;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.RootService;
import com.wci.umls.server.services.handlers.ComputePreferredNameHandler;

/**
 * Algorithm to write the RRF content files.
 */
public class WriteRrfContentFilesAlgorithm
    extends AbstractInsertMaintReleaseAlgorithm {

  /** The sem type map. */
  private Map<String, SemanticType> semTypeMap = new HashMap<>(10000);

  /** The term map. */
  private Map<String, Terminology> termMap = new HashMap<>(10000);

  /** The writer map. */
  Map<String, PrintWriter> writerMap = new HashMap<>(10000);

  /** The aui cui map. */
  private Map<String, String> auiCuiMap = new HashMap<>(10000);

  /** The att atui map. */
  private Map<Long, String> attAtuiMap = new HashMap<>(10000);

  /** The rel rui map. */
  private Map<Long, String> relAtomRuiMap = new HashMap<>(10000);

  /** The rel concept rui map. */
  private Map<Long, String> relConceptRuiMap = new HashMap<>(10000);

  /** The rel code rui map. */
  private Map<Long, String> relCodeRuiMap = new HashMap<>(10000);

  /** The rel comp atom rui map. */
  private Map<Long, String> relCompRuiMap = new HashMap<>(10000);

  /** The rel descriptor rui map. */
  private Map<Long, String> relDescriptorRuiMap = new HashMap<>(10000);

  /** The atom treepos set. */
  private Map<Long, AtomContents> atomContentsMap = new HashMap<>(10000);

  /** The concept contents map. */
  private Map<Long, Contents> conceptContentsMap = new HashMap<>(10000);

  /** The code contents map. */
  private Map<Long, Contents> codeContentsMap = new HashMap<>(10000);

  /** The descripto contents map. */
  private Map<Long, Contents> descriptorContentsMap = new HashMap<>(10000);

  /** The rui attribute terminologies. */
  private Set<String> ruiAttributeTerminologies = new HashSet<>();

  /** The terminology to src rht name map. */
  private Map<String, String> terminologyToSrcRhtNameMap = new HashMap<>();

  /** The terminology to src atom id map. */
  private Map<String, String> terminologyToSrcAuiMap = new HashMap<>();

  /** The terminology using src root. */
  private Set<String> terminologyUsingSrcRoot = new HashSet<>();

  /** The component info rel map. */
  private Map<String, List<ComponentInfoRelationship>> componentInfoRelMap =
      new HashMap<>();

  /** The precedence list. */
  private PrecedenceList precedenceList;

  /**
   * Instantiates an empty {@link WriteRrfContentFilesAlgorithm}.
   *
   * @throws Exception the exception
   */
  public WriteRrfContentFilesAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("RRFCONTENT");
  }

  /**
   * Check preconditions.
   *
   * @return the validation result
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    return new ValidationResultJpa();
  }

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting " + getName());
    fireProgressEvent(0, "Starting");

    // open print writers
    openWriters();

    prepareMaps();
    commitClearBegin();

    // Collect all concepts
    final List<Long> conceptIds = executeSingleComponentIdQuery(
        "select distinct c.id from ConceptJpa c join c.atoms a "
            + "where c.terminology = :terminology "
            + "  and c.version = :version and a.publishable = true "
            + "  and c.publishable = true order by c.terminologyId",
        QueryType.JPQL, getDefaultQueryParams(getProject()), ConceptJpa.class,
        false);
    commitClearBegin();
    setSteps(conceptIds.size());

    // Write AMBIG files
    writeAmbig();

    // Close Ambig writers
    writerMap.get("AMBIGSUI.RRF").close();
    writerMap.get("AMBIGLUI.RRF").close();

    // Parallelize output
    final Thread[] threads = new Thread[3];
    final Exception[] exceptions = new Exception[3];

    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        WriteRrfContentFilesAlgorithm service = null;
        try {
          service = new WriteRrfContentFilesAlgorithm();
          service.setTransactionPerOperation(false);
          service.beginTransaction();

          service.setProject(getProject());
          service.setProcess(getProcess());

          int ct = 0;
          for (final Long conceptId : conceptIds) {
            final Concept c = service.getConcept(conceptId);

            String prev = "";
            for (final String line : writeMrrel(c, service)) {
              if (!line.equals(prev)) {
                writerMap.get("MRREL.RRF").print(line);
              }
              prev = line;
            }

            if (ct++ % 1000 == 0) {
              checkCancel();
              service.commitClearBegin();
            }
          }
          service.commit();
          service.close();
          Logger.getLogger(getClass()).info("After MRREL completes.");

        } catch (Exception e) {
          Logger.getLogger(getClass()).error(e.getMessage(), e);
          exceptions[0] = e;
        } finally {
          writerMap.get("MRREL.RRF").close();
          try {
            service.close();
          } catch (Exception e) {
            exceptions[0] = e;
          }
        }
      }
    });
    threads[0] = t;
    t.start();

    t = new Thread(new Runnable() {
      @Override
      public void run() {
        WriteRrfContentFilesAlgorithm service = null;
        try {
          service = new WriteRrfContentFilesAlgorithm();
          service.setTransactionPerOperation(false);
          service.beginTransaction();

          service.setProject(getProject());
          service.setProcess(getProcess());

          int ct = 0;
          for (final Long conceptId : conceptIds) {
            final Concept c = service.getConcept(conceptId);

            String prev = "";
            for (final String line : writeMrhier(c, service)) {
              if (!line.equals(prev)) {
                writerMap.get("MRHIER.RRF").print(line);
              }
              prev = line;
            }

            if (ct++ % RootService.commitCt == 0) {
              checkCancel();
              service.commitClearBegin();
            }
          }
          service.commit();
          service.close();
          Logger.getLogger(getClass()).info("After MRHIER completes.");

        } catch (Exception e) {
          Logger.getLogger(getClass()).error(e.getMessage(), e);
          exceptions[1] = e;
        } finally {
          writerMap.get("MRHIER.RRF").close();
          try {
            service.close();
          } catch (Exception e) {
            exceptions[1] = e;
          }
        }
      }
    });
    threads[1] = t;
    t.start();

    t = new Thread(new Runnable() {
      @Override
      public void run() {
        WriteRrfContentFilesAlgorithm service = null;
        try {
          service = new WriteRrfContentFilesAlgorithm();
          service.setTransactionPerOperation(false);
          service.beginTransaction();

          service.setProject(getProject());
          service.setProcess(getProcess());

          int ct = 0;
          for (final Long conceptId : conceptIds) {
            final Concept c = service.getConcept(conceptId);

            String prev = null;
            for (final String line : writeMrsat(c, service)) {
              if (!line.equals(prev)) {
                writerMap.get("MRSAT.RRF").print(line);
              }
              prev = line;
            }
            writerMap.get("MRSAT.RRF").flush();
            if (ct++ % 100 == 0) {
              checkCancel();
              service.commitClearBegin();
            }
          }
          service.commit();
          service.close();
          Logger.getLogger(getClass()).info("After MRSAT completes.");

        } catch (Exception e) {
          Logger.getLogger(getClass()).error(e.getMessage(), e);
          exceptions[0] = e;
        } finally {
          writerMap.get("MRSAT.RRF").close();
          try {
            service.close();
          } catch (Exception e) {
            exceptions[2] = e;
          }
        }
      }
    });
    threads[2] = t;
    t.start();

    // Start writing other files
    try {
      for (final Long conceptId : conceptIds) {
        final Concept c = getConcept(conceptId);
        String prev = "";
        for (final String line : writeMrconso(c)) {
          if (!line.equals(prev)) {
            writerMap.get("MRCONSO.RRF").print(line);
          }
          prev = line;
        }

        prev = "";
        for (final String line : writeMrdef(c)) {
          if (!line.equals(prev)) {
            writerMap.get("MRDEF.RRF").print(line);
          }
          prev = line;
        }

        prev = "";
        for (final String line : writeMrsty(c)) {
          if (!line.equals(prev)) {
            writerMap.get("MRSTY.RRF").print(line);
          }
          prev = line;
        }
        updateProgress();
      }
    } catch (Exception e) {
      Logger.getLogger(getClass()).error(e.getMessage(), e);
      exceptions[2] = e;
    } finally {
      // Close final writers
      writerMap.get("MRCONSO.RRF").close();
      writerMap.get("MRDEF.RRF").close();
      writerMap.get("MRSTY.RRF").close();
    }

    // Wait for threads
    for (final Thread thread : threads) {
      thread.join();
    }

    // close print writers (if any are still open)
    closeWriters();

    // Report exceptions
    for (final Exception e : exceptions) {
      if (e != null) {
        throw e;
      }
    }

    fireProgressEvent(100, "Finished");
    logInfo("Finished " + getName());

  }

  /**
   * Prepare maps.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private void prepareMaps() throws Exception {

    // Precedencelist
    precedenceList = getPrecedenceList(getProject().getTerminology(),
        getProject().getVersion());

    // make semantic types map
    logInfo("  Prepare semantic type map");
    for (final SemanticType semType : getSemanticTypes(
        getProject().getTerminology(), getProject().getVersion())
            .getObjects()) {
      semTypeMap.put(semType.getExpandedForm(), semType);
    }

    // make terminologies map
    logInfo("  Prepare terminologies maps");
    for (final Terminology term : getCurrentTerminologies().getObjects()) {
      termMap.put(term.getTerminology(), term);
    }
    for (final Terminology term : getTerminologyLatestVersions().getObjects()) {
      Atom srcRhtAtom = null;
      SearchResultList searchResults = findConceptSearchResults(
          getProject().getTerminology(), getProject().getVersion(),
          getProject().getBranch(), " atoms.codeId:V-" + term.getTerminology()
              + " AND atoms.terminology:SRC AND atoms.termType:RPT",
          null);
      if (searchResults.size() == 1) {
        final Concept concept =
            getConcept(searchResults.getObjects().get(0).getId());
        for (final Atom a : concept.getAtoms()) {
          if (a.getTermType().equals("RHT") && a.isPublishable()) {
            srcRhtAtom = a;
            break;
          }
        }

        if (srcRhtAtom != null) {

          // Look for terminology-specific atom matching RHT on string in same
          // concept
          boolean found = false;
          for (final Atom a : concept.getAtoms()) {
            if (a.getTerminology().equals(term.getTerminology())
                && a.isPublishable()
                && a.getName().equals(srcRhtAtom.getName())) {
              found = true;
              break;
            }
          }
          if (!found) {
            terminologyUsingSrcRoot.add(term.getTerminology());
          }

          final String srcAui = srcRhtAtom.getAlternateTerminologyIds()
              .get(getProject().getTerminology());
          final String name = srcRhtAtom.getName();
          terminologyToSrcRhtNameMap.put(term.getTerminology(), name);
          terminologyToSrcAuiMap.put(term.getTerminology(), srcAui);
        }
      } else {
        logWarn("missing root SRC concept " + term.getTerminology());
      }
    }

    final ComputePreferredNameHandler handler =
        getComputePreferredNameHandler(getProject().getTerminology());
    final PrecedenceList list = getPrecedenceList(getProject().getTerminology(),
        getProject().getVersion());
    // Lazy init
    list.getPrecedence().getKeyValuePairs().size();

    // Atom -> AUI map
    // Load alternateTerminologyIds
    logInfo("  Cache atom->AUI map");
    Query query = getEntityManager().createQuery(
        "select a.id, value(b) from AtomJpa a join a.alternateTerminologyIds b "
            + "where KEY(b) = :terminology and a.publishable=true");
    query.setParameter("terminology", getProject().getTerminology());
    final List<Object[]> results2 = query.getResultList();
    int ct = 0;
    for (final Object[] result : results2) {
      final Long id = Long.valueOf(result[0].toString());
      final String alternateTerminologyId = result[1].toString();
      initAtomContents(id);
      atomContentsMap.get(id).setAui(alternateTerminologyId);
      logAndCommit(ct++, RootService.logCt, RootService.commitCt);
    }

    // Atom -> Source AUI map
    // Load alternateTerminologyIds
    logInfo("  Cache atom->Source AUI map");
    query = getEntityManager().createQuery(
        "select a.id, value(b) from AtomJpa a join a.alternateTerminologyIds b "
            + "where KEY(b) = :terminology and a.publishable=true");
    query.setParameter("terminology", getProject().getTerminology() + "-SRC");
    final List<Object[]> results3 = query.getResultList();
    ct = 0;
    for (final Object[] result : results3) {
      final Long id = Long.valueOf(result[0].toString());
      final String alternateTerminologyId = result[1].toString();
      initAtomContents(id);
      atomContentsMap.get(id).setSrcAui(alternateTerminologyId);
      logAndCommit(ct++, RootService.logCt, RootService.commitCt);
    }

    // Attribute -> ATUI map
    logInfo("  Cache attribute->ATUI map");
    query = getEntityManager().createQuery(
        "select a.id, value(b) from AttributeJpa a join a.alternateTerminologyIds b "
            + "where KEY(b) = :terminology and a.publishable=true");
    query.setParameter("terminology", getProject().getTerminology());
    final List<Object[]> results4 = query.getResultList();
    ct = 0;
    for (final Object[] result : results4) {
      final Long id = Long.valueOf(result[0].toString());
      final String alternateTerminologyId = result[1].toString();
      attAtuiMap.put(id, alternateTerminologyId);
      logAndCommit(ct++, RootService.logCt, RootService.commitCt);
    }
    // Relationship -> RUI map
    logInfo("  Cache relationship->RUI map (atom rels)");
    query = getEntityManager().createQuery(
        "select a.id, value(b) from AtomRelationshipJpa a join a.alternateTerminologyIds b "
            + "where KEY(b) = :terminology and a.publishable = true");
    query.setParameter("terminology", getProject().getTerminology());
    List<Object[]> results5 = query.getResultList();
    ct = 0;
    for (final Object[] result : results5) {
      final Long id = Long.valueOf(result[0].toString());
      final String alternateTerminologyId = result[1].toString();
      relAtomRuiMap.put(id, alternateTerminologyId);
      logAndCommit(ct++, RootService.logCt, RootService.commitCt);
    }
    logInfo("  Cache relationship->RUI map (concept rels)");
    query = getEntityManager().createQuery(
        "select a.id, value(b) from ConceptRelationshipJpa a join a.alternateTerminologyIds b "
            + "where KEY(b) = :terminology and a.publishable = true");
    query.setParameter("terminology", getProject().getTerminology());
    results5 = query.getResultList();
    ct = 0;
    for (final Object[] result : results5) {
      final Long id = Long.valueOf(result[0].toString());
      final String alternateTerminologyId = result[1].toString();
      relConceptRuiMap.put(id, alternateTerminologyId);
      logAndCommit(ct++, RootService.logCt, RootService.commitCt);
    }
    logInfo("  Cache relationship->RUI map (descriptor rels)");
    query = getEntityManager().createQuery(
        "select a.id, value(b) from DescriptorRelationshipJpa a join a.alternateTerminologyIds b "
            + "where KEY(b) = :terminology and a.publishable = true");
    query.setParameter("terminology", getProject().getTerminology());
    results5 = query.getResultList();
    ct = 0;
    for (final Object[] result : results5) {
      final Long id = Long.valueOf(result[0].toString());
      final String alternateTerminologyId = result[1].toString();
      relDescriptorRuiMap.put(id, alternateTerminologyId);
      logAndCommit(ct++, RootService.logCt, RootService.commitCt);
    }
    logInfo("  Cache relationship->RUI map (code rels)");
    query = getEntityManager().createQuery(
        "select a.id, value(b) from CodeRelationshipJpa a join a.alternateTerminologyIds b "
            + "where KEY(b) = :terminology and a.publishable = true");
    query.setParameter("terminology", getProject().getTerminology());
    results5 = query.getResultList();
    ct = 0;
    for (final Object[] result : results5) {
      final Long id = Long.valueOf(result[0].toString());
      final String alternateTerminologyId = result[1].toString();
      relCodeRuiMap.put(id, alternateTerminologyId);
      logAndCommit(ct++, RootService.logCt, RootService.commitCt);
    }

    // Determine preferred atoms for all concepts
    logInfo(
        "  Determine preferred atoms for all concepts, and cache concept->AUI maps");
    final List<Long> conceptIds = executeSingleComponentIdQuery(
        "select c.id from ConceptJpa c where publishable = true",
        QueryType.JPQL, getDefaultQueryParams(getProject()), ConceptJpa.class,
        false);
    commitClearBegin();
    ct = 0;
    for (Long conceptId : conceptIds) {
      final Concept concept = getConcept(conceptId);
      // compute preferred atom of the concept
      final Atom atom = handler.sortAtoms(concept.getAtoms(), list).get(0);
      // Save AUI->CUI map for the project terminology
      if (concept.getTerminology().equals(getProject().getTerminology())) {
        // Put all AUIs in the map
        for (final Atom atom2 : concept.getAtoms()) {
          if (atom2.isPublishable()) {
            auiCuiMap.put(atomContentsMap.get(atom2.getId()).getAui(),
                concept.getTerminologyId());
          }
        }
      }
      // otherwise save fact that atom is preferred id of its concept.
      else {
        // Verify there is a preferred atom
        if (!atomContentsMap.containsKey(atom.getId())) {
          throw new Exception(
              "Atom without an AUI, or possibly an publishable concept with unpublishable atom = "
                  + atom.getId() + ", " + concept.getId());
        }
        atomContentsMap.get(atom.getId()).setConceptId(concept.getId());
      }
      // Verify there is a preferred atom
      if (!atomContentsMap.containsKey(atom.getId())) {
        throw new Exception(
            "Atom without an AUI, or possibly an publishable concept with unpublishable atom = "
                + atom.getId() + ", " + concept.getId());
      }
      initContents(conceptContentsMap, concept.getId());
      conceptContentsMap.get(concept.getId())
          .setAui(atomContentsMap.get(atom.getId()).getAui());
      logAndCommit(ct++, RootService.logCt, RootService.commitCt);
    }

    // Determine preferred atoms for all descriptors
    logInfo(
        "  Determine preferred atoms for all descriptors, and cache descriptor->AUI maps");
    final List<Long> descriptorIds = executeSingleComponentIdQuery(
        "select d.id from DescriptorJpa d where publishable = true",
        QueryType.JPQL, getDefaultQueryParams(getProject()),
        DescriptorJpa.class, false);
    commitClearBegin();
    ct = 0;
    for (Long descriptorId : descriptorIds) {
      final Descriptor descriptor = getDescriptor(descriptorId);

      // compute preferred atom of the descriptor
      final Atom atom = handler.sortAtoms(descriptor.getAtoms(), list).get(0);
      if (!atomContentsMap.containsKey(atom.getId())) {
        throw new Exception(
            "Atom without an AUI, or possibly an publishable descriptor with unpublishable atom = "
                + atom.getId() + ", " + descriptor.getId());
      }
      atomContentsMap.get(atom.getId()).setDescriptorId(descriptor.getId());
      initContents(descriptorContentsMap, descriptor.getId());
      // skip if atom is not publishable
      if (!atom.isPublishable()) {
        continue;
      }
      descriptorContentsMap.get(descriptor.getId())
          .setAui(atomContentsMap.get(atom.getId()).getAui());
      logAndCommit(ct++, RootService.logCt, RootService.commitCt);
    }

    // Determine preferred atoms for all codes
    logInfo(
        "  Determine preferred atoms for all codes, and cache code->AUI maps");
    final List<Long> codeIds = executeSingleComponentIdQuery(
        "select c.id from CodeJpa c join c.atoms a where c.publishable = true "
            + "and a.publishable = true",
        QueryType.JPQL, getDefaultQueryParams(getProject()), CodeJpa.class,
        false);
    commitClearBegin();
    ct = 0;
    for (Long codeId : codeIds) {
      final Code code = getCode(codeId);
      // compute preferred atom of the code
      final Atom atom = handler.sortAtoms(code.getAtoms(), list).get(0);
      if (!atomContentsMap.containsKey(atom.getId())) {
        throw new Exception(
            "Atom without an AUI, or possibly an publishable code with unpublishable atom = "
                + atom.getId() + ", " + code.getId());
      }
      atomContentsMap.get(atom.getId()).setCodeId(code.getId());
      initContents(codeContentsMap, code.getId());
      codeContentsMap.get(code.getId())
          .setAui(atomContentsMap.get(atom.getId()).getAui());
      logAndCommit(ct++, RootService.logCt, RootService.commitCt);
    }

    // Determine terminologies that have relationship attributes
    logInfo("  Determine all terminologies with relationship attributes");
    query = manager.createQuery("select distinct r.terminology "
        + "from ConceptRelationshipJpa r join r.attributes a "
        + "where r.terminology != :terminology");
    query.setParameter("terminology", getProject().getTerminology());
    List<String> results = query.getResultList();
    for (final String result : results) {
      ruiAttributeTerminologies.add(result);
    }

    query = manager.createQuery("select distinct r.terminology "
        + "from CodeRelationshipJpa r join r.attributes a "
        + "where r.terminology != :terminology");
    query.setParameter("terminology", getProject().getTerminology());
    results = query.getResultList();
    for (final String result : results) {
      ruiAttributeTerminologies.add(result);
    }

    query = manager.createQuery("select distinct r.terminology "
        + "from DescriptorRelationshipJpa r join r.attributes a "
        + "where r.terminology != :terminology");
    query.setParameter("terminology", getProject().getTerminology());
    results = query.getResultList();
    for (final String result : results) {
      ruiAttributeTerminologies.add(result);
    }

    // TBD: because only atom and component info rels don't have RUI attributes
    // so far
    //
    // query = manager.createQuery("select distinct r.terminology "
    // + "from AtomRelationshipJpa r join r.attributes a "
    // + "where r.terminology != :terminology");
    // query.setParameter("terminology", getProject().getTerminology());
    // results = query.getResultList();
    // for (final String result : results) {
    // ruiAttributeTerminologies.add(result);
    // }

    // Cache component info relationships
    logInfo("  Cache component info relationships");
    query = manager.createQuery(
        "select r from ComponentInfoRelationshipJpa r where publishable = true");
    final List<ComponentInfoRelationship> rels = query.getResultList();
    final Map<String, String> SAUIToAUI = new HashMap<>();
    for (final ComponentInfoRelationship rel : rels) {
      String key = rel.getTo().getTerminologyId() + rel.getTo().getTerminology()
          + rel.getTo().getVersion() + rel.getTo().getType();
      if (rel.getTo().getType() == IdType.ATOM) {
        // AUI+terminology+type
        key = rel.getTo().getTerminologyId() + rel.getTo().getTerminology()
            + rel.getTo().getType();
      }
      if (!componentInfoRelMap.containsKey(key)) {
        componentInfoRelMap.put(key, new ArrayList<>());
      }
      componentInfoRelMap.get(key).add(rel);
    }

    // Cache Contents
    for (final String type : new String[] {
        "Atom", "Concept", "Code", "Descriptor"
    }) {

      Map<Long, ? extends Contents> map = null;
      if (type.equals("Atom")) {
        map = atomContentsMap;
      } else if (type.equals("Concept")) {
        map = conceptContentsMap;
      } else if (type.equals("Code")) {
        map = codeContentsMap;
      } else if (type.equals("Descriptor")) {
        map = descriptorContentsMap;
      }

      logInfo("  Determine " + type + " contents");

      logInfo("    attributes");
      query = manager.createQuery(
          "select distinct a.id from " + type + "Jpa a join a.attributes b "
              + "where a.publishable = true and b.publishable = true");
      ct = 0;
      for (final Long id : (List<Long>) query.getResultList()) {
        if (map.get(id) != null) {
          map.get(id).markAttributes();
          logAndCommit(ct++, RootService.logCt, RootService.commitCt);
        }
      }
      logInfo("      ct = " + ct);

      logInfo("    relationships");
      query = manager.createQuery(
          "select distinct a.to.id from " + type + "RelationshipJpa a "
              + "where a.publishable = true and a.to.publishable = true");
      ct = 0;
      for (final Long id : (List<Long>) query.getResultList()) {
        if (map.get(id) != null) {
          map.get(id).markRelationships();
          logAndCommit(ct++, RootService.logCt, RootService.commitCt);
        }
      }
      logInfo("      ct = " + ct);

      logInfo("    tree positions");
      query = manager.createQuery(
          "select distinct a.node.id from " + type + "TreePositionJpa a "
              + "where a.publishable = true and a.node.publishable = true");
      ct = 0;
      for (final Long id : (List<Long>) query.getResultList()) {
        if (map.get(id) != null) {
          map.get(id).markTreePositions();
          logAndCommit(ct++, RootService.logCt, RootService.commitCt);
        }
      }
      logInfo("      ct = " + ct);

      // Only concepts and atoms have subset members
      if (type.equals("Concept") || type.equals("Atom")) {
        logInfo("    members");
        query = manager.createQuery(
            "select distinct a.member.id from " + type + "SubsetMemberJpa a "
                + "where a.publishable = true and a.member.publishable = true");
        ct = 0;
        for (final Long id : (List<Long>) query.getResultList()) {
          if (map.get(id) != null) {
            map.get(id).markMembers();
            logAndCommit(ct++, RootService.logCt, RootService.commitCt);
          }
        }
        logInfo("      ct = " + ct);
      }

      // Codes don't have definitions
      if (!type.equals("Code")) {
        logInfo("    definitions");
        query = manager.createQuery("select distinct a.id from " + type
            + "Jpa a join a.definitions d where a.publishable = true "
            + "and d.publishable = true");
        ct = 0;
        for (final Long id : (List<Long>) query.getResultList()) {
          if (map.get(id) != null) {
            map.get(id).markDefinitions();
            logAndCommit(ct++, RootService.logCt, RootService.commitCt);
          }
        }
        logInfo("      ct = " + ct);
      }
    }
  }

  /**
   * Inits the contents.
   *
   * @param map the map
   * @param id the id
   */
  private void initContents(Map<Long, Contents> map, Long id) {
    if (!map.containsKey(id)) {
      map.put(id, new Contents());
    }
  }

  /**
   * Inits the atom contents.
   *
   * @param id the id
   */
  private void initAtomContents(Long id) {
    if (!atomContentsMap.containsKey(id)) {
      atomContentsMap.put(id, new AtomContents());
    }
  }

  /**
   * Open writers.
   *
   * @throws Exception the exception
   */
  private void openWriters() throws Exception {
    final File dir = new File(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath() + "/" + getProcess().getVersion() + "/"
        + "META");

    writerMap.put("AMBIGSUI.RRF",
        new PrintWriter(new FileWriter(new File(dir, "AMBIGSUI.RRF"))));
    writerMap.put("AMBIGLUI.RRF",
        new PrintWriter(new FileWriter(new File(dir, "AMBIGLUI.RRF"))));
    writerMap.put("MRCONSO.RRF",
        new PrintWriter(new FileWriter(new File(dir, "MRCONSO.RRF"))));
    writerMap.put("MRDEF.RRF",
        new PrintWriter(new FileWriter(new File(dir, "MRDEF.RRF"))));
    writerMap.put("MRREL.RRF",
        new PrintWriter(new FileWriter(new File(dir, "MRREL.RRF"))));
    writerMap.put("MRSTY.RRF",
        new PrintWriter(new FileWriter(new File(dir, "MRSTY.RRF"))));
    writerMap.put("MRSAT.RRF",
        new PrintWriter(new FileWriter(new File(dir, "MRSAT.RRF"))));
    writerMap.put("MRHIER.RRF",
        new PrintWriter(new FileWriter(new File(dir, "MRHIER.RRF"))));
    writerMap.put("MRHIST.RRF",
        new PrintWriter(new FileWriter(new File(dir, "MRHIST.RRF"))));
    writerMap.put("MRMAP.RRF",
        new PrintWriter(new FileWriter(new File(dir, "MRMAP.RRF"))));
    writerMap.put("MRSMAP.RRF",
        new PrintWriter(new FileWriter(new File(dir, "MRSMAP.RRF"))));
  }

  /**
   * Close writers.
   */
  private void closeWriters() {
    for (final PrintWriter writer : writerMap.values()) {
      writer.close();
    }
  }

  /**
   * Write mrconso.
   *
   * @param c the c
   * @return the string
   * @throws Exception the exception
   */
  List<String> writeMrconso(Concept c) throws Exception {

    // Field Description
    // 0 CUI
    // 1 LAT
    // 2 TS
    // 3 LUI
    // 4 STT
    // 5 SUI
    // 6 ISPREF
    // 7 AUI
    // 8 SAUI
    // 9 SCUI
    // 10 SDUI
    // 11 SAB
    // 12 TTY
    // 13 CODE
    // 14 STR
    // 15 SRL
    // 16 SUPPRESS
    // 17 CVF
    //
    // e.g.
    // C0000005|ENG|P|L0000005|PF|S0007492|Y|A7755565||M0019694|D012711|MSH|PEN|D012711|(131)I-Macroaggregated
    // Albumin|0|N|256|

    // sort the atoms in precedence order
    final List<Atom> sortedAtoms = new ArrayList<>(c.getAtoms());
    Collections.sort(sortedAtoms, new ReportsAtomComparator(c, precedenceList));

    final String[] pref = new String[3];
    final int lui = 0;
    final int sui = 1;
    final int aui = 2;

    final String[] prev = new String[3];
    final int lat = 2;

    final boolean[] flags = new boolean[3];
    final int ts = 0;
    final int stt = 1;
    final int ispref = 2;
    final List<String> lines = new ArrayList<>();
    for (final Atom a : sortedAtoms) {
      if (!a.isPublishable()) {
        continue;
      }
      final StringBuilder sb = new StringBuilder(200);
      // CUI
      sb.append(c.getTerminologyId()).append("|");
      // LAT
      sb.append(a.getLanguage()).append("|");

      // Compute rank
      if (!a.getLanguage().equals(prev[lat])) {
        pref[lui] = null;
        pref[sui] = null;
        pref[aui] = null;
      }
      flags[ts] = false;
      if (pref[lui] == null) {
        pref[lui] = a.getLexicalClassId();
        flags[ts] = true;
      } else if (a.getLexicalClassId().equals(pref[lui])) {
        flags[ts] = true;
      } else if (!a.getLexicalClassId().equals(prev[lui])) {
        pref[sui] = null;
      }
      flags[stt] = false;
      if (pref[sui] == null) {
        pref[sui] = a.getStringClassId();
        flags[stt] = true;
      } else if (a.getStringClassId().equals(pref[sui])) {
        flags[stt] = true;
      } else if (!a.getStringClassId().equals(prev[sui])) {
        pref[aui] = null;
      }
      flags[ispref] = false;
      if (pref[aui] == null) {
        pref[aui] = atomContentsMap.get(a.getId()).getAui();
        flags[ispref] = true;
      }

      prev[lui] = a.getLexicalClassId();
      prev[sui] = a.getStringClassId();
      prev[lat] = a.getLanguage();

      // TS
      sb.append(flags[ts] ? "P" : "S").append("|");
      // LUI
      sb.append(a.getLexicalClassId()).append("|");
      // STT
      sb.append(flags[stt] ? "PF" : "VO").append("|");
      // SUI
      sb.append(a.getStringClassId()).append("|");
      // ISPREF
      sb.append(flags[ispref] ? "Y" : "N").append("|");
      final String ui = atomContentsMap.get(a.getId()).getAui();
      // AUI
      sb.append(ui != null ? ui : "").append("|");
      // SAUI
      sb.append(a.getTerminologyId()).append("|");
      // SCUI
      sb.append(a.getConceptId()).append("|");
      // SDUI
      sb.append(a.getDescriptorId()).append("|");
      // SAB
      sb.append(a.getTerminology()).append("|");
      // TTY
      sb.append(a.getTermType()).append("|");
      // CODE
      sb.append(a.getCodeId()).append("|");
      // STR
      sb.append(a.getName()).append("|");
      // SRL
      sb.append(termMap.get(a.getTerminology()).getRootTerminology()
          .getRestrictionLevel()).append("|");
      // SUPPRESS
      if (a.isObsolete()) {
        sb.append("O");
      } else if (a.isSuppressible()
          && getTermType(a.getTermType(), getProject().getTerminology(),
              getProject().getVersion()).isSuppressible()) {
        sb.append("Y");
      } else if (a.isSuppressible()
          && !getTermType(a.getTermType(), getProject().getTerminology(),
              getProject().getVersion()).isSuppressible()) {
        sb.append("E");
      } else {
        sb.append("N");
      }
      // CVF
      sb.append("||\n");
      lines.add(sb.toString());

      // Collect the mapset concepts and cache
      if (a.getTermType().equals("XM")) {
        final MapSet mapSet = getMapSet(a.getCodeId(), a.getTerminology(),
            a.getVersion(), Branch.ROOT);

        if (mapSet.isPublishable()) {
          for (final String line : writeMrmap(mapSet, c.getTerminologyId())) {
            writerMap.get("MRMAP.RRF").print(line);
          }
          for (final String line : writeMrsmap(mapSet, c.getTerminologyId())) {
            writerMap.get("MRSMAP.RRF").print(line);
          }
        }
      }
    }
    // TODO: consider sorting all files at the end just once.
    Collections.sort(lines);
    return lines;
  }

  /**
   * Write mrdef.
   *
   * @param c the c
   * @return the list
   * @throws Exception the exception
   */
  List<String> writeMrdef(Concept c) throws Exception {

    // Field Description
    // 0 CUI
    // 1 AUI
    // 2 ATUI
    // 3 SATUI
    // 4 SAB
    // 5 DEF
    // 6 SUPPRESS
    // 7 CVF
    //
    // e.g.
    // C0001175|A0019180|AT38139119||MSH|An acquired...|N||
    final List<String> lines = new ArrayList<>();
    for (final Atom a : c.getAtoms()) {
      if (!a.isPublishable()) {
        continue;
      }
      // Skip if atom doesn't have definitions
      if (!atomContentsMap.containsKey(a.getId())
          || !atomContentsMap.get(a.getId()).hasDefinitions()) {
        continue;
      }
      for (final Definition d : a.getDefinitions()) {
        if (!d.isPublishable()) {
          continue;
        }
        final StringBuilder sb = new StringBuilder(200);
        // CUI
        sb.append(c.getTerminologyId()).append("|");
        // AUI
        final String aui = atomContentsMap.get(a.getId()).getAui();
        sb.append(aui).append("|");
        // ATUI
        final String atui =
            d.getAlternateTerminologyIds().get(getProject().getTerminology());
        sb.append(atui).append("|");
        // SATUI
        sb.append(d.getTerminologyId()).append("|");
        // SAB
        sb.append(d.getTerminology()).append("|");
        // DEF
        sb.append(d.getValue()).append("|");
        // SUPPRESS
        if (d.isObsolete()) {
          sb.append("O");
        } else if (d.isSuppressible()) {
          sb.append("Y");
        } else {
          sb.append("N");
        }
        // CVF
        sb.append("||\n");
        lines.add(sb.toString());
      }
    }
    Collections.sort(lines);
    return lines;
  }

  /**
   * Write mrmap.
   *
   * @param mapset the mapset
   * @param terminologyId the terminology id
   * @return the list
   */
  private List<String> writeMrmap(MapSet mapset, String terminologyId) {

    // Field Description
    // 0 MAPSETCUI Unique identifier for the UMLS concept which represents the
    // whole map set.
    // 1 MAPSETSAB Source abbreviation (SAB) for the provider of the map set.
    // 2 MAPSUBSETID Map subset identifier used to identify a subset of related
    // mappings within a map set. This is used for cases where the FROMEXPR may
    // have more than one potential mapping (optional).
    // 3 MAPRANK Order in which mappings in a subset should be applied. Used
    // only where MAPSUBSETID is used. (optional)
    // 4 MAPID Unique identifier for this individual mapping. Primary key of
    // this table to identify a particular row.
    // 5 MAPSID Source asserted identifier for this mapping (optional).
    // 6 FROMID Identifier for the entity being mapped from. This is an internal
    // UMLS identifier used to point to an external entity in a source
    // vocabulary (represented by the FROMEXPR). When the source provides such
    // an identifier, it is reused here. Otherwise, it is generated by NLM. The
    // FROMID is only unique within a map set. It is not a pointer to UMLS
    // entities like atoms or concepts. There is a one-to-one correlation
    // between FROMID and a unique set of values in FROMSID, FROMEXPR, FROMTYPE,
    // FROMRULE, and FROMRES within a map set.
    // 7 FROMSID Source asserted identifier for the entity being mapped from
    // (optional).
    // 8 FROMEXPR Entity being mapped from - can be a single code/identifier
    // /concept name or a complex expression involving multiple
    // codes/identifiers/concept names, Boolean operators and/or punctuation
    // 9 FROMTYPE Type of entity being mapped from.
    // 10 FROMRULE Machine processable rule applicable to the entity being
    // mapped from (optional)
    // 11 FROMRES Restriction applicable to the entity being mapped from
    // (optional).
    // 12 REL Relationship of the entity being mapped from to the entity being
    // mapped to.
    // 13 RELA Additional relationship label (optional).
    // 14 TOID Identifier for the entity being mapped to. This is an internal
    // identifier used to point to an external entity in a source vocabulary
    // (represented by the TOEXPR). When the source provides such an identifier,
    // it is reused here. Otherwise, it is generated by NLM. The TOID is only
    // unique within a map set. It is not a pointer to UMLS entities like atoms
    // or concepts. There is a one-to-one correlation between TOID and a unique
    // set of values in TOSID, TOEXPR, TOTYPE, TORULE, TORES within a map set.
    // 15 TOSID Source asserted identifier for the entity being mapped to
    // (optional).
    // 16 TOEXPR Entity being mapped to - can be a single
    // code/identifier/concept name or a complex expression involving multiple
    // codes/identifiers/concept names, Boolean operators and/or punctuation.
    // 17 TOTYPE Type of entity being mapped to.
    // 18 TORULE Machine processable rule applicable to the entity being mapped
    // to (optional).
    // 19 TORES Restriction applicable to the entity being mapped to (optional).
    // 20 MAPRULE Machine processable rule applicable to this mapping
    // (optional).
    // 21 MAPRES Restriction applicable to this mapping (optional).
    // 22 MAPTYPE Type of mapping (optional).
    // 23 MAPATN The name of the attribute associated with this mapping [not yet
    // in use]
    // 24 MAPATV The value of the attribute associated with this mapping [not
    // yet in use]
    // 25 CVF The Content View Flag is a bit field used to indicate membership
    // in a content view.

    // Sample Records
    // C1306694|MTH|||AT28307527||C0011764||C0011764|CUI|||RO||2201||<Developmental
    // Disabilities> AND <Writing>|BOOLEAN_EXPRESSION_STR|||||ATX||||
    // C1306694|MTH|||AT52620421||C0010700||C0010700|CUI|||RN||1552||<Urinary
    // Bladder>/<surgery>|BOOLEAN_EXPRESSION_STR|||||ATX||||
    // C2919943|SNOMEDCT|0|0|AT127959271||302759005||302759005|SCUI|||RN|mapped_to|9571037057|9571037057|799.59|BOOLEAN_EXPRESSION_SDUI|||||2||||
    // C2919943|SNOMEDCT|0|0|AT127959272||43498006||43498006|SCUI|||RQ|mapped_to|9571050056|9571050056|276.69|BOOLEAN_EXPRESSION_SDUI|||||1||||

    final List<String> lines = new ArrayList<>();

    for (final Mapping mapping : mapset.getMappings()) {
      if (!mapping.isPublishable()) {
        continue;
      }
      final StringBuilder sb = new StringBuilder(200);
      // CUI
      sb.append(terminologyId).append("|");
      // MAPSETSAB
      sb.append(mapset.getTerminology()).append("|");
      // MAPSUBSETID
      sb.append(mapping.getGroup()).append("|");
      // MAPRANK
      sb.append(mapping.getRank()).append("|");
      // MAPID
      if (mapping.getAlternateTerminologyIds()
          .containsKey(getProject().getTerminology())) {
        sb.append(mapping.getAlternateTerminologyIds()
            .get(getProject().getTerminology()));
      }
      sb.append("|");
      // MAPSID
      sb.append(mapping.getTerminologyId()).append("|");
      // FROMID
      if (mapping.getAlternateTerminologyIds()
          .containsKey(getProject().getTerminology() + "-FROMID")) {
        sb.append(mapping.getAlternateTerminologyIds()
            .get(getProject().getTerminology() + "-FROMID"));
      }
      sb.append("|");
      // FROMSID
      if (mapping.getAlternateTerminologyIds()
          .containsKey(getProject().getTerminology() + "-FROMSID")) {
        sb.append(mapping.getAlternateTerminologyIds()
            .get(getProject().getTerminology() + "-FROMSID"));
      }
      sb.append("|");
      // FROMEXPR
      sb.append(mapping.getFromTerminologyId()).append("|");
      // FROMTYPE
      if (mapping.getFromIdType().toString().equals("DESCRIPTOR")) {
        sb.append("SDUI");
      } else if (mapping.getFromIdType().toString().equals("CONCEPT") && mapset
          .getFromTerminology().equals(getProject().getTerminology())) {
        sb.append("CUI");
      } else if (mapping.getFromIdType().toString().equals("CONCEPT")) {
        sb.append("SCUI");
      } else {
        mapping.getFromIdType().toString();
      }
      sb.append("|");
      // FROMRULE
      for (Attribute att : mapping.getAttributes()) {
        if (att.getName().equals("FROMRULE")) {
          sb.append(att.getValue());
        }
      }
      sb.append("|");
      // FROMRES
      for (Attribute att : mapping.getAttributes()) {
        if (att.getName().equals("FROMRES")) {
          sb.append(att.getValue());
        }
      }
      sb.append("|");
      // REL
      sb.append(mapping.getRelationshipType()).append("|");
      // RELA
      sb.append(mapping.getAdditionalRelationshipType()).append("|");
      // TOID
      if (mapping.getAlternateTerminologyIds()
          .containsKey(getProject().getTerminology() + "-TOID")) {
        sb.append(mapping.getAlternateTerminologyIds()
            .get(getProject().getTerminology() + "-TOID"));
      }
      sb.append("|");
      // TOSID
      if (mapping.getAlternateTerminologyIds()
          .containsKey(getProject().getTerminology() + "-TOSID")) {
        sb.append(mapping.getAlternateTerminologyIds()
            .get(getProject().getTerminology() + "-TOSID"));
      }
      sb.append("|");
      // TOEXPR
      sb.append(mapping.getToTerminologyId()).append("|");
      // TOTYPE
      if (mapping.getToIdType().toString().equals("DESCRIPTOR")) {
        sb.append("SDUI");
      } else if (mapping.getToIdType().toString().equals("CONCEPT")
          && mapset.getToTerminology().equals(getProject().getTerminology())) {
        sb.append("CUI");
      } else if (mapping.getToIdType().toString().equals("CONCEPT")) {
        sb.append("SCUI");
      } else {
        mapping.getToIdType().toString();
      }
      sb.append("|");
      // TORULE
      for (Attribute att : mapping.getAttributes()) {
        if (att.getName().equals("TORULE")) {
          sb.append(att.getValue());
        }
      }
      sb.append("|");
      // TORES
      for (Attribute att : mapping.getAttributes()) {
        if (att.getName().equals("TORES")) {
          sb.append(att.getValue());
        }
      }
      sb.append("|");
      // MAPRULE
      sb.append(mapping.getRule()).append("|");
      // MAPRES
      sb.append(mapping.getAdvice()).append("|");
      // MAPTYPE
      sb.append(mapset.getMapType() != null ? mapset.getMapType() : "")
          .append("|");
      // MAPATN && MAPATV
      if (mapping.getTerminology().equals("SNOMEDCT_US")) {
        sb.append("ACTIVE").append("|");
        sb.append(mapping.isObsolete() ? "0" : "1").append("|");
      } else {
        sb.append("||");
      }
      // CVF
      sb.append("|\n");
      lines.add(sb.toString());
    }
    Collections.sort(lines);
    return lines;
  }

  /**
   * Write mrsty.
   *
   * @param mapset the mapset
   * @param terminologyId the terminology id
   * @return the list
   */
  private List<String> writeMrsmap(MapSet mapset, String terminologyId) {

    // Field Description
    // MAPSETCUI Unique identifier for the UMLS concept which represents the
    // whole map set.
    // MAPSETSAB Source abbreviation for the map set.
    // MAPID Unique identifier for this individual mapping. Primary key of this
    // table to identify a particular row.
    // MAPSID Source asserted identifier for this mapping (optional).
    // FROMEXPR Entity being mapped from - can be a single
    // code/identifier/concept name or a complex expression involving multiple
    // codes/identifiers/concept names, Boolean operators and/or punctuation.
    // FROMTYPE Type of entity being mapped from.
    // REL Relationship of the entity being mapped from to the entity being
    // mapped to.
    // RELA Additional relationship label (optional).
    // TOEXPR Entity being mapped to - can be a single code/identifier /concept
    // name or a complex expression involving multiple codes/identifiers/concept
    // names, Boolean operators and/or punctuation.
    // TOTYPE Type of entity being mapped to.
    // CVF The Content View Flag is a bit field used to indicate membership in a
    // content view.

    // Sample Records
    // C1306694|MTH|AT28312030||C0009215|CUI|SY||<Codeine> AND <Drug
    // Hypersensitivity>|BOOLEAN_EXPRESSION_STR||
    // C1306694|MTH|AT28312033||C0795964|CUI|RU||<Speech
    // Disorders>|BOOLEAN_EXPRESSION_STR||
    // C2919943|SNOMEDCT|AT127959271||302759005|SCUI|RN|mapped_to|799.59|BOOLEAN_EXPRESSION_SDUI||
    // C2919943|SNOMEDCT|AT127959272||43498006|SCUI|RQ|mapped_to|276.69|BOOLEAN_EXPRESSION_SDUI||

    final List<String> lines = new ArrayList<>();
    for (final Mapping mapping : mapset.getMappings()) {
      if (!mapping.isPublishable()
          || (mapping.getGroup().equals("0") && mapping.getRank().equals("0"))
          || (mapping.getGroup().equals("")) && mapping.getRank().equals("")) {
        continue;
      }
      final StringBuilder sb = new StringBuilder(200);
      // CUI
      sb.append(terminologyId).append("|");
      // MAPSETSAB
      sb.append(mapset.getTerminology()).append("|");
      // MAPID
      if (mapping.getAlternateTerminologyIds()
          .containsKey(getProject().getTerminology())) {
        sb.append(mapping.getAlternateTerminologyIds()
            .get(getProject().getTerminology()));
      }
      sb.append("|");
      // MAPSID
      sb.append(mapping.getTerminologyId()).append("|");

      // FROMEXPR
      sb.append(mapping.getFromTerminologyId()).append("|");
      // FROMTYPE
      if (mapping.getFromIdType().toString().equals("DESCRIPTOR")) {
        sb.append("SDUI");
      } else if (mapping.getFromIdType().toString().equals("CONCEPT") && mapset
          .getFromTerminology().equals(getProject().getTerminology())) {
        sb.append("CUI");
      } else if (mapping.getFromIdType().toString().equals("CONCEPT")) {
        sb.append("SCUI");
      }
      sb.append("|");

      // REL
      sb.append(mapping.getRelationshipType()).append("|");
      // RELA
      sb.append(mapping.getAdditionalRelationshipType()).append("|");

      // TOEXPR
      sb.append(mapping.getToTerminologyId()).append("|");
      // TOTYPE
      if (mapping.getToIdType().toString().equals("DESCRIPTOR")) {
        sb.append("SDUI");
      } else if (mapping.getToIdType().toString().equals("CONCEPT")
          && mapset.getToTerminology().equals(getProject().getTerminology())) {
        sb.append("CUI");
      } else if (mapping.getToIdType().toString().equals("CONCEPT")) {
        sb.append("SCUI");
      }
      // CVF
      sb.append("||\n");
      lines.add(sb.toString());
    }
    Collections.sort(lines);
    return lines;
  }

  /**
   * Write mrsty.
   *
   * @param c the c
   * @return the list
   */
  List<String> writeMrsty(Concept c) {

    // Field Description
    // 0 CUI Unique identifier of concept
    // 1 TUI Unique identifier of Semantic Type
    // 2 STN Semantic Type tree number
    // 3 STY Semantic Type. The valid values are defined in the Semantic
    // Network.
    // 4 ATUI Unique identifier for attribute
    // 5 CVF Content View Flag. Bit field used to flag rows included in
    // Content View. This field is a varchar field to maximize the number of
    // bits available for use.

    // Sample Record
    // C0001175|T047|B2.2.1.2.1|Disease or Syndrome|AT17683839|3840|
    final List<String> lines = new ArrayList<>();

    for (final SemanticTypeComponent sty : c.getSemanticTypes()) {
      if (!sty.isPublishable()) {
        continue;
      }

      final StringBuilder sb = new StringBuilder(200);
      // CUI
      sb.append(c.getTerminologyId()).append("|");
      // TUI
      sb.append(semTypeMap.get(sty.getSemanticType()).getTypeId()).append("|");
      // STN
      sb.append(semTypeMap.get(sty.getSemanticType()).getTreeNumber())
          .append("|");
      // STY
      sb.append(sty.getSemanticType()).append("|");
      // ATUI
      sb.append(sty.getTerminologyId()).append("|");
      // CVF
      sb.append("|\n");
      lines.add(sb.toString());
    }
    Collections.sort(lines);
    return lines;
  }

  /**
   * Write mrrel.
   *
   * @param c the c
   * @param service the service
   * @return the list
   * @throws Exception the exception
   */
  List<String> writeMrrel(Concept c, WriteRrfContentFilesAlgorithm service)
    throws Exception {
    // Field description
    // 0 CUI1
    // 1 AUI1
    // 2 STYPE1
    // 3 REL
    // 4 CUI2
    // 5 AUI2
    // 6 STYPE2
    // 7 RELA
    // 8 RUI
    // 9 SRUI
    // 10 SAB
    // 11 SL
    // 12 RG
    // 13 DIR
    // 14 SUPPRESS
    // 15 CVF
    //
    // e.g. C0002372|A0021548|AUI|SY|C0002372|A16796726|AUI||R112184262||
    // RXNORM|RXNORM|||N|| C0002372|A0022283|AUI|RO|C2241537|A14211642|AUI
    // |has_ingredient|R91984327||MMSL|MMSL|||N||

    final List<String> lines = new ArrayList<>();

    final String cui1 = c.getTerminologyId();

    // Concept relationships
    if (conceptContentsMap.containsKey(c.getId())
        && conceptContentsMap.get(c.getId()).hasRelationships()) {

      for (final ConceptRelationship rel : c.getInverseRelationships()) {
        if (!rel.isPublishable() || !rel.getFrom().isPublishable()
            || !rel.getTo().isPublishable()) {
          continue;
        }

        lines.add(getRelLine(rel, cui1, "", "CUI",
            rel.getFrom().getTerminologyId(), "", "CUI", relConceptRuiMap));
      }
    }

    // CUI->AUI component info relationships
    String key = c.getTerminologyId() + c.getTerminology() + c.getVersion()
        + c.getType();
    for (final ComponentInfoRelationship rel : getComponentInfoRels(key)) {
      if (!rel.isPublishable()) {
        continue;
      }

      // determine aui2
      String aui2 = null;
      String stype2 = null;
      final Component from =
          service.findComponent(rel.getFrom(), atomContentsMap);

      if (!from.isPublishable()) {
        continue;
      }

      if (from.getType() == IdType.CONCEPT) {
        aui2 = conceptContentsMap.get(from.getId()).getAui();
        stype2 = "SCUI";
      } else if (from.getType() == IdType.CODE) {
        aui2 = codeContentsMap.get(from.getId()).getAui();
        stype2 = "CODE";
      } else if (from.getType() == IdType.DESCRIPTOR) {
        aui2 = descriptorContentsMap.get(from.getId()).getAui();
        stype2 = "SDUI";
      } else if (from.getType() == IdType.ATOM) {
        aui2 = atomContentsMap.get(from.getId()).getAui();
        stype2 = "AUI";
      }
      relCompRuiMap.put(rel.getId(),
          rel.getAlternateTerminologyIds().get(getProject().getTerminology()));
      lines.add(
          getRelLine(rel, cui1, "", "CUI", null, aui2, stype2, relCompRuiMap));

    }

    // Atom relationships
    // C0000005|A4345877|AUI|RB|C0036775|A3586555|AUI||R17427607||MSH|MSH|||N||
    for (final Atom a : c.getAtoms()) {
      if (!a.isPublishable()) {
        continue;
      }

      final String aui1 = atomContentsMap.get(a.getId()).getAui();
      final String saui1 = atomContentsMap.get(a.getId()).getSrcAui();

      if (atomContentsMap.containsKey(a.getId())
          && atomContentsMap.get(a.getId()).hasRelationships()) {

        for (final AtomRelationship r : a.getInverseRelationships()) {
          if (!r.isPublishable() || !r.getFrom().isPublishable()
              || !r.getTo().isPublishable()) {
            continue;
          }
          final String aui2 = atomContentsMap.get(r.getFrom().getId()).getAui();
          lines.add(getRelLine(r, cui1, aui1, "AUI", null, aui2, "AUI",
              relAtomRuiMap));
        }
      }

      // look up component info relationships where STYPE1=AUI
      key = atomContentsMap.get(a.getId()).getAui()
          + getProject().getTerminology() + a.getType();
      List<ComponentInfoRelationship> comInfoRels = new ArrayList<>();
      if (getComponentInfoRels(key) != null && !getComponentInfoRels(key).isEmpty()) {
        comInfoRels.addAll(getComponentInfoRels(key));
      }      
      key = atomContentsMap.get(a.getId()).getSrcAui()
          + getProject().getTerminology() + a.getType();
      if (getComponentInfoRels(key) != null && !getComponentInfoRels(key).isEmpty()) {
        comInfoRels.addAll(getComponentInfoRels(key));
      }      
     
      for (final ComponentInfoRelationship rel : comInfoRels) {
        if (!rel.isPublishable()) {
          continue;
        }

        // determine aui2
        String aui2 = null;
        String stype2 = null;
        String cui2 = null;
        final Component from =
            service.findComponent(rel.getFrom(), atomContentsMap);
        if (!from.isPublishable()) {
          continue;
        }
        if (from.getType() == IdType.CONCEPT) {
          stype2 = from.getTerminology().equals(getProject().getTerminology())
              ? "CUI" : "SCUI";
          aui2 = stype2.equals("CUI") ? ""
              : conceptContentsMap.get(from.getId()).getAui();
          cui2 = stype2.equals("CUI") ? from.getTerminologyId() : null;
        } else if (from.getType() == IdType.CODE) {
          aui2 = codeContentsMap.get(from.getId()).getAui();
          stype2 = "CODE";
        } else if (from.getType() == IdType.DESCRIPTOR) {
          aui2 = descriptorContentsMap.get(from.getId()).getAui();
          stype2 = "SDUI";
        }
        relCompRuiMap.put(rel.getId(), rel.getAlternateTerminologyIds()
            .get(getProject().getTerminology()));
        lines.add(getRelLine(rel, cui1, aui1, "AUI", cui2, aui2, stype2,
            relCompRuiMap));

      }

      // SCUI relationships, if preferred atom of the SCUI
      // e.g.
      // C0000097|A3134287|SCUI|PAR|C0576798|A3476803|SCUI|inverse_isa|R96279727|107042028|SNOMEDCT_US|SNOMEDCT_US|0|N|N||
      if (atomContentsMap.get(a.getId()).getConceptId() != null) {
        final Concept scui =
            service.getConcept(atomContentsMap.get(a.getId()).getConceptId());

        if (!scui.isPublishable()) {
          continue;
        }

        if (conceptContentsMap.containsKey(scui.getId())
            && conceptContentsMap.get(scui.getId()).hasRelationships()) {

          for (final ConceptRelationship rel : scui.getInverseRelationships()) {
            if (!rel.isPublishable() || !rel.getFrom().isPublishable()
                || !rel.getTo().isPublishable()) {
              continue;
            }

            final String aui2 =
                conceptContentsMap.get(rel.getFrom().getId()).getAui();
            lines.add(getRelLine(rel, cui1, aui1, "SCUI", null, aui2, "SCUI",
                relConceptRuiMap));
          }
        }

        // look up component info relationships where STYPE1=SCUI
        key = scui.getTerminologyId() + scui.getTerminology()
            + scui.getVersion() + scui.getType();
        for (final ComponentInfoRelationship rel : getComponentInfoRels(key)) {
          if (!rel.isPublishable()) {
            continue;
          }

          String aui2 = null;
          String stype2 = null;
          final Component from =
              service.findComponent(rel.getFrom(), atomContentsMap);
          if (from == null) {
            throw new Exception("No component found for: " + rel.getFrom());
          }
          if (!from.isPublishable()) {
            continue;
          }

          if (from.getType() == IdType.CODE) {
            aui2 = codeContentsMap.get(from.getId()).getAui();
            stype2 = "CODE";
          } else if (from.getType() == IdType.DESCRIPTOR) {
            aui2 = descriptorContentsMap.get(from.getId()).getAui();
            stype2 = "SDUI";
          } else if (from.getType() == IdType.ATOM) {
            aui2 = atomContentsMap.get(((Atom) from).getId()).getAui();
            stype2 = "AUI";
          }
          relCompRuiMap.put(rel.getId(), rel.getAlternateTerminologyIds()
              .get(getProject().getTerminology()));
          lines.add(getRelLine(rel, cui1, aui1, "SCUI", null, aui2, stype2,
              relCompRuiMap));
        }
      }

      if (atomContentsMap.get(a.getId()).getCodeId() != null) {
        final Code code =
            service.getCode(atomContentsMap.get(a.getId()).getCodeId());

        if (!code.isPublishable()) {
          continue;
        }

        if (codeContentsMap.containsKey(code.getId())
            && codeContentsMap.get(code.getId()).hasRelationships()) {
          for (final CodeRelationship rel : code.getInverseRelationships()) {
            if (!rel.isPublishable()) {
              continue;
            }

            final Code fromCode = rel.getFrom();
            if (fromCode == null) {
              logWarn("Null from component for rel=" + rel);
              continue;
            }
            if (!fromCode.isPublishable()) {
              continue;
            }
            final Contents fromCodeContents =
                codeContentsMap.get(fromCode.getId());
            if (fromCodeContents == null) {
              logWarn("Null codeContents map for code=" + fromCode
                  + ", from rel=" + rel);
              continue;
            }
            final String aui2 = fromCodeContents.getAui();
            if (aui2 == null) {
              logWarn("Null AUI for codeContents=" + fromCodeContents
                  + ", from code=" + fromCode + ", from rel=" + rel);
              continue;
            }
            lines.add(getRelLine(rel, cui1, aui1, "CODE", null, aui2, "CODE",
                relCodeRuiMap));
          }
        }

        // look up component info relationships where STYPE1=CODE
        key = code.getTerminologyId() + code.getTerminology()
            + code.getVersion() + code.getType();
        for (final ComponentInfoRelationship rel : getComponentInfoRels(key)) {

          if (!rel.isPublishable()) {
            continue;
          }

          // determine aui2
          String aui2 = null;
          String stype2 = null;
          final Component from =
              service.findComponent(rel.getFrom(), atomContentsMap);
          if (!from.isPublishable()) {
            continue;
          }
          if (from.getType() == IdType.CONCEPT) {
            aui2 = conceptContentsMap.get(from.getId()).getAui();
            stype2 = "SCUI";
          } else if (from.getType() == IdType.ATOM) {
            aui2 = atomContentsMap.get(((Atom) from).getId()).getAui();
            stype2 = "AUI";
          } else if (from.getType() == IdType.DESCRIPTOR) {
            aui2 = descriptorContentsMap.get(from.getId()).getAui();
            stype2 = "SDUI";
          }
          relCompRuiMap.put(rel.getId(), rel.getAlternateTerminologyIds()
              .get(getProject().getTerminology()));
          lines.add(getRelLine(rel, cui1, aui1, "CODE", null, aui2, stype2,
              relCompRuiMap));

        }
      }

      if (atomContentsMap.get(a.getId()).getDescriptorId() != null) {
        final Descriptor sdui = service
            .getDescriptor(atomContentsMap.get(a.getId()).getDescriptorId());
        if (!sdui.isPublishable()) {
          continue;
        }
        if (descriptorContentsMap.containsKey(sdui.getId())
            && descriptorContentsMap.get(sdui.getId()).hasRelationships()) {
          for (final DescriptorRelationship rel : sdui
              .getInverseRelationships()) {
            if (!rel.isPublishable() || !rel.getFrom().isPublishable()
                || !rel.getTo().isPublishable()) {
              continue;
            }

            final String aui2 =
                descriptorContentsMap.get(rel.getFrom().getId()).getAui();
            lines.add(getRelLine(rel, cui1, aui1, "SDUI", null, aui2, "SDUI",
                relDescriptorRuiMap));
          }
        }

        // look up component info relationships where STYPE1=SDUI
        key = sdui.getTerminologyId() + sdui.getTerminology()
            + sdui.getVersion() + sdui.getType();
        for (final ComponentInfoRelationship rel : getComponentInfoRels(key)) {
          if (!rel.isPublishable()) {
            continue;
          }

          // determine aui2
          String aui2 = null;
          String stype2 = rel.getFrom().getType().toString();
          final Component from =
              service.findComponent(rel.getFrom(), atomContentsMap);
          if (!from.isPublishable()) {
            continue;
          }
          if (from.getType() == IdType.CONCEPT) {
            aui2 = conceptContentsMap.get(from.getId()).getAui();
            stype2 = "SCUI";
          } else if (from.getType() == IdType.CODE) {
            aui2 = codeContentsMap.get(from.getId()).getAui();
            stype2 = "CODE";
          } else if (from.getType() == IdType.ATOM) {
            aui2 = atomContentsMap.get(from.getId()).getAui();
            stype2 = "AUI";
          }
          relCompRuiMap.put(rel.getId(), rel.getAlternateTerminologyIds()
              .get(getProject().getTerminology()));
          lines.add(getRelLine(rel, cui1, aui1, "SDUI", null, aui2, stype2,
              relCompRuiMap));
        }
      }
    } // end for(Atom... concept.getAtoms())

    // PAR/CHD rels to/from SRC should be addressed by component info rels
    // sections

    Collections.sort(lines);
    return lines;

  }

  /**
   * Find component.
   *
   * @param componentInfo the component info
   * @param atomContentsMap the atom contents map
   * @return the component
   * @throws Exception the exception
   */
  private Component findComponent(ComponentInfo componentInfo,
    Map<Long, AtomContents> atomContentsMap) throws Exception {
    if (componentInfo.getType() == IdType.CONCEPT) {
      return getConcept(componentInfo.getTerminologyId(),
          componentInfo.getTerminology(), componentInfo.getVersion(),
          Branch.ROOT);
    } else if (componentInfo.getType() == IdType.CODE) {
      return getCode(componentInfo.getTerminologyId(),
          componentInfo.getTerminology(), componentInfo.getVersion(),
          Branch.ROOT);
    } else if (componentInfo.getType() == IdType.DESCRIPTOR) {
      return getDescriptor(componentInfo.getTerminologyId(),
          componentInfo.getTerminology(), componentInfo.getVersion(),
          Branch.ROOT);
    } else if (componentInfo.getType() == IdType.ATOM) {
      ConceptList list = findConcepts(getProject().getTerminology(),
          getProject().getVersion(), Branch.ROOT,
          "atoms.alternateTerminologyIds:\"" + getProject().getTerminology()
              + "=" + componentInfo.getTerminologyId() + "\"",
          null);
      // If 0 results, try again as an SRC atom search.
      if (list.size() == 0) {
        list = findConcepts(getProject().getTerminology(),
            getProject().getVersion(), Branch.ROOT,
            "atoms.alternateTerminologyIds:\"" + getProject().getTerminology()
                + "-SRC=" + componentInfo.getTerminologyId() + "\"",
            null);
      }
      if (list.size() != 1) {
        logError("ERROR: unexpected number of concepts with AUI "
            + componentInfo.getTerminologyId() + ", " + list.size());
        return null;
      }
      for (final Atom atom : list.getObjects().get(0).getAtoms()) {
        if (atom.isPublishable() && atomContentsMap.get(atom.getId()).getAui()
            .equals(componentInfo.getTerminologyId())) {
          return atom;
        }
        // If no AUI, try again for SRC atom ids
        if (atom.isPublishable() && atomContentsMap.get(atom.getId())
            .getSrcAui().equals(componentInfo.getTerminologyId())) {
          return atom;
        }
      }
    }

    return null;
  }

  /**
   * Returns the component info rels.
   *
   * @param key the key
   * @return the component info rels
   */
  private List<ComponentInfoRelationship> getComponentInfoRels(String key) {
    if (componentInfoRelMap.containsKey(key)) {
      return componentInfoRelMap.get(key);
    } else {
      return new ArrayList<>(0);
    }
  }

  /**
   * Returns the rel line.
   *
   * @param rel the rel
   * @param cui1 the cui 1
   * @param aui1 the aui 1
   * @param stype1 the stype 1
   * @param cui2 the cui 2
   * @param aui2 the aui 2
   * @param stype2 the stype 2
   * @param relRuiMap the rel rui map
   * @return the rel line
   */
  private String getRelLine(Relationship<?, ?> rel, String cui1, String aui1,
    String stype1, String cui2, String aui2, String stype2,
    Map<Long, String> relRuiMap) {
    final StringBuilder sb = new StringBuilder(200);
    // 0 CUI1
    sb.append(cui1).append("|");
    // 1 AUI1
    sb.append(aui1).append("|");
    // 2 STYPE
    sb.append(stype1).append("|");
    // 3 REL
    sb.append(rel.getRelationshipType()).append("|"); // 3
    // 4 CUI2
    sb.append(cui2 == null ? auiCuiMap.get(aui2) : cui2).append("|");
    // 5 AUI2
    sb.append(aui2).append("|");
    // 6 STYPE2
    sb.append(stype2).append("|");
    // 7 RELA
    sb.append(rel.getAdditionalRelationshipType()).append("|");
    // for non-project or non C rels, the RUI is the attached RUI
    final String rui = relRuiMap.get(rel.getId());
    // 8 RUI
    sb.append(rui != null ? rui : "").append("|");
    // 9 SRUI
    sb.append(rel.getTerminologyId().replaceAll("~DA:[\\d]+", "")).append("|");
    // 10 SAB
    sb.append(rel.getTerminology()).append("|");
    // 11 SL
    sb.append(rel.getTerminology()).append("|");
    // 12 RG
    sb.append(rel.getGroup()).append("|");
    // 13 DIR
    final boolean asserts =
        termMap.get(rel.getTerminology()).isAssertsRelDirection();
    sb.append(asserts ? (rel.isAssertedDirection() ? "Y" : "N") : "")
        .append("|");
    // 14 SUPPRESS
    if (rel.isObsolete()) {
      sb.append("O");
    } else if (rel.isSuppressible()) {
      sb.append("Y");
    } else {
      sb.append("N");
    }
    // 15 CVF
    sb.append("||\n");
    return sb.toString();
  }

  /**
   * Write mrhier.
   *
   * @param c the c
   * @param service the service
   * @return the list
   * @throws Exception the exception
   */
  List<String> writeMrhier(Concept c, ContentService service) throws Exception {

    // Field description
    // 0 CUI
    // 1 AUI
    // 2 CXN
    // 3 PAUI
    // 4 SAB
    // 5 RELA
    // 6 PTR
    // 7 HCD
    // 8 CVF
    //
    // e.g. C0001175|A2878223|1|A3316611|SNOMEDCT|isa|
    // A3684559.A3886745.A2880798.A3512117.A3082701.A3316611|||

    final List<String> lines = new ArrayList<>();

    // Atoms
    for (final Atom atom : c.getAtoms()) {
      if (!atom.isPublishable()) {
        continue;
      }
      int ct = 1;
      final String aui = atomContentsMap.get(atom.getId()).getAui();

      // If the atom is an SRC/RHT atom for a terminology that uses SRC root
      // atoms
      if (atom.getTerminology().equals("SRC")
          && atom.getTermType().equals("RHT")
          && terminologyUsingSrcRoot.contains(atom.getCodeId().substring(2))) {
        final StringBuilder sb = new StringBuilder(200);
        sb.append(c.getTerminologyId()).append("|");
        sb.append(aui).append("|");
        sb.append("1|");
        sb.append("|");
        // codeId is something like V-MSH
        sb.append(atom.getCodeId().substring(2)).append("|");
        sb.append("|||||\n");
        lines.add(sb.toString());
        // If writing this line, there won't be other tree positions
        continue;
      }

      if (atomContentsMap.containsKey(atom.getId())
          && atomContentsMap.get(atom.getId()).hasTreePositions()) {
        // Find tree positions for this atom
        for (final AtomTreePosition treepos : atom.getTreePositions()) {

          final StringBuilder ptr = new StringBuilder(200);
          String paui = null;
          String root = null;
          for (final String atomId : FieldedStringTokenizer
              .split(treepos.getAncestorPath(), "~")) {
            if (paui != null) {
              ptr.append(".");
            }
            paui = atomContentsMap.get(Long.valueOf(atomId)).getAui();
            if (paui == null) {
              throw new Exception("atom from ptr is null " + atomId);
            }
            ptr.append(paui);
            if (root == null) {
              final Atom atom2 = service.getAtom(Long.valueOf(atomId));
              root = atom2.getName();
            }
          }

          // e.g. C0001175|A2878223|1|A3316611|SNOMEDCT|isa|
          // A3684559.A3886745.A2880798.A3512117.A3082701.A3316611|||
          final StringBuilder sb = new StringBuilder(200);

          final String srcRhtName =
              terminologyToSrcRhtNameMap.get(treepos.getTerminology());
          // If there was no ancestor path and this isn't the tree-top SRC atom,
          // write the SRC atom as the paui and the ptr
          if (root == null && !atom.getName().equals(srcRhtName)) {
            sb.append(c.getTerminologyId()).append("|");
            sb.append(aui).append("|");
            sb.append("" + ct++).append("|");
            sb.append(terminologyToSrcAuiMap.get(treepos.getTerminology()))
                .append("|");
            sb.append(treepos.getTerminology()).append("|");
            sb.append(treepos.getAdditionalRelationshipType()).append("|");
            sb.append(terminologyToSrcAuiMap.get(treepos.getTerminology()));
            sb.append("|");
            sb.append(treepos.getTerminologyId()).append("|");
            sb.append("|");
          }
          // If there was an ancestor path but it did go all the way to the
          // tree-top SRC atom, prepend the SRC atom to the ptr
          else if (root != null && !root.equals(srcRhtName)) {
            sb.append(c.getTerminologyId()).append("|");
            sb.append(aui).append("|");
            sb.append("" + ct++).append("|");
            sb.append(paui != null ? paui : "").append("|");
            sb.append(treepos.getTerminology()).append("|");
            sb.append(treepos.getAdditionalRelationshipType()).append("|");
            sb.append(terminologyToSrcAuiMap.get(treepos.getTerminology()))
                .append(".").append(ptr.toString()).append("|");
            sb.append(treepos.getTerminologyId()).append("|");
            sb.append("|");
          }
          // Otherwise write out the paui and ptr as-constructed
          else {
            sb.append(c.getTerminologyId()).append("|");
            sb.append(aui).append("|");
            sb.append("" + ct++).append("|");
            sb.append(paui != null ? paui : "").append("|");
            sb.append(treepos.getTerminology()).append("|");
            sb.append(treepos.getAdditionalRelationshipType()).append("|");
            sb.append(ptr.toString()).append("|");
            sb.append(treepos.getTerminologyId()).append("|");
            sb.append("|");
          }
          
          sb.append("\n");
          lines.add(sb.toString());
        }
      }

      // Try for concept treepos
      final Long cId = atomContentsMap.get(atom.getId()).getConceptId();
      if (conceptContentsMap.containsKey(cId)
          && conceptContentsMap.get(cId).hasTreePositions()) {
        final Concept scui = service
            .getConcept(atomContentsMap.get(atom.getId()).getConceptId());
        for (final ConceptTreePosition treepos : scui.getTreePositions()) {

          final StringBuilder ptr = new StringBuilder(200);
          String paui = null;
          String root = null;
          for (final String conceptId : FieldedStringTokenizer
              .split(treepos.getAncestorPath(), "~")) {
            if (paui != null) {
              ptr.append(".");
            }
            paui = conceptContentsMap.get(Long.valueOf(conceptId)).getAui();
            if (paui == null) {
              throw new Exception("concept from ptr is null " + conceptId);
            }

            ptr.append(paui);
            if (root == null) {
              final Concept concept2 =
                  service.getConcept(Long.valueOf(conceptId));
              root = concept2.getName();
            }
          }

          // e.g. C0001175|A2878223|1|A3316611|SNOMEDCT|isa|
          // A3684559.A3886745.A2880798.A3512117.A3082701.A3316611|||
          final StringBuilder sb = new StringBuilder(200);

          final String srcRhtName =
              terminologyToSrcRhtNameMap.get(treepos.getTerminology());
          // If there was no ancestor path and this isn't the tree-top SRC atom,
          // write the SRC atom as the paui and the ptr
          if (root == null && !atom.getName().equals(srcRhtName)) {
            sb.append(c.getTerminologyId()).append("|");
            sb.append(aui).append("|");
            sb.append("" + ct++).append("|");
            sb.append(terminologyToSrcAuiMap.get(treepos.getTerminology()))
                .append("|");
            sb.append(treepos.getTerminology()).append("|");
            sb.append(treepos.getAdditionalRelationshipType()).append("|");
            sb.append(terminologyToSrcAuiMap.get(treepos.getTerminology()));
            sb.append("|");
            sb.append(treepos.getTerminologyId()).append("|");
            sb.append("|");
          }
          // If there was an ancestor path but it did go all the way to the
          // tree-top SRC atom, prepend the SRC atom to the ptr
          else if (root != null && !root.equals(srcRhtName)) {
            sb.append(c.getTerminologyId()).append("|");
            sb.append(aui).append("|");
            sb.append("" + ct++).append("|");
            sb.append(paui != null ? paui : "").append("|");
            sb.append(treepos.getTerminology()).append("|");
            sb.append(treepos.getAdditionalRelationshipType()).append("|");
            sb.append(terminologyToSrcAuiMap.get(treepos.getTerminology()))
                .append(".").append(ptr.toString()).append("|");
            sb.append(treepos.getTerminologyId()).append("|");
            sb.append("|");
          }
          // Otherwise write out the paui and ptr as-constructed
          else {
            sb.append(c.getTerminologyId()).append("|");
            sb.append(aui).append("|");
            sb.append("" + ct++).append("|");
            sb.append(paui != null ? paui : "").append("|");
            sb.append(treepos.getTerminology()).append("|");
            sb.append(treepos.getAdditionalRelationshipType()).append("|");
            sb.append(ptr.toString()).append("|");
            sb.append(treepos.getTerminologyId()).append("|");
            sb.append("|");
          }

          sb.append("\n");
          lines.add(sb.toString());
        }
      }

      // Try for descriptor treepos
      final Long dId = atomContentsMap.get(atom.getId()).getDescriptorId();
      if (descriptorContentsMap.containsKey(dId)
          && descriptorContentsMap.get(dId).hasTreePositions()) {
        final Descriptor sdui = service
            .getDescriptor(atomContentsMap.get(atom.getId()).getDescriptorId());
        for (final DescriptorTreePosition treepos : sdui.getTreePositions()) {

          final StringBuilder ptr = new StringBuilder(200);
          String paui = null;
          String root = null;
          for (final String descriptorId : FieldedStringTokenizer
              .split(treepos.getAncestorPath(), "~")) {
            if (paui != null) {
              ptr.append(".");
            }
            paui =
                descriptorContentsMap.get(Long.valueOf(descriptorId)).getAui();
            if (paui == null) {
              throw new Exception(
                  "descriptor from ptr is null " + descriptorId);
            }
            ptr.append(paui);
            if (root == null) {
              final Descriptor descriptor2 =
                  service.getDescriptor(Long.valueOf(descriptorId));
              root = descriptor2.getName();
            }
          }

          // e.g. C0001175|A2878223|1|A3316611|SNOMEDCT|isa|
          // A3684559.A3886745.A2880798.A3512117.A3082701.A3316611|||
          final StringBuilder sb = new StringBuilder(200);

          final String srcRhtName =
              terminologyToSrcRhtNameMap.get(treepos.getTerminology());
          // If there was no ancestor path and this isn't the tree-top SRC atom,
          // write the SRC atom as the paui and the ptr
          if (root == null && !atom.getName().equals(srcRhtName)) {
            sb.append(c.getTerminologyId()).append("|");
            sb.append(aui).append("|");
            sb.append("" + ct++).append("|");
            sb.append(terminologyToSrcAuiMap.get(treepos.getTerminology()))
                .append("|");
            sb.append(treepos.getTerminology()).append("|");
            sb.append(treepos.getAdditionalRelationshipType()).append("|");
            sb.append(terminologyToSrcAuiMap.get(treepos.getTerminology()));
            sb.append("|");
            sb.append(treepos.getTerminologyId()).append("|");
            sb.append("|");
          }
          // If there was an ancestor path but it did go all the way to the
          // tree-top SRC atom, prepend the SRC atom to the ptr
          else if (root != null && !root.equals(srcRhtName)) {
            sb.append(c.getTerminologyId()).append("|");
            sb.append(aui).append("|");
            sb.append("" + ct++).append("|");
            sb.append(paui != null ? paui : "").append("|");
            sb.append(treepos.getTerminology()).append("|");
            sb.append(treepos.getAdditionalRelationshipType()).append("|");
            sb.append(terminologyToSrcAuiMap.get(treepos.getTerminology()))
                .append(".").append(ptr.toString()).append("|");
            sb.append(treepos.getTerminologyId()).append("|");
            sb.append("|");
          }
          // Otherwise write out the paui and ptr as-constructed
          else {
            sb.append(c.getTerminologyId()).append("|");
            sb.append(aui).append("|");
            sb.append("" + ct++).append("|");
            sb.append(paui != null ? paui : "").append("|");
            sb.append(treepos.getTerminology()).append("|");
            sb.append(treepos.getAdditionalRelationshipType()).append("|");
            sb.append(ptr.toString()).append("|");
            sb.append(treepos.getTerminologyId()).append("|");
            sb.append("|");
          }

          sb.append("\n");
          lines.add(sb.toString());
        }

      }

      // Try for code treepos
      final Long cdId = atomContentsMap.get(atom.getId()).getCodeId();
      if (codeContentsMap.containsKey(cdId)
          && codeContentsMap.get(cdId).hasTreePositions()) {

        final Code code =
            service.getCode(atomContentsMap.get(atom.getId()).getCodeId());
        for (final CodeTreePosition treepos : code.getTreePositions()) {

          final StringBuilder ptr = new StringBuilder(200);
          String paui = null;
          String root = null;
          for (final String codeId : FieldedStringTokenizer
              .split(treepos.getAncestorPath(), "~")) {
            if (paui != null) {
              ptr.append(".");
            }
            paui = codeContentsMap.get(Long.valueOf(codeId)).getAui();
            if (paui == null) {
              throw new Exception("code from ptr is null " + codeId);
            }
            ptr.append(paui);
            if (root == null) {
              final Code code2 = service.getCode(Long.valueOf(codeId));
              root = code2.getName();
            }
          }

          // e.g. C0001175|A2878223|1|A3316611|SNOMEDCT|isa|
          // A3684559.A3886745.A2880798.A3512117.A3082701.A3316611|||
          final StringBuilder sb = new StringBuilder(200);

          final String srcRhtName =
              terminologyToSrcRhtNameMap.get(treepos.getTerminology());
          // If there was no ancestor path and this isn't the tree-top SRC atom,
          // write the SRC atom as the paui and the ptr
          if (root == null && !atom.getName().equals(srcRhtName)) {
            sb.append(c.getTerminologyId()).append("|");
            sb.append(aui).append("|");
            sb.append("" + ct++).append("|");
            sb.append(terminologyToSrcAuiMap.get(treepos.getTerminology()))
                .append("|");
            sb.append(treepos.getTerminology()).append("|");
            sb.append(treepos.getAdditionalRelationshipType()).append("|");
            sb.append(terminologyToSrcAuiMap.get(treepos.getTerminology()));
            sb.append("|");
            sb.append(treepos.getTerminologyId()).append("|");
            sb.append("|");
          }
          // If there was an ancestor path but it did go all the way to the
          // tree-top SRC atom, prepend the SRC atom to the ptr
          else if (root != null && !root.equals(srcRhtName)) {
            sb.append(c.getTerminologyId()).append("|");
            sb.append(aui).append("|");
            sb.append("" + ct++).append("|");
            sb.append(paui != null ? paui : "").append("|");
            sb.append(treepos.getTerminology()).append("|");
            sb.append(treepos.getAdditionalRelationshipType()).append("|");
            sb.append(terminologyToSrcAuiMap.get(treepos.getTerminology()))
                .append(".").append(ptr.toString()).append("|");
            sb.append(treepos.getTerminologyId()).append("|");
            sb.append("|");
          }
          // Otherwise write out the paui and ptr as-constructed
          else {
            sb.append(c.getTerminologyId()).append("|");
            sb.append(aui).append("|");
            sb.append("" + ct++).append("|");
            sb.append(paui != null ? paui : "").append("|");
            sb.append(treepos.getTerminology()).append("|");
            sb.append(treepos.getAdditionalRelationshipType()).append("|");
            sb.append(ptr.toString()).append("|");
            sb.append(treepos.getTerminologyId()).append("|");
            sb.append("|");
          }

          sb.append("\n");
          lines.add(sb.toString());
        }
      }

    } // end for (final Atom...

    Collections.sort(lines);
    return lines;
  }

  /**
   * Write mrsat.
   *
   * @param c the c
   * @param service the service
   * @return the list
   * @throws Exception the exception
   */
  List<String> writeMrsat(Concept c, ContentService service) throws Exception {

    // Field Description
    // 0 CUI
    // 1 LUI
    // 2 SUI
    // 3 METAUI
    // 4 STYPE
    // 5 CODE
    // 6 ATUI
    // 7 SATUI
    // 8 ATN
    // 9 SAB
    // 10 ATV
    // 11 SUPPRESS
    // 12 CVF
    //
    // e.g.
    // C0001175|L0001175|S0010339|A0019180|SDUI|D000163|AT38209082||FX|MSH|D015492|N||
    // C0001175|L0001175|S0354232|A2922342|AUI|62479008|AT24600515||DESCRIPTIONSTATUS|SNOMEDCT|0|N||
    // C0001175|L0001842|S0011877|A15662389|CODE|T1|AT100434486||URL|MEDLINEPLUS|http://www.nlm.nih.gov/medlineplus/aids.html|N||
    // C0001175|||R54775538|RUI||AT63713072||CHARACTERISTICTYPE|SNOMEDCT|0|N||
    // C0001175|||R54775538|RUI||AT69142126||REFINABILITY|SNOMEDCT|1|N||

    // NOTE: MR/ST/DA attributes are not written out for NCIMETA

    final List<String> lines = new ArrayList<>();

    // Concept attributes (CUIs)
    // Only do this if the concept has attributes
    if (conceptContentsMap.containsKey(c.getId())
        && conceptContentsMap.get(c.getId()).hasAttributes()) {

      for (final Attribute att : c.getAttributes()) {

        if (!att.isPublishable()) {
          continue;
        }

        final StringBuilder sb = new StringBuilder(200);
        // CUI
        sb.append(c.getTerminologyId()).append("|");
        // LUI, SUI, METAUI
        sb.append("|||");
        // STYPE
        sb.append("CUI").append("|");
        // CODE
        sb.append("|");
        // ATUI
        final String atui = attAtuiMap.get(att.getId());
        sb.append(atui != null ? atui : "").append("|");
        // SATUI
        sb.append(att.getTerminologyId() != null ? att.getTerminologyId() : "")
            .append("|");
        // ATN
        sb.append(att.getName()).append("|");
        // SAB
        sb.append(att.getTerminology()).append("|");
        // ATV
        sb.append(att.getValue()).append("|");
        // SUPPRESS
        if (att.isObsolete()) {
          sb.append("O");
        } else if (att.isSuppressible()) {
          sb.append("Y");
        } else {
          sb.append("N");
        }
        // CVF
        sb.append("||\n");
        lines.add(sb.toString());
      }
    }

    // Handle atom, and atom class attributes
    for (final Atom a : c.getAtoms()) {
      if (!a.isPublishable()) {
        continue;
      }

      // Atom attributes (AUIs)
      // e.g.
      // C0000005|L0186915|S2192525|A4345877|AUI|D012711|AT25166652||TERMUI|MSH|T037573|N||
      // Only do this if the atom has attributes
      if (atomContentsMap.containsKey(a.getId())
          && atomContentsMap.get(a.getId()).hasAttributes()) {

        for (final Attribute att : a.getAttributes()) {
          if (!att.isPublishable()) {
            continue;
          }

          final StringBuilder sb = new StringBuilder(200);
          // CUI
          sb.append(c.getTerminologyId()).append("|");
          // LUI
          sb.append(a.getLexicalClassId()).append("|");
          // SUI
          sb.append(a.getStringClassId()).append("|");
          // METAUI
          sb.append(atomContentsMap.get(a.getId()).getAui()).append("|");
          // STYPE
          sb.append("AUI").append("|");
          // CODE
          sb.append(a.getCodeId()).append("|");
          // ATUI
          final String atui = attAtuiMap.get(att.getId());
          sb.append(atui != null ? atui : "").append("|");
          // SATUI
          sb.append(
              att.getTerminologyId() != null ? att.getTerminologyId() : "")
              .append("|");
          // ATN
          sb.append(att.getName()).append("|");
          // SAB
          sb.append(att.getTerminology()).append("|");
          // ATV
          sb.append(att.getValue()).append("|");
          // SUPPRESS
          if (att.isObsolete()) {
            sb.append("O");
          } else if (att.isSuppressible()) {
            sb.append("Y");
          } else {
            sb.append("N");
          }
          // CVF
          sb.append("||\n");
          lines.add(sb.toString());
        }
      }

      // Atom relationship attributes (RUIs)
      // e.g.
      // C0000097|||R94999574|RUI||AT110096379||CHARACTERISTIC_TYPE_ID|SNOMEDCT_US|900000000000011006|N||
      if (ruiAttributeTerminologies.contains(a.getTerminology())) {
        for (final AtomRelationship rel : a.getInverseRelationships()) {
          if (!rel.isPublishable()) {
            continue;
          }

          for (final Attribute attribute : rel.getAttributes()) {
            if (!attribute.isPublishable()) {
              continue;
            }
            final StringBuilder sb = new StringBuilder(200);
            // CUI
            sb.append(c.getTerminologyId()).append("|");
            // LUI
            sb.append("|");
            // SUI
            sb.append("|");
            // METAUI
            sb.append(relAtomRuiMap.get(rel.getId())).append("|");
            // STYPE
            sb.append("RUI").append("|");
            // CODE
            sb.append("|");
            // ATUI
            final String atui = attAtuiMap.get(attribute.getId());
            sb.append(atui != null ? atui : "").append("|");
            // SATUI
            sb.append(attribute.getTerminologyId() != null
                ? attribute.getTerminologyId() : "").append("|");
            // ATN
            sb.append(attribute.getName()).append("|");
            // SAB
            sb.append(attribute.getTerminology()).append("|");
            // ATV
            sb.append(attribute.getValue()).append("|");
            // SUPPRESS
            if (attribute.isObsolete()) {
              sb.append("O");
            } else if (attribute.isSuppressible()) {
              sb.append("Y");
            } else {
              sb.append("N");
            }
            // CVF
            sb.append("||\n");
            lines.add(sb.toString());
          }
        }
      }

      // Subset members
      // e.g.
      // C0000052|L3853359|S4536829|A23245828|AUI|58488005|AT166631006|
      // cf28ec3d-cf07-59cb-944a-10ef4f43b725|SUBSET_MEMBER|SCTSPA|
      // 450828004~ACCEPTABILITYID~900000000000549004|N||
      // C0000052|L3853359|S4536829|A23245828|AUI|58488005|AT166631006|
      // cf28ec3d-cf07-59cb-944a-10ef4f43b725|SUBSET_MEMBER|SNOMEDCT|
      // 450828004|N||
      if (atomContentsMap.containsKey(a.getId())
          && atomContentsMap.get(a.getId()).hasMembers()) {

        for (final AtomSubsetMember member : a.getMembers()) {
          if (!member.isPublishable()) {
            continue;
          }

          for (final Attribute att : member.getAttributes()) {
            if (!att.isPublishable()) {
              continue;
            }
            final StringBuilder sb = new StringBuilder(200);
            sb.append(c.getTerminologyId()).append("|");
            sb.append(a.getLexicalClassId()).append("|");
            sb.append(a.getStringClassId()).append("|");
            sb.append(atomContentsMap.get(a.getId()).getAui()).append("|");
            sb.append("AUI").append("|");
            sb.append(a.getCodeId()).append("|");
            sb.append(attAtuiMap.get(att.getId())).append("|");
            sb.append(member.getTerminologyId()).append("|");
            sb.append("SUBSET_MEMBER").append("|");
            sb.append(att.getTerminology()).append("|");
            sb.append(member.getSubset().getTerminologyId());
            if (!ConfigUtility.isEmpty(att.getName())) {
              sb.append("~").append(att.getName());
              sb.append("~").append(att.getValue());
            }
            sb.append("|");
            if (att.isObsolete()) {
              sb.append("O");
            } else if (att.isSuppressible()) {
              sb.append("Y");
            } else {
              sb.append("N");
            }
            // CVF
            sb.append("||\n");
            lines.add(sb.toString());
          }

        }
      }
      // Source concept attributes (SCUIs)
      // e.g.
      // C0000102|L0121443|S1286670|A3714229|SCUI|13579002|AT112719256||ACTIVE|SNOMEDCT_US|1|N||
      // If this is the preferred atom id of the scui
      if (atomContentsMap.get(a.getId()).getConceptId() != null) {

        final Concept scui =
            service.getConcept(atomContentsMap.get(a.getId()).getConceptId());

        if (conceptContentsMap.containsKey(scui.getId())
            && conceptContentsMap.get(scui.getId()).hasAttributes()) {

          for (final Attribute attribute : scui.getAttributes()) {
            if (!attribute.isPublishable()) {
              continue;
            }

            final StringBuilder sb = new StringBuilder(200);
            sb.append(c.getTerminologyId()).append("|");
            sb.append(a.getLexicalClassId()).append("|");
            sb.append(a.getStringClassId()).append("|");
            sb.append(atomContentsMap.get(a.getId()).getAui()).append("|");
            sb.append("SCUI").append("|");
            sb.append(a.getCodeId()).append("|");
            final String atui = attAtuiMap.get(attribute.getId());
            sb.append(atui != null ? atui : "").append("|");
            sb.append(attribute.getTerminologyId() != null
                ? attribute.getTerminologyId() : "").append("|");
            sb.append(attribute.getName()).append("|");
            sb.append(attribute.getTerminology()).append("|");
            sb.append(attribute.getValue()).append("|");
            if (attribute.isObsolete()) {
              sb.append("O");
            } else if (attribute.isSuppressible()) {
              sb.append("Y");
            } else {
              sb.append("N");
            }
            // CVF
            sb.append("||\n");
            lines.add(sb.toString());
          }
        }

        // Source concept relationship attributes (RUIs)
        if (ruiAttributeTerminologies.contains(scui.getTerminology())) {
          for (final ConceptRelationship rel : scui.getInverseRelationships()) {
            if (!rel.isPublishable()) {
              continue;
            }

            for (final Attribute attribute : rel.getAttributes()) {
              if (!attribute.isPublishable()) {
                continue;
              }
              final StringBuilder sb = new StringBuilder(200);
              sb.append(c.getTerminologyId()).append("|");
              sb.append("|");
              sb.append("|");
              sb.append(relConceptRuiMap.get(rel.getId())).append("|");
              sb.append("RUI").append("|");
              sb.append("|");
              final String atui = attAtuiMap.get(attribute.getId());
              sb.append(atui != null ? atui : "").append("|");
              sb.append(attribute.getTerminologyId() != null
                  ? attribute.getTerminologyId() : "").append("|");
              sb.append(attribute.getName()).append("|");
              sb.append(attribute.getTerminology()).append("|");
              sb.append(attribute.getValue()).append("|");
              if (attribute.isObsolete()) {
                sb.append("O");
              } else if (attribute.isSuppressible()) {
                sb.append("Y");
              } else {
                sb.append("N");
              }
              // CVF
              sb.append("||\n");
              lines.add(sb.toString());
            }
          }
        }

        // Concept subset members
        // C0000102|L0121443|S1286670|A3714229|SCUI|13579002|AT109859972|cbe76318-0356-54e6-9935-03962bd340eb|SUBSET_MEMBER|SNOMEDCT_US|900000000000498005~MAPTARGET~C-29040|N||
        if (conceptContentsMap.containsKey(scui.getId())
            && conceptContentsMap.get(scui.getId()).hasMembers()) {

          for (final ConceptSubsetMember member : scui.getMembers()) {

            if (!member.isPublishable()) {
              continue;
            }

            for (final Attribute att : member.getAttributes()) {
              if (!att.isPublishable()) {
                continue;
              }

              final StringBuilder sb = new StringBuilder(200);
              sb.append(c.getTerminologyId()).append("|");
              sb.append(a.getLexicalClassId()).append("|");
              sb.append(a.getStringClassId()).append("|");
              sb.append(atomContentsMap.get(a.getId()).getAui()).append("|");
              sb.append("SCUI").append("|");
              sb.append(a.getCodeId()).append("|");
              sb.append(attAtuiMap.get(att.getId())).append("|");
              sb.append(member.getTerminologyId()).append("|");
              sb.append("SUBSET_MEMBER").append("|");
              sb.append(att.getTerminology()).append("|");
              sb.append(member.getSubset().getTerminologyId());
              if (!ConfigUtility.isEmpty(att.getName())) {
                sb.append("~").append(att.getName());
                sb.append("~").append(att.getValue());
              }
              sb.append("|");
              if (att.isObsolete()) {
                sb.append("O");
              } else if (att.isSuppressible()) {
                sb.append("Y");
              } else {
                sb.append("N");
              }
              // CVF
              sb.append("||\n");
              lines.add(sb.toString());
            }

          }
        }
      }

      // Code attributes
      // e.g.
      // C0010654|L1371351|S2026553|A10006797|SCUI|NPO_384|AT73054966||CODE|NPO|NPO_384|N||
      // If atom is the preferred atom of the CODE
      if (atomContentsMap.get(a.getId()).getCodeId() != null) {
        final Code code =
            service.getCode(atomContentsMap.get(a.getId()).getCodeId());

        if (codeContentsMap.containsKey(code.getId())
            && codeContentsMap.get(code.getId()).hasAttributes()) {
          for (final Attribute attribute : code.getAttributes()) {
            if (!attribute.isPublishable()) {
              continue;
            }

            final StringBuilder sb = new StringBuilder(200);
            // CUI
            sb.append(c.getTerminologyId()).append("|");
            // LUI
            sb.append(a.getLexicalClassId()).append("|");
            // SUI
            sb.append(a.getStringClassId()).append("|");
            // METAUI
            sb.append(atomContentsMap.get(a.getId()).getAui()).append("|");
            // STYPE
            sb.append("CODE").append("|");
            // CODE
            sb.append(a.getCodeId()).append("|");
            // ATUI
            final String atui = attAtuiMap.get(attribute.getId());
            sb.append(atui).append("|");
            // SATUI
            sb.append(attribute.getTerminologyId() != null
                ? attribute.getTerminologyId() : "").append("|");
            // ATN
            sb.append(attribute.getName()).append("|");
            // SAB
            sb.append(attribute.getTerminology()).append("|");
            // ATV
            sb.append(attribute.getValue()).append("|");
            // SUPPRESS
            if (attribute.isObsolete()) {
              sb.append("O");
            } else if (attribute.isSuppressible()) {
              sb.append("Y");
            } else {
              sb.append("N");
            }
            // CVF
            sb.append("||\n");
            lines.add(sb.toString());
          }
        }

        // Code relationship attributes (RUIs)
        if (ruiAttributeTerminologies.contains(code.getTerminology())) {
          for (final CodeRelationship rel : code.getInverseRelationships()) {
            if (!rel.isPublishable()) {
              continue;
            }

            for (final Attribute attribute : rel.getAttributes()) {
              if (!attribute.isPublishable()) {
                continue;
              }
              final StringBuilder sb = new StringBuilder(200);
              sb.append(c.getTerminologyId()).append("|");
              sb.append("|");
              sb.append("|");
              sb.append(relCodeRuiMap.get(rel.getId())).append("|");
              sb.append("RUI").append("|");
              sb.append("|");
              final String atui = attAtuiMap.get(attribute.getId());
              sb.append(atui != null ? atui : "").append("|");
              sb.append(attribute.getTerminologyId() != null
                  ? attribute.getTerminologyId() : "").append("|");
              sb.append(attribute.getName()).append("|");
              sb.append(attribute.getTerminology()).append("|");
              sb.append(attribute.getValue()).append("|");
              if (attribute.isObsolete()) {
                sb.append("O");
              } else if (attribute.isSuppressible()) {
                sb.append("Y");
              } else {
                sb.append("N");
              }
              // CVF
              sb.append("||\n");
              lines.add(sb.toString());
            }
          }
        }

      }

      // Source Descriptor attributes
      // if atom is preferred atom of the descriptor
      if (atomContentsMap.get(a.getId()).getDescriptorId() != null) {
        final Descriptor sdui = service
            .getDescriptor(atomContentsMap.get(a.getId()).getDescriptorId());
        if (descriptorContentsMap.containsKey(sdui.getId())
            && descriptorContentsMap.get(sdui.getId()).hasAttributes()) {

          for (final Attribute attribute : sdui.getAttributes()) {
            if (!attribute.isPublishable()) {
              continue;
            }
            final StringBuilder sb = new StringBuilder(200);
            sb.append(c.getTerminologyId()).append("|");
            sb.append(a.getLexicalClassId()).append("|");
            sb.append(a.getStringClassId()).append("|");
            sb.append(atomContentsMap.get(a.getId()).getAui()).append("|");
            sb.append("SDUI").append("|");
            sb.append(a.getCodeId()).append("|");
            final String atui = attAtuiMap.get(attribute.getId());
            sb.append(atui != null ? atui : "").append("|");
            sb.append(attribute.getTerminologyId() != null
                ? attribute.getTerminologyId() : "").append("|");
            sb.append(attribute.getName()).append("|");
            sb.append(attribute.getTerminology()).append("|");
            sb.append(attribute.getValue()).append("|");
            if (attribute.isObsolete()) {
              sb.append("O");
            } else if (attribute.isSuppressible()) {
              sb.append("Y");
            } else {
              sb.append("N");
            }
            sb.append("||\n");
            lines.add(sb.toString());
          }
        }

        // Descriptor relationship attributes (RUIs)
        if (ruiAttributeTerminologies.contains(sdui.getTerminology())) {
          for (final DescriptorRelationship rel : sdui.getInverseRelationships()) {
            if (!rel.isPublishable()) {
              continue;
            }

            for (final Attribute attribute : rel.getAttributes()) {
              if (!attribute.isPublishable()) {
                continue;
              }
              final StringBuilder sb = new StringBuilder(200);
              sb.append(c.getTerminologyId()).append("|");
              sb.append("|");
              sb.append("|");
              sb.append(relDescriptorRuiMap.get(rel.getId())).append("|");
              sb.append("RUI").append("|");
              sb.append("|");
              final String atui = attAtuiMap.get(attribute.getId());
              sb.append(atui != null ? atui : "").append("|");
              sb.append(attribute.getTerminologyId() != null
                  ? attribute.getTerminologyId() : "").append("|");
              sb.append(attribute.getName()).append("|");
              sb.append(attribute.getTerminology()).append("|");
              sb.append(attribute.getValue()).append("|");
              if (attribute.isObsolete()) {
                sb.append("O");
              } else if (attribute.isSuppressible()) {
                sb.append("Y");
              } else {
                sb.append("N");
              }
              // CVF
              sb.append("||\n");
              lines.add(sb.toString());
            }
          }
        }
      }

    } // end for (c.getAtoms)
    Collections.sort(lines);
    return lines;
  }
  /**
   * Write ambig.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private void writeAmbig() throws Exception {
    // Find ambig SUIs, write them out.
    logInfo("  Write AMBIGSUI.RRF");
    Query query = manager
        .createQuery("select distinct a.stringClassId, c.terminologyId from "
            + "ConceptJpa c join c.atoms a, ConceptJpa c2 join c2.atoms a2 "
            + "where c.id != c2.id and a.stringClassId = a2.stringClassId"
            + "  and c.terminology = :terminology and c2.terminology = :terminology"
            + "  and c.version = :version and c2.version = :version"
            + "  and a.publishable = true and a2.publishable = true order by 1,2");
    query.setParameter("terminology", getProject().getTerminology());
    query.setParameter("version", getProject().getVersion());
    List<Object[]> results = query.getResultList();
    logInfo("    count = " + results.size());
    for (final Object[] result : results) {
      writerMap.get("AMBIGSUI.RRF").print(result[0] + "|" + result[1] + "|\n");
    }

    // Find ambig LUIs, write them out.
    logInfo("  Write AMBIGLUI.RRF");
    query = manager
        .createQuery("select distinct a.lexicalClassId, c.terminologyId from "
            + "ConceptJpa c join c.atoms a, ConceptJpa c2 join c2.atoms a2 "
            + "where c.id != c2.id"
            + "  and a.lexicalClassId = a2.lexicalClassId"
            + "  and c.terminology = :terminology and c2.terminology = :terminology"
            + "  and c.version = :version and c2.version = :version"
            + "  and a.publishable = true and a2.publishable = true order by 1,2");
    query.setParameter("terminology", getProject().getTerminology());
    query.setParameter("version", getProject().getVersion());
    results = query.getResultList();
    logInfo("    count = " + results.size());
    for (final Object[] result : results) {
      writerMap.get("AMBIGLUI.RRF").print(result[0] + "|" + result[1] + "|\n");
    }
  }

  /**
   * Reset.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void reset() throws Exception {
    logInfo("Starting RESET " + getName());
    // n/a
    logInfo("Finished RESET " + getName());

  }

  /**
   * Check properties.
   *
   * @param p the p
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        ""
    }, p);
  }

  /**
   * Sets the properties.
   *
   * @param p the properties
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        ""
    }, p);
  }

  /**
   * Returns the description.
   *
   * @return the description
   */
  /* see superclass */
  @Override
  public String getDescription() {
    return ConfigUtility.getNameFromClass(getClass());
  }

  /**
   * Represents connected data for an object type (see maps at the top).
   */
  class Contents {
    // 0 - attributes
    // 1 - relationships
    // 2 - tree positions
    // 3 - members
    // 4 - definitions
    /** The data. */
    private boolean[] data = new boolean[5];

    /** The aui. */
    private String aui = null;

    /**
     * Returns the aui.
     *
     * @return the aui
     */
    public String getAui() {
      return aui;
    }

    /**
     * Sets the aui.
     *
     * @param aui the aui
     */
    public void setAui(String aui) {
      this.aui = aui;
    }

    /** The source aui. */
    private String srcAui = "";

    /**
     * Returns the src aui.
     *
     * @return the src aui
     */
    public String getSrcAui() {
      return srcAui;
    }

    /**
     * Sets the src aui.
     *
     * @param srcAui the src aui
     */
    public void setSrcAui(String srcAui) {
      this.srcAui = srcAui;
    }

    /**
     * Checks for attributes.
     *
     * @return true, if successful
     */
    public boolean hasAttributes() {
      return data[0];
    }

    /**
     * Mark attributes.
     */
    public void markAttributes() {
      data[0] = true;
    }

    /**
     * Checks for relationships.
     *
     * @return true, if successful
     */
    public boolean hasRelationships() {
      return data[1];
    }

    /**
     * Mark relationships.
     */
    public void markRelationships() {
      data[1] = true;
    }

    /**
     * Checks for tree positions.
     *
     * @return true, if successful
     */
    public boolean hasTreePositions() {
      return data[2];
    }

    /**
     * Mark tre positions.
     */
    public void markTreePositions() {
      data[2] = true;
    }

    /**
     * Checks for members.
     *
     * @return true, if successful
     */
    public boolean hasMembers() {
      return data[3];
    }

    /**
     * Mark members.
     */
    public void markMembers() {
      data[3] = true;
    }

    /**
     * Checks for definitions.
     *
     * @return true, if successful
     */
    public boolean hasDefinitions() {
      return data[4];
    }

    /**
     * Mark definitions.
     */
    public void markDefinitions() {
      data[4] = true;
    }
  }

  /**
   * The Class AtomContents.
   */
  class AtomContents extends Contents {

    /** The code id. */
    private Long codeId;

    /** The concept id. */
    private Long conceptId;

    /** The descriptor id. */
    private Long descriptorId;

    /**
     * Returns the code id.
     *
     * @return the code id
     */
    public Long getCodeId() {
      return codeId;
    }

    /**
     * Sets the code id.
     *
     * @param codeId the code id
     */
    public void setCodeId(Long codeId) {
      this.codeId = codeId;
    }

    /**
     * Returns the concept id.
     *
     * @return the concept id
     */
    public Long getConceptId() {
      return conceptId;
    }

    /**
     * Sets the concept id.
     *
     * @param conceptId the concept id
     */
    public void setConceptId(Long conceptId) {
      this.conceptId = conceptId;
    }

    /**
     * Returns the descriptor id.
     *
     * @return the descriptor id
     */
    public Long getDescriptorId() {
      return descriptorId;
    }

    /**
     * Sets the descriptor id.
     *
     * @param descriptorId the descriptor id
     */
    public void setDescriptorId(Long descriptorId) {
      this.descriptorId = descriptorId;
    }

  }

}
