/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.reflections.Reflections;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.AlgorithmParameterJpa;
import com.wci.umls.server.jpa.ValidationResultJpa;

/**
 * Implementation of an algorithm to reindex all classes annotated
 * with @Indexed.
 */
public class LuceneReindexAlgorithm extends AbstractAlgorithm {

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

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting " + getName());
    if (fullTextEntityManager == null) {
      fullTextEntityManager = Search.getFullTextEntityManager(manager);
    }
    computeLuceneIndexes(indexedObjects);
    // fullTextEntityManager.close();
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    if (fullTextEntityManager == null) {
      fullTextEntityManager = Search.getFullTextEntityManager(manager);
    }
    clearLuceneIndexes();
    // fullTextEntityManager.close();
  }

  /**
   * Compute lucene indexes.
   *
   * @param indexedObjects the indexed objects
   * @throws Exception the exception
   */
  private void computeLuceneIndexes(String indexedObjects) throws Exception {
    // set of objects to be re-indexed
    final Set<String> objectsToReindex = new HashSet<>();
    final Map<String, Class<?>> reindexMap = new HashMap<>();
    final Reflections reflections = new Reflections();
    for (final Class<?> clazz : reflections
        .getTypesAnnotatedWith(Indexed.class)) {
      reindexMap.put(clazz.getSimpleName(), clazz);
    }

    // if no parameter specified, re-index all objects
    if (indexedObjects == null || indexedObjects.isEmpty()) {

      // Add all class names
      for (final String className : reindexMap.keySet()) {
        if (objectsToReindex.contains(className)) {
          // This restriction can be removed by using full class names
          // however, then calling the mojo is more complicated
          throw new Exception(
              "Reindex process assumes simple class names are different.");
        }
        objectsToReindex.add(className);
      }

      // otherwise, construct set of indexed objects
    } else {

      // remove white-space and split by comma
      String[] objects = indexedObjects.replaceAll(" ", "").split(",");

      // add each value to the set
      for (final String object : objects)
        objectsToReindex.add(object);

    }

    Logger.getLogger(getClass()).info("Starting reindexing for:");
    for (final String objectToReindex : objectsToReindex) {
      Logger.getLogger(getClass()).info("  " + objectToReindex);
    }

    // Reindex each object
    for (final String key : reindexMap.keySet()) {
      // Concepts
      if (objectsToReindex.contains(key)) {
        Logger.getLogger(getClass()).info("  Creating indexes for " + key);
        fullTextEntityManager.purgeAll(reindexMap.get(key));
        fullTextEntityManager.flushToIndexes();
        fullTextEntityManager.createIndexer(reindexMap.get(key))
            .batchSizeToLoadObjects(100).cacheMode(CacheMode.NORMAL)
            .threadsToLoadObjects(4).startAndWait();

        objectsToReindex.remove(key);
      }
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

    final Reflections reflections = new Reflections();
    for (final Class<?> clazz : reflections
        .getTypesAnnotatedWith(Indexed.class)) {
      fullTextEntityManager.purgeAll(clazz);
    }
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    if (p.getProperty("indexedObjects") != null) {
      indexedObjects = p.getProperty("indexedObjects");
    }

  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() throws Exception {
    final List<AlgorithmParameter> params = super.getParameters();
    final AlgorithmParameter param =
        new AlgorithmParameterJpa("Indexed Objects", "indexedObjects",
            "Comma-separated list of simple object class names to reindex.", "",
            255, AlgorithmParameter.Type.STRING, "");
    params.add(param);
    return params;

  }

  /* see superclass */
  @Override
  public String getDescription() {
    return ConfigUtility.getNameFromClass(getClass());
  }
}
