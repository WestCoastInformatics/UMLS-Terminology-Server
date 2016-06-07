/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.algo;

import java.util.List;
import java.util.Set;

import com.wci.umls.server.Project;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;

/**
 * Represents a classifier.
 */
public interface Classifier extends Algorithm {

  /**
   * Prepares classifier data. Includes loading concepts, relationships, roots,
   * role roots, and other information needed by the classifier and converting
   * it to a compatible form, if necessary.
   *
   * @param terminology the terminology
   * @param version the version
   * @param project the project
   * @throws Exception the exception
   */
  public void preClassify(String terminology, String version, Project project)
    throws Exception;

  /**
   * Adds the modified concepts. For incremental classification. Re-running
   * classification will incrementally classify or throw an exception.
   *
   * @param modifiedConcepts the modified concepts
   * @throws Exception the exception
   */
  public void addModifiedConcepts(Set<Concept> modifiedConcepts)
    throws Exception;

  /**
   * Indicates whether or not the terminology is consistent.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   * @throws Exception the exception
   */
  public boolean isConsistent() throws Exception;

  /**
   * Returns the unsatisfiable concepts.
   *
   * @return the unsatisfiable concepts
   * @throws Exception the exception
   */
  public List<Concept> getUnsatisfiableConcepts() throws Exception;

  /**
   * Returns the equivalent classes based on terminology id.
   *
   * @return the equivalent classes
   * @throws Exception the exception
   */
  public Set<Set<Concept>> getEquivalentClasses() throws Exception;

  /**
   * Returns the inferred hierarchical relationships.
   *
   * @return the inferred hierarchical relationships
   * @throws Exception the exception
   */
  public List<ConceptRelationship> getInferredHierarchicalRelationships() throws Exception;

  /**
   * Returns the new inferred hierarchical relationships.
   *
   * @return the new inferred hierarchical relationships
   * @throws Exception the exception
   */
  public List<ConceptRelationship> getNewInferredHierarchicalRelationships() throws Exception;

  /**
   * Returns the old inferred hierarchical relationships.
   *
   * @return the old inferred hierarchical relationships
   * @throws Exception the exception
   */
  public List<ConceptRelationship> getOldInferredHierarchicalRelationships() throws Exception;

  /**
   * Returns the inferred relationships.
   *
   * @return the inferred relationships
   * @throws Exception the exception
   */
  public List<ConceptRelationship> getInferredRelationships() throws Exception;

  /**
   * Returns the new inferred relationships.
   *
   * @return the new inferred relationships
   * @throws Exception the exception
   */
  public List<ConceptRelationship> getNewInferredRelationships() throws Exception;

  /**
   * Returns the old inferred relationships.
   *
   * @return the old inferred relationships
   * @throws Exception the exception
   */
  public List<ConceptRelationship> getOldInferredRelationships() throws Exception;

}
