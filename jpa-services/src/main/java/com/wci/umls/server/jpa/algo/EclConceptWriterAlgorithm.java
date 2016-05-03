package com.wci.umls.server.jpa.algo;

import java.io.File;

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
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.PfscParameterJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.ecl.ConceptFieldNames;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.ConceptSubsetMember;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.helpers.ProgressListener;

/**
 * The Expression Constraint Language Index Writer
 */
// TODO Make name reference indexing
public class EclConceptWriterAlgorithm implements Algorithm {

  private String terminology = null;

  private String version = null;
  
  private String idType = null;

  private ContentServiceJpa contentService = null;

  private IndexWriter iwriter = null;

  private Directory directory = null;

  private int batchSize = 2000;

  /**
   * Instantiates a new ecl concept writer algorithm.
   *
   * @throws Exception the exception
   */
  public EclConceptWriterAlgorithm() throws Exception {
    // constructor
  }

  /**
   * @param terminology
   * @param version
   * @param contentService
   * @throws Exception
   */
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

    // TODO Get the terminology object itself and check the idType
    // For now, fail if not Concept
    
    // TODO Add ConfigUtility.clearEclIndexes(t, v)
    // TODO Move all property name references to COnfigUtility.getPropNameXXX() functions
    String eclDirStr = ConfigUtility.getConfigProperties()
        .getProperty("hibernate.search.default.indexBase") + "/ecl/" + terminology + "/" + version + "/";
    Logger.getLogger(getClass()).info("  ECL Index Directory: " + eclDirStr);

    File eclDir = new File(eclDirStr);

    // delete folder and contents if it exists
    // TODO Dangerous deletion here, perhaps add a "has indexes" check
    // TODO Copy this to the remove terminology relevant areas
    // TODO Put this in createdb (NOT in update mode!) and any location with a "mode" parameter
    // TODO On startup (init in root service), delete all directories without a terminology entry in database
    // Use the ConfigUtility for file structure only
    if (eclDir.exists()) {
      Logger.getLogger(getClass()).info("    Directory exists, deleting");
      ConfigUtility.deleteDirectory(eclDir);
    }
    eclDir.mkdirs();
    directory = new NIOFSDirectory(eclDir);

    Logger.getLogger(getClass()).info("  Configuring index writer...");

    IndexWriterConfig config =
        new IndexWriterConfig(Version.LATEST, new StandardAnalyzer());

    iwriter = new IndexWriter(directory, config);

    Logger.getLogger(getClass()).info("  Cycling over concepts");

    int pos = 0;
    SearchResultList concepts;
    PfscParameter pfsc = new PfscParameterJpa();
    pfsc.setMaxResults(batchSize);
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
      Logger.getLogger(getClass()).info("  " + pos + " concepts processed");
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
    // TODO Decide which fields to stores
    // TODO Change concept to idType once retrieved
    conceptDoc.add(new StringField("type", "concept", Field.Store.NO));
    conceptDoc.add(new LongField(ConceptFieldNames.INTERNAL_ID, concept.getId(), Field.Store.YES));
    conceptDoc.add(new StringField(ConceptFieldNames.ID,
        concept.getTerminologyId().toString(), Field.Store.NO));

    // Logger.getLogger(getClass()).info(" Writing relationships");

    // write the active IsA relationships
    for (ConceptRelationship relationship : concept.getRelationships()) {
  
      // Logger.getLogger(getClass()).info(" Relationship: " +
      // relationship.toString());
      
      // Restrict to obsolete and inferred
       if (!relationship.isObsolete() && relationship.isInferred()) {
        String type = relationship.getAdditionalRelationshipType();
        String value = relationship.getTo().getTerminologyId();
        // Logger.getLogger(getClass()).info(" Eligible, type = " + type + ",
        // value = " + value);

        conceptDoc.add(new StringField(type, value, Field.Store.NO));
      }
    }

    //
    // write the ancestors
    //
    // TODO Make find method return search results instead of forcing full
    // concept initialization
    // Logger.getLogger(getClass()).info(" Writing ancestors");
    // TODO Cache transitive rels in advance
    
    ConceptList ancestors =
        contentService.findAncestorConcepts(concept.getTerminologyId(),
            terminology, version, false, Branch.ROOT, new PfsParameterJpa());
    
    
    // contentService.getEntityManager();
    
    // Logger.getLogger(getClass()).info(" " + ancestors.getCount() + "
    // ancestors found");
    for (Concept ancestor : ancestors.getObjects()) {
      // Logger.getLogger(getClass()).info(" Writing ancestor " +
      // ancestor.getTerminologyId() + ", " + ancestor.getName());
      conceptDoc.add(new StringField(ConceptFieldNames.ANCESTOR,
          ancestor.getTerminologyId(), Field.Store.NO));
    }

    //
    // write the refset members from subset terminology ids
    //
    // TODO Precache these as well
    for (ConceptSubsetMember member : concept.getMembers()) {
      conceptDoc.add(new StringField(ConceptFieldNames.MEMBER_OF,
          member.getSubset().getTerminologyId(), Field.Store.NO));
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
