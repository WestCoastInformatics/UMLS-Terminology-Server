package com.wci.umls.server.jpa.algo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.PfscParameter;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.jpa.helpers.PfscParameterJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.handlers.expr.EclConceptFieldNames;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.helpers.ProgressListener;

/**
 * The Expression Constraint Language Index Writer
 */
public class EclConceptIndexingAlgorithm implements Algorithm {

  /** The terminology */
  private String terminology = null;

  /** The version */
  private String version = null;

  /** The organizing class type */
  private IdType idType = null;

  /** The content service. */
  private ContentServiceJpa contentService = null;

  /** The index writer. */
  private IndexWriter iwriter = null;

  /** The index output directory. */
  private Directory directory = null;

  /** The number of concepts to process at each step */
  private int batchSize = 2000;

  /** The id map, hibernate ids to terminology ids */
  private Map<Long, String> idMap = new HashMap<>();

  /** The ancestor map, terminologyId -> ancestor terminology ids */
  private Map<String, Set<String>> ancestorMap = new HashMap<>();

  /** The subset map, terminologyId -> subset terminology id. */
  private Map<String, Set<String>> subsetMemberMap = new HashMap<>();

  /** The subsets map, id -> terminologyId */
  private Map<Long, String> subsetMap = new HashMap<>();

  /** Tracking variables */
  private int ancestorCt = 0;

  private int relationshipCt = 0;

  private int subsetCt = 0;

  /**
   * Instantiates a new ecl concept writer algorithm.
   *
   * @throws Exception the exception
   */
  public EclConceptIndexingAlgorithm() throws Exception {
    // constructor
  }

