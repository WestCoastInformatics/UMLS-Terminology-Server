/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;

import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.content.AtomSubsetMemberJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.CodeTreePositionJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetMemberJpa;
import com.wci.umls.server.jpa.content.ConceptTreePositionJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.DescriptorTreePositionJpa;
import com.wci.umls.server.jpa.content.LexicalClassJpa;
import com.wci.umls.server.jpa.content.StringClassJpa;
import com.wci.umls.server.jpa.services.RootServiceJpa;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;

/**
 * Implementation of an algorithm to compute transitive closure using the
 * {@link ContentService}.
 */
public class LuceneReindexAlgorithm extends RootServiceJpa implements Algorithm {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The request cancel flag. */
  boolean requestCancel = false;

  /** The terminology. */
  private String indexedObjects;

  /** The full text entity manager. */
  private FullTextEntityManager fullTextEntityManager;

  /**
   * Instantiates an empty {@link LuceneReindexAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public LuceneReindexAlgorithm() throws Exception {
    super();
  }

  /**
   * Sets the indexed objects.
   *
   * @param indexedObjects the indexed objects
   */
  public void setIndexedObjects(String indexedObjects) {
    this.indexedObjects = indexedObjects;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.jpa.algo.Algorithm#compute()
   */
  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  @Override
  public void compute() throws Exception {
    if (fullTextEntityManager == null) {
      fullTextEntityManager = Search.getFullTextEntityManager(manager);
    }
    computeLuceneIndexes(indexedObjects);
    fullTextEntityManager.close();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.jpa.algo.Algorithm#reset()
   */
  /**
   * Reset.
   *
   * @throws Exception the exception
   */
  @Override
  public void reset() throws Exception {
    if (fullTextEntityManager == null) {
      fullTextEntityManager = Search.getFullTextEntityManager(manager);
    }
    clearLuceneIndexes();
    fullTextEntityManager.close();
  }

  /**
   * Compute lucene indexes.
   *
   * @param indexedObjects the indexed objects
   * @throws Exception the exception
   */
  private void computeLuceneIndexes(String indexedObjects) throws Exception {
    // set of objects to be re-indexed
    Set<String> objectsToReindex = new HashSet<>();

    // if no parameter specified, re-index all objects
    if (indexedObjects == null || indexedObjects.isEmpty()) {
      objectsToReindex.add("ConceptJpa");
      objectsToReindex.add("CodeJpa");
      objectsToReindex.add("DescriptorJpa");
      objectsToReindex.add("LexicalClassJpa");
      objectsToReindex.add("StringClassJpa");
      objectsToReindex.add("AtomSubsetMemberJpa");
      objectsToReindex.add("ConceptSubsetMemberJpa");
      objectsToReindex.add("ConceptTreePositionJpa");
      objectsToReindex.add("DescriptorTreePositionJpa");
      objectsToReindex.add("CodeTreePositionJpa");
      objectsToReindex.add("CodeRelationshipJpa");
      objectsToReindex.add("ConceptRelationshipJpa");
      objectsToReindex.add("DescriptorRelationshipJpa");

      // otherwise, construct set of indexed objects
    } else {

      // remove white-space and split by comma
      String[] objects = indexedObjects.replaceAll(" ", "").split(",");

      // add each value to the set
      for (String object : objects)
        objectsToReindex.add(object);

    }

    Logger.getLogger(getClass()).info("Starting reindexing for:");
    for (String objectToReindex : objectsToReindex) {
      Logger.getLogger(getClass()).info("  " + objectToReindex);
    }

    // full text entity manager
    FullTextEntityManager fullTextEntityManager =
        Search.getFullTextEntityManager(manager);

    // Concepts
    if (objectsToReindex.contains("ConceptJpa")) {
      Logger.getLogger(getClass()).info("  Creating indexes for ConceptJpa");
      fullTextEntityManager.purgeAll(ConceptJpa.class);
      fullTextEntityManager.flushToIndexes();
      fullTextEntityManager.createIndexer(ConceptJpa.class)
          .batchSizeToLoadObjects(100).cacheMode(CacheMode.NORMAL)
          .threadsToLoadObjects(4).startAndWait();

      objectsToReindex.remove("ConceptJpa");
    }

    // ConceptSubsetMember
    if (objectsToReindex.contains("ConceptSubsetMemberJpa")) {
      Logger.getLogger(getClass()).info(
          "  Creating indexes for ConceptSubsetMemberJpa");
      fullTextEntityManager.purgeAll(ConceptSubsetMemberJpa.class);
      fullTextEntityManager.flushToIndexes();
      fullTextEntityManager.createIndexer(ConceptSubsetMemberJpa.class)
          .batchSizeToLoadObjects(100).cacheMode(CacheMode.NORMAL)
          .threadsToLoadObjects(4).startAndWait();

      objectsToReindex.remove("ConceptSubsetMemberJpa");
    }

    // Descriptor
    if (objectsToReindex.contains("DescriptorJpa")) {
      Logger.getLogger(getClass()).info("  Creating indexes for DescriptorJpa");
      fullTextEntityManager.purgeAll(DescriptorJpa.class);
      fullTextEntityManager.flushToIndexes();
      fullTextEntityManager.createIndexer(DescriptorJpa.class)
          .batchSizeToLoadObjects(100).cacheMode(CacheMode.NORMAL)
          .threadsToLoadObjects(4).startAndWait();

      objectsToReindex.remove("DescriptorJpa");
    }

    // Code
    if (objectsToReindex.contains("CodeJpa")) {
      Logger.getLogger(getClass()).info("  Creating indexes for CodeJpa");
      fullTextEntityManager.purgeAll(CodeJpa.class);
      fullTextEntityManager.flushToIndexes();
      fullTextEntityManager.createIndexer(CodeJpa.class)
          .batchSizeToLoadObjects(100).cacheMode(CacheMode.NORMAL)
          .threadsToLoadObjects(4).startAndWait();

      objectsToReindex.remove("CodeJpa");
    }

    // Lexical Class
    if (objectsToReindex.contains("LexicalClassJpa")) {
      Logger.getLogger(getClass()).info(
          "  Creating indexes for LexicalClassJpa");
      fullTextEntityManager.purgeAll(LexicalClassJpa.class);
      fullTextEntityManager.flushToIndexes();
      fullTextEntityManager.createIndexer(LexicalClassJpa.class)
          .batchSizeToLoadObjects(100).cacheMode(CacheMode.NORMAL)
          .threadsToLoadObjects(4).startAndWait();

      objectsToReindex.remove("LexicalClassJpa");
    }

    // StringClass
    if (objectsToReindex.contains("StringClassJpa")) {
      Logger.getLogger(getClass())
          .info("  Creating indexes for StringClassJpa");
      fullTextEntityManager.purgeAll(StringClassJpa.class);
      fullTextEntityManager.flushToIndexes();
      fullTextEntityManager.createIndexer(StringClassJpa.class)
          .batchSizeToLoadObjects(100).cacheMode(CacheMode.NORMAL)
          .threadsToLoadObjects(4).startAndWait();

      objectsToReindex.remove("StringClassJpa");
    }

    // AtomSubsetMember
    if (objectsToReindex.contains("AtomSubsetMemberJpa")) {
      Logger.getLogger(getClass()).info(
          "  Creating indexes for AtomSubsetMemberJpa");
      fullTextEntityManager.purgeAll(AtomSubsetMemberJpa.class);
      fullTextEntityManager.flushToIndexes();
      fullTextEntityManager.createIndexer(AtomSubsetMemberJpa.class)
          .batchSizeToLoadObjects(100).cacheMode(CacheMode.NORMAL)
          .threadsToLoadObjects(4).startAndWait();

      objectsToReindex.remove("AtomSubsetMemberJpa");
    }

    // Concept tree position
    if (objectsToReindex.contains("ConceptTreePositionJpa")) {
      Logger.getLogger(getClass()).info(
          "  Creating indexes for ConceptTreePositionJpa");
      fullTextEntityManager.purgeAll(ConceptTreePositionJpa.class);
      fullTextEntityManager.flushToIndexes();
      fullTextEntityManager.createIndexer(ConceptTreePositionJpa.class)
          .batchSizeToLoadObjects(100).cacheMode(CacheMode.NORMAL)
          .threadsToLoadObjects(4).startAndWait();

      objectsToReindex.remove("ConceptTreePositionJpa");
    }

    // Descriptor tree position
    if (objectsToReindex.contains("DescriptorTreePositionJpa")) {
      Logger.getLogger(getClass()).info(
          "  Creating indexes for DescriptorTreePositionJpa");
      fullTextEntityManager.purgeAll(DescriptorTreePositionJpa.class);
      fullTextEntityManager.flushToIndexes();
      fullTextEntityManager.createIndexer(DescriptorTreePositionJpa.class)
          .batchSizeToLoadObjects(100).cacheMode(CacheMode.NORMAL)
          .threadsToLoadObjects(4).startAndWait();

      objectsToReindex.remove("DescriptorTreePositionJpa");
    }

    // Code tree position
    if (objectsToReindex.contains("CodeTreePositionJpa")) {
      Logger.getLogger(getClass()).info(
          "  Creating indexes for CodeTreePositionJpa");
      fullTextEntityManager.purgeAll(CodeTreePositionJpa.class);
      fullTextEntityManager.flushToIndexes();
      fullTextEntityManager.createIndexer(CodeTreePositionJpa.class)
          .batchSizeToLoadObjects(100).cacheMode(CacheMode.NORMAL)
          .threadsToLoadObjects(4).startAndWait();

      objectsToReindex.remove("CodeTreePositionJpa");
    }

    // Projects
    if (objectsToReindex.contains("ProjectJpa")) {
      Logger.getLogger(getClass()).info("  Creating indexes for ProjectJpa");
      fullTextEntityManager.purgeAll(ProjectJpa.class);
      fullTextEntityManager.flushToIndexes();
      fullTextEntityManager.createIndexer(ProjectJpa.class)
          .batchSizeToLoadObjects(100).cacheMode(CacheMode.NORMAL)
          .threadsToLoadObjects(4).startAndWait();

      objectsToReindex.remove("ProjectJpa");
    }

    // ConceptRelationships
    if (objectsToReindex.contains("ConceptRelationshipJpa")) {
      Logger.getLogger(getClass()).info("  Creating indexes for ConceptRelationshipJpa");
      fullTextEntityManager.purgeAll(ConceptRelationshipJpa.class);
      fullTextEntityManager.flushToIndexes();
      fullTextEntityManager.createIndexer(ConceptRelationshipJpa.class)
          .batchSizeToLoadObjects(100).cacheMode(CacheMode.NORMAL)
          .threadsToLoadObjects(4).startAndWait();

      objectsToReindex.remove("ConceptRelationshipJpa");
    }

    // DescriptorRelationship
    if (objectsToReindex.contains("DescriptorRelationshipJpa")) {
      Logger.getLogger(getClass()).info("  Creating indexes for DescriptorRelationshipJpa");
      fullTextEntityManager.purgeAll(DescriptorRelationshipJpa.class);
      fullTextEntityManager.flushToIndexes();
      fullTextEntityManager.createIndexer(DescriptorRelationshipJpa.class)
          .batchSizeToLoadObjects(100).cacheMode(CacheMode.NORMAL)
          .threadsToLoadObjects(4).startAndWait();

      objectsToReindex.remove("DescriptorRelationshipJpa");
    }

    // CodeRelationship
    if (objectsToReindex.contains("CodeRelationshipJpa")) {
      Logger.getLogger(getClass()).info("  Creating indexes for CodeRelationshipJpa");
      fullTextEntityManager.purgeAll(CodeRelationshipJpa.class);
      fullTextEntityManager.flushToIndexes();
      fullTextEntityManager.createIndexer(CodeRelationshipJpa.class)
          .batchSizeToLoadObjects(100).cacheMode(CacheMode.NORMAL)
          .threadsToLoadObjects(4).startAndWait();

      objectsToReindex.remove("CodeRelationshipJpa");
    }

    if (objectsToReindex.size() != 0) {
      throw new Exception(
          "The following objects were specified for re-indexing, but do not exist as indexed objects: "
              + objectsToReindex.toString());
    }

    // Cleanup
    Logger.getLogger(getClass()).info("done ...");
  }

  /**
   * Clear lucene indexes.
   *
   * @throws Exception the exception
   */
  private void clearLuceneIndexes() throws Exception {

    fullTextEntityManager.purgeAll(ConceptJpa.class);
    fullTextEntityManager.purgeAll(CodeJpa.class);
    fullTextEntityManager.purgeAll(DescriptorJpa.class);
    fullTextEntityManager.purgeAll(LexicalClassJpa.class);
    fullTextEntityManager.purgeAll(StringClassJpa.class);
    fullTextEntityManager.purgeAll(ProjectJpa.class);
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
  /**
   * Adds the progress listener.
   *
   * @param l the l
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
  /**
   * Removes the progress listener.
   *
   * @param l the l
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
  /**
   * Cancel.
   */
  @Override
  public void cancel() {
    requestCancel = true;
  }

  @Override
  public void refreshCaches() throws Exception {
    // n/a
  }
}