  /**
   * @param terminology
   * @param version
   * @param contentService
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public void compute() throws Exception {

    Logger.getLogger(getClass())
        .info("Computing expression constraint language indexes for "
            + terminology + ", " + version);

    if (terminology == null) {
      throw new Exception("Must specify terminology");
    }
    if (version == null) {
      throw new Exception("Must specify version");
    }
    if (contentService == null) {
      throw new Exception("Must specify content service");

    }

    // Get the terminology object itself to retrieve idType
    Terminology termObj = contentService.getTerminology(terminology, version);
    idType = termObj.getOrganizingClassType();

    // if not concept, throw exception
    if (!idType.equals(IdType.CONCEPT)) {
      throw new Exception(
          "Expression constraint language indexing is only valid for organizing class type CONCEPT");
    }

    // remove (if exists) and create the directory
    ConfigUtility.createEclIndexDirectory(terminology, version);
    directory = new NIOFSDirectory(
        new File(ConfigUtility.getEclIndexDirectoryName(terminology, version)));

    // get entity manager for direct queries
    EntityManager manager = contentService.getEntityManager();
    List<Object[]> results = new ArrayList<>();
    javax.persistence.Query query = null;

    //
    // Cache concept hibernate id -> terminologyId
    //
    Logger.getLogger(getClass())
        .info("Constructing id to terminology id map...");

    // construct and execute query
    query = manager
        .createQuery("select c.id, c.terminologyId from ConceptJpa c where "
            + "version = :version and terminology = :terminology");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    results = query.getResultList();

    Logger.getLogger(getClass())
        .info("  " + results.size() + " concepts found");

    // add the id->terminologyId mapping
    for (Object[] o : results) {
      idMap.put((Long) o[0], o[1].toString());
    }

    // clear the results array and log
    results.clear();
    Logger.getLogger(getClass()).info("  Finished caching concept ids");

    //
    // Cache transitive rel ancestor information
    //
    Logger.getLogger(getClass()).info("Caching ancestor information...");

    // construct and execute query
    query = manager.createQuery(
        "select r.subType.id, r.superType.id from ConceptTransitiveRelationshipJpa r where "
            + "version = :version and terminology = :terminology and depth != 0");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    results = query.getResultList();

    Logger.getLogger(getClass())
        .info("  " + results.size() + " transitive relationships retrieved");

    // cycle over results
    for (Object[] o : results) {

      // get the terminology ids
      String conceptId = idMap.get((Long) o[0]);
      String ancestorId = idMap.get((Long) o[1]);

      // get/create the existing ancestors for this concept
      Set<String> ancestors = ancestorMap.get(conceptId);
      if (ancestors == null) {
        ancestors = new HashSet<>();
      }

      // add the ancestor to the set and replace in map
      ancestors.add(ancestorId);
      ancestorMap.put(conceptId, ancestors);
    }

    // clear the results and log
    results.clear();
    Logger.getLogger(getClass())
        .info("  Finished caching ancestor information for "
            + ancestorMap.keySet().size() + " concepts");

    //
    // Cache subsets
    //
    Logger.getLogger(getClass()).info("Caching subset member information...");

    // construct and execute query
    query = manager.createQuery(
        "select s.id, s.terminologyId from ConceptSubsetJpa s where "
            + "version = :version and terminology = :terminology");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    results = query.getResultList();

    for (Object[] o : results) {
      subsetMap.put((Long) o[0], o[1].toString());
    }
    results.clear();

    // construct and execute query
    query = manager.createQuery(
        "select s.member.id, s.subset.id from ConceptSubsetMemberJpa s where "
            + "version = :version and terminology = :terminology");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    results = query.getResultList();

    Logger.getLogger(getClass())
        .info("  " + results.size() + " subset members retrieved");

    // cycle over results
    for (Object[] o : results) {

      // get the cached terminology ids
      String conceptId = idMap.get((Long) o[0]);
      String subsetId = subsetMap.get((Long) o[1]);

      // get/create the existing subset list for this concept
      Set<String> subsets = subsetMemberMap.get(conceptId);
      if (subsets == null) {
        subsets = new HashSet<>();
      }
      subsets.add(subsetId);
      subsetMemberMap.put(conceptId, subsets);
    }
    results.clear();
    Logger.getLogger(getClass())
        .info("  Finished caching subset information for "
            + subsetMap.keySet().size() + " subsets and "
            + subsetMemberMap.keySet().size() + " concepts");

    Logger.getLogger(getClass()).info("  Configuring index writer...");

    IndexWriterConfig config =
        new IndexWriterConfig(Version.LATEST, new StandardAnalyzer());

    iwriter = new IndexWriter(directory, config);

    Logger.getLogger(getClass()).info("  Cycling over concepts");

    // declare position, results list, and PFS parameter
    int pos = 0;
    SearchResultList concepts;
    PfscParameter pfsc = new PfscParameterJpa();
    pfsc.setMaxResults(batchSize);

    // cycle over concepts
    do {
      pfsc.setStartIndex(pos);
      concepts = contentService.findConceptsForQuery(terminology, version,
          Branch.ROOT, null, pfsc);

      // logging content on first retrieval
      if (pos == 0) {
        Logger.getLogger(getClass())
            .info("    " + concepts.getTotalCount() + " total concepts");
      }

      for (SearchResult sr : concepts.getObjects()) {
        Concept c = contentService.getConcept(sr.getId());
        iwriter.addDocument(getConceptDocument(c));
      }
      pos += concepts.getCount();
      Logger.getLogger(getClass())
          .info("  " + pos + "/" + relationshipCt + "/" + ancestorCt + "/"
              + subsetCt
              + " concepts/relationships/ancestors/members processed");

    } while (pos < concepts.getTotalCount());

    Logger.getLogger(getClass()).info("Closing index writer...");
    iwriter.close();

    Logger.getLogger(getClass())
        .info("ECL Index writing finished successfully.");
  }

  private Document getConceptDocument(Concept concept) throws Exception {

    Document conceptDoc = new Document();

    // Logger.getLogger(getClass()).info("Concept: " + concept.toString());

    // write the basic fields
    conceptDoc.add(new StringField("type", idType.toString(), Field.Store.YES));
    conceptDoc.add(new LongField(EclConceptFieldNames.INTERNAL_ID, concept.getId(),
        Field.Store.YES));
    conceptDoc.add(new StringField(EclConceptFieldNames.ID,
        concept.getTerminologyId().toString(), Field.Store.YES));

    // write the relationships
    for (ConceptRelationship relationship : concept.getRelationships()) {

      // Restrict to obsolete and inferred
      if (!relationship.isObsolete() && relationship.isInferred()) {
        relationshipCt++;
        String type = relationship.getAdditionalRelationshipType();
        String value = relationship.getTo().getTerminologyId();
        conceptDoc.add(new StringField(type, value, Field.Store.YES));
      }
    }

    // write the ancestors
    if (ancestorMap.get(concept.getTerminologyId()) != null) {
      for (String ancestor : ancestorMap.get(concept.getTerminologyId())) {
        ancestorCt++;
        conceptDoc.add(new StringField(EclConceptFieldNames.ANCESTOR, ancestor,
            Field.Store.YES));
      }
    }

    // write the subsets
    if (subsetMemberMap.get(concept.getTerminologyId()) != null) {
      for (String subset : subsetMemberMap.get(concept.getTerminologyId())) {
        subsetCt++;
        conceptDoc.add(new StringField(EclConceptFieldNames.MEMBER_OF, subset,
            Field.Store.YES));
      }
    }

    return conceptDoc;
  }

  @Override
  public void addProgressListener(ProgressListener l) {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeProgressListener(ProgressListener l) {
    // TODO Auto-generated method stub

  }

  @Override
  public void reset() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void cancel() throws Exception {

  }

  @Override
  public void close() throws Exception {
    // do nothing
  }

  /**
   * Sets the terminology.
   *
   * @param terminology the new terminology
   */
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  /**
   * Sets the version.
   *
   * @param version the new version
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Sets the content service.
   *
   * @param contentService the new content service
   */
  public void setContentService(ContentServiceJpa contentService) {
    this.contentService = contentService;
  }

}
